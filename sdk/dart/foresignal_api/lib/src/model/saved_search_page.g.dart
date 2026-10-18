// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'saved_search_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SavedSearchPageCWProxy {
  SavedSearchPage items(List<SavedSearch> items);

  SavedSearchPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchPage(...).copyWith(id: 12, name: "My name")
  /// ```
  SavedSearchPage call({List<SavedSearch> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSavedSearchPage.copyWith(...)` or call `instanceOfSavedSearchPage.copyWith.fieldName(value)` for a single field.
class _$SavedSearchPageCWProxyImpl implements _$SavedSearchPageCWProxy {
  const _$SavedSearchPageCWProxyImpl(this._value);

  final SavedSearchPage _value;

  @override
  SavedSearchPage items(List<SavedSearch> items) => call(items: items);

  @override
  SavedSearchPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SavedSearchPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return SavedSearchPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SavedSearch>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $SavedSearchPageCopyWith on SavedSearchPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSavedSearchPage.copyWith(...)` or `instanceOfSavedSearchPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SavedSearchPageCWProxy get copyWith => _$SavedSearchPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SavedSearchPage _$SavedSearchPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SavedSearchPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
      final val = SavedSearchPage(
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => SavedSearch.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$SavedSearchPageToJson(SavedSearchPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': instance.nextCursor,
    };
