// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather_digest_next.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherDigestNextCWProxy {
  PlaceWeatherDigestNext nws(PlaceWeatherDigestNextNws? nws);

  PlaceWeatherDigestNext gfs(PlaceWeatherDigestNextGfs? gfs);

  PlaceWeatherDigestNext hrrr(PlaceWeatherDigestNextHrrr? hrrr);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNext(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNext(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeatherDigestNext call({
    PlaceWeatherDigestNextNws? nws,
    PlaceWeatherDigestNextGfs? gfs,
    PlaceWeatherDigestNextHrrr? hrrr,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeatherDigestNext.copyWith(...)` or call `instanceOfPlaceWeatherDigestNext.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherDigestNextCWProxyImpl
    implements _$PlaceWeatherDigestNextCWProxy {
  const _$PlaceWeatherDigestNextCWProxyImpl(this._value);

  final PlaceWeatherDigestNext _value;

  @override
  PlaceWeatherDigestNext nws(PlaceWeatherDigestNextNws? nws) => call(nws: nws);

  @override
  PlaceWeatherDigestNext gfs(PlaceWeatherDigestNextGfs? gfs) => call(gfs: gfs);

  @override
  PlaceWeatherDigestNext hrrr(PlaceWeatherDigestNextHrrr? hrrr) =>
      call(hrrr: hrrr);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeatherDigestNext(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeatherDigestNext(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeatherDigestNext call({
    Object? nws = const $CopyWithPlaceholder(),
    Object? gfs = const $CopyWithPlaceholder(),
    Object? hrrr = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeatherDigestNext(
      nws: nws == const $CopyWithPlaceholder()
          ? _value.nws
          // ignore: cast_nullable_to_non_nullable
          : nws as PlaceWeatherDigestNextNws?,
      gfs: gfs == const $CopyWithPlaceholder()
          ? _value.gfs
          // ignore: cast_nullable_to_non_nullable
          : gfs as PlaceWeatherDigestNextGfs?,
      hrrr: hrrr == const $CopyWithPlaceholder()
          ? _value.hrrr
          // ignore: cast_nullable_to_non_nullable
          : hrrr as PlaceWeatherDigestNextHrrr?,
    );
  }
}

extension $PlaceWeatherDigestNextCopyWith on PlaceWeatherDigestNext {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeatherDigestNext.copyWith(...)` or `instanceOfPlaceWeatherDigestNext.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherDigestNextCWProxy get copyWith =>
      _$PlaceWeatherDigestNextCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeatherDigestNext _$PlaceWeatherDigestNextFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PlaceWeatherDigestNext', json, ($checkedConvert) {
  final val = PlaceWeatherDigestNext(
    nws: $checkedConvert(
      'nws',
      (v) => v == null
          ? null
          : PlaceWeatherDigestNextNws.fromJson(v as Map<String, dynamic>),
    ),
    gfs: $checkedConvert(
      'gfs',
      (v) => v == null
          ? null
          : PlaceWeatherDigestNextGfs.fromJson(v as Map<String, dynamic>),
    ),
    hrrr: $checkedConvert(
      'hrrr',
      (v) => v == null
          ? null
          : PlaceWeatherDigestNextHrrr.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$PlaceWeatherDigestNextToJson(
  PlaceWeatherDigestNext instance,
) => <String, dynamic>{
  'nws': ?instance.nws?.toJson(),
  'gfs': ?instance.gfs?.toJson(),
  'hrrr': ?instance.hrrr?.toJson(),
};
