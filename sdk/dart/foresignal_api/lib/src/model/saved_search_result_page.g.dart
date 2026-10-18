// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'saved_search_result_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SavedSearchResultPageCWProxy {
  SavedSearchResultPage items(List<SavedSearchHit> items);

  SavedSearchResultPage nextCursor(String? nextCursor);

  SavedSearchResultPage ranAt(DateTime? ranAt);

  SavedSearchResultPage semantic(bool? semantic);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchResultPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchResultPage(...).copyWith(id: 12, name: "My name")
  /// ```
  SavedSearchResultPage call({
    List<SavedSearchHit> items,
    String? nextCursor,
    DateTime? ranAt,
    bool? semantic,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSavedSearchResultPage.copyWith(...)` or call `instanceOfSavedSearchResultPage.copyWith.fieldName(value)` for a single field.
class _$SavedSearchResultPageCWProxyImpl
    implements _$SavedSearchResultPageCWProxy {
  const _$SavedSearchResultPageCWProxyImpl(this._value);

  final SavedSearchResultPage _value;

  @override
  SavedSearchResultPage items(List<SavedSearchHit> items) => call(items: items);

  @override
  SavedSearchResultPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  @override
  SavedSearchResultPage ranAt(DateTime? ranAt) => call(ranAt: ranAt);

  @override
  SavedSearchResultPage semantic(bool? semantic) => call(semantic: semantic);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SavedSearchResultPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SavedSearchResultPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SavedSearchResultPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
    Object? ranAt = const $CopyWithPlaceholder(),
    Object? semantic = const $CopyWithPlaceholder(),
  }) {
    return SavedSearchResultPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SavedSearchHit>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
      ranAt: ranAt == const $CopyWithPlaceholder()
          ? _value.ranAt
          // ignore: cast_nullable_to_non_nullable
          : ranAt as DateTime?,
      semantic: semantic == const $CopyWithPlaceholder()
          ? _value.semantic
          // ignore: cast_nullable_to_non_nullable
          : semantic as bool?,
    );
  }
}

extension $SavedSearchResultPageCopyWith on SavedSearchResultPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSavedSearchResultPage.copyWith(...)` or `instanceOfSavedSearchResultPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SavedSearchResultPageCWProxy get copyWith =>
      _$SavedSearchResultPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SavedSearchResultPage _$SavedSearchResultPageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SavedSearchResultPage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['items', 'nextCursor']);
  final val = SavedSearchResultPage(
    items: $checkedConvert(
      'items',
      (v) => (v as List<dynamic>)
          .map((e) => SavedSearchHit.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
    ranAt: $checkedConvert(
      'ranAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    semantic: $checkedConvert('semantic', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$SavedSearchResultPageToJson(
  SavedSearchResultPage instance,
) => <String, dynamic>{
  'items': instance.items.map((e) => e.toJson()).toList(),
  'nextCursor': instance.nextCursor,
  'ranAt': ?instance.ranAt?.toIso8601String(),
  'semantic': ?instance.semantic,
};
