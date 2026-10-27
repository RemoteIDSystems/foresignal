// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'role_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RolePageCWProxy {
  RolePage items(List<Role> items);

  RolePage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RolePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RolePage(...).copyWith(id: 12, name: "My name")
  /// ```
  RolePage call({List<Role> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRolePage.copyWith(...)` or call `instanceOfRolePage.copyWith.fieldName(value)` for a single field.
class _$RolePageCWProxyImpl implements _$RolePageCWProxy {
  const _$RolePageCWProxyImpl(this._value);

  final RolePage _value;

  @override
  RolePage items(List<Role> items) => call(items: items);

  @override
  RolePage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RolePage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RolePage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RolePage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return RolePage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Role>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $RolePageCopyWith on RolePage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRolePage.copyWith(...)` or `instanceOfRolePage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RolePageCWProxy get copyWith => _$RolePageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RolePage _$RolePageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('RolePage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = RolePage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Role.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$RolePageToJson(RolePage instance) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
