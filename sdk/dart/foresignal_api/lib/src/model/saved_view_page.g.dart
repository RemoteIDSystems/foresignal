// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'saved_view_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SavedViewPageCWProxy {
  SavedViewPage items(List<SavedView> items);

  SavedViewPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedViewPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedViewPage(...).copyWith(id: 12, name: "My name")
  /// ```
  SavedViewPage call({List<SavedView> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSavedViewPage.copyWith(...)` or call `instanceOfSavedViewPage.copyWith.fieldName(value)` for a single field.
class _$SavedViewPageCWProxyImpl implements _$SavedViewPageCWProxy {
  const _$SavedViewPageCWProxyImpl(this._value);

  final SavedViewPage _value;

  @override
  SavedViewPage items(List<SavedView> items) => call(items: items);

  @override
  SavedViewPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedViewPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedViewPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SavedViewPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return SavedViewPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SavedView>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $SavedViewPageCopyWith on SavedViewPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSavedViewPage.copyWith(...)` or `instanceOfSavedViewPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SavedViewPageCWProxy get copyWith => _$SavedViewPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SavedViewPage _$SavedViewPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SavedViewPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items']);
      final val = SavedViewPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => SavedView.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SavedViewPageToJson(SavedViewPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
