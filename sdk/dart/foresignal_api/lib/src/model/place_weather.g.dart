// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'place_weather.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PlaceWeatherCWProxy {
  PlaceWeather place(String place);

  PlaceWeather radiusKm(num radiusKm);

  PlaceWeather radar(PlaceWeatherRadar? radar);

  PlaceWeather hail(PlaceWeatherHail? hail);

  PlaceWeather precip(PlaceWeatherPrecip? precip);

  PlaceWeather lightning(PlaceWeatherLightning? lightning);

  PlaceWeather cell(PlaceWeatherCell? cell);

  PlaceWeather smoke(PlaceWeatherSmoke? smoke);

  PlaceWeather forecast(PlaceWeatherForecast? forecast);

  PlaceWeather automated(bool automated);

  PlaceWeather notAWarning(bool notAWarning);

  PlaceWeather marine(PlaceWeatherMarine? marine);

  PlaceWeather aurora(PlaceWeatherAurora? aurora);

  PlaceWeather heat(PlaceWeatherHeat? heat);

  PlaceWeather uv(PlaceWeatherUv? uv);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeather(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeather(...).copyWith(id: 12, name: "My name")
  /// ```
  PlaceWeather call({
    String place,
    num radiusKm,
    PlaceWeatherRadar? radar,
    PlaceWeatherHail? hail,
    PlaceWeatherPrecip? precip,
    PlaceWeatherLightning? lightning,
    PlaceWeatherCell? cell,
    PlaceWeatherSmoke? smoke,
    PlaceWeatherForecast? forecast,
    bool automated,
    bool notAWarning,
    PlaceWeatherMarine? marine,
    PlaceWeatherAurora? aurora,
    PlaceWeatherHeat? heat,
    PlaceWeatherUv? uv,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPlaceWeather.copyWith(...)` or call `instanceOfPlaceWeather.copyWith.fieldName(value)` for a single field.
class _$PlaceWeatherCWProxyImpl implements _$PlaceWeatherCWProxy {
  const _$PlaceWeatherCWProxyImpl(this._value);

  final PlaceWeather _value;

  @override
  PlaceWeather place(String place) => call(place: place);

  @override
  PlaceWeather radiusKm(num radiusKm) => call(radiusKm: radiusKm);

  @override
  PlaceWeather radar(PlaceWeatherRadar? radar) => call(radar: radar);

  @override
  PlaceWeather hail(PlaceWeatherHail? hail) => call(hail: hail);

  @override
  PlaceWeather precip(PlaceWeatherPrecip? precip) => call(precip: precip);

  @override
  PlaceWeather lightning(PlaceWeatherLightning? lightning) =>
      call(lightning: lightning);

  @override
  PlaceWeather cell(PlaceWeatherCell? cell) => call(cell: cell);

  @override
  PlaceWeather smoke(PlaceWeatherSmoke? smoke) => call(smoke: smoke);

  @override
  PlaceWeather forecast(PlaceWeatherForecast? forecast) =>
      call(forecast: forecast);

  @override
  PlaceWeather automated(bool automated) => call(automated: automated);

  @override
  PlaceWeather notAWarning(bool notAWarning) => call(notAWarning: notAWarning);

  @override
  PlaceWeather marine(PlaceWeatherMarine? marine) => call(marine: marine);

  @override
  PlaceWeather aurora(PlaceWeatherAurora? aurora) => call(aurora: aurora);

  @override
  PlaceWeather heat(PlaceWeatherHeat? heat) => call(heat: heat);

  @override
  PlaceWeather uv(PlaceWeatherUv? uv) => call(uv: uv);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PlaceWeather(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PlaceWeather(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PlaceWeather call({
    Object? place = const $CopyWithPlaceholder(),
    Object? radiusKm = const $CopyWithPlaceholder(),
    Object? radar = const $CopyWithPlaceholder(),
    Object? hail = const $CopyWithPlaceholder(),
    Object? precip = const $CopyWithPlaceholder(),
    Object? lightning = const $CopyWithPlaceholder(),
    Object? cell = const $CopyWithPlaceholder(),
    Object? smoke = const $CopyWithPlaceholder(),
    Object? forecast = const $CopyWithPlaceholder(),
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? marine = const $CopyWithPlaceholder(),
    Object? aurora = const $CopyWithPlaceholder(),
    Object? heat = const $CopyWithPlaceholder(),
    Object? uv = const $CopyWithPlaceholder(),
  }) {
    return PlaceWeather(
      place: place == const $CopyWithPlaceholder() || place == null
          ? _value.place
          // ignore: cast_nullable_to_non_nullable
          : place as String,
      radiusKm: radiusKm == const $CopyWithPlaceholder() || radiusKm == null
          ? _value.radiusKm
          // ignore: cast_nullable_to_non_nullable
          : radiusKm as num,
      radar: radar == const $CopyWithPlaceholder()
          ? _value.radar
          // ignore: cast_nullable_to_non_nullable
          : radar as PlaceWeatherRadar?,
      hail: hail == const $CopyWithPlaceholder()
          ? _value.hail
          // ignore: cast_nullable_to_non_nullable
          : hail as PlaceWeatherHail?,
      precip: precip == const $CopyWithPlaceholder()
          ? _value.precip
          // ignore: cast_nullable_to_non_nullable
          : precip as PlaceWeatherPrecip?,
      lightning: lightning == const $CopyWithPlaceholder()
          ? _value.lightning
          // ignore: cast_nullable_to_non_nullable
          : lightning as PlaceWeatherLightning?,
      cell: cell == const $CopyWithPlaceholder()
          ? _value.cell
          // ignore: cast_nullable_to_non_nullable
          : cell as PlaceWeatherCell?,
      smoke: smoke == const $CopyWithPlaceholder()
          ? _value.smoke
          // ignore: cast_nullable_to_non_nullable
          : smoke as PlaceWeatherSmoke?,
      forecast: forecast == const $CopyWithPlaceholder()
          ? _value.forecast
          // ignore: cast_nullable_to_non_nullable
          : forecast as PlaceWeatherForecast?,
      automated: automated == const $CopyWithPlaceholder() || automated == null
          ? _value.automated
          // ignore: cast_nullable_to_non_nullable
          : automated as bool,
      notAWarning:
          notAWarning == const $CopyWithPlaceholder() || notAWarning == null
          ? _value.notAWarning
          // ignore: cast_nullable_to_non_nullable
          : notAWarning as bool,
      marine: marine == const $CopyWithPlaceholder()
          ? _value.marine
          // ignore: cast_nullable_to_non_nullable
          : marine as PlaceWeatherMarine?,
      aurora: aurora == const $CopyWithPlaceholder()
          ? _value.aurora
          // ignore: cast_nullable_to_non_nullable
          : aurora as PlaceWeatherAurora?,
      heat: heat == const $CopyWithPlaceholder()
          ? _value.heat
          // ignore: cast_nullable_to_non_nullable
          : heat as PlaceWeatherHeat?,
      uv: uv == const $CopyWithPlaceholder()
          ? _value.uv
          // ignore: cast_nullable_to_non_nullable
          : uv as PlaceWeatherUv?,
    );
  }
}

extension $PlaceWeatherCopyWith on PlaceWeather {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPlaceWeather.copyWith(...)` or `instanceOfPlaceWeather.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PlaceWeatherCWProxy get copyWith => _$PlaceWeatherCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PlaceWeather _$PlaceWeatherFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PlaceWeather', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['place', 'radiusKm', 'automated', 'notAWarning'],
      );
      final val = PlaceWeather(
        place: $checkedConvert('place', (v) => v as String),
        radiusKm: $checkedConvert('radiusKm', (v) => v as num),
        radar: $checkedConvert(
          'radar',
          (v) => v == null
              ? null
              : PlaceWeatherRadar.fromJson(v as Map<String, dynamic>),
        ),
        hail: $checkedConvert(
          'hail',
          (v) => v == null
              ? null
              : PlaceWeatherHail.fromJson(v as Map<String, dynamic>),
        ),
        precip: $checkedConvert(
          'precip',
          (v) => v == null
              ? null
              : PlaceWeatherPrecip.fromJson(v as Map<String, dynamic>),
        ),
        lightning: $checkedConvert(
          'lightning',
          (v) => v == null
              ? null
              : PlaceWeatherLightning.fromJson(v as Map<String, dynamic>),
        ),
        cell: $checkedConvert(
          'cell',
          (v) => v == null
              ? null
              : PlaceWeatherCell.fromJson(v as Map<String, dynamic>),
        ),
        smoke: $checkedConvert(
          'smoke',
          (v) => v == null
              ? null
              : PlaceWeatherSmoke.fromJson(v as Map<String, dynamic>),
        ),
        forecast: $checkedConvert(
          'forecast',
          (v) => v == null
              ? null
              : PlaceWeatherForecast.fromJson(v as Map<String, dynamic>),
        ),
        automated: $checkedConvert('automated', (v) => v as bool),
        notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
        marine: $checkedConvert(
          'marine',
          (v) => v == null
              ? null
              : PlaceWeatherMarine.fromJson(v as Map<String, dynamic>),
        ),
        aurora: $checkedConvert(
          'aurora',
          (v) => v == null
              ? null
              : PlaceWeatherAurora.fromJson(v as Map<String, dynamic>),
        ),
        heat: $checkedConvert(
          'heat',
          (v) => v == null
              ? null
              : PlaceWeatherHeat.fromJson(v as Map<String, dynamic>),
        ),
        uv: $checkedConvert(
          'uv',
          (v) => v == null
              ? null
              : PlaceWeatherUv.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$PlaceWeatherToJson(PlaceWeather instance) =>
    <String, dynamic>{
      'place': instance.place,
      'radiusKm': instance.radiusKm,
      'radar': ?instance.radar?.toJson(),
      'hail': ?instance.hail?.toJson(),
      'precip': ?instance.precip?.toJson(),
      'lightning': ?instance.lightning?.toJson(),
      'cell': ?instance.cell?.toJson(),
      'smoke': ?instance.smoke?.toJson(),
      'forecast': ?instance.forecast?.toJson(),
      'automated': instance.automated,
      'notAWarning': instance.notAWarning,
      'marine': ?instance.marine?.toJson(),
      'aurora': ?instance.aurora?.toJson(),
      'heat': ?instance.heat?.toJson(),
      'uv': ?instance.uv?.toJson(),
    };
