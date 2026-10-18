// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'channel_type_config_fields_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChannelTypeConfigFieldsInnerCWProxy {
  ChannelTypeConfigFieldsInner name(String name);

  ChannelTypeConfigFieldsInner type(ChannelTypeConfigFieldsInnerTypeEnum type);

  ChannelTypeConfigFieldsInner required_(bool required_);

  ChannelTypeConfigFieldsInner description(String? description);

  ChannelTypeConfigFieldsInner default_(Object? default_);

  ChannelTypeConfigFieldsInner allowed(List<String>? allowed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelTypeConfigFieldsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelTypeConfigFieldsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ChannelTypeConfigFieldsInner call({
    String name,
    ChannelTypeConfigFieldsInnerTypeEnum type,
    bool required_,
    String? description,
    Object? default_,
    List<String>? allowed,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChannelTypeConfigFieldsInner.copyWith(...)` or call `instanceOfChannelTypeConfigFieldsInner.copyWith.fieldName(value)` for a single field.
class _$ChannelTypeConfigFieldsInnerCWProxyImpl
    implements _$ChannelTypeConfigFieldsInnerCWProxy {
  const _$ChannelTypeConfigFieldsInnerCWProxyImpl(this._value);

  final ChannelTypeConfigFieldsInner _value;

  @override
  ChannelTypeConfigFieldsInner name(String name) => call(name: name);

  @override
  ChannelTypeConfigFieldsInner type(
    ChannelTypeConfigFieldsInnerTypeEnum type,
  ) => call(type: type);

  @override
  ChannelTypeConfigFieldsInner required_(bool required_) =>
      call(required_: required_);

  @override
  ChannelTypeConfigFieldsInner description(String? description) =>
      call(description: description);

  @override
  ChannelTypeConfigFieldsInner default_(Object? default_) =>
      call(default_: default_);

  @override
  ChannelTypeConfigFieldsInner allowed(List<String>? allowed) =>
      call(allowed: allowed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChannelTypeConfigFieldsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChannelTypeConfigFieldsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChannelTypeConfigFieldsInner call({
    Object? name = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? required_ = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? default_ = const $CopyWithPlaceholder(),
    Object? allowed = const $CopyWithPlaceholder(),
  }) {
    return ChannelTypeConfigFieldsInner(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ChannelTypeConfigFieldsInnerTypeEnum,
      required_: required_ == const $CopyWithPlaceholder() || required_ == null
          ? _value.required_
          // ignore: cast_nullable_to_non_nullable
          : required_ as bool,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      default_: default_ == const $CopyWithPlaceholder()
          ? _value.default_
          // ignore: cast_nullable_to_non_nullable
          : default_ as Object?,
      allowed: allowed == const $CopyWithPlaceholder()
          ? _value.allowed
          // ignore: cast_nullable_to_non_nullable
          : allowed as List<String>?,
    );
  }
}

extension $ChannelTypeConfigFieldsInnerCopyWith
    on ChannelTypeConfigFieldsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChannelTypeConfigFieldsInner.copyWith(...)` or `instanceOfChannelTypeConfigFieldsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChannelTypeConfigFieldsInnerCWProxy get copyWith =>
      _$ChannelTypeConfigFieldsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChannelTypeConfigFieldsInner _$ChannelTypeConfigFieldsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChannelTypeConfigFieldsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name', 'type', 'required']);
  final val = ChannelTypeConfigFieldsInner(
    name: $checkedConvert('name', (v) => v as String),
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$ChannelTypeConfigFieldsInnerTypeEnumEnumMap, v),
    ),
    required_: $checkedConvert('required', (v) => v as bool),
    description: $checkedConvert('description', (v) => v as String?),
    default_: $checkedConvert('default', (v) => v),
    allowed: $checkedConvert(
      'allowed',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
}, fieldKeyMap: const {'required_': 'required', 'default_': 'default'});

Map<String, dynamic> _$ChannelTypeConfigFieldsInnerToJson(
  ChannelTypeConfigFieldsInner instance,
) => <String, dynamic>{
  'name': instance.name,
  'type': _$ChannelTypeConfigFieldsInnerTypeEnumEnumMap[instance.type]!,
  'required': instance.required_,
  'description': ?instance.description,
  'default': ?instance.default_,
  'allowed': ?instance.allowed,
};

const _$ChannelTypeConfigFieldsInnerTypeEnumEnumMap = {
  ChannelTypeConfigFieldsInnerTypeEnum.string: 'string',
  ChannelTypeConfigFieldsInnerTypeEnum.url: 'url',
  ChannelTypeConfigFieldsInnerTypeEnum.integer: 'integer',
  ChannelTypeConfigFieldsInnerTypeEnum.number: 'number',
  ChannelTypeConfigFieldsInnerTypeEnum.boolean: 'boolean',
  ChannelTypeConfigFieldsInnerTypeEnum.stringList: 'string-list',
  ChannelTypeConfigFieldsInnerTypeEnum.map: 'map',
  ChannelTypeConfigFieldsInnerTypeEnum.enum_: 'enum',
};
