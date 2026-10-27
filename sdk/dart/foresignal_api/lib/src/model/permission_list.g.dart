// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'permission_list.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PermissionListCWProxy {
  PermissionList items(List<Permission> items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PermissionList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PermissionList(...).copyWith(id: 12, name: "My name")
  /// ```
  PermissionList call({List<Permission> items});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPermissionList.copyWith(...)` or call `instanceOfPermissionList.copyWith.fieldName(value)` for a single field.
class _$PermissionListCWProxyImpl implements _$PermissionListCWProxy {
  const _$PermissionListCWProxyImpl(this._value);

  final PermissionList _value;

  @override
  PermissionList items(List<Permission> items) => call(items: items);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PermissionList(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PermissionList(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PermissionList call({Object? items = const $CopyWithPlaceholder()}) {
    return PermissionList(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Permission>,
    );
  }
}

extension $PermissionListCopyWith on PermissionList {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPermissionList.copyWith(...)` or `instanceOfPermissionList.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PermissionListCWProxy get copyWith => _$PermissionListCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PermissionList _$PermissionListFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PermissionList', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = PermissionList(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Permission.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PermissionListToJson(PermissionList instance) =>
    <String, dynamic>{'items': instance.items.map((e) => e.toJson()).toList()};
