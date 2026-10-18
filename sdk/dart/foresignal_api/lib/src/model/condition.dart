//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'condition.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Condition {
  /// Returns a new [Condition] instance.
  Condition({

     this.path,

    required  this.op,

    required  this.value,
  });

      /// Dot path into payload. Required for every operator except inWatchlist and notInWatchlist, which without a path read each source's declared identity field (descriptor entityKeys, 036); a source that does not declare the watchlist's identity never matches
  @JsonKey(
    
    name: r'path',
    required: false,
    includeIfNull: false,
  )


  final String? path;



      /// Comparison ops take numbers (== and != also strings and booleans); in/notIn a non-empty homogeneous scalar array (≤ 1000); exists a boolean; startsWith a string ≤ 256; matches a Java regex ≤ 256 without backreferences, lookaround or nested quantifiers (only (?:…) groups and the escapes \\d \\D \\w \\W \\s \\S \\n \\t \\r \\f, so PostgreSQL reads it the same way). A value of another JSON type than the operator expects, a missing path or JSON null never matches (except exists); string operators need a JSON string (the number 5 does not equal \"5\"); != and notIn need the value present. inWatchlist/notInWatchlist (036) take a watchlist id; the payload value must be a JSON string or number (compared by its JSON text after the list's normalization); anything else, a missing path or JSON null never matches either operator.
  @JsonKey(
    
    name: r'op',
    required: true,
    includeIfNull: false,
  )


  final ConditionOpEnum op;



  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: true,
  )


  final Object? value;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Condition &&
      other.path == path &&
      other.op == op &&
      other.value == value;

    @override
    int get hashCode =>
        path.hashCode +
        op.hashCode +
        (value == null ? 0 : value.hashCode);

  factory Condition.fromJson(Map<String, dynamic> json) => _$ConditionFromJson(json);

  Map<String, dynamic> toJson() => _$ConditionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Comparison ops take numbers (== and != also strings and booleans); in/notIn a non-empty homogeneous scalar array (≤ 1000); exists a boolean; startsWith a string ≤ 256; matches a Java regex ≤ 256 without backreferences, lookaround or nested quantifiers (only (?:…) groups and the escapes \\d \\D \\w \\W \\s \\S \\n \\t \\r \\f, so PostgreSQL reads it the same way). A value of another JSON type than the operator expects, a missing path or JSON null never matches (except exists); string operators need a JSON string (the number 5 does not equal \"5\"); != and notIn need the value present. inWatchlist/notInWatchlist (036) take a watchlist id; the payload value must be a JSON string or number (compared by its JSON text after the list's normalization); anything else, a missing path or JSON null never matches either operator.
enum ConditionOpEnum {
@JsonValue(r'>')
greaterThan(r'>'),
@JsonValue(r'>=')
greaterThanEqual(r'>='),
@JsonValue(r'<')
lessThan(r'<'),
@JsonValue(r'<=')
lessThanEqual(r'<='),
@JsonValue(r'==')
equalEqual(r'=='),
@JsonValue(r'!=')
exclamationEqual(r'!='),
@JsonValue(r'in')
in_(r'in'),
@JsonValue(r'notIn')
notIn(r'notIn'),
@JsonValue(r'exists')
exists(r'exists'),
@JsonValue(r'startsWith')
startsWith(r'startsWith'),
@JsonValue(r'matches')
matches(r'matches'),
@JsonValue(r'inWatchlist')
inWatchlist(r'inWatchlist'),
@JsonValue(r'notInWatchlist')
notInWatchlist(r'notInWatchlist');

const ConditionOpEnum(this.value);

final String value;

@override
String toString() => value;
}


