// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analysis_manifest_build.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalysisManifestBuildCWProxy {
  AnalysisManifestBuild version(String version);

  AnalysisManifestBuild gitCommit(String? gitCommit);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisManifestBuild(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisManifestBuild(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalysisManifestBuild call({String version, String? gitCommit});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalysisManifestBuild.copyWith(...)` or call `instanceOfAnalysisManifestBuild.copyWith.fieldName(value)` for a single field.
class _$AnalysisManifestBuildCWProxyImpl
    implements _$AnalysisManifestBuildCWProxy {
  const _$AnalysisManifestBuildCWProxyImpl(this._value);

  final AnalysisManifestBuild _value;

  @override
  AnalysisManifestBuild version(String version) => call(version: version);

  @override
  AnalysisManifestBuild gitCommit(String? gitCommit) =>
      call(gitCommit: gitCommit);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisManifestBuild(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisManifestBuild(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalysisManifestBuild call({
    Object? version = const $CopyWithPlaceholder(),
    Object? gitCommit = const $CopyWithPlaceholder(),
  }) {
    return AnalysisManifestBuild(
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String,
      gitCommit: gitCommit == const $CopyWithPlaceholder()
          ? _value.gitCommit
          // ignore: cast_nullable_to_non_nullable
          : gitCommit as String?,
    );
  }
}

extension $AnalysisManifestBuildCopyWith on AnalysisManifestBuild {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalysisManifestBuild.copyWith(...)` or `instanceOfAnalysisManifestBuild.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalysisManifestBuildCWProxy get copyWith =>
      _$AnalysisManifestBuildCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalysisManifestBuild _$AnalysisManifestBuildFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalysisManifestBuild', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['version']);
  final val = AnalysisManifestBuild(
    version: $checkedConvert('version', (v) => v as String),
    gitCommit: $checkedConvert('gitCommit', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$AnalysisManifestBuildToJson(
  AnalysisManifestBuild instance,
) => <String, dynamic>{
  'version': instance.version,
  'gitCommit': ?instance.gitCommit,
};
