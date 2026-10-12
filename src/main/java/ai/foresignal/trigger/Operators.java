package ai.foresignal.trigger;

import ai.foresignal.store.query.PgArrays;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * The built-in condition operators (prompt 017), each with its SQL and its Java meaning.
 *
 * <p>Shared rules, which the truth table pins:
 *
 * <ul>
 *   <li>A value of another JSON type than the operator expects, a missing path and JSON null never
 *       satisfy a condition; SQL yields NULL there and Java {@link Evaluation#AMBIGUOUS}. The only
 *       exception is {@code exists}, which is defined on presence.
 *   <li>Numbers compare as {@code numeric}: {@code 6} equals {@code 6.0}.
 *   <li>String operators ({@code == != in notIn startsWith matches} with string values) require a
 *       JSON string: the number {@code 5} does not equal the string {@code "5"}.
 *   <li>{@code !=} and {@code notIn} require the value to be present and of the expected type.
 * </ul>
 */
public final class Operators {

    /** The regex deadline of the Java {@code matches} test (prompt 017: 10 ms). */
    public static final long REGEX_BUDGET_NANOS = 10_000_000L;

    /** The longest {@code startsWith} prefix and {@code matches} pattern. */
    public static final int MAX_STRING = 256;

    /** The most values of an {@code in}/{@code notIn} list. */
    public static final int MAX_LIST = 1_000;

    private Operators() {}

    /** Every built-in operator, in the order of the spec's {@code Condition.op} enum. */
    public static List<Operator> builtIns() {
        return List.of(
                new Compare(">"), new Compare(">="), new Compare("<"), new Compare("<="),
                new Equality(false), new Equality(true),
                new Membership(false), new Membership(true),
                new Exists(), new StartsWith(), new Matches());
    }

    // ------------------------------------------------------------------------- value helpers

    /** {@code value} as a {@code BigDecimal} when it is a finite JSON number, else null. */
    public static BigDecimal number(Object value) {
        return switch (value) {
            case null -> null;
            case Boolean b -> null;
            case BigDecimal d -> d;
            case BigInteger i -> new BigDecimal(i);
            case Long l -> BigDecimal.valueOf(l);
            case Integer i -> BigDecimal.valueOf(i);
            case Short s -> BigDecimal.valueOf(s);
            case Byte b -> BigDecimal.valueOf(b);
            case Double d -> Double.isFinite(d) ? new BigDecimal(Double.toString(d)) : null;
            case Float f -> Float.isFinite(f) ? new BigDecimal(Float.toString(f)) : null;
            case Number n -> {
                try {
                    yield new BigDecimal(n.toString());
                } catch (NumberFormatException e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    /** The JSON scalar type of {@code value}: number, string, boolean, or null for anything else. */
    static String scalarType(Object value) {
        if (value instanceof Boolean) {
            return "boolean";
        }
        if (value instanceof String) {
            return "string";
        }
        if (number(value) != null) {
            return "number";
        }
        return null;
    }

    /** A normalized scalar: numbers become {@code BigDecimal}. */
    static Object normalizeScalar(Object value) {
        String type = scalarType(value);
        if (type == null) {
            throw new IllegalArgumentException("must be a number, string or boolean");
        }
        return "number".equals(type) ? number(value) : value;
    }

    /** True when {@code actual} and the normalized {@code expected} are equal JSON scalars. */
    static boolean sameScalar(Object actual, Object expected) {
        if (expected instanceof BigDecimal e) {
            BigDecimal a = number(actual);
            return a != null && a.compareTo(e) == 0;
        }
        return expected.equals(actual);
    }

    /** The typed SQL expression of {@code term} for values of {@code type}. */
    static String typed(SqlTerm term, String type) {
        return switch (type) {
            case "number" -> term.number();
            case "string" -> term.string();
            default -> term.bool();
        };
    }

    // ------------------------------------------------------------------------- comparisons

    /** {@code > >= < <=}: number values only. */
    static final class Compare implements Operator {

        private final String name;

        Compare(String name) {
            this.name = name;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public Object normalize(Object value) {
            BigDecimal number = number(value);
            if (number == null) {
                throw new IllegalArgumentException("must be a number for " + name);
            }
            return number;
        }

        @Override
        public String sql(SqlTerm term, Object value) {
            return term.number() + " " + name + " " + term.bind("v", value, "numeric");
        }

        @Override
        public Evaluation evaluate(Object actual, Object value) {
            BigDecimal a = number(actual);
            if (a == null) {
                return Evaluation.AMBIGUOUS;
            }
            int cmp = a.compareTo((BigDecimal) value);
            return Evaluation.of(switch (name) {
                case ">" -> cmp > 0;
                case ">=" -> cmp >= 0;
                case "<" -> cmp < 0;
                default -> cmp <= 0;
            });
        }
    }

    /** {@code ==} and {@code !=}: a number, string or boolean. */
    static final class Equality implements Operator {

        private final boolean negated;

        Equality(boolean negated) {
            this.negated = negated;
        }

        @Override
        public String name() {
            return negated ? "!=" : "==";
        }

        @Override
        public Object normalize(Object value) {
            return normalizeScalar(value);
        }

        @Override
        public String sql(SqlTerm term, Object value) {
            String type = scalarType(value);
            String cast = switch (type) {
                case "number" -> "numeric";
                case "string" -> "text";
                default -> "boolean";
            };
            return typed(term, type) + (negated ? " <> " : " = ") + term.bind("v", value, cast);
        }

        @Override
        public Evaluation evaluate(Object actual, Object value) {
            String expected = scalarType(value);
            if (actual == null || !expected.equals(scalarType(actual))) {
                return Evaluation.AMBIGUOUS;
            }
            boolean equal = sameScalar(actual, value);
            return Evaluation.of(negated != equal);
        }
    }

    /** {@code in} and {@code notIn}: a non-empty homogeneous list of scalars, at most 1,000. */
    static final class Membership implements Operator {

        private final boolean negated;

        Membership(boolean negated) {
            this.negated = negated;
        }

        @Override
        public String name() {
            return negated ? "notIn" : "in";
        }

        @Override
        public Object normalize(Object value) {
            if (!(value instanceof List<?> list) || list.isEmpty()) {
                throw new IllegalArgumentException("must be a non-empty array for " + name());
            }
            if (list.size() > MAX_LIST) {
                throw new IllegalArgumentException("must have at most " + MAX_LIST + " values");
            }
            String type = null;
            List<Object> normalized = new ArrayList<>(list.size());
            for (Object item : list) {
                String itemType = scalarType(item);
                if (itemType == null) {
                    throw new IllegalArgumentException("must contain only numbers, strings or booleans");
                }
                if (type != null && !type.equals(itemType)) {
                    throw new IllegalArgumentException("must contain values of one type");
                }
                type = itemType;
                normalized.add(normalizeScalar(item));
            }
            return List.copyOf(normalized);
        }

        @Override
        public String sql(SqlTerm term, Object value) {
            List<?> values = (List<?>) value;
            String type = scalarType(values.getFirst());
            String literal;
            String cast;
            switch (type) {
                case "number" -> {
                    List<String> texts = values.stream().map(v -> ((BigDecimal) v).toPlainString()).toList();
                    literal = "{" + String.join(",", texts) + "}";
                    cast = "numeric[]";
                }
                case "string" -> {
                    literal = PgArrays.text(values.stream().map(String.class::cast).toList());
                    cast = "text[]";
                }
                default -> {
                    literal = "{" + String.join(",", values.stream().map(String::valueOf).toList()) + "}";
                    cast = "boolean[]";
                }
            }
            String array = term.bind("v", literal, cast);
            return typed(term, type) + (negated ? " <> ALL(" : " = ANY(") + array + ")";
        }

        @Override
        public Evaluation evaluate(Object actual, Object value) {
            List<?> values = (List<?>) value;
            String expected = scalarType(values.getFirst());
            if (actual == null || !expected.equals(scalarType(actual))) {
                return Evaluation.AMBIGUOUS;
            }
            boolean member = false;
            for (Object candidate : values) {
                if (sameScalar(actual, candidate)) {
                    member = true;
                    break;
                }
            }
            return Evaluation.of(negated != member);
        }
    }

    /** {@code exists}: true means present and not JSON null; false means the opposite. */
    static final class Exists implements Operator {

        @Override
        public String name() {
            return "exists";
        }

        @Override
        public Object normalize(Object value) {
            if (!(value instanceof Boolean)) {
                throw new IllegalArgumentException("must be true or false for exists");
            }
            return value;
        }

        @Override
        public String sql(SqlTerm term, Object value) {
            return "((" + term.json() + " IS NOT NULL AND " + term.typeof() + " <> 'null') = "
                    + term.bind("v", value, "boolean") + ")";
        }

        @Override
        public Evaluation evaluate(Object actual, Object value) {
            return Evaluation.of((actual != null) == (Boolean) value);
        }
    }

    /** {@code startsWith}: a non-empty string of at most 256 characters. */
    static final class StartsWith implements Operator {

        @Override
        public String name() {
            return "startsWith";
        }

        @Override
        public Object normalize(Object value) {
            if (!(value instanceof String s) || s.isEmpty()) {
                throw new IllegalArgumentException("must be a non-empty string for startsWith");
            }
            if (s.codePointCount(0, s.length()) > MAX_STRING) {
                throw new IllegalArgumentException("must be at most " + MAX_STRING + " characters");
            }
            return s;
        }

        @Override
        public String sql(SqlTerm term, Object value) {
            String prefix = (String) value;
            int length = prefix.codePointCount(0, prefix.length());
            return "left(" + term.string() + ", " + term.bind("len", length, "integer") + ") = "
                    + term.bind("v", prefix, "text");
        }

        @Override
        public Evaluation evaluate(Object actual, Object value) {
            if (!(actual instanceof String s)) {
                return Evaluation.AMBIGUOUS;
            }
            return Evaluation.of(s.startsWith((String) value));
        }
    }

    /** {@code matches}: a guarded regex ({@link RegexGuard}), searched, not anchored. */
    static final class Matches implements Operator {

        private final Map<String, Pattern> compiled = new ConcurrentHashMap<>();

        @Override
        public String name() {
            return "matches";
        }

        @Override
        public Object normalize(Object value) {
            if (!(value instanceof String s)) {
                throw new IllegalArgumentException("must be a string regular expression for matches");
            }
            compiled.computeIfAbsent(s, RegexGuard::compile);
            return s;
        }

        @Override
        public String sql(SqlTerm term, Object value) {
            return term.string() + " ~ " + term.bind("v", value, "text");
        }

        @Override
        public Evaluation evaluate(Object actual, Object value) {
            if (!(actual instanceof String s)) {
                return Evaluation.AMBIGUOUS;
            }
            Pattern pattern = compiled.computeIfAbsent((String) value, RegexGuard::compile);
            try {
                return Evaluation.of(pattern.matcher(new DeadlineCharSequence(s, REGEX_BUDGET_NANOS)).find());
            } catch (DeadlineCharSequence.Timeout e) {
                return Evaluation.AMBIGUOUS;
            }
        }
    }
}
