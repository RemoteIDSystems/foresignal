// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'group_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GroupPageCWProxy {
  GroupPage items(List<Group> items);

  GroupPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GroupPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GroupPage(...).copyWith(id: 12, name: "My name")
  /// ```
  GroupPage call({List<Group> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGroupPage.copyWith(...)` or call `instanceOfGroupPage.copyWith.fieldName(value)` for a single field.
class _$GroupPageCWProxyImpl implements _$GroupPageCWProxy {
  const _$GroupPageCWProxyImpl(this._value);

  final GroupPage _value;

  @override
  GroupPage items(List<Group> items) => call(items: items);

  @override
  GroupPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GroupPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GroupPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GroupPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return GroupPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<Group>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $GroupPageCopyWith on GroupPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGroupPage.copyWith(...)` or `instanceOfGroupPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GroupPageCWProxy get copyWith => _$GroupPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GroupPage _$GroupPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GroupPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = GroupPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => Group.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$GroupPageToJson(GroupPage instance) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': instance.nextCursor,
};
