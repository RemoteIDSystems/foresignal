// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analysis_manifest.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalysisManifestCWProxy {
  AnalysisManifest id(String id);

  AnalysisManifest component(String component);

  AnalysisManifest componentVersion(String componentVersion);

  AnalysisManifest build(AnalysisManifestBuild build);

  AnalysisManifest config(Map<String, Object> config);

  AnalysisManifest configHash(String configHash);

  AnalysisManifest registries(Map<String, String> registries);

  AnalysisManifest asOfSeq(int? asOfSeq);

  AnalysisManifest inputs(AnalysisManifestInputs inputs);

  AnalysisManifest model(AnalysisManifestModel? model);

  AnalysisManifest contentHash(String contentHash);

  AnalysisManifest createdAt(DateTime createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisManifest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisManifest(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalysisManifest call({
    String id,
    String component,
    String componentVersion,
    AnalysisManifestBuild build,
    Map<String, Object> config,
    String configHash,
    Map<String, String> registries,
    int? asOfSeq,
    AnalysisManifestInputs inputs,
    AnalysisManifestModel? model,
    String contentHash,
    DateTime createdAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalysisManifest.copyWith(...)` or call `instanceOfAnalysisManifest.copyWith.fieldName(value)` for a single field.
class _$AnalysisManifestCWProxyImpl implements _$AnalysisManifestCWProxy {
  const _$AnalysisManifestCWProxyImpl(this._value);

  final AnalysisManifest _value;

  @override
  AnalysisManifest id(String id) => call(id: id);

  @override
  AnalysisManifest component(String component) => call(component: component);

  @override
  AnalysisManifest componentVersion(String componentVersion) =>
      call(componentVersion: componentVersion);

  @override
  AnalysisManifest build(AnalysisManifestBuild build) => call(build: build);

  @override
  AnalysisManifest config(Map<String, Object> config) => call(config: config);

  @override
  AnalysisManifest configHash(String configHash) =>
      call(configHash: configHash);

  @override
  AnalysisManifest registries(Map<String, String> registries) =>
      call(registries: registries);

  @override
  AnalysisManifest asOfSeq(int? asOfSeq) => call(asOfSeq: asOfSeq);

  @override
  AnalysisManifest inputs(AnalysisManifestInputs inputs) =>
      call(inputs: inputs);

  @override
  AnalysisManifest model(AnalysisManifestModel? model) => call(model: model);

  @override
  AnalysisManifest contentHash(String contentHash) =>
      call(contentHash: contentHash);

  @override
  AnalysisManifest createdAt(DateTime createdAt) => call(createdAt: createdAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisManifest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisManifest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalysisManifest call({
    Object? id = const $CopyWithPlaceholder(),
    Object? component = const $CopyWithPlaceholder(),
    Object? componentVersion = const $CopyWithPlaceholder(),
    Object? build = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
    Object? configHash = const $CopyWithPlaceholder(),
    Object? registries = const $CopyWithPlaceholder(),
    Object? asOfSeq = const $CopyWithPlaceholder(),
    Object? inputs = const $CopyWithPlaceholder(),
    Object? model = const $CopyWithPlaceholder(),
    Object? contentHash = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
  }) {
    return AnalysisManifest(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      component: component == const $CopyWithPlaceholder() || component == null
          ? _value.component
          // ignore: cast_nullable_to_non_nullable
          : component as String,
      componentVersion:
          componentVersion == const $CopyWithPlaceholder() ||
              componentVersion == null
          ? _value.componentVersion
          // ignore: cast_nullable_to_non_nullable
          : componentVersion as String,
      build: build == const $CopyWithPlaceholder() || build == null
          ? _value.build
          // ignore: cast_nullable_to_non_nullable
          : build as AnalysisManifestBuild,
      config: config == const $CopyWithPlaceholder() || config == null
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as Map<String, Object>,
      configHash:
          configHash == const $CopyWithPlaceholder() || configHash == null
          ? _value.configHash
          // ignore: cast_nullable_to_non_nullable
          : configHash as String,
      registries:
          registries == const $CopyWithPlaceholder() || registries == null
          ? _value.registries
          // ignore: cast_nullable_to_non_nullable
          : registries as Map<String, String>,
      asOfSeq: asOfSeq == const $CopyWithPlaceholder()
          ? _value.asOfSeq
          // ignore: cast_nullable_to_non_nullable
          : asOfSeq as int?,
      inputs: inputs == const $CopyWithPlaceholder() || inputs == null
          ? _value.inputs
          // ignore: cast_nullable_to_non_nullable
          : inputs as AnalysisManifestInputs,
      model: model == const $CopyWithPlaceholder()
          ? _value.model
          // ignore: cast_nullable_to_non_nullable
          : model as AnalysisManifestModel?,
      contentHash:
          contentHash == const $CopyWithPlaceholder() || contentHash == null
          ? _value.contentHash
          // ignore: cast_nullable_to_non_nullable
          : contentHash as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
    );
  }
}

extension $AnalysisManifestCopyWith on AnalysisManifest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalysisManifest.copyWith(...)` or `instanceOfAnalysisManifest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalysisManifestCWProxy get copyWith => _$AnalysisManifestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalysisManifest _$AnalysisManifestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalysisManifest', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'component',
      'componentVersion',
      'build',
      'config',
      'configHash',
      'registries',
      'inputs',
      'contentHash',
      'createdAt',
    ],
  );
  final val = AnalysisManifest(
    id: $checkedConvert('id', (v) => v as String),
    component: $checkedConvert('component', (v) => v as String),
    componentVersion: $checkedConvert('componentVersion', (v) => v as String),
    build: $checkedConvert(
      'build',
      (v) => AnalysisManifestBuild.fromJson(v as Map<String, dynamic>),
    ),
    config: $checkedConvert(
      'config',
      (v) =>
          (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
    ),
    configHash: $checkedConvert('configHash', (v) => v as String),
    registries: $checkedConvert(
      'registries',
      (v) => Map<String, String>.from(v as Map),
    ),
    asOfSeq: $checkedConvert('asOfSeq', (v) => (v as num?)?.toInt()),
    inputs: $checkedConvert(
      'inputs',
      (v) => AnalysisManifestInputs.fromJson(v as Map<String, dynamic>),
    ),
    model: $checkedConvert(
      'model',
      (v) => v == null
          ? null
          : AnalysisManifestModel.fromJson(v as Map<String, dynamic>),
    ),
    contentHash: $checkedConvert('contentHash', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$AnalysisManifestToJson(AnalysisManifest instance) =>
    <String, dynamic>{
      'id': instance.id,
      'component': instance.component,
      'componentVersion': instance.componentVersion,
      'build': instance.build.toJson(),
      'config': instance.config,
      'configHash': instance.configHash,
      'registries': instance.registries,
      'asOfSeq': ?instance.asOfSeq,
      'inputs': instance.inputs.toJson(),
      'model': ?instance.model?.toJson(),
      'contentHash': instance.contentHash,
      'createdAt': instance.createdAt.toIso8601String(),
    };
