// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mapping_value_spec.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MappingValueSpecCWProxy {
  MappingValueSpec path(String path);

  MappingValueSpec template(String template);

  MappingValueSpec const_(Object? const_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingValueSpec(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingValueSpec(...).copyWith(id: 12, name: "My name")
  /// ```
  MappingValueSpec call({String path, String template, Object? const_});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMappingValueSpec.copyWith(...)` or call `instanceOfMappingValueSpec.copyWith.fieldName(value)` for a single field.
class _$MappingValueSpecCWProxyImpl implements _$MappingValueSpecCWProxy {
  const _$MappingValueSpecCWProxyImpl(this._value);

  final MappingValueSpec _value;

  @override
  MappingValueSpec path(String path) => call(path: path);

  @override
  MappingValueSpec template(String template) => call(template: template);

  @override
  MappingValueSpec const_(Object? const_) => call(const_: const_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MappingValueSpec(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MappingValueSpec(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MappingValueSpec call({
    Object? path = const $CopyWithPlaceholder(),
    Object? template = const $CopyWithPlaceholder(),
    Object? const_ = const $CopyWithPlaceholder(),
  }) {
    return MappingValueSpec(
      path: path == const $CopyWithPlaceholder() || path == null
          ? _value.path
          // ignore: cast_nullable_to_non_nullable
          : path as String,
      template: template == const $CopyWithPlaceholder() || template == null
          ? _value.template
          // ignore: cast_nullable_to_non_nullable
          : template as String,
      const_: const_ == const $CopyWithPlaceholder()
          ? _value.const_
          // ignore: cast_nullable_to_non_nullable
          : const_ as Object?,
    );
  }
}

extension $MappingValueSpecCopyWith on MappingValueSpec {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMappingValueSpec.copyWith(...)` or `instanceOfMappingValueSpec.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MappingValueSpecCWProxy get copyWith => _$MappingValueSpecCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MappingValueSpec _$MappingValueSpecFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MappingValueSpec', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['path', 'template', 'const']);
      final val = MappingValueSpec(
        path: $checkedConvert('path', (v) => v as String),
        template: $checkedConvert('template', (v) => v as String),
        const_: $checkedConvert('const', (v) => v),
      );
      return val;
    }, fieldKeyMap: const {'const_': 'const'});

Map<String, dynamic> _$MappingValueSpecToJson(MappingValueSpec instance) =>
    <String, dynamic>{
      'path': instance.path,
      'template': instance.template,
      'const': instance.const_,
    };
