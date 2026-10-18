// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_time_spec_one_of.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingTimeSpecOneOfCWProxy {
  MappingTimeSpecOneOf path(String path);

  MappingTimeSpecOneOf format(String format);

  MappingTimeSpecOneOf zone(String? zone);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingTimeSpecOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingTimeSpecOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingTimeSpecOneOf call({String path, String format, String? zone});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingTimeSpecOneOf.copyWith(...)` or call `instanceOfMappingTimeSpecOneOf.copyWith.fieldName(value)` for a single field.
class _$MappingTimeSpecOneOfCWProxyImpl
    implements _$MappingTimeSpecOneOfCWProxy {
  const _$MappingTimeSpecOneOfCWProxyImpl(this._value);

  final MappingTimeSpecOneOf _value;

  @override
  MappingTimeSpecOneOf path(String path) => call(path: path);

  @override
  MappingTimeSpecOneOf format(String format) => call(format: format);

  @override
  MappingTimeSpecOneOf zone(String? zone) => call(zone: zone);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingTimeSpecOneOf(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingTimeSpecOneOf(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingTimeSpecOneOf call({
    Object? path = const $CopyWithPlaceholder(),
    Object? format = const $CopyWithPlaceholder(),
    Object? zone = const $CopyWithPlaceholder(),
  }) {
    return MappingTimeSpecOneOf(
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
    );
  }
}

extension $MappingTimeSpecOneOfCopyWith on MappingTimeSpecOneOf {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingTimeSpecOneOf.copyWith(...)` or `instanceOfMappingTimeSpecOneOf.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingTimeSpecOneOfCWProxy get copyWith =>
      _$MappingTimeSpecOneOfCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingTimeSpecOneOf _$MappingTimeSpecOneOfFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MappingTimeSpecOneOf', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['path', 'format']);
  final val = MappingTimeSpecOneOf(
    path: $checkedConvert('path', (v) => v as String),
    format: $checkedConvert('format', (v) => v as String),
    zone: $checkedConvert('zone', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$MappingTimeSpecOneOfToJson(
  MappingTimeSpecOneOf instance,
) => <String, dynamic>{
  'path': instance.path,
  'format': instance.format,
  'zone': ?instance.zone,
};
