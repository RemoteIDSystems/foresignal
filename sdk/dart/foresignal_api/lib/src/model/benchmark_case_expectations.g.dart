// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_case_expectations.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkCaseExpectationsCWProxy {
  BenchmarkCaseExpectations findings(
    List<BenchmarkCaseExpectationsFindingsInner>? findings,
  );

  BenchmarkCaseExpectations precursors(
    List<BenchmarkCaseExpectationsPrecursorsInner>? precursors,
  );

  BenchmarkCaseExpectations questions(
    List<BenchmarkCaseExpectationsQuestionsInner>? questions,
  );

  BenchmarkCaseExpectations notes(String? notes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkCaseExpectations(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkCaseExpectations(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkCaseExpectations call({
    List<BenchmarkCaseExpectationsFindingsInner>? findings,
    List<BenchmarkCaseExpectationsPrecursorsInner>? precursors,
    List<BenchmarkCaseExpectationsQuestionsInner>? questions,
    String? notes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkCaseExpectations.copyWith(...)` or call `instanceOfBenchmarkCaseExpectations.copyWith.fieldName(value)` for a single field.
class _$BenchmarkCaseExpectationsCWProxyImpl
    implements _$BenchmarkCaseExpectationsCWProxy {
  const _$BenchmarkCaseExpectationsCWProxyImpl(this._value);

  final BenchmarkCaseExpectations _value;

  @override
  BenchmarkCaseExpectations findings(
    List<BenchmarkCaseExpectationsFindingsInner>? findings,
  ) => call(findings: findings);

  @override
  BenchmarkCaseExpectations precursors(
    List<BenchmarkCaseExpectationsPrecursorsInner>? precursors,
  ) => call(precursors: precursors);

  @override
  BenchmarkCaseExpectations questions(
    List<BenchmarkCaseExpectationsQuestionsInner>? questions,
  ) => call(questions: questions);

  @override
  BenchmarkCaseExpectations notes(String? notes) => call(notes: notes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkCaseExpectations(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkCaseExpectations(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkCaseExpectations call({
    Object? findings = const $CopyWithPlaceholder(),
    Object? precursors = const $CopyWithPlaceholder(),
    Object? questions = const $CopyWithPlaceholder(),
    Object? notes = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkCaseExpectations(
      findings: findings == const $CopyWithPlaceholder()
          ? _value.findings
          // ignore: cast_nullable_to_non_nullable
          : findings as List<BenchmarkCaseExpectationsFindingsInner>?,
      precursors: precursors == const $CopyWithPlaceholder()
          ? _value.precursors
          // ignore: cast_nullable_to_non_nullable
          : precursors as List<BenchmarkCaseExpectationsPrecursorsInner>?,
      questions: questions == const $CopyWithPlaceholder()
          ? _value.questions
          // ignore: cast_nullable_to_non_nullable
          : questions as List<BenchmarkCaseExpectationsQuestionsInner>?,
      notes: notes == const $CopyWithPlaceholder()
          ? _value.notes
          // ignore: cast_nullable_to_non_nullable
          : notes as String?,
    );
  }
}

extension $BenchmarkCaseExpectationsCopyWith on BenchmarkCaseExpectations {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkCaseExpectations.copyWith(...)` or `instanceOfBenchmarkCaseExpectations.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkCaseExpectationsCWProxy get copyWith =>
      _$BenchmarkCaseExpectationsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkCaseExpectations _$BenchmarkCaseExpectationsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('BenchmarkCaseExpectations', json, ($checkedConvert) {
  final val = BenchmarkCaseExpectations(
    findings: $checkedConvert(
      'findings',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => BenchmarkCaseExpectationsFindingsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    precursors: $checkedConvert(
      'precursors',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => BenchmarkCaseExpectationsPrecursorsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    questions: $checkedConvert(
      'questions',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => BenchmarkCaseExpectationsQuestionsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    notes: $checkedConvert('notes', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$BenchmarkCaseExpectationsToJson(
  BenchmarkCaseExpectations instance,
) => <String, dynamic>{
  'findings': ?instance.findings?.map((e) => e.toJson()).toList(),
  'precursors': ?instance.precursors?.map((e) => e.toJson()).toList(),
  'questions': ?instance.questions?.map((e) => e.toJson()).toList(),
  'notes': ?instance.notes,
};
