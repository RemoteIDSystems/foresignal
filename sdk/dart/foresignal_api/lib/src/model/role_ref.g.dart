// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'role_ref.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RoleRefCWProxy {
  RoleRef id(String id);

  RoleRef name(String name);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RoleRef(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RoleRef(...).copyWith(id: 12, name: "My name")
  /// ```
  RoleRef call({String id, String name});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRoleRef.copyWith(...)` or call `instanceOfRoleRef.copyWith.fieldName(value)` for a single field.
class _$RoleRefCWProxyImpl implements _$RoleRefCWProxy {
  const _$RoleRefCWProxyImpl(this._value);

  final RoleRef _value;

  @override
  RoleRef id(String id) => call(id: id);

  @override
  RoleRef name(String name) => call(name: name);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RoleRef(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RoleRef(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RoleRef call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
  }) {
    return RoleRef(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
    );
  }
}

extension $RoleRefCopyWith on RoleRef {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRoleRef.copyWith(...)` or `instanceOfRoleRef.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RoleRefCWProxy get copyWith => _$RoleRefCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RoleRef _$RoleRefFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RoleRef', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'name']);
      final val = RoleRef(
        id: $checkedConvert('id', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$RoleRefToJson(RoleRef instance) => <String, dynamic>{
  'id': instance.id,
  'name': instance.name,
};
