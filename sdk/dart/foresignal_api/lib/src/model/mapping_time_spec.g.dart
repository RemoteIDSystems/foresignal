// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_time_spec.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingTimeSpecCWProxy {
  MappingTimeSpec path(String path);

  MappingTimeSpec format(String format);

  MappingTimeSpec zone(String? zone);

  MappingTimeSpec received(bool received);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingTimeSpec(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingTimeSpec(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingTimeSpec call({
    String path,
    String format,
    String? zone,
    bool received,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingTimeSpec.copyWith(...)` or call `instanceOfMappingTimeSpec.copyWith.fieldName(value)` for a single field.
class _$MappingTimeSpecCWProxyImpl implements _$MappingTimeSpecCWProxy {
  const _$MappingTimeSpecCWProxyImpl(this._value);

  final MappingTimeSpec _value;

  @override
  MappingTimeSpec path(String path) => call(path: path);

  @override
  MappingTimeSpec format(String format) => call(format: format);

  @override
  MappingTimeSpec zone(String? zone) => call(zone: zone);

  @override
  MappingTimeSpec received(bool received) => call(received: received);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingTimeSpec(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingTimeSpec(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingTimeSpec call({
    Object? path = const $CopyWithPlaceholder(),
    Object? format = const $CopyWithPlaceholder(),
    Object? zone = const $CopyWithPlaceholder(),
    Object? received = const $CopyWithPlaceholder(),
  }) {
    return MappingTimeSpec(
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String,
      format: format == const $CopyWithPlaceholder() || format == null
          ? _value.format
          // ignore: cast_nullable_to_non_nullable
          : format as String,
      zone: zone == const $CopyWithPlaceholder()
          ? _value.zone
          // ignore: cast_nullable_to_non_nullable
          : zone as String?,
      received: received == const $CopyWithPlaceholder() || received == null
          ? _value.received
          // ignore: cast_nullable_to_non_nullable
          : received as bool,
    );
  }
}

extension $MappingTimeSpecCopyWith on MappingTimeSpec {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingTimeSpec.copyWith(...)` or `instanceOfMappingTimeSpec.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingTimeSpecCWProxy get copyWith => _$MappingTimeSpecCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingTimeSpec _$MappingTimeSpecFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MappingTimeSpec', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['path', 'format', 'received']);
      final val = MappingTimeSpec(
        path: $checkedConvert('path', (v) => v as String),
        format: $checkedConvert('format', (v) => v as String),
        zone: $checkedConvert('zone', (v) => v as String?),
        received: $checkedConvert('received', (v) => v as bool),
      );
      return val;
    });

Map<String, dynamic> _$MappingTimeSpecToJson(MappingTimeSpec instance) =>
    <String, dynamic>{
      'path': instance.path,
      'format': instance.format,
      'zone': ?instance.zone,
      'received': instance.received,
    };
