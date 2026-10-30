// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'translation_settings.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TranslationSettingsCWProxy {
  TranslationSettings enabled(bool enabled);

  TranslationSettings targetLanguages(Set<String> targetLanguages);

  TranslationSettings fields(Set<String> fields);

  TranslationSettings engine(String engine);

  TranslationSettings budget(TranslationSettingsInputBudget? budget);

  TranslationSettings minSourceLanguageConfidence(
    num? minSourceLanguageConfidence,
  );

  TranslationSettings spend(LlmSpendWindow? spend);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TranslationSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TranslationSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  TranslationSettings call({
    bool enabled,
    Set<String> targetLanguages,
    Set<String> fields,
    String engine,
    TranslationSettingsInputBudget? budget,
    num? minSourceLanguageConfidence,
    LlmSpendWindow? spend,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTranslationSettings.copyWith(...)` or call `instanceOfTranslationSettings.copyWith.fieldName(value)` for a single field.
class _$TranslationSettingsCWProxyImpl implements _$TranslationSettingsCWProxy {
  const _$TranslationSettingsCWProxyImpl(this._value);

  final TranslationSettings _value;

  @override
  TranslationSettings enabled(bool enabled) => call(enabled: enabled);

  @override
  TranslationSettings targetLanguages(Set<String> targetLanguages) =>
      call(targetLanguages: targetLanguages);

  @override
  TranslationSettings fields(Set<String> fields) => call(fields: fields);

  @override
  TranslationSettings engine(String engine) => call(engine: engine);

  @override
  TranslationSettings budget(TranslationSettingsInputBudget? budget) =>
      call(budget: budget);

  @override
  TranslationSettings minSourceLanguageConfidence(
    num? minSourceLanguageConfidence,
  ) => call(minSourceLanguageConfidence: minSourceLanguageConfidence);

  @override
  TranslationSettings spend(LlmSpendWindow? spend) => call(spend: spend);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TranslationSettings(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TranslationSettings(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TranslationSettings call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? targetLanguages = const $CopyWithPlaceholder(),
    Object? fields = const $CopyWithPlaceholder(),
    Object? engine = const $CopyWithPlaceholder(),
    Object? budget = const $CopyWithPlaceholder(),
    Object? minSourceLanguageConfidence = const $CopyWithPlaceholder(),
    Object? spend = const $CopyWithPlaceholder(),
  }) {
    return TranslationSettings(
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      targetLanguages:
          targetLanguages == const $CopyWithPlaceholder() ||
              targetLanguages == null
          ? _value.targetLanguages
          // ignore: cast_nullable_to_non_nullable
          : targetLanguages as Set<String>,
      fields: fields == const $CopyWithPlaceholder() || fields == null
          ? _value.fields
          // ignore: cast_nullable_to_non_nullable
          : fields as Set<String>,
      engine: engine == const $CopyWithPlaceholder() || engine == null
          ? _value.engine
          // ignore: cast_nullable_to_non_nullable
          : engine as String,
      budget: budget == const $CopyWithPlaceholder()
          ? _value.budget
          // ignore: cast_nullable_to_non_nullable
          : budget as TranslationSettingsInputBudget?,
      minSourceLanguageConfidence:
          minSourceLanguageConfidence == const $CopyWithPlaceholder()
          ? _value.minSourceLanguageConfidence
          // ignore: cast_nullable_to_non_nullable
          : minSourceLanguageConfidence as num?,
      spend: spend == const $CopyWithPlaceholder()
          ? _value.spend
          // ignore: cast_nullable_to_non_nullable
          : spend as LlmSpendWindow?,
    );
  }
}

extension $TranslationSettingsCopyWith on TranslationSettings {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTranslationSettings.copyWith(...)` or `instanceOfTranslationSettings.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TranslationSettingsCWProxy get copyWith =>
      _$TranslationSettingsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TranslationSettings _$TranslationSettingsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TranslationSettings', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['enabled', 'targetLanguages', 'fields', 'engine'],
  );
  final val = TranslationSettings(
    enabled: $checkedConvert('enabled', (v) => v as bool? ?? false),
    targetLanguages: $checkedConvert(
      'targetLanguages',
      (v) => (v as List<dynamic>).map((e) => e as String).toSet(),
    ),
    fields: $checkedConvert(
      'fields',
      (v) => (v as List<dynamic>).map((e) => e as String).toSet(),
    ),
    engine: $checkedConvert('engine', (v) => v as String? ?? 'llm'),
    budget: $checkedConvert(
      'budget',
      (v) => v == null
          ? null
          : TranslationSettingsInputBudget.fromJson(v as Map<String, dynamic>),
    ),
    minSourceLanguageConfidence: $checkedConvert(
      'minSourceLanguageConfidence',
      (v) => v as num? ?? 0.8,
    ),
    spend: $checkedConvert(
      'spend',
      (v) =>
          v == null ? null : LlmSpendWindow.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$TranslationSettingsToJson(
  TranslationSettings instance,
) => <String, dynamic>{
  'enabled': instance.enabled,
  'targetLanguages': instance.targetLanguages.toList(),
  'fields': instance.fields.toList(),
  'engine': instance.engine,
  'budget': ?instance.budget?.toJson(),
  'minSourceLanguageConfidence': ?instance.minSourceLanguageConfidence,
  'spend': ?instance.spend?.toJson(),
};
