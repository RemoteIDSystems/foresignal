// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UserPageCWProxy {
  UserPage items(List<User> items);

  UserPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserPage(...).copyWith(id: 12, name: "My name")
  /// ```
  UserPage call({List<User> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUserPage.copyWith(...)` or call `instanceOfUserPage.copyWith.fieldName(value)` for a single field.
class _$UserPageCWProxyImpl implements _$UserPageCWProxy {
  const _$UserPageCWProxyImpl(this._value);

  final UserPage _value;

  @override
  UserPage items(List<User> items) => call(items: items);

  @override
  UserPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UserPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return UserPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<User>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $UserPageCopyWith on UserPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUserPage.copyWith(...)` or `instanceOfUserPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UserPageCWProxy get copyWith => _$UserPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserPage _$UserPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('UserPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = UserPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => User.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$UserPageToJson(UserPage instance) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': ?instance.nextCursor,
};
