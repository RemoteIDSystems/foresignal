// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_digest.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherDigestCWProxy {
  PlaceWeatherDigest placeId(String placeId);

  PlaceWeatherDigest place(String place);

  PlaceWeatherDigest timezone(String timezone);

  PlaceWeatherDigest now(PlaceWeatherDigestNow? now);

  PlaceWeatherDigest period(PlaceWeatherDigestPeriod? period);

  PlaceWeatherDigest next(PlaceWeatherDigestNext? next);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigest(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherDigest call({
    String placeId,
    String place,
    String timezone,
    PlaceWeatherDigestNow? now,
    PlaceWeatherDigestPeriod? period,
    PlaceWeatherDigestNext? next,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherDigest.copyWith(...)` or call `instanceOfPlaceWeatherDigest.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherDigestCWProxyImpl implements _$PlaceWeatherDigestCWProxy {
  const _$PlaceWeatherDigestCWProxyImpl(this._value);

  final PlaceWeatherDigest _value;

  @override
  PlaceWeatherDigest placeId(String placeId) => call(placeId: placeId);

  @override
  PlaceWeatherDigest place(String place) => call(place: place);

  @override
  PlaceWeatherDigest timezone(String timezone) => call(timezone: timezone);

  @override
  PlaceWeatherDigest now(PlaceWeatherDigestNow? now) => call(now: now);

  @override
  PlaceWeatherDigest period(PlaceWeatherDigestPeriod? period) =>
      call(period: period);

  @override
  PlaceWeatherDigest next(PlaceWeatherDigestNext? next) => call(next: next);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherDigest call({
    Object? placeId = const $CopyWithPlaceholder(),
    Object? place = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
    Object? now = const $CopyWithPlaceholder(),
    Object? period = const $CopyWithPlaceholder(),
    Object? next = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherDigest(
      placeId: placeId == const $CopyWithPlaceholder() || placeId == null
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String,
      place: place == const $CopyWithPlaceholder() || place == null
          ? _value.place
          // ignore: cast_nullable_to_non_nullable
          : place as String,
      timezone: timezone == const $CopyWithPlaceholder() || timezone == null
          ? _value.timezone
          // ignore: cast_nullable_to_non_nullable
          : timezone as String,
      now: now == const $CopyWithPlaceholder()
          ? _value.now
          // ignore: cast_nullable_to_non_nullable
          : now as PlaceWeatherDigestNow?,
      period: period == const $CopyWithPlaceholder()
          ? _value.period
          // ignore: cast_nullable_to_non_nullable
          : period as PlaceWeatherDigestPeriod?,
      next: next == const $CopyWithPlaceholder()
          ? _value.next
          // ignore: cast_nullable_to_non_nullable
          : next as PlaceWeatherDigestNext?,
    );
  }
}

extension $PlaceWeatherDigestCopyWith on PlaceWeatherDigest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherDigest.copyWith(...)` or `instanceOfPlaceWeatherDigest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherDigestCWProxy get copyWith =>
      _$PlaceWeatherDigestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherDigest _$PlaceWeatherDigestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PlaceWeatherDigest', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['placeId', 'place', 'timezone']);
      final val = PlaceWeatherDigest(
        placeId: $checkedConvert('placeId', (v) => v as String),
        place: $checkedConvert('place', (v) => v as String),
        timezone: $checkedConvert('timezone', (v) => v as String),
        now: $checkedConvert(
          'now',
          (v) => v == null
              ? null
              : PlaceWeatherDigestNow.fromJson(v as Map<String, dynamic>),
        ),
        period: $checkedConvert(
          'period',
          (v) => v == null
              ? null
              : PlaceWeatherDigestPeriod.fromJson(v as Map<String, dynamic>),
        ),
        next: $checkedConvert(
          'next',
          (v) => v == null
              ? null
              : PlaceWeatherDigestNext.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PlaceWeatherDigestToJson(PlaceWeatherDigest instance) =>
    <String, dynamic>{
      'placeId': instance.placeId,
      'place': instance.place,
      'timezone': instance.timezone,
      'now': ?instance.now?.toJson(),
      'period': ?instance.period?.toJson(),
      'next': ?instance.next?.toJson(),
    };
