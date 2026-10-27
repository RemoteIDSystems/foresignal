// GENERATED CODE - DO NOT MODIFY BY HAND

part of 's3_target.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$S3TargetCWProxy {
  S3Target endpoint(String endpoint);

  S3Target bucket(String bucket);

  S3Target prefix(String? prefix);

  S3Target region(String? region);

  S3Target pathStyle(bool? pathStyle);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `S3Target(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// S3Target(...).copyWith(id: 12, name: "My name")
  /// ```
  S3Target call({
    String endpoint,
    String bucket,
    String? prefix,
    String? region,
    bool? pathStyle,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfS3Target.copyWith(...)` or call `instanceOfS3Target.copyWith.fieldName(value)` for a single field.
class _$S3TargetCWProxyImpl implements _$S3TargetCWProxy {
  const _$S3TargetCWProxyImpl(this._value);

  final S3Target _value;

  @override
  S3Target endpoint(String endpoint) => call(endpoint: endpoint);

  @override
  S3Target bucket(String bucket) => call(bucket: bucket);

  @override
  S3Target prefix(String? prefix) => call(prefix: prefix);

  @override
  S3Target region(String? region) => call(region: region);

  @override
  S3Target pathStyle(bool? pathStyle) => call(pathStyle: pathStyle);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `S3Target(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// S3Target(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  S3Target call({
    Object? endpoint = const $CopyWithPlaceholder(),
    Object? bucket = const $CopyWithPlaceholder(),
    Object? prefix = const $CopyWithPlaceholder(),
    Object? region = const $CopyWithPlaceholder(),
    Object? pathStyle = const $CopyWithPlaceholder(),
  }) {
    return S3Target(
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

extension $S3TargetCopyWith on S3Target {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfS3Target.copyWith(...)` or `instanceOfS3Target.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$S3TargetCWProxy get copyWith => _$S3TargetCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

S3Target _$S3TargetFromJson(Map<String, dynamic> json) =>
    $checkedCreate('S3Target', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['endpoint', 'bucket']);
      final val = S3Target(
        endpoint: $checkedConvert('endpoint', (v) => v as String),
        bucket: $checkedConvert('bucket', (v) => v as String),
        prefix: $checkedConvert('prefix', (v) => v as String?),
        region: $checkedConvert('region', (v) => v as String?),
        pathStyle: $checkedConvert('pathStyle', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$S3TargetToJson(S3Target instance) => <String, dynamic>{
  'endpoint': instance.endpoint,
  'bucket': instance.bucket,
  'prefix': ?instance.prefix,
  'region': ?instance.region,
  'pathStyle': ?instance.pathStyle,
};
