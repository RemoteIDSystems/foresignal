// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'translation_settings_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TranslationSettingsInputCWProxy {
  TranslationSettingsInput enabled(bool enabled);

  TranslationSettingsInput targetLanguages(Set<String>? targetLanguages);

  TranslationSettingsInput fields(Set<String>? fields);

  TranslationSettingsInput engine(String? engine);

  TranslationSettingsInput budget(TranslationSettingsInputBudget? budget);

  TranslationSettingsInput minSourceLanguageConfidence(
    num? minSourceLanguageConfidence,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TranslationSettingsInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TranslationSettingsInput(...).copyWith(id: 12, name: "My name")
  /// ```
  TranslationSettingsInput call({
    bool enabled,
    Set<String>? targetLanguages,
    Set<String>? fields,
    String? engine,
    TranslationSettingsInputBudget? budget,
    num? minSourceLanguageConfidence,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTranslationSettingsInput.copyWith(...)` or call `instanceOfTranslationSettingsInput.copyWith.fieldName(value)` for a single field.
class _$TranslationSettingsInputCWProxyImpl
    implements _$TranslationSettingsInputCWProxy {
  const _$TranslationSettingsInputCWProxyImpl(this._value);

  final TranslationSettingsInput _value;

  @override
  TranslationSettingsInput enabled(bool enabled) => call(enabled: enabled);

  @override
  TranslationSettingsInput targetLanguages(Set<String>? targetLanguages) =>
      call(targetLanguages: targetLanguages);

  @override
  TranslationSettingsInput fields(Set<String>? fields) => call(fields: fields);

  @override
  TranslationSettingsInput engine(String? engine) => call(engine: engine);

  @override
  TranslationSettingsInput budget(TranslationSettingsInputBudget? budget) =>
      call(budget: budget);

  @override
  TranslationSettingsInput minSourceLanguageConfidence(
    num? minSourceLanguageConfidence,
  ) => call(minSourceLanguageConfidence: minSourceLanguageConfidence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TranslationSettingsInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TranslationSettingsInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TranslationSettingsInput call({
    Object? enabled = const $CopyWithPlaceholder(),
    Object? targetLanguages = const $CopyWithPlaceholder(),
    Object? fields = const $CopyWithPlaceholder(),
    Object? engine = const $CopyWithPlaceholder(),
    Object? budget = const $CopyWithPlaceholder(),
    Object? minSourceLanguageConfidence = const $CopyWithPlaceholder(),
  }) {
    return TranslationSettingsInput(
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      targetLanguages: targetLanguages == const $CopyWithPlaceholder()
          ? _value.targetLanguages
          // ignore: cast_nullable_to_non_nullable
          : targetLanguages as Set<String>?,
      fields: fields == const $CopyWithPlaceholder()
          ? _value.fields
          // ignore: cast_nullable_to_non_nullable
          : fields as Set<String>?,
      engine: engine == const $CopyWithPlaceholder()
          ? _value.engine
          // ignore: cast_nullable_to_non_nullable
          : engine as String?,
      budget: budget == const $CopyWithPlaceholder()
          ? _value.budget
          // ignore: cast_nullable_to_non_nullable
          : budget as TranslationSettingsInputBudget?,
      minSourceLanguageConfidence:
          minSourceLanguageConfidence == const $CopyWithPlaceholder()
          ? _value.minSourceLanguageConfidence
          // ignore: cast_nullable_to_non_nullable
          : minSourceLanguageConfidence as num?,
    );
  }
}

extension $TranslationSettingsInputCopyWith on TranslationSettingsInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTranslationSettingsInput.copyWith(...)` or `instanceOfTranslationSettingsInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TranslationSettingsInputCWProxy get copyWith =>
      _$TranslationSettingsInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TranslationSettingsInput _$TranslationSettingsInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('TranslationSettingsInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['enabled']);
  final val = TranslationSettingsInput(
    enabled: $checkedConvert('enabled', (v) => v as bool? ?? false),
    targetLanguages: $checkedConvert(
      'targetLanguages',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toSet(),
    ),
    fields: $checkedConvert(
      'fields',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toSet(),
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
  );
  return val;
});

Map<String, dynamic> _$TranslationSettingsInputToJson(
  TranslationSettingsInput instance,
) => <String, dynamic>{
  'enabled': instance.enabled,
  'targetLanguages': ?instance.targetLanguages?.toList(),
  'fields': ?instance.fields?.toList(),
  'engine': ?instance.engine,
  'budget': ?instance.budget?.toJson(),
  'minSourceLanguageConfidence': ?instance.minSourceLanguageConfidence,
};
