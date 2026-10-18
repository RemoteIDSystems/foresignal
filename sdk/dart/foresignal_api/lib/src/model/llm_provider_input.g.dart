// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_provider_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmProviderInputCWProxy {
  LlmProviderInput id(String id);

  LlmProviderInput type(LlmProviderInputTypeEnum type);

  LlmProviderInput baseUrl(String? baseUrl);

  LlmProviderInput chatModel(String? chatModel);

  LlmProviderInput embeddingModel(String? embeddingModel);

  LlmProviderInput embeddingDimensions(int? embeddingDimensions);

  LlmProviderInput apiKey(String? apiKey);

  LlmProviderInput options(LlmProviderInputOptions? options);

  LlmProviderInput requestsPerMinute(int? requestsPerMinute);

  LlmProviderInput tokensPerDay(int? tokensPerDay);

  LlmProviderInput enabled(bool? enabled);

  LlmProviderInput knowledgeCutoff(DateTime? knowledgeCutoff);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProviderInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProviderInput(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmProviderInput call({
    String id,
    LlmProviderInputTypeEnum type,
    String? baseUrl,
    String? chatModel,
    String? embeddingModel,
    int? embeddingDimensions,
    String? apiKey,
    LlmProviderInputOptions? options,
    int? requestsPerMinute,
    int? tokensPerDay,
    bool? enabled,
    DateTime? knowledgeCutoff,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmProviderInput.copyWith(...)` or call `instanceOfLlmProviderInput.copyWith.fieldName(value)` for a single field.
class _$LlmProviderInputCWProxyImpl implements _$LlmProviderInputCWProxy {
  const _$LlmProviderInputCWProxyImpl(this._value);

  final LlmProviderInput _value;

  @override
  LlmProviderInput id(String id) => call(id: id);

  @override
  LlmProviderInput type(LlmProviderInputTypeEnum type) => call(type: type);

  @override
  LlmProviderInput baseUrl(String? baseUrl) => call(baseUrl: baseUrl);

  @override
  LlmProviderInput chatModel(String? chatModel) => call(chatModel: chatModel);

  @override
  LlmProviderInput embeddingModel(String? embeddingModel) =>
      call(embeddingModel: embeddingModel);

  @override
  LlmProviderInput embeddingDimensions(int? embeddingDimensions) =>
      call(embeddingDimensions: embeddingDimensions);

  @override
  LlmProviderInput apiKey(String? apiKey) => call(apiKey: apiKey);

  @override
  LlmProviderInput options(LlmProviderInputOptions? options) =>
      call(options: options);

  @override
  LlmProviderInput requestsPerMinute(int? requestsPerMinute) =>
      call(requestsPerMinute: requestsPerMinute);

  @override
  LlmProviderInput tokensPerDay(int? tokensPerDay) =>
      call(tokensPerDay: tokensPerDay);

  @override
  LlmProviderInput enabled(bool? enabled) => call(enabled: enabled);

  @override
  LlmProviderInput knowledgeCutoff(DateTime? knowledgeCutoff) =>
      call(knowledgeCutoff: knowledgeCutoff);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProviderInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProviderInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmProviderInput call({
    Object? id = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? baseUrl = const $CopyWithPlaceholder(),
    Object? chatModel = const $CopyWithPlaceholder(),
    Object? embeddingModel = const $CopyWithPlaceholder(),
    Object? embeddingDimensions = const $CopyWithPlaceholder(),
    Object? apiKey = const $CopyWithPlaceholder(),
    Object? options = const $CopyWithPlaceholder(),
    Object? requestsPerMinute = const $CopyWithPlaceholder(),
    Object? tokensPerDay = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? knowledgeCutoff = const $CopyWithPlaceholder(),
  }) {
    return LlmProviderInput(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as LlmProviderInputTypeEnum,
      baseUrl: baseUrl == const $CopyWithPlaceholder()
          ? _value.baseUrl
          // ignore: cast_nullable_to_non_nullable
          : baseUrl as String?,
      chatModel: chatModel == const $CopyWithPlaceholder()
          ? _value.chatModel
          // ignore: cast_nullable_to_non_nullable
          : chatModel as String?,
      embeddingModel: embeddingModel == const $CopyWithPlaceholder()
          ? _value.embeddingModel
          // ignore: cast_nullable_to_non_nullable
          : embeddingModel as String?,
      embeddingDimensions: embeddingDimensions == const $CopyWithPlaceholder()
          ? _value.embeddingDimensions
          // ignore: cast_nullable_to_non_nullable
          : embeddingDimensions as int?,
      apiKey: apiKey == const $CopyWithPlaceholder()
          ? _value.apiKey
          // ignore: cast_nullable_to_non_nullable
          : apiKey as String?,
      options: options == const $CopyWithPlaceholder()
          ? _value.options
          // ignore: cast_nullable_to_non_nullable
          : options as LlmProviderInputOptions?,
      requestsPerMinute: requestsPerMinute == const $CopyWithPlaceholder()
          ? _value.requestsPerMinute
          // ignore: cast_nullable_to_non_nullable
          : requestsPerMinute as int?,
      tokensPerDay: tokensPerDay == const $CopyWithPlaceholder()
          ? _value.tokensPerDay
          // ignore: cast_nullable_to_non_nullable
          : tokensPerDay as int?,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      knowledgeCutoff: knowledgeCutoff == const $CopyWithPlaceholder()
          ? _value.knowledgeCutoff
          // ignore: cast_nullable_to_non_nullable
          : knowledgeCutoff as DateTime?,
    );
  }
}

extension $LlmProviderInputCopyWith on LlmProviderInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmProviderInput.copyWith(...)` or `instanceOfLlmProviderInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmProviderInputCWProxy get copyWith => _$LlmProviderInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmProviderInput _$LlmProviderInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LlmProviderInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'type']);
      final val = LlmProviderInput(
        id: $checkedConvert('id', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$LlmProviderInputTypeEnumEnumMap, v),
        ),
        baseUrl: $checkedConvert('baseUrl', (v) => v as String?),
        chatModel: $checkedConvert('chatModel', (v) => v as String?),
        embeddingModel: $checkedConvert('embeddingModel', (v) => v as String?),
        embeddingDimensions: $checkedConvert(
          'embeddingDimensions',
          (v) => (v as num?)?.toInt(),
        ),
        apiKey: $checkedConvert('apiKey', (v) => v as String?),
        options: $checkedConvert(
          'options',
          (v) => v == null
              ? null
              : LlmProviderInputOptions.fromJson(v as Map<String, dynamic>),
        ),
        requestsPerMinute: $checkedConvert(
          'requestsPerMinute',
          (v) => (v as num?)?.toInt() ?? 30,
        ),
        tokensPerDay: $checkedConvert(
          'tokensPerDay',
          (v) => (v as num?)?.toInt() ?? 2000000,
        ),
        enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
        knowledgeCutoff: $checkedConvert(
          'knowledgeCutoff',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$LlmProviderInputToJson(LlmProviderInput instance) =>
    <String, dynamic>{
      'id': instance.id,
      'type': _$LlmProviderInputTypeEnumEnumMap[instance.type]!,
      'baseUrl': ?instance.baseUrl,
      'chatModel': ?instance.chatModel,
      'embeddingModel': ?instance.embeddingModel,
      'embeddingDimensions': ?instance.embeddingDimensions,
      'apiKey': ?instance.apiKey,
      'options': ?instance.options?.toJson(),
      'requestsPerMinute': ?instance.requestsPerMinute,
      'tokensPerDay': ?instance.tokensPerDay,
      'enabled': ?instance.enabled,
      'knowledgeCutoff': ?instance.knowledgeCutoff?.toIso8601String(),
    };

const _$LlmProviderInputTypeEnumEnumMap = {
  LlmProviderInputTypeEnum.anthropic: 'anthropic',
  LlmProviderInputTypeEnum.openaiCompatible: 'openai-compatible',
  LlmProviderInputTypeEnum.ollama: 'ollama',
};
