// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_test_result_chat.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmTestResultChatCWProxy {
  LlmTestResultChat model(String? model);

  LlmTestResultChat latencyMs(int? latencyMs);

  LlmTestResultChat inputTokens(int? inputTokens);

  LlmTestResultChat outputTokens(int? outputTokens);

  LlmTestResultChat text(String? text);

  LlmTestResultChat cachedTokens(int? cachedTokens);

  LlmTestResultChat stopReason(LlmTestResultChatStopReasonEnum? stopReason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmTestResultChat(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmTestResultChat(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmTestResultChat call({
    String? model,
    int? latencyMs,
    int? inputTokens,
    int? outputTokens,
    String? text,
    int? cachedTokens,
    LlmTestResultChatStopReasonEnum? stopReason,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmTestResultChat.copyWith(...)` or call `instanceOfLlmTestResultChat.copyWith.fieldName(value)` for a single field.
class _$LlmTestResultChatCWProxyImpl implements _$LlmTestResultChatCWProxy {
  const _$LlmTestResultChatCWProxyImpl(this._value);

  final LlmTestResultChat _value;

  @override
  LlmTestResultChat model(String? model) => call(model: model);

  @override
  LlmTestResultChat latencyMs(int? latencyMs) => call(latencyMs: latencyMs);

  @override
  LlmTestResultChat inputTokens(int? inputTokens) =>
      call(inputTokens: inputTokens);

  @override
  LlmTestResultChat outputTokens(int? outputTokens) =>
      call(outputTokens: outputTokens);

  @override
  LlmTestResultChat text(String? text) => call(text: text);

  @override
  LlmTestResultChat cachedTokens(int? cachedTokens) =>
      call(cachedTokens: cachedTokens);

  @override
  LlmTestResultChat stopReason(LlmTestResultChatStopReasonEnum? stopReason) =>
      call(stopReason: stopReason);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmTestResultChat(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmTestResultChat(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmTestResultChat call({
    Object? model = const $CopyWithPlaceholder(),
    Object? latencyMs = const $CopyWithPlaceholder(),
    Object? inputTokens = const $CopyWithPlaceholder(),
    Object? outputTokens = const $CopyWithPlaceholder(),
    Object? text = const $CopyWithPlaceholder(),
    Object? cachedTokens = const $CopyWithPlaceholder(),
    Object? stopReason = const $CopyWithPlaceholder(),
  }) {
    return LlmTestResultChat(
      model: model == const $CopyWithPlaceholder()
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as String?,
      latencyMs: latencyMs == const $CopyWithPlaceholder()
          ? _value.latencyMs
          // ignore: cast_nullable_to_non_nullable
          : latencyMs as int?,
      inputTokens: inputTokens == const $CopyWithPlaceholder()
          ? _value.inputTokens
          // ignore: cast_nullable_to_non_nullable
          : inputTokens as int?,
      outputTokens: outputTokens == const $CopyWithPlaceholder()
          ? _value.outputTokens
          // ignore: cast_nullable_to_non_nullable
          : outputTokens as int?,
      text: text == const $CopyWithPlaceholder()
          ? _value.text
          // ignore: cast_nullable_to_non_nullable
          : text as String?,
      cachedTokens: cachedTokens == const $CopyWithPlaceholder()
          ? _value.cachedTokens
          // ignore: cast_nullable_to_non_nullable
          : cachedTokens as int?,
      stopReason: stopReason == const $CopyWithPlaceholder()
          ? _value.stopReason
          // ignore: cast_nullable_to_non_nullable
          : stopReason as LlmTestResultChatStopReasonEnum?,
    );
  }
}

extension $LlmTestResultChatCopyWith on LlmTestResultChat {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmTestResultChat.copyWith(...)` or `instanceOfLlmTestResultChat.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmTestResultChatCWProxy get copyWith =>
      _$LlmTestResultChatCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmTestResultChat _$LlmTestResultChatFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('LlmTestResultChat', json, ($checkedConvert) {
  final val = LlmTestResultChat(
    model: $checkedConvert('model', (v) => v as String?),
    latencyMs: $checkedConvert('latencyMs', (v) => (v as num?)?.toInt()),
    inputTokens: $checkedConvert('inputTokens', (v) => (v as num?)?.toInt()),
    outputTokens: $checkedConvert('outputTokens', (v) => (v as num?)?.toInt()),
    text: $checkedConvert('text', (v) => v as String?),
    cachedTokens: $checkedConvert('cachedTokens', (v) => (v as num?)?.toInt()),
    stopReason: $checkedConvert(
      'stopReason',
      (v) => $enumDecodeNullable(_$LlmTestResultChatStopReasonEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$LlmTestResultChatToJson(
  LlmTestResultChat instance,
) => <String, dynamic>{
  'model': ?instance.model,
  'latencyMs': ?instance.latencyMs,
  'inputTokens': ?instance.inputTokens,
  'outputTokens': ?instance.outputTokens,
  'text': ?instance.text,
  'cachedTokens': ?instance.cachedTokens,
  'stopReason': ?_$LlmTestResultChatStopReasonEnumEnumMap[instance.stopReason],
};

const _$LlmTestResultChatStopReasonEnumEnumMap = {
  LlmTestResultChatStopReasonEnum.end: 'end',
  LlmTestResultChatStopReasonEnum.toolUse: 'tool_use',
  LlmTestResultChatStopReasonEnum.maxTokens: 'max_tokens',
  LlmTestResultChatStopReasonEnum.refusal: 'refusal',
  LlmTestResultChatStopReasonEnum.error: 'error',
};
