// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_series_point.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastSeriesPointCWProxy {
  ForecastSeriesPoint time(DateTime time);

  ForecastSeriesPoint tempC(num? tempC);

  ForecastSeriesPoint windSpeedMs(num? windSpeedMs);

  ForecastSeriesPoint windFromDeg(int? windFromDeg);

  ForecastSeriesPoint gustMs(num? gustMs);

  ForecastSeriesPoint popPct(int? popPct);

  ForecastSeriesPoint precipMm(num? precipMm);

  ForecastSeriesPoint skyCoverPct(int? skyCoverPct);

  ForecastSeriesPoint smokeUgM3(num? smokeUgM3);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastSeriesPoint(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastSeriesPoint(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastSeriesPoint call({
    DateTime time,
    num? tempC,
    num? windSpeedMs,
    int? windFromDeg,
    num? gustMs,
    int? popPct,
    num? precipMm,
    int? skyCoverPct,
    num? smokeUgM3,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastSeriesPoint.copyWith(...)` or call `instanceOfForecastSeriesPoint.copyWith.fieldName(value)` for a single field.
class _$ForecastSeriesPointCWProxyImpl implements _$ForecastSeriesPointCWProxy {
  const _$ForecastSeriesPointCWProxyImpl(this._value);

  final ForecastSeriesPoint _value;

  @override
  ForecastSeriesPoint time(DateTime time) => call(time: time);

  @override
  ForecastSeriesPoint tempC(num? tempC) => call(tempC: tempC);

  @override
  ForecastSeriesPoint windSpeedMs(num? windSpeedMs) =>
      call(windSpeedMs: windSpeedMs);

  @override
  ForecastSeriesPoint windFromDeg(int? windFromDeg) =>
      call(windFromDeg: windFromDeg);

  @override
  ForecastSeriesPoint gustMs(num? gustMs) => call(gustMs: gustMs);

  @override
  ForecastSeriesPoint popPct(int? popPct) => call(popPct: popPct);

  @override
  ForecastSeriesPoint precipMm(num? precipMm) => call(precipMm: precipMm);

  @override
  ForecastSeriesPoint skyCoverPct(int? skyCoverPct) =>
      call(skyCoverPct: skyCoverPct);

  @override
  ForecastSeriesPoint smokeUgM3(num? smokeUgM3) => call(smokeUgM3: smokeUgM3);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastSeriesPoint(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastSeriesPoint(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastSeriesPoint call({
    Object? time = const $CopyWithPlaceholder(),
    Object? tempC = const $CopyWithPlaceholder(),
    Object? windSpeedMs = const $CopyWithPlaceholder(),
    Object? windFromDeg = const $CopyWithPlaceholder(),
    Object? gustMs = const $CopyWithPlaceholder(),
    Object? popPct = const $CopyWithPlaceholder(),
    Object? precipMm = const $CopyWithPlaceholder(),
    Object? skyCoverPct = const $CopyWithPlaceholder(),
    Object? smokeUgM3 = const $CopyWithPlaceholder(),
  }) {
    return ForecastSeriesPoint(
      time: time == const $CopyWithPlaceholder() || time == null
          ? _value.time
          // ignore: cast_nullable_to_non_nullable
          : time as DateTime,
      tempC: tempC == const $CopyWithPlaceholder()
          ? _value.tempC
          // ignore: cast_nullable_to_non_nullable
          : tempC as num?,
      windSpeedMs: windSpeedMs == const $CopyWithPlaceholder()
          ? _value.windSpeedMs
          // ignore: cast_nullable_to_non_nullable
          : windSpeedMs as num?,
      windFromDeg: windFromDeg == const $CopyWithPlaceholder()
          ? _value.windFromDeg
          // ignore: cast_nullable_to_non_nullable
          : windFromDeg as int?,
      gustMs: gustMs == const $CopyWithPlaceholder()
          ? _value.gustMs
          // ignore: cast_nullable_to_non_nullable
          : gustMs as num?,
      popPct: popPct == const $CopyWithPlaceholder()
          ? _value.popPct
          // ignore: cast_nullable_to_non_nullable
          : popPct as int?,
      precipMm: precipMm == const $CopyWithPlaceholder()
          ? _value.precipMm
          // ignore: cast_nullable_to_non_nullable
          : precipMm as num?,
      skyCoverPct: skyCoverPct == const $CopyWithPlaceholder()
          ? _value.skyCoverPct
          // ignore: cast_nullable_to_non_nullable
          : skyCoverPct as int?,
      smokeUgM3: smokeUgM3 == const $CopyWithPlaceholder()
          ? _value.smokeUgM3
          // ignore: cast_nullable_to_non_nullable
          : smokeUgM3 as num?,
    );
  }
}

extension $ForecastSeriesPointCopyWith on ForecastSeriesPoint {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastSeriesPoint.copyWith(...)` or `instanceOfForecastSeriesPoint.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastSeriesPointCWProxy get copyWith =>
      _$ForecastSeriesPointCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastSeriesPoint _$ForecastSeriesPointFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastSeriesPoint', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['time']);
  final val = ForecastSeriesPoint(
    time: $checkedConvert('time', (v) => DateTime.parse(v as String)),
    tempC: $checkedConvert('tempC', (v) => v as num?),
    windSpeedMs: $checkedConvert('windSpeedMs', (v) => v as num?),
    windFromDeg: $checkedConvert('windFromDeg', (v) => (v as num?)?.toInt()),
    gustMs: $checkedConvert('gustMs', (v) => v as num?),
    popPct: $checkedConvert('popPct', (v) => (v as num?)?.toInt()),
    precipMm: $checkedConvert('precipMm', (v) => v as num?),
    skyCoverPct: $checkedConvert('skyCoverPct', (v) => (v as num?)?.toInt()),
    smokeUgM3: $checkedConvert('smokeUgM3', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$ForecastSeriesPointToJson(
  ForecastSeriesPoint instance,
) => <String, dynamic>{
  'time': instance.time.toIso8601String(),
  'tempC': ?instance.tempC,
  'windSpeedMs': ?instance.windSpeedMs,
  'windFromDeg': ?instance.windFromDeg,
  'gustMs': ?instance.gustMs,
  'popPct': ?instance.popPct,
  'precipMm': ?instance.precipMm,
  'skyCoverPct': ?instance.skyCoverPct,
  'smokeUgM3': ?instance.smokeUgM3,
};
