// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'llm_provider_input_options.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LlmProviderInputOptionsCWProxy {
  LlmProviderInputOptions effort(LlmProviderInputOptionsEffortEnum? effort);

  LlmProviderInputOptions maxTokens(int? maxTokens);

  LlmProviderInputOptions temperature(num? temperature);

  LlmProviderInputOptions extraHeaders(Map<String, String>? extraHeaders);

  LlmProviderInputOptions local(bool? local);

  LlmProviderInputOptions ttsModel(String? ttsModel);

  LlmProviderInputOptions ttsCharactersPerDay(int? ttsCharactersPerDay);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProviderInputOptions(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProviderInputOptions(...).copyWith(id: 12, name: "My name")
  /// ```
  LlmProviderInputOptions call({
    LlmProviderInputOptionsEffortEnum? effort,
    int? maxTokens,
    num? temperature,
    Map<String, String>? extraHeaders,
    bool? local,
    String? ttsModel,
    int? ttsCharactersPerDay,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLlmProviderInputOptions.copyWith(...)` or call `instanceOfLlmProviderInputOptions.copyWith.fieldName(value)` for a single field.
class _$LlmProviderInputOptionsCWProxyImpl
    implements _$LlmProviderInputOptionsCWProxy {
  const _$LlmProviderInputOptionsCWProxyImpl(this._value);

  final LlmProviderInputOptions _value;

  @override
  LlmProviderInputOptions effort(LlmProviderInputOptionsEffortEnum? effort) =>
      call(effort: effort);

  @override
  LlmProviderInputOptions maxTokens(int? maxTokens) =>
      call(maxTokens: maxTokens);

  @override
  LlmProviderInputOptions temperature(num? temperature) =>
      call(temperature: temperature);

  @override
  LlmProviderInputOptions extraHeaders(Map<String, String>? extraHeaders) =>
      call(extraHeaders: extraHeaders);

  @override
  LlmProviderInputOptions local(bool? local) => call(local: local);

  @override
  LlmProviderInputOptions ttsModel(String? ttsModel) =>
      call(ttsModel: ttsModel);

  @override
  LlmProviderInputOptions ttsCharactersPerDay(int? ttsCharactersPerDay) =>
      call(ttsCharactersPerDay: ttsCharactersPerDay);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LlmProviderInputOptions(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LlmProviderInputOptions(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LlmProviderInputOptions call({
    Object? effort = const $CopyWithPlaceholder(),
    Object? maxTokens = const $CopyWithPlaceholder(),
    Object? temperature = const $CopyWithPlaceholder(),
    Object? extraHeaders = const $CopyWithPlaceholder(),
    Object? local = const $CopyWithPlaceholder(),
    Object? ttsModel = const $CopyWithPlaceholder(),
    Object? ttsCharactersPerDay = const $CopyWithPlaceholder(),
  }) {
    return LlmProviderInputOptions(
      effort: effort == const $CopyWithPlaceholder()
          ? _value.effort
          // ignore: cast_nullable_to_non_nullable
          : effort as LlmProviderInputOptionsEffortEnum?,
      maxTokens: maxTokens == const $CopyWithPlaceholder()
          ? _value.maxTokens
          // ignore: cast_nullable_to_non_nullable
          : maxTokens as int?,
      temperature: temperature == const $CopyWithPlaceholder()
          ? _value.temperature
          // ignore: cast_nullable_to_non_nullable
          : temperature as num?,
      extraHeaders: extraHeaders == const $CopyWithPlaceholder()
          ? _value.extraHeaders
          // ignore: cast_nullable_to_non_nullable
          : extraHeaders as Map<String, String>?,
      local: local == const $CopyWithPlaceholder()
          ? _value.local
          // ignore: cast_nullable_to_non_nullable
          : local as bool?,
      ttsModel: ttsModel == const $CopyWithPlaceholder()
          ? _value.ttsModel
          // ignore: cast_nullable_to_non_nullable
          : ttsModel as String?,
      ttsCharactersPerDay: ttsCharactersPerDay == const $CopyWithPlaceholder()
          ? _value.ttsCharactersPerDay
          // ignore: cast_nullable_to_non_nullable
          : ttsCharactersPerDay as int?,
    );
  }
}

extension $LlmProviderInputOptionsCopyWith on LlmProviderInputOptions {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLlmProviderInputOptions.copyWith(...)` or `instanceOfLlmProviderInputOptions.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LlmProviderInputOptionsCWProxy get copyWith =>
      _$LlmProviderInputOptionsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LlmProviderInputOptions _$LlmProviderInputOptionsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('LlmProviderInputOptions', json, ($checkedConvert) {
  final val = LlmProviderInputOptions(
    effort: $checkedConvert(
      'effort',
      (v) => $enumDecodeNullable(_$LlmProviderInputOptionsEffortEnumEnumMap, v),
    ),
    maxTokens: $checkedConvert('maxTokens', (v) => (v as num?)?.toInt()),
    temperature: $checkedConvert('temperature', (v) => v as num?),
    extraHeaders: $checkedConvert(
      'extraHeaders',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
    local: $checkedConvert('local', (v) => v as bool? ?? false),
    ttsModel: $checkedConvert('ttsModel', (v) => v as String? ?? 'tts-1'),
    ttsCharactersPerDay: $checkedConvert(
      'ttsCharactersPerDay',
      (v) => (v as num?)?.toInt() ?? 100000,
    ),
  );
  return val;
});

Map<String, dynamic> _$LlmProviderInputOptionsToJson(
  LlmProviderInputOptions instance,
) => <String, dynamic>{
  'effort': ?_$LlmProviderInputOptionsEffortEnumEnumMap[instance.effort],
  'maxTokens': ?instance.maxTokens,
  'temperature': ?instance.temperature,
  'extraHeaders': ?instance.extraHeaders,
  'local': ?instance.local,
  'ttsModel': ?instance.ttsModel,
  'ttsCharactersPerDay': ?instance.ttsCharactersPerDay,
};

const _$LlmProviderInputOptionsEffortEnumEnumMap = {
  LlmProviderInputOptionsEffortEnum.low: 'low',
  LlmProviderInputOptionsEffortEnum.medium: 'medium',
  LlmProviderInputOptionsEffortEnum.high: 'high',
  LlmProviderInputOptionsEffortEnum.xhigh: 'xhigh',
  LlmProviderInputOptionsEffortEnum.max: 'max',
};
