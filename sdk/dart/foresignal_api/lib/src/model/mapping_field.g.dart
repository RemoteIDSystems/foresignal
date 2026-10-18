// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_field.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingFieldCWProxy {
  MappingField name(String name);

  MappingField path(String path);

  MappingField type(MappingFieldTypeEnum type);

  MappingField unit(MappingFieldUnit? unit);

  MappingField scale(num? scale);

  MappingField offset(num? offset);

  MappingField default_(Object? default_);

  MappingField sentinels(List<Object>? sentinels);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingField(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingField(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingField call({
    String name,
    String path,
    MappingFieldTypeEnum type,
    MappingFieldUnit? unit,
    num? scale,
    num? offset,
    Object? default_,
    List<Object>? sentinels,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingField.copyWith(...)` or call `instanceOfMappingField.copyWith.fieldName(value)` for a single field.
class _$MappingFieldCWProxyImpl implements _$MappingFieldCWProxy {
  const _$MappingFieldCWProxyImpl(this._value);

  final MappingField _value;

  @override
  MappingField name(String name) => call(name: name);

  @override
  MappingField path(String path) => call(path: path);

  @override
  MappingField type(MappingFieldTypeEnum type) => call(type: type);

  @override
  MappingField unit(MappingFieldUnit? unit) => call(unit: unit);

  @override
  MappingField scale(num? scale) => call(scale: scale);

  @override
  MappingField offset(num? offset) => call(offset: offset);

  @override
  MappingField default_(Object? default_) => call(default_: default_);

  @override
  MappingField sentinels(List<Object>? sentinels) => call(sentinels: sentinels);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingField(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingField(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingField call({
    Object? name = const $CopyWithPlaceholder(),
    Object? path = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? unit = const $CopyWithPlaceholder(),
    Object? scale = const $CopyWithPlaceholder(),
    Object? offset = const $CopyWithPlaceholder(),
    Object? default_ = const $CopyWithPlaceholder(),
    Object? sentinels = const $CopyWithPlaceholder(),
  }) {
    return MappingField(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as MappingFieldTypeEnum,
      unit: unit == const $CopyWithPlaceholder()
          ? _value.unit
          // ignore: cast_nullable_to_non_nullable
          : unit as MappingFieldUnit?,
      scale: scale == const $CopyWithPlaceholder()
          ? _value.scale
          // ignore: cast_nullable_to_non_nullable
          : scale as num?,
      offset: offset == const $CopyWithPlaceholder()
          ? _value.offset
          // ignore: cast_nullable_to_non_nullable
          : offset as num?,
      default_: default_ == const $CopyWithPlaceholder()
          ? _value.default_
          // ignore: cast_nullable_to_non_nullable
          : default_ as Object?,
      sentinels: sentinels == const $CopyWithPlaceholder()
          ? _value.sentinels
          // ignore: cast_nullable_to_non_nullable
          : sentinels as List<Object>?,
    );
  }
}

extension $MappingFieldCopyWith on MappingField {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingField.copyWith(...)` or `instanceOfMappingField.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingFieldCWProxy get copyWith => _$MappingFieldCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingField _$MappingFieldFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MappingField', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'path', 'type']);
      final val = MappingField(
        name: $checkedConvert('name', (v) => v as String),
        path: $checkedConvert('path', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$MappingFieldTypeEnumEnumMap, v),
        ),
        unit: $checkedConvert(
          'unit',
          (v) => v == null
              ? null
              : MappingFieldUnit.fromJson(v as Map<String, dynamic>),
        ),
        scale: $checkedConvert('scale', (v) => v as num?),
        offset: $checkedConvert('offset', (v) => v as num?),
        default_: $checkedConvert('default', (v) => v),
        sentinels: $checkedConvert(
          'sentinels',
          (v) => (v as List<dynamic>?)?.map((e) => e as Object).toList(),
        ),
      );
      return val;
    }, fieldKeyMap: const {'default_': 'default'});

Map<String, dynamic> _$MappingFieldToJson(MappingField instance) =>
    <String, dynamic>{
      'name': instance.name,
      'path': instance.path,
      'type': _$MappingFieldTypeEnumEnumMap[instance.type]!,
      'unit': ?instance.unit?.toJson(),
      'scale': ?instance.scale,
      'offset': ?instance.offset,
      'default': ?instance.default_,
      'sentinels': ?instance.sentinels,
    };

const _$MappingFieldTypeEnumEnumMap = {
  MappingFieldTypeEnum.number: 'number',
  MappingFieldTypeEnum.integer: 'integer',
  MappingFieldTypeEnum.string: 'string',
  MappingFieldTypeEnum.boolean: 'boolean',
};
