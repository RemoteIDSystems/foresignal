// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'series.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SeriesCWProxy {
  Series productId(String productId);

  Series lon(num lon);

  Series lat(num lat);

  Series placeId(String? placeId);

  Series timezone(String? timezone);

  Series units(String? units);

  Series label(String? label);

  Series from(DateTime? from);

  Series to(DateTime? to);

  Series run(String? run);

  Series items(List<SeriesSample> items);

  Series attribution(Attribution? attribution);

  Series serverTime(DateTime serverTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Series(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Series(...).copyWith(id: 12, name: "My name")
  /// ```
  Series call({
    String productId,
    num lon,
    num lat,
    String? placeId,
    String? timezone,
    String? units,
    String? label,
    DateTime? from,
    DateTime? to,
    String? run,
    List<SeriesSample> items,
    Attribution? attribution,
    DateTime serverTime,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSeries.copyWith(...)` or call `instanceOfSeries.copyWith.fieldName(value)` for a single field.
class _$SeriesCWProxyImpl implements _$SeriesCWProxy {
  const _$SeriesCWProxyImpl(this._value);

  final Series _value;

  @override
  Series productId(String productId) => call(productId: productId);

  @override
  Series lon(num lon) => call(lon: lon);

  @override
  Series lat(num lat) => call(lat: lat);

  @override
  Series placeId(String? placeId) => call(placeId: placeId);

  @override
  Series timezone(String? timezone) => call(timezone: timezone);

  @override
  Series units(String? units) => call(units: units);

  @override
  Series label(String? label) => call(label: label);

  @override
  Series from(DateTime? from) => call(from: from);

  @override
  Series to(DateTime? to) => call(to: to);

  @override
  Series run(String? run) => call(run: run);

  @override
  Series items(List<SeriesSample> items) => call(items: items);

  @override
  Series attribution(Attribution? attribution) =>
      call(attribution: attribution);

  @override
  Series serverTime(DateTime serverTime) => call(serverTime: serverTime);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Series(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Series(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Series call({
    Object? productId = const $CopyWithPlaceholder(),
    Object? lon = const $CopyWithPlaceholder(),
    Object? lat = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
    Object? units = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? items = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? serverTime = const $CopyWithPlaceholder(),
  }) {
    return Series(
      productId: productId == const $CopyWithPlaceholder() || productId == null
          ? _value.productId
          // ignore: cast_nullable_to_non_nullable
          : productId as String,
      lon: lon == const $CopyWithPlaceholder() || lon == null
          ? _value.lon
          // ignore: cast_nullable_to_non_nullable
          : lon as num,
      lat: lat == const $CopyWithPlaceholder() || lat == null
          ? _value.lat
          // ignore: cast_nullable_to_non_nullable
          : lat as num,
      placeId: placeId == const $CopyWithPlaceholder()
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String?,
      timezone: timezone == const $CopyWithPlaceholder()
          ? _value.timezone
          // ignore: cast_nullable_to_non_nullable
          : timezone as String?,
      units: units == const $CopyWithPlaceholder()
          ? _value.units
          // ignore: cast_nullable_to_non_nullable
          : units as String?,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
      from: from == const $CopyWithPlaceholder()
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime?,
      to: to == const $CopyWithPlaceholder()
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime?,
      run: run == const $CopyWithPlaceholder()
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String?,
      items: items == const $CopyWithPlaceholder() || items == null
          ? _value.items
          // ignore: cast_nullable_to_non_nullable
          : items as List<SeriesSample>,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as Attribution?,
      serverTime:
          serverTime == const $CopyWithPlaceholder() || serverTime == null
          ? _value.serverTime
          // ignore: cast_nullable_to_non_nullable
          : serverTime as DateTime,
    );
  }
}

extension $SeriesCopyWith on Series {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSeries.copyWith(...)` or `instanceOfSeries.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SeriesCWProxy get copyWith => _$SeriesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Series _$SeriesFromJson(Map<String, dynamic> json) => $checkedCreate(
  'Series',
  json,
  ($checkedConvert) {
    $checkKeys(
      json,
      requiredKeys: const ['productId', 'lon', 'lat', 'items', 'serverTime'],
    );
    final val = Series(
      productId: $checkedConvert('productId', (v) => v as String),
      lon: $checkedConvert('lon', (v) => v as num),
      lat: $checkedConvert('lat', (v) => v as num),
      placeId: $checkedConvert('placeId', (v) => v as String?),
      timezone: $checkedConvert('timezone', (v) => v as String?),
      units: $checkedConvert('units', (v) => v as String?),
      label: $checkedConvert('label', (v) => v as String?),
      from: $checkedConvert(
        'from',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      to: $checkedConvert(
        'to',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      run: $checkedConvert('run', (v) => v as String?),
      items: $checkedConvert(
        'items',
        (v) => (v as List<dynamic>)
            .map((e) => SeriesSample.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      attribution: $checkedConvert(
        'attribution',
        (v) =>
            v == null ? null : Attribution.fromJson(v as Map<String, dynamic>),
      ),
      serverTime: $checkedConvert(
        'serverTime',
        (v) => DateTime.parse(v as String),
      ),
    );
    return val;
  },
);

Map<String, dynamic> _$SeriesToJson(Series instance) => <String, dynamic>{
  'productId': instance.productId,
  'lon': instance.lon,
  'lat': instance.lat,
  'placeId': ?instance.placeId,
  'timezone': ?instance.timezone,
  'units': ?instance.units,
  'label': ?instance.label,
  'from': ?instance.from?.toIso8601String(),
  'to': ?instance.to?.toIso8601String(),
  'run': ?instance.run,
  'items': instance.items.map((e) => e.toJson()).toList(),
  'attribution': ?instance.attribution?.toJson(),
  'serverTime': instance.serverTime.toIso8601String(),
};
