// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'me_groups_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MeGroupsInnerCWProxy {
  MeGroupsInner id(String id);

  MeGroupsInner name(String name);

  MeGroupsInner role(MeGroupsInnerRoleEnum role);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MeGroupsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MeGroupsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  MeGroupsInner call({String id, String name, MeGroupsInnerRoleEnum role});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMeGroupsInner.copyWith(...)` or call `instanceOfMeGroupsInner.copyWith.fieldName(value)` for a single field.
class _$MeGroupsInnerCWProxyImpl implements _$MeGroupsInnerCWProxy {
  const _$MeGroupsInnerCWProxyImpl(this._value);

  final MeGroupsInner _value;

  @override
  MeGroupsInner id(String id) => call(id: id);

  @override
  MeGroupsInner name(String name) => call(name: name);

  @override
  MeGroupsInner role(MeGroupsInnerRoleEnum role) => call(role: role);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MeGroupsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MeGroupsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MeGroupsInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? role = const $CopyWithPlaceholder(),
  }) {
    return MeGroupsInner(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      role: role == const $CopyWithPlaceholder() || role == null
          ? _value.role
          // ignore: cast_nullable_to_non_nullable
          : role as MeGroupsInnerRoleEnum,
    );
  }
}

extension $MeGroupsInnerCopyWith on MeGroupsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMeGroupsInner.copyWith(...)` or `instanceOfMeGroupsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MeGroupsInnerCWProxy get copyWith => _$MeGroupsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MeGroupsInner _$MeGroupsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MeGroupsInner', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['id', 'name', 'role']);
      final val = MeGroupsInner(
        id: $checkedConvert('id', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String),
        role: $checkedConvert(
          'role',
          (v) => $enumDecode(_$MeGroupsInnerRoleEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$MeGroupsInnerToJson(MeGroupsInner instance) =>
    <String, dynamic>{
      'id': instance.id,
      'name': instance.name,
      'role': _$MeGroupsInnerRoleEnumEnumMap[instance.role]!,
    };

const _$MeGroupsInnerRoleEnumEnumMap = {
  MeGroupsInnerRoleEnum.member: 'member',
  MeGroupsInnerRoleEnum.manager: 'manager',
};
