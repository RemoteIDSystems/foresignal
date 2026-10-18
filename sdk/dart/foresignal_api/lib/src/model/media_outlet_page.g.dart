// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'media_outlet_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MediaOutletPageCWProxy {
  MediaOutletPage items(List<MediaOutlet> items);

  MediaOutletPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaOutletPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaOutletPage(...).copyWith(id: 12, name: "My name")
  /// ```
  MediaOutletPage call({List<MediaOutlet> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMediaOutletPage.copyWith(...)` or call `instanceOfMediaOutletPage.copyWith.fieldName(value)` for a single field.
class _$MediaOutletPageCWProxyImpl implements _$MediaOutletPageCWProxy {
  const _$MediaOutletPageCWProxyImpl(this._value);

  final MediaOutletPage _value;

  @override
  MediaOutletPage items(List<MediaOutlet> items) => call(items: items);

  @override
  MediaOutletPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MediaOutletPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MediaOutletPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MediaOutletPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return MediaOutletPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<MediaOutlet>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $MediaOutletPageCopyWith on MediaOutletPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMediaOutletPage.copyWith(...)` or `instanceOfMediaOutletPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MediaOutletPageCWProxy get copyWith => _$MediaOutletPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MediaOutletPage _$MediaOutletPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MediaOutletPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = MediaOutletPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => MediaOutlet.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$MediaOutletPageToJson(MediaOutletPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
