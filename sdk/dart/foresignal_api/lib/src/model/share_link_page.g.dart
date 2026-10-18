// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'share_link_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ShareLinkPageCWProxy {
  ShareLinkPage items(List<ShareLink> items);

  ShareLinkPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ShareLinkPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ShareLinkPage(...).copyWith(id: 12, name: "My name")
  /// ```
  ShareLinkPage call({List<ShareLink> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfShareLinkPage.copyWith(...)` or call `instanceOfShareLinkPage.copyWith.fieldName(value)` for a single field.
class _$ShareLinkPageCWProxyImpl implements _$ShareLinkPageCWProxy {
  const _$ShareLinkPageCWProxyImpl(this._value);

  final ShareLinkPage _value;

  @override
  ShareLinkPage items(List<ShareLink> items) => call(items: items);

  @override
  ShareLinkPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ShareLinkPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ShareLinkPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ShareLinkPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return ShareLinkPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<ShareLink>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $ShareLinkPageCopyWith on ShareLinkPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfShareLinkPage.copyWith(...)` or `instanceOfShareLinkPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ShareLinkPageCWProxy get copyWith => _$ShareLinkPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ShareLinkPage _$ShareLinkPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ShareLinkPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = ShareLinkPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => ShareLink.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$ShareLinkPageToJson(ShareLinkPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
