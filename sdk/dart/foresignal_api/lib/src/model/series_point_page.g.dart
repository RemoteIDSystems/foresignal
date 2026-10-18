// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'series_point_page.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SeriesPointPageCWProxy {
  SeriesPointPage series(SeriesCatalogEntry series);

  SeriesPointPage items(List<SeriesPoint> items);

  SeriesPointPage nextCursor(String? nextCursor);

  SeriesPointPage truncated(bool? truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesPointPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesPointPage(...).copyWith(id: 12, name: "My name")
  /// ```
  SeriesPointPage call({
    SeriesCatalogEntry series,
    List<SeriesPoint> items,
    String? nextCursor,
    bool? truncated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSeriesPointPage.copyWith(...)` or call `instanceOfSeriesPointPage.copyWith.fieldName(value)` for a single field.
class _$SeriesPointPageCWProxyImpl implements _$SeriesPointPageCWProxy {
  const _$SeriesPointPageCWProxyImpl(this._value);

  final SeriesPointPage _value;

  @override
  SeriesPointPage series(SeriesCatalogEntry series) => call(series: series);

  @override
  SeriesPointPage items(List<SeriesPoint> items) => call(items: items);

  @override
  SeriesPointPage nextCursor(String? nextCursor) =>
      call(nextCursor: nextCursor);

  @override
  SeriesPointPage truncated(bool? truncated) => call(truncated: truncated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SeriesPointPage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SeriesPointPage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SeriesPointPage call({
    Object? series = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
    Object? nextCursor = const $CopyWithPlaceholder(),
    Object? truncated = const $CopyWithPlaceholder(),
  }) {
    return SeriesPointPage(
      series: series == const $CopyWithPlaceholder() || series == null
          ? _value.series
          // ignore: cast_nullable_to_non_nullable
          : series as SeriesCatalogEntry,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SeriesPoint>,
      nextCursor: nextCursor == const $CopyWithPlaceholder()
          ? _value.nextCursor
          // ignore: cast_nullable_to_non_nullable
          : nextCursor as String?,
      truncated: truncated == const $CopyWithPlaceholder()
          ? _value.truncated
          // ignore: cast_nullable_to_non_nullable
          : truncated as bool?,
    );
  }
}

extension $SeriesPointPageCopyWith on SeriesPointPage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSeriesPointPage.copyWith(...)` or `instanceOfSeriesPointPage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SeriesPointPageCWProxy get copyWith => _$SeriesPointPageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SeriesPointPage _$SeriesPointPageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SeriesPointPage', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['series', 'items']);
      final val = SeriesPointPage(
        series: $checkedConvert(
          'series',
          (v) => SeriesCatalogEntry.fromJson(v as Map<String, dynamic>),
        ),
        items: $checkedConvert(
          'items',
          (v) => (v as List<dynamic>)
              .map((e) => SeriesPoint.fromJson(e as Map<String, dynamic>))
              .toList(),
        ),
        nextCursor: $checkedConvert('nextCursor', (v) => v as String?),
        truncated: $checkedConvert('truncated', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$SeriesPointPageToJson(SeriesPointPage instance) =>
    <String, dynamic>{
      'series': instance.series.toJson(),
      'items': instance.items.map((e) => e.toJson()).toList(),
      'nextCursor': ?instance.nextCursor,
      'truncated': ?instance.truncated,
    };
