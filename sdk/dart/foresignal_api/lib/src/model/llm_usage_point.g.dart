// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_usage_point.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmUsagePointCWProxy {
  LlmUsagePoint t(DateTime t);

  LlmUsagePoint requests(int requests);

  LlmUsagePoint inputTokens(int inputTokens);

  LlmUsagePoint outputTokens(int outputTokens);

  LlmUsagePoint cachedTokens(int? cachedTokens);

  LlmUsagePoint errors(int? errors);

  LlmUsagePoint refused(int? refused);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmUsagePoint(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmUsagePoint(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmUsagePoint call({
    DateTime t,
    int requests,
    int inputTokens,
    int outputTokens,
    int? cachedTokens,
    int? errors,
    int? refused,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmUsagePoint.copyWith(...)` or call `instanceOfLlmUsagePoint.copyWith.fieldName(value)` for a single field.
class _$LlmUsagePointCWProxyImpl implements _$LlmUsagePointCWProxy {
  const _$LlmUsagePointCWProxyImpl(this._value);

  final LlmUsagePoint _value;

  @override
  LlmUsagePoint t(DateTime t) => call(t: t);

  @override
  LlmUsagePoint requests(int requests) => call(requests: requests);

  @override
  LlmUsagePoint inputTokens(int inputTokens) => call(inputTokens: inputTokens);

  @override
  LlmUsagePoint outputTokens(int outputTokens) =>
      call(outputTokens: outputTokens);

  @override
  LlmUsagePoint cachedTokens(int? cachedTokens) =>
      call(cachedTokens: cachedTokens);

  @override
  LlmUsagePoint errors(int? errors) => call(errors: errors);

  @override
  LlmUsagePoint refused(int? refused) => call(refused: refused);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmUsagePoint(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmUsagePoint(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmUsagePoint call({
    Object? t = const $CopyWithPlaceholder(),
    Object? requests = const $CopyWithPlaceholder(),
    Object? inputTokens = const $CopyWithPlaceholder(),
    Object? outputTokens = const $CopyWithPlaceholder(),
    Object? cachedTokens = const $CopyWithPlaceholder(),
    Object? errors = const $CopyWithPlaceholder(),
    Object? refused = const $CopyWithPlaceholder(),
  }) {
    return LlmUsagePoint(
      t: t == const $CopyWithPlaceholder() || t == null
          ? _value.t
          // ignore: cast_nullable_to_non_nullable
          : t as DateTime,
      requests: requests == const $CopyWithPlaceholder() || requests == null
          ? _value.requests
          // ignore: cast_nullable_to_non_nullable
          : requests as int,
      inputTokens:
          inputTokens == const $CopyWithPlaceholder() || inputTokens == null
          ? _value.inputTokens
          // ignore: cast_nullable_to_non_nullable
          : inputTokens as int,
      outputTokens:
          outputTokens == const $CopyWithPlaceholder() || outputTokens == null
          ? _value.outputTokens
          // ignore: cast_nullable_to_non_nullable
          : outputTokens as int,
      cachedTokens: cachedTokens == const $CopyWithPlaceholder()
          ? _value.cachedTokens
          // ignore: cast_nullable_to_non_nullable
          : cachedTokens as int?,
      errors: errors == const $CopyWithPlaceholder()
          ? _value.errors
          // ignore: cast_nullable_to_non_nullable
          : errors as int?,
      refused: refused == const $CopyWithPlaceholder()
          ? _value.refused
          // ignore: cast_nullable_to_non_nullable
          : refused as int?,
    );
  }
}

extension $LlmUsagePointCopyWith on LlmUsagePoint {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmUsagePoint.copyWith(...)` or `instanceOfLlmUsagePoint.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmUsagePointCWProxy get copyWith => _$LlmUsagePointCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmUsagePoint _$LlmUsagePointFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('LlmUsagePoint', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['t', 'requests', 'inputTokens', 'outputTokens'],
  );
  final val = LlmUsagePoint(
    t: $checkedConvert('t', (v) => DateTime.parse(v as String)),
    requests: $checkedConvert('requests', (v) => (v as num).toInt()),
    inputTokens: $checkedConvert('inputTokens', (v) => (v as num).toInt()),
    outputTokens: $checkedConvert('outputTokens', (v) => (v as num).toInt()),
    cachedTokens: $checkedConvert('cachedTokens', (v) => (v as num?)?.toInt()),
    errors: $checkedConvert('errors', (v) => (v as num?)?.toInt()),
    refused: $checkedConvert('refused', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$LlmUsagePointToJson(LlmUsagePoint instance) =>
    <String, dynamic>{
      't': instance.t.toIso8601String(),
      'requests': instance.requests,
      'inputTokens': instance.inputTokens,
      'outputTokens': instance.outputTokens,
      'cachedTokens': ?instance.cachedTokens,
      'errors': ?instance.errors,
      'refused': ?instance.refused,
    };
