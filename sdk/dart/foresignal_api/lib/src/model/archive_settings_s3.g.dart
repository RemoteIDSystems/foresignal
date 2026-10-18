// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'archive_settings_s3.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ArchiveSettingsS3CWProxy {
  ArchiveSettingsS3 endpoint(String endpoint);

  ArchiveSettingsS3 bucket(String bucket);

  ArchiveSettingsS3 prefix(String? prefix);

  ArchiveSettingsS3 region(String? region);

  ArchiveSettingsS3 pathStyle(bool? pathStyle);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchiveSettingsS3(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchiveSettingsS3(...).copyWith(id: 12, name: "My name")
  /// ```
  ArchiveSettingsS3 call({
    String endpoint,
    String bucket,
    String? prefix,
    String? region,
    bool? pathStyle,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfArchiveSettingsS3.copyWith(...)` or call `instanceOfArchiveSettingsS3.copyWith.fieldName(value)` for a single field.
class _$ArchiveSettingsS3CWProxyImpl implements _$ArchiveSettingsS3CWProxy {
  const _$ArchiveSettingsS3CWProxyImpl(this._value);

  final ArchiveSettingsS3 _value;

  @override
  ArchiveSettingsS3 endpoint(String endpoint) => call(endpoint: endpoint);

  @override
  ArchiveSettingsS3 bucket(String bucket) => call(bucket: bucket);

  @override
  ArchiveSettingsS3 prefix(String? prefix) => call(prefix: prefix);

  @override
  ArchiveSettingsS3 region(String? region) => call(region: region);

  @override
  ArchiveSettingsS3 pathStyle(bool? pathStyle) => call(pathStyle: pathStyle);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ArchiveSettingsS3(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ArchiveSettingsS3(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ArchiveSettingsS3 call({
    Object? endpoint = const $CopyWithPlaceholder(),
    Object? bucket = const $CopyWithPlaceholder(),
    Object? prefix = const $CopyWithPlaceholder(),
    Object? region = const $CopyWithPlaceholder(),
    Object? pathStyle = const $CopyWithPlaceholder(),
  }) {
    return ArchiveSettingsS3(
      endpoint: endpoint == const $CopyWithPlaceholder() || endpoint == null
          ? _value.endpoint
          // ignore: cast_nullable_to_non_nullable
          : endpoint as String,
      bucket: bucket == const $CopyWithPlaceholder() || bucket == null
          ? _value.bucket
          // ignore: cast_nullable_to_non_nullable
          : bucket as String,
      prefix: prefix == const $CopyWithPlaceholder()
          ? _value.prefix
          // ignore: cast_nullable_to_non_nullable
          : prefix as String?,
      region: region == const $CopyWithPlaceholder()
          ? _value.region
          // ignore: cast_nullable_to_non_nullable
          : region as String?,
      pathStyle: pathStyle == const $CopyWithPlaceholder()
          ? _value.pathStyle
          // ignore: cast_nullable_to_non_nullable
          : pathStyle as bool?,
    );
  }
}

extension $ArchiveSettingsS3CopyWith on ArchiveSettingsS3 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfArchiveSettingsS3.copyWith(...)` or `instanceOfArchiveSettingsS3.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ArchiveSettingsS3CWProxy get copyWith =>
      _$ArchiveSettingsS3CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ArchiveSettingsS3 _$ArchiveSettingsS3FromJson(Map<String, dynamic> json) =>
    $checkedCreate('ArchiveSettingsS3', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['endpoint', 'bucket']);
      final val = ArchiveSettingsS3(
        endpoint: $checkedConvert('endpoint', (v) => v as String),
        bucket: $checkedConvert('bucket', (v) => v as String),
        prefix: $checkedConvert('prefix', (v) => v as String?),
        region: $checkedConvert('region', (v) => v as String?),
        pathStyle: $checkedConvert('pathStyle', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$ArchiveSettingsS3ToJson(ArchiveSettingsS3 instance) =>
    <String, dynamic>{
      'endpoint': instance.endpoint,
      'bucket': instance.bucket,
      'prefix': ?instance.prefix,
      'region': ?instance.region,
      'pathStyle': ?instance.pathStyle,
    };
