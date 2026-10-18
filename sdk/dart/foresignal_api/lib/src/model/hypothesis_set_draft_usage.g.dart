// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_set_draft_usage.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisSetDraftUsageCWProxy {
  HypothesisSetDraftUsage inputTokens(int? inputTokens);

  HypothesisSetDraftUsage outputTokens(int? outputTokens);

  HypothesisSetDraftUsage cachedTokens(int? cachedTokens);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetDraftUsage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetDraftUsage(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisSetDraftUsage call({
    int? inputTokens,
    int? outputTokens,
    int? cachedTokens,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisSetDraftUsage.copyWith(...)` or call `instanceOfHypothesisSetDraftUsage.copyWith.fieldName(value)` for a single field.
class _$HypothesisSetDraftUsageCWProxyImpl
    implements _$HypothesisSetDraftUsageCWProxy {
  const _$HypothesisSetDraftUsageCWProxyImpl(this._value);

  final HypothesisSetDraftUsage _value;

  @override
  HypothesisSetDraftUsage inputTokens(int? inputTokens) =>
      call(inputTokens: inputTokens);

  @override
  HypothesisSetDraftUsage outputTokens(int? outputTokens) =>
      call(outputTokens: outputTokens);

  @override
  HypothesisSetDraftUsage cachedTokens(int? cachedTokens) =>
      call(cachedTokens: cachedTokens);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisSetDraftUsage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisSetDraftUsage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisSetDraftUsage call({
    Object? inputTokens = const $CopyWithPlaceholder(),
    Object? outputTokens = const $CopyWithPlaceholder(),
    Object? cachedTokens = const $CopyWithPlaceholder(),
  }) {
    return HypothesisSetDraftUsage(
      inputTokens: inputTokens == const $CopyWithPlaceholder()
          ? _value.inputTokens
          // ignore: cast_nullable_to_non_nullable
          : inputTokens as int?,
      outputTokens: outputTokens == const $CopyWithPlaceholder()
          ? _value.outputTokens
          // ignore: cast_nullable_to_non_nullable
          : outputTokens as int?,
      cachedTokens: cachedTokens == const $CopyWithPlaceholder()
          ? _value.cachedTokens
          // ignore: cast_nullable_to_non_nullable
          : cachedTokens as int?,
    );
  }
}

extension $HypothesisSetDraftUsageCopyWith on HypothesisSetDraftUsage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisSetDraftUsage.copyWith(...)` or `instanceOfHypothesisSetDraftUsage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisSetDraftUsageCWProxy get copyWith =>
      _$HypothesisSetDraftUsageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisSetDraftUsage _$HypothesisSetDraftUsageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisSetDraftUsage', json, ($checkedConvert) {
  final val = HypothesisSetDraftUsage(
    inputTokens: $checkedConvert('inputTokens', (v) => (v as num?)?.toInt()),
    outputTokens: $checkedConvert('outputTokens', (v) => (v as num?)?.toInt()),
    cachedTokens: $checkedConvert('cachedTokens', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$HypothesisSetDraftUsageToJson(
  HypothesisSetDraftUsage instance,
) => <String, dynamic>{
  'inputTokens': ?instance.inputTokens,
  'outputTokens': ?instance.outputTokens,
  'cachedTokens': ?instance.cachedTokens,
};
