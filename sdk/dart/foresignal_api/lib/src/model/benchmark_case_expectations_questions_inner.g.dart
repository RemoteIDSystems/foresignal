// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'benchmark_case_expectations_questions_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$BenchmarkCaseExpectationsQuestionsInnerCWProxy {
  BenchmarkCaseExpectationsQuestionsInner text(String text);

  BenchmarkCaseExpectationsQuestionsInner type(
    BenchmarkCaseExpectationsQuestionsInnerTypeEnum type,
  );

  BenchmarkCaseExpectationsQuestionsInner outcome(Object? outcome);

  BenchmarkCaseExpectationsQuestionsInner resolvedAt(DateTime? resolvedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkCaseExpectationsQuestionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkCaseExpectationsQuestionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  BenchmarkCaseExpectationsQuestionsInner call({
    String text,
    BenchmarkCaseExpectationsQuestionsInnerTypeEnum type,
    Object? outcome,
    DateTime? resolvedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfBenchmarkCaseExpectationsQuestionsInner.copyWith(...)` or call `instanceOfBenchmarkCaseExpectationsQuestionsInner.copyWith.fieldName(value)` for a single field.
class _$BenchmarkCaseExpectationsQuestionsInnerCWProxyImpl
    implements _$BenchmarkCaseExpectationsQuestionsInnerCWProxy {
  const _$BenchmarkCaseExpectationsQuestionsInnerCWProxyImpl(this._value);

  final BenchmarkCaseExpectationsQuestionsInner _value;

  @override
  BenchmarkCaseExpectationsQuestionsInner text(String text) => call(text: text);

  @override
  BenchmarkCaseExpectationsQuestionsInner type(
    BenchmarkCaseExpectationsQuestionsInnerTypeEnum type,
  ) => call(type: type);

  @override
  BenchmarkCaseExpectationsQuestionsInner outcome(Object? outcome) =>
      call(outcome: outcome);

  @override
  BenchmarkCaseExpectationsQuestionsInner resolvedAt(DateTime? resolvedAt) =>
      call(resolvedAt: resolvedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `BenchmarkCaseExpectationsQuestionsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// BenchmarkCaseExpectationsQuestionsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  BenchmarkCaseExpectationsQuestionsInner call({
    Object? text = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? outcome = const $CopyWithPlaceholder(),
    Object? resolvedAt = const $CopyWithPlaceholder(),
  }) {
    return BenchmarkCaseExpectationsQuestionsInner(
      text: text == const $CopyWithPlaceholder() || text == null
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as BenchmarkCaseExpectationsQuestionsInnerTypeEnum,
      outcome: outcome == const $CopyWithPlaceholder()
          ? _value.outcome
          // ignore: cast_nullable_to_non_nullable
          : outcome as Object?,
      resolvedAt: resolvedAt == const $CopyWithPlaceholder()
          ? _value.resolvedAt
          // ignore: cast_nullable_to_non_nullable
          : resolvedAt as DateTime?,
    );
  }
}

extension $BenchmarkCaseExpectationsQuestionsInnerCopyWith
    on BenchmarkCaseExpectationsQuestionsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfBenchmarkCaseExpectationsQuestionsInner.copyWith(...)` or `instanceOfBenchmarkCaseExpectationsQuestionsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$BenchmarkCaseExpectationsQuestionsInnerCWProxy get copyWith =>
      _$BenchmarkCaseExpectationsQuestionsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

BenchmarkCaseExpectationsQuestionsInner
_$BenchmarkCaseExpectationsQuestionsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('BenchmarkCaseExpectationsQuestionsInner', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['text', 'type', 'outcome']);
      final val = BenchmarkCaseExpectationsQuestionsInner(
        text: $checkedConvert('text', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(
            _$BenchmarkCaseExpectationsQuestionsInnerTypeEnumEnumMap,
            v,
          ),
        ),
        outcome: $checkedConvert('outcome', (v) => v),
        resolvedAt: $checkedConvert(
          'resolvedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$BenchmarkCaseExpectationsQuestionsInnerToJson(
  BenchmarkCaseExpectationsQuestionsInner instance,
) => <String, dynamic>{
  'text': instance.text,
  'type':
      _$BenchmarkCaseExpectationsQuestionsInnerTypeEnumEnumMap[instance.type]!,
  'outcome': instance.outcome,
  'resolvedAt': ?instance.resolvedAt?.toIso8601String(),
};

const _$BenchmarkCaseExpectationsQuestionsInnerTypeEnumEnumMap = {
  BenchmarkCaseExpectationsQuestionsInnerTypeEnum.binary: 'binary',
  BenchmarkCaseExpectationsQuestionsInnerTypeEnum.numeric: 'numeric',
};
