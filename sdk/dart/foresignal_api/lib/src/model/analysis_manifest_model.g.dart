// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analysis_manifest_model.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalysisManifestModelCWProxy {
  AnalysisManifestModel provider(String provider);

  AnalysisManifestModel model(String model);

  AnalysisManifestModel effort(String? effort);

  AnalysisManifestModel promptHash(String? promptHash);

  AnalysisManifestModel toolsHash(String? toolsHash);

  AnalysisManifestModel samples(int? samples);

  AnalysisManifestModel templates(
    List<AnalysisManifestModelTemplatesInner>? templates,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisManifestModel(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisManifestModel(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalysisManifestModel call({
    String provider,
    String model,
    String? effort,
    String? promptHash,
    String? toolsHash,
    int? samples,
    List<AnalysisManifestModelTemplatesInner>? templates,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalysisManifestModel.copyWith(...)` or call `instanceOfAnalysisManifestModel.copyWith.fieldName(value)` for a single field.
class _$AnalysisManifestModelCWProxyImpl
    implements _$AnalysisManifestModelCWProxy {
  const _$AnalysisManifestModelCWProxyImpl(this._value);

  final AnalysisManifestModel _value;

  @override
  AnalysisManifestModel provider(String provider) => call(provider: provider);

  @override
  AnalysisManifestModel model(String model) => call(model: model);

  @override
  AnalysisManifestModel effort(String? effort) => call(effort: effort);

  @override
  AnalysisManifestModel promptHash(String? promptHash) =>
      call(promptHash: promptHash);

  @override
  AnalysisManifestModel toolsHash(String? toolsHash) =>
      call(toolsHash: toolsHash);

  @override
  AnalysisManifestModel samples(int? samples) => call(samples: samples);

  @override
  AnalysisManifestModel templates(
    List<AnalysisManifestModelTemplatesInner>? templates,
  ) => call(templates: templates);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisManifestModel(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisManifestModel(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalysisManifestModel call({
    Object? provider = const $CopyWithPlaceholder(),
    Object? model = const $CopyWithPlaceholder(),
    Object? effort = const $CopyWithPlaceholder(),
    Object? promptHash = const $CopyWithPlaceholder(),
    Object? toolsHash = const $CopyWithPlaceholder(),
    Object? samples = const $CopyWithPlaceholder(),
    Object? templates = const $CopyWithPlaceholder(),
  }) {
    return AnalysisManifestModel(
      provider: provider == const $CopyWithPlaceholder() || provider == null
          ? _value.provider
          // ignore: cast_nullable_to_non_nullable
          : provider as String,
      model: model == const $CopyWithPlaceholder() || model == null
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as String,
      effort: effort == const $CopyWithPlaceholder()
          ? _value.effort
          // ignore: cast_nullable_to_non_nullable
          : effort as String?,
      promptHash: promptHash == const $CopyWithPlaceholder()
          ? _value.promptHash
          // ignore: cast_nullable_to_non_nullable
          : promptHash as String?,
      toolsHash: toolsHash == const $CopyWithPlaceholder()
          ? _value.toolsHash
          // ignore: cast_nullable_to_non_nullable
          : toolsHash as String?,
      samples: samples == const $CopyWithPlaceholder()
          ? _value.samples
          // ignore: cast_nullable_to_non_nullable
          : samples as int?,
      templates: templates == const $CopyWithPlaceholder()
          ? _value.templates
          // ignore: cast_nullable_to_non_nullable
          : templates as List<AnalysisManifestModelTemplatesInner>?,
    );
  }
}

extension $AnalysisManifestModelCopyWith on AnalysisManifestModel {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalysisManifestModel.copyWith(...)` or `instanceOfAnalysisManifestModel.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalysisManifestModelCWProxy get copyWith =>
      _$AnalysisManifestModelCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalysisManifestModel _$AnalysisManifestModelFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalysisManifestModel', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['provider', 'model']);
  final val = AnalysisManifestModel(
    provider: $checkedConvert('provider', (v) => v as String),
    model: $checkedConvert('model', (v) => v as String),
    effort: $checkedConvert('effort', (v) => v as String?),
    promptHash: $checkedConvert('promptHash', (v) => v as String?),
    toolsHash: $checkedConvert('toolsHash', (v) => v as String?),
    samples: $checkedConvert('samples', (v) => (v as num?)?.toInt()),
    templates: $checkedConvert(
      'templates',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => AnalysisManifestModelTemplatesInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$AnalysisManifestModelToJson(
  AnalysisManifestModel instance,
) => <String, dynamic>{
  'provider': instance.provider,
  'model': instance.model,
  'effort': ?instance.effort,
  'promptHash': ?instance.promptHash,
  'toolsHash': ?instance.toolsHash,
  'samples': ?instance.samples,
  'templates': ?instance.templates?.map((e) => e.toJson()).toList(),
};
