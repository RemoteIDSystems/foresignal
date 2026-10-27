// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'permission.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PermissionCWProxy {
  Permission key(String key);

  Permission group(String group);

  Permission description(String? description);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Permission(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Permission(...).copyWith(id: 12, name: "My name")
  /// ```
  Permission call({String key, String group, String? description});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPermission.copyWith(...)` or call `instanceOfPermission.copyWith.fieldName(value)` for a single field.
class _$PermissionCWProxyImpl implements _$PermissionCWProxy {
  const _$PermissionCWProxyImpl(this._value);

  final Permission _value;

  @override
  Permission key(String key) => call(key: key);

  @override
  Permission group(String group) => call(group: group);

  @override
  Permission description(String? description) => call(description: description);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Permission(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Permission(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Permission call({
    Object? key = const $CopyWithPlaceholder(),
    Object? group = const $CopyWithPlaceholder(),
    Object? description = const $CopyWithPlaceholder(),
  }) {
    return Permission(
      key: key == const $CopyWithPlaceholder() || key == null
          ? _value.key
          // ignore: cast_nullable_to_non_nullable
          : key as String,
      group: group == const $CopyWithPlaceholder() || group == null
          ? _value.group
          // ignore: cast_nullable_to_non_nullable
          : group as String,
      description: description == const $CopyWithPlaceholder()
          ? _value.description
          // ignore: cast_nullable_to_non_nullable
          : description as String?,
    );
  }
}

extension $PermissionCopyWith on Permission {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPermission.copyWith(...)` or `instanceOfPermission.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PermissionCWProxy get copyWith => _$PermissionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Permission _$PermissionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Permission', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['key', 'group']);
      final val = Permission(
        key: $checkedConvert('key', (v) => v as String),
        group: $checkedConvert('group', (v) => v as String),
        description: $checkedConvert('description', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PermissionToJson(Permission instance) =>
    <String, dynamic>{
      'key': instance.key,
      'group': instance.group,
      'description': ?instance.description,
    };
