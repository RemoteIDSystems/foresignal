// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'gps_logger_fix.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GpsLoggerFixCWProxy {
  GpsLoggerFix lat(num lat);

  GpsLoggerFix lon(num lon);

  GpsLoggerFix time(String? time);

  GpsLoggerFix acc(num? acc);

  GpsLoggerFix alt(num? alt);

  GpsLoggerFix spd(num? spd);

  GpsLoggerFix dir(num? dir);

  GpsLoggerFix batt(num? batt);

  GpsLoggerFix dev(String? dev);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GpsLoggerFix(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GpsLoggerFix(...).copyWith(id: 12, name: "My name")
  /// ```
  GpsLoggerFix call({
    num lat,
    num lon,
    String? time,
    num? acc,
    num? alt,
    num? spd,
    num? dir,
    num? batt,
    String? dev,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGpsLoggerFix.copyWith(...)` or call `instanceOfGpsLoggerFix.copyWith.fieldName(value)` for a single field.
class _$GpsLoggerFixCWProxyImpl implements _$GpsLoggerFixCWProxy {
  const _$GpsLoggerFixCWProxyImpl(this._value);

  final GpsLoggerFix _value;

  @override
  GpsLoggerFix lat(num lat) => call(lat: lat);

  @override
  GpsLoggerFix lon(num lon) => call(lon: lon);

  @override
  GpsLoggerFix time(String? time) => call(time: time);

  @override
  GpsLoggerFix acc(num? acc) => call(acc: acc);

  @override
  GpsLoggerFix alt(num? alt) => call(alt: alt);

  @override
  GpsLoggerFix spd(num? spd) => call(spd: spd);

  @override
  GpsLoggerFix dir(num? dir) => call(dir: dir);

  @override
  GpsLoggerFix batt(num? batt) => call(batt: batt);

  @override
  GpsLoggerFix dev(String? dev) => call(dev: dev);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GpsLoggerFix(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GpsLoggerFix(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GpsLoggerFix call({
    Object? lat = const $CopyWithPlaceholder(),
    Object? lon = const $CopyWithPlaceholder(),
    Object? time = const $CopyWithPlaceholder(),
    Object? acc = const $CopyWithPlaceholder(),
    Object? alt = const $CopyWithPlaceholder(),
    Object? spd = const $CopyWithPlaceholder(),
    Object? dir = const $CopyWithPlaceholder(),
    Object? batt = const $CopyWithPlaceholder(),
    Object? dev = const $CopyWithPlaceholder(),
  }) {
    return GpsLoggerFix(
      lat: lat == const $CopyWithPlaceholder() || lat == null
          ? _value.lat
          // ignore: cast_nullable_to_non_nullable
          : lat as num,
      lon: lon == const $CopyWithPlaceholder() || lon == null
          ? _value.lon
          // ignore: cast_nullable_to_non_nullable
          : lon as num,
      time: time == const $CopyWithPlaceholder()
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as String?,
      acc: acc == const $CopyWithPlaceholder()
          ? _value.acc
          // ignore: cast_nullable_to_non_nullable
          : acc as num?,
      alt: alt == const $CopyWithPlaceholder()
          ? _value.alt
          // ignore: cast_nullable_to_non_nullable
          : alt as num?,
      spd: spd == const $CopyWithPlaceholder()
          ? _value.spd
          // ignore: cast_nullable_to_non_nullable
          : spd as num?,
      dir: dir == const $CopyWithPlaceholder()
          ? _value.dir
          // ignore: cast_nullable_to_non_nullable
          : dir as num?,
      batt: batt == const $CopyWithPlaceholder()
          ? _value.batt
          // ignore: cast_nullable_to_non_nullable
          : batt as num?,
      dev: dev == const $CopyWithPlaceholder()
          ? _value.dev
          // ignore: cast_nullable_to_non_nullable
          : dev as String?,
    );
  }
}

extension $GpsLoggerFixCopyWith on GpsLoggerFix {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGpsLoggerFix.copyWith(...)` or `instanceOfGpsLoggerFix.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GpsLoggerFixCWProxy get copyWith => _$GpsLoggerFixCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GpsLoggerFix _$GpsLoggerFixFromJson(Map<String, dynamic> json) =>
    $checkedCreate('GpsLoggerFix', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['lat', 'lon']);
      final val = GpsLoggerFix(
        lat: $checkedConvert('lat', (v) => v as num),
        lon: $checkedConvert('lon', (v) => v as num),
        time: $checkedConvert('time', (v) => v as String?),
        acc: $checkedConvert('acc', (v) => v as num?),
        alt: $checkedConvert('alt', (v) => v as num?),
        spd: $checkedConvert('spd', (v) => v as num?),
        dir: $checkedConvert('dir', (v) => v as num?),
        batt: $checkedConvert('batt', (v) => v as num?),
        dev: $checkedConvert('dev', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$GpsLoggerFixToJson(GpsLoggerFix instance) =>
    <String, dynamic>{
      'lat': instance.lat,
      'lon': instance.lon,
      'time': ?instance.time,
      'acc': ?instance.acc,
      'alt': ?instance.alt,
      'spd': ?instance.spd,
      'dir': ?instance.dir,
      'batt': ?instance.batt,
      'dev': ?instance.dev,
    };
