// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'param_spec.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ParamSpecCWProxy {
  ParamSpec name(String name);

  ParamSpec type(ParamSpecTypeEnum type);

  ParamSpec description(String? description);

  ParamSpec default_(Object? default_);

  ParamSpec required_(bool? required_);

  ParamSpec allowed(List<String>? allowed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ParamSpec(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ParamSpec(...).copyWith(id: 12, name: "My name")
  /// ```
  ParamSpec call({
    String name,
    ParamSpecTypeEnum type,
    String? description,
    Object? default_,
    bool? required_,
    List<String>? allowed,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfParamSpec.copyWith(...)` or call `instanceOfParamSpec.copyWith.fieldName(value)` for a single field.
class _$ParamSpecCWProxyImpl implements _$ParamSpecCWProxy {
  const _$ParamSpecCWProxyImpl(this._value);

  final ParamSpec _value;

  @override
  ParamSpec name(String name) => call(name: name);

  @override
  ParamSpec type(ParamSpecTypeEnum type) => call(type: type);

  @override
  ParamSpec description(String? description) => call(description: description);

  @override
  ParamSpec default_(Object? default_) => call(default_: default_);

  @override
  ParamSpec required_(bool? required_) => call(required_: required_);

  @override
  ParamSpec allowed(List<String>? allowed) => call(allowed: allowed);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ParamSpec(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ParamSpec(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ParamSpec call({
    Object? name = const $CopyWithPlaceholder(),
    Object? type = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
    Object? default_ = const $CopyWithPlaceholder(),
    Object? required_ = const $CopyWithPlaceholder(),
    Object? allowed = const $CopyWithPlaceholder(),
  }) {
    return ParamSpec(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as ParamSpecTypeEnum,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
      default_: default_ == const $CopyWithPlaceholder()
          ? _value.default_
          // ignore: cast_nullable_to_non_nullable
          : default_ as Object?,
      required_: required_ == const $CopyWithPlaceholder()
          ? _value.required_
          // ignore: cast_nullable_to_non_nullable
          : required_ as bool?,
      allowed: allowed == const $CopyWithPlaceholder()
          ? _value.allowed
          // ignore: cast_nullable_to_non_nullable
          : allowed as List<String>?,
    );
  }
}

extension $ParamSpecCopyWith on ParamSpec {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfParamSpec.copyWith(...)` or `instanceOfParamSpec.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ParamSpecCWProxy get copyWith => _$ParamSpecCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ParamSpec _$ParamSpecFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ParamSpec', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['name', 'type']);
      final val = ParamSpec(
        name: $checkedConvert('name', (v) => v as String),
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$ParamSpecTypeEnumEnumMap, v),
        ),
        description: $checkedConvert('description', (v) => v as String?),
        default_: $checkedConvert('default', (v) => v),
        required_: $checkedConvert('required', (v) => v as bool?),
        allowed: $checkedConvert(
          'allowed',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    }, fieldKeyMap: const {'default_': 'default', 'required_': 'required'});

Map<String, dynamic> _$ParamSpecToJson(ParamSpec instance) => <String, dynamic>{
  'name': instance.name,
  'type': _$ParamSpecTypeEnumEnumMap[instance.type]!,
  'description': ?instance.description,
  'default': ?instance.default_,
  'required': ?instance.required_,
  'allowed': ?instance.allowed,
};

const _$ParamSpecTypeEnumEnumMap = {
  ParamSpecTypeEnum.string: 'string',
  ParamSpecTypeEnum.integer: 'integer',
  ParamSpecTypeEnum.number: 'number',
  ParamSpecTypeEnum.boolean: 'boolean',
  ParamSpecTypeEnum.duration: 'duration',
  ParamSpecTypeEnum.bbox: 'bbox',
  ParamSpecTypeEnum.stringList: 'string-list',
  ParamSpecTypeEnum.lanUrl: 'lan-url',
  ParamSpecTypeEnum.inboxPath: 'inbox-path',
  ParamSpecTypeEnum.json: 'json',
};
