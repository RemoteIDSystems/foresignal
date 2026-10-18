// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_test_result_embedding.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmTestResultEmbeddingCWProxy {
  LlmTestResultEmbedding model(String? model);

  LlmTestResultEmbedding dimensions(int? dimensions);

  LlmTestResultEmbedding latencyMs(int? latencyMs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmTestResultEmbedding(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmTestResultEmbedding(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmTestResultEmbedding call({String? model, int? dimensions, int? latencyMs});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmTestResultEmbedding.copyWith(...)` or call `instanceOfLlmTestResultEmbedding.copyWith.fieldName(value)` for a single field.
class _$LlmTestResultEmbeddingCWProxyImpl
    implements _$LlmTestResultEmbeddingCWProxy {
  const _$LlmTestResultEmbeddingCWProxyImpl(this._value);

  final LlmTestResultEmbedding _value;

  @override
  LlmTestResultEmbedding model(String? model) => call(model: model);

  @override
  LlmTestResultEmbedding dimensions(int? dimensions) =>
      call(dimensions: dimensions);

  @override
  LlmTestResultEmbedding latencyMs(int? latencyMs) =>
      call(latencyMs: latencyMs);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmTestResultEmbedding(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmTestResultEmbedding(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmTestResultEmbedding call({
    Object? model = const $CopyWithPlaceholder(),
    Object? dimensions = const $CopyWithPlaceholder(),
    Object? latencyMs = const $CopyWithPlaceholder(),
  }) {
    return LlmTestResultEmbedding(
      model: model == const $CopyWithPlaceholder()
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as String?,
      dimensions: dimensions == const $CopyWithPlaceholder()
          ? _value.dimensions
          // ignore: cast_nullable_to_non_nullable
          : dimensions as int?,
      latencyMs: latencyMs == const $CopyWithPlaceholder()
          ? _value.latencyMs
          // ignore: cast_nullable_to_non_nullable
          : latencyMs as int?,
    );
  }
}

extension $LlmTestResultEmbeddingCopyWith on LlmTestResultEmbedding {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmTestResultEmbedding.copyWith(...)` or `instanceOfLlmTestResultEmbedding.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmTestResultEmbeddingCWProxy get copyWith =>
      _$LlmTestResultEmbeddingCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmTestResultEmbedding _$LlmTestResultEmbeddingFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('LlmTestResultEmbedding', json, ($checkedConvert) {
  final val = LlmTestResultEmbedding(
    model: $checkedConvert('model', (v) => v as String?),
    dimensions: $checkedConvert('dimensions', (v) => (v as num?)?.toInt()),
    latencyMs: $checkedConvert('latencyMs', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$LlmTestResultEmbeddingToJson(
  LlmTestResultEmbedding instance,
) => <String, dynamic>{
  'model': ?instance.model,
  'dimensions': ?instance.dimensions,
  'latencyMs': ?instance.latencyMs,
};
