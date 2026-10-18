// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_suggest_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingSuggestResultCWProxy {
  MappingSuggestResult draft(RecordMapping draft);

  MappingSuggestResult explanation(String explanation);

  MappingSuggestResult warnings(List<MappingWarning> warnings);

  MappingSuggestResult preview(MappingPreviewResult? preview);

  MappingSuggestResult repairRounds(int? repairRounds);

  MappingSuggestResult provider(String provider);

  MappingSuggestResult model(String model);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingSuggestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingSuggestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingSuggestResult call({
    RecordMapping draft,
    String explanation,
    List<MappingWarning> warnings,
    MappingPreviewResult? preview,
    int? repairRounds,
    String provider,
    String model,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingSuggestResult.copyWith(...)` or call `instanceOfMappingSuggestResult.copyWith.fieldName(value)` for a single field.
class _$MappingSuggestResultCWProxyImpl
    implements _$MappingSuggestResultCWProxy {
  const _$MappingSuggestResultCWProxyImpl(this._value);

  final MappingSuggestResult _value;

  @override
  MappingSuggestResult draft(RecordMapping draft) => call(draft: draft);

  @override
  MappingSuggestResult explanation(String explanation) =>
      call(explanation: explanation);

  @override
  MappingSuggestResult warnings(List<MappingWarning> warnings) =>
      call(warnings: warnings);

  @override
  MappingSuggestResult preview(MappingPreviewResult? preview) =>
      call(preview: preview);

  @override
  MappingSuggestResult repairRounds(int? repairRounds) =>
      call(repairRounds: repairRounds);

  @override
  MappingSuggestResult provider(String provider) => call(provider: provider);

  @override
  MappingSuggestResult model(String model) => call(model: model);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingSuggestResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingSuggestResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingSuggestResult call({
    Object? draft = const $CopyWithPlaceholder(),
    Object? explanation = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
    Object? preview = const $CopyWithPlaceholder(),
    Object? repairRounds = const $CopyWithPlaceholder(),
    Object? provider = const $CopyWithPlaceholder(),
    Object? model = const $CopyWithPlaceholder(),
  }) {
    return MappingSuggestResult(
      draft: draft == const $CopyWithPlaceholder() || draft == null
          ? _value.draft
          // ignore: cast_nullable_to_non_nullable
          : draft as RecordMapping,
      explanation:
          explanation == const $CopyWithPlaceholder() || explanation == null
          ? _value.explanation
          // ignore: cast_nullable_to_non_nullable
          : explanation as String,
      warnings: warnings == const $CopyWithPlaceholder() || warnings == null
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<MappingWarning>,
      preview: preview == const $CopyWithPlaceholder()
          ? _value.preview
          // ignore: cast_nullable_to_non_nullable
          : preview as MappingPreviewResult?,
      repairRounds: repairRounds == const $CopyWithPlaceholder()
          ? _value.repairRounds
          // ignore: cast_nullable_to_non_nullable
          : repairRounds as int?,
      provider: provider == const $CopyWithPlaceholder() || provider == null
          ? _value.provider
          // ignore: cast_nullable_to_non_nullable
          : provider as String,
      model: model == const $CopyWithPlaceholder() || model == null
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as String,
    );
  }
}

extension $MappingSuggestResultCopyWith on MappingSuggestResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingSuggestResult.copyWith(...)` or `instanceOfMappingSuggestResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingSuggestResultCWProxy get copyWith =>
      _$MappingSuggestResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingSuggestResult _$MappingSuggestResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MappingSuggestResult', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'draft',
      'explanation',
      'warnings',
      'provider',
      'model',
    ],
  );
  final val = MappingSuggestResult(
    draft: $checkedConvert(
      'draft',
      (v) => RecordMapping.fromJson(v as Map<String, dynamic>),
    ),
    explanation: $checkedConvert('explanation', (v) => v as String),
    warnings: $checkedConvert(
      'warnings',
      (v) => (v as List<dynamic>)
          .map((e) => MappingWarning.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    preview: $checkedConvert(
      'preview',
      (v) => v == null
          ? null
          : MappingPreviewResult.fromJson(v as Map<String, dynamic>),
    ),
    repairRounds: $checkedConvert('repairRounds', (v) => (v as num?)?.toInt()),
    provider: $checkedConvert('provider', (v) => v as String),
    model: $checkedConvert('model', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$MappingSuggestResultToJson(
  MappingSuggestResult instance,
) => <String, dynamic>{
  'draft': instance.draft.toJson(),
  'explanation': instance.explanation,
  'warnings': instance.warnings.map((e) => e.toJson()).toList(),
  'preview': ?instance.preview?.toJson(),
  'repairRounds': ?instance.repairRounds,
  'provider': instance.provider,
  'model': instance.model,
};
