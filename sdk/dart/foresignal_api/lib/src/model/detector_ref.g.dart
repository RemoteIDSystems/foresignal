// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'detector_ref.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DetectorRefCWProxy {
  DetectorRef name(String name);

  DetectorRef version(String version);

  DetectorRef params(Map<String, Object>? params);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DetectorRef(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DetectorRef(...).copyWith(id: 12, name: "My name")
  /// ```
  DetectorRef call({String name, String version, Map<String, Object>? params});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDetectorRef.copyWith(...)` or call `instanceOfDetectorRef.copyWith.fieldName(value)` for a single field.
class _$DetectorRefCWProxyImpl implements _$DetectorRefCWProxy {
  const _$DetectorRefCWProxyImpl(this._value);

  final DetectorRef _value;

  @override
  DetectorRef name(String name) => call(name: name);

  @override
  DetectorRef version(String version) => call(version: version);

  @override
  DetectorRef params(Map<String, Object>? params) => call(params: params);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DetectorRef(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DetectorRef(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DetectorRef call({
    Object? name = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
  }) {
    return DetectorRef(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as String,
      params: params == const $CopyWithPlaceholder()
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as Map<String, Object>?,
    );
  }
}

extension $DetectorRefCopyWith on DetectorRef {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDetectorRef.copyWith(...)` or `instanceOfDetectorRef.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DetectorRefCWProxy get copyWith => _$DetectorRefCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DetectorRef _$DetectorRefFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DetectorRef', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'version']);
      final val = DetectorRef(
        name: $checkedConvert('name', (v) => v as String),
        version: $checkedConvert('version', (v) => v as String),
        params: $checkedConvert(
          'params',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$DetectorRefToJson(DetectorRef instance) =>
    <String, dynamic>{
      'name': instance.name,
      'version': instance.version,
      'params': ?instance.params,
    };
