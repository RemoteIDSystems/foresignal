// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_test_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmTestResultCWProxy {
  LlmTestResult success(bool success);

  LlmTestResult chat(LlmTestResultChat? chat);

  LlmTestResult embedding(LlmTestResultEmbedding? embedding);

  LlmTestResult error(String? error);

  LlmTestResult refusalCategory(String? refusalCategory);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmTestResult call({
    bool success,
    LlmTestResultChat? chat,
    LlmTestResultEmbedding? embedding,
    String? error,
    String? refusalCategory,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmTestResult.copyWith(...)` or call `instanceOfLlmTestResult.copyWith.fieldName(value)` for a single field.
class _$LlmTestResultCWProxyImpl implements _$LlmTestResultCWProxy {
  const _$LlmTestResultCWProxyImpl(this._value);

  final LlmTestResult _value;

  @override
  LlmTestResult success(bool success) => call(success: success);

  @override
  LlmTestResult chat(LlmTestResultChat? chat) => call(chat: chat);

  @override
  LlmTestResult embedding(LlmTestResultEmbedding? embedding) =>
      call(embedding: embedding);

  @override
  LlmTestResult error(String? error) => call(error: error);

  @override
  LlmTestResult refusalCategory(String? refusalCategory) =>
      call(refusalCategory: refusalCategory);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmTestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmTestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmTestResult call({
    Object? success = const $CopyWithPlaceholder(),
    Object? chat = const $CopyWithPlaceholder(),
    Object? embedding = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
    Object? refusalCategory = const $CopyWithPlaceholder(),
  }) {
    return LlmTestResult(
      success: success == const $CopyWithPlaceholder() || success == null
          ? _value.success
          // ignore: cast_nullable_to_non_nullable
          : success as bool,
      chat: chat == const $CopyWithPlaceholder()
          ? _value.chat
          // ignore: cast_nullable_to_non_nullable
          : chat as LlmTestResultChat?,
      embedding: embedding == const $CopyWithPlaceholder()
          ? _value.embedding
          // ignore: cast_nullable_to_non_nullable
          : embedding as LlmTestResultEmbedding?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
      refusalCategory: refusalCategory == const $CopyWithPlaceholder()
          ? _value.refusalCategory
          // ignore: cast_nullable_to_non_nullable
          : refusalCategory as String?,
    );
  }
}

extension $LlmTestResultCopyWith on LlmTestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmTestResult.copyWith(...)` or `instanceOfLlmTestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmTestResultCWProxy get copyWith => _$LlmTestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmTestResult _$LlmTestResultFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LlmTestResult', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['success']);
      final val = LlmTestResult(
        success: $checkedConvert('success', (v) => v as bool),
        chat: $checkedConvert(
          'chat',
          (v) => v == null
              ? null
              : LlmTestResultChat.fromJson(v as Map<String, dynamic>),
        ),
        embedding: $checkedConvert(
          'embedding',
          (v) => v == null
              ? null
              : LlmTestResultEmbedding.fromJson(v as Map<String, dynamic>),
        ),
        error: $checkedConvert('error', (v) => v as String?),
        refusalCategory: $checkedConvert(
          'refusalCategory',
          (v) => v as String?,
        ),
      );
      return val;
    });

Map<String, dynamic> _$LlmTestResultToJson(LlmTestResult instance) =>
    <String, dynamic>{
      'success': instance.success,
      'chat': ?instance.chat?.toJson(),
      'embedding': ?instance.embedding?.toJson(),
      'error': ?instance.error,
      'refusalCategory': ?instance.refusalCategory,
    };
