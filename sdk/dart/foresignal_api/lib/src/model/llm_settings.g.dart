// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmSettingsCWProxy {
  LlmSettings chatProvider(String? chatProvider);

  LlmSettings embeddingProvider(String? embeddingProvider);

  LlmSettings askEnabled(bool? askEnabled);

  LlmSettings enrichmentEnabled(bool? enrichmentEnabled);

  LlmSettings logContent(bool? logContent);

  LlmSettings askMaxTokens(int? askMaxTokens);

  LlmSettings allowPrivateLocal(bool? allowPrivateLocal);

  LlmSettings ttsProvider(String? ttsProvider);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmSettings call({
    String? chatProvider,
    String? embeddingProvider,
    bool? askEnabled,
    bool? enrichmentEnabled,
    bool? logContent,
    int? askMaxTokens,
    bool? allowPrivateLocal,
    String? ttsProvider,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmSettings.copyWith(...)` or call `instanceOfLlmSettings.copyWith.fieldName(value)` for a single field.
class _$LlmSettingsCWProxyImpl implements _$LlmSettingsCWProxy {
  const _$LlmSettingsCWProxyImpl(this._value);

  final LlmSettings _value;

  @override
  LlmSettings chatProvider(String? chatProvider) =>
      call(chatProvider: chatProvider);

  @override
  LlmSettings embeddingProvider(String? embeddingProvider) =>
      call(embeddingProvider: embeddingProvider);

  @override
  LlmSettings askEnabled(bool? askEnabled) => call(askEnabled: askEnabled);

  @override
  LlmSettings enrichmentEnabled(bool? enrichmentEnabled) =>
      call(enrichmentEnabled: enrichmentEnabled);

  @override
  LlmSettings logContent(bool? logContent) => call(logContent: logContent);

  @override
  LlmSettings askMaxTokens(int? askMaxTokens) =>
      call(askMaxTokens: askMaxTokens);

  @override
  LlmSettings allowPrivateLocal(bool? allowPrivateLocal) =>
      call(allowPrivateLocal: allowPrivateLocal);

  @override
  LlmSettings ttsProvider(String? ttsProvider) =>
      call(ttsProvider: ttsProvider);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmSettings call({
    Object? chatProvider = const $CopyWithPlaceholder(),
    Object? embeddingProvider = const $CopyWithPlaceholder(),
    Object? askEnabled = const $CopyWithPlaceholder(),
    Object? enrichmentEnabled = const $CopyWithPlaceholder(),
    Object? logContent = const $CopyWithPlaceholder(),
    Object? askMaxTokens = const $CopyWithPlaceholder(),
    Object? allowPrivateLocal = const $CopyWithPlaceholder(),
    Object? ttsProvider = const $CopyWithPlaceholder(),
  }) {
    return LlmSettings(
      chatProvider: chatProvider == const $CopyWithPlaceholder()
          ? _value.chatProvider
          // ignore: cast_nullable_to_non_nullable
          : chatProvider as String?,
      embeddingProvider: embeddingProvider == const $CopyWithPlaceholder()
          ? _value.embeddingProvider
          // ignore: cast_nullable_to_non_nullable
          : embeddingProvider as String?,
      askEnabled: askEnabled == const $CopyWithPlaceholder()
          ? _value.askEnabled
          // ignore: cast_nullable_to_non_nullable
          : askEnabled as bool?,
      enrichmentEnabled: enrichmentEnabled == const $CopyWithPlaceholder()
          ? _value.enrichmentEnabled
          // ignore: cast_nullable_to_non_nullable
          : enrichmentEnabled as bool?,
      logContent: logContent == const $CopyWithPlaceholder()
          ? _value.logContent
          // ignore: cast_nullable_to_non_nullable
          : logContent as bool?,
      askMaxTokens: askMaxTokens == const $CopyWithPlaceholder()
          ? _value.askMaxTokens
          // ignore: cast_nullable_to_non_nullable
          : askMaxTokens as int?,
      allowPrivateLocal: allowPrivateLocal == const $CopyWithPlaceholder()
          ? _value.allowPrivateLocal
          // ignore: cast_nullable_to_non_nullable
          : allowPrivateLocal as bool?,
      ttsProvider: ttsProvider == const $CopyWithPlaceholder()
          ? _value.ttsProvider
          // ignore: cast_nullable_to_non_nullable
          : ttsProvider as String?,
    );
  }
}

extension $LlmSettingsCopyWith on LlmSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmSettings.copyWith(...)` or `instanceOfLlmSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmSettingsCWProxy get copyWith => _$LlmSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmSettings _$LlmSettingsFromJson(Map<String, dynamic> json) =>
    $checkedCreate('LlmSettings', json, ($checkedConvert) {
      final val = LlmSettings(
        chatProvider: $checkedConvert('chatProvider', (v) => v as String?),
        embeddingProvider: $checkedConvert(
          'embeddingProvider',
          (v) => v as String?,
        ),
        askEnabled: $checkedConvert('askEnabled', (v) => v as bool? ?? true),
        enrichmentEnabled: $checkedConvert(
          'enrichmentEnabled',
          (v) => v as bool? ?? true,
        ),
        logContent: $checkedConvert('logContent', (v) => v as bool? ?? false),
        askMaxTokens: $checkedConvert(
          'askMaxTokens',
          (v) => (v as num?)?.toInt() ?? 200000,
        ),
        allowPrivateLocal: $checkedConvert(
          'allowPrivateLocal',
          (v) => v as bool? ?? false,
        ),
        ttsProvider: $checkedConvert('ttsProvider', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$LlmSettingsToJson(LlmSettings instance) =>
    <String, dynamic>{
      'chatProvider': ?instance.chatProvider,
      'embeddingProvider': ?instance.embeddingProvider,
      'askEnabled': ?instance.askEnabled,
      'enrichmentEnabled': ?instance.enrichmentEnabled,
      'logContent': ?instance.logContent,
      'askMaxTokens': ?instance.askMaxTokens,
      'allowPrivateLocal': ?instance.allowPrivateLocal,
      'ttsProvider': ?instance.ttsProvider,
    };
