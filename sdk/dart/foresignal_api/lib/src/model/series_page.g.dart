// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'series_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SeriesPageCWProxy {
  SeriesPage items(List<SeriesCatalogEntry> items);

  SeriesPage nextCursor(String? nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesPage(...).copyWith(id: 12, name: "My name")
  /// ```
  SeriesPage call({List<SeriesCatalogEntry> items, String? nextCursor});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSeriesPage.copyWith(...)` or call `instanceOfSeriesPage.copyWith.fieldName(value)` for a single field.
class _$SeriesPageCWProxyImpl implements _$SeriesPageCWProxy {
  const _$SeriesPageCWProxyImpl(this._value);

  final SeriesPage _value;

  @override
  SeriesPage items(List<SeriesCatalogEntry> items) => call(items: items);

  @override
  SeriesPage nextCursor(String? nextCursor) => call(nextCursor: nextCursor);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SeriesPage call({
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
  }) {
    return SeriesPage(
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SeriesCatalogEntry>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
    );
  }
}

extension $SeriesPageCopyWith on SeriesPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSeriesPage.copyWith(...)` or `instanceOfSeriesPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SeriesPageCWProxy get copyWith => _$SeriesPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SeriesPage _$SeriesPageFromJson(Map<String, dynamic> json) => $checkedCreate(
  'SeriesPage',
  json,
  ($checkedConvert) {
    $checkKeys(json, requiredKeys: const ['items']);
    final val = SeriesPage(
      items: $checkedConvert(
        'items',
        (v) => (v as List<dynamic>)
            .map((e) => SeriesCatalogEntry.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
    );
    return val;
  },
);

Map<String, dynamic> _$SeriesPageToJson(SeriesPage instance) =>
    <String, dynamic>{
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
    };
