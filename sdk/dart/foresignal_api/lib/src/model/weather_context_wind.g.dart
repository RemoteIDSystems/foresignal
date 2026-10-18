// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'weather_context_wind.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WeatherContextWindCWProxy {
  WeatherContextWind status(WeatherSectionStatus status);

  WeatherContextWind asOf(DateTime? asOf);

  WeatherContextWind statusText(String? statusText);

  WeatherContextWind productId(String? productId);

  WeatherContextWind speedMs(num? speedMs);

  WeatherContextWind gustMs(num? gustMs);

  WeatherContextWind fromDeg(int? fromDeg);

  WeatherContextWind run(String? run);

  WeatherContextWind validTime(DateTime? validTime);

  WeatherContextWind step(int? step);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextWind(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextWind(...).copyWith(id: 12, name: "My name")
  /// ```
  WeatherContextWind call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    String? productId,
    num? speedMs,
    num? gustMs,
    int? fromDeg,
    String? run,
    DateTime? validTime,
    int? step,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWeatherContextWind.copyWith(...)` or call `instanceOfWeatherContextWind.copyWith.fieldName(value)` for a single field.
class _$WeatherContextWindCWProxyImpl implements _$WeatherContextWindCWProxy {
  const _$WeatherContextWindCWProxyImpl(this._value);

  final WeatherContextWind _value;

  @override
  WeatherContextWind status(WeatherSectionStatus status) =>
      call(status: status);

  @override
  WeatherContextWind asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  WeatherContextWind statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  WeatherContextWind productId(String? productId) => call(productId: productId);

  @override
  WeatherContextWind speedMs(num? speedMs) => call(speedMs: speedMs);

  @override
  WeatherContextWind gustMs(num? gustMs) => call(gustMs: gustMs);

  @override
  WeatherContextWind fromDeg(int? fromDeg) => call(fromDeg: fromDeg);

  @override
  WeatherContextWind run(String? run) => call(run: run);

  @override
  WeatherContextWind validTime(DateTime? validTime) =>
      call(validTime: validTime);

  @override
  WeatherContextWind step(int? step) => call(step: step);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextWind(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextWind(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WeatherContextWind call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? productId = const $CopyWithPlaceholder(),
    Object? speedMs = const $CopyWithPlaceholder(),
    Object? gustMs = const $CopyWithPlaceholder(),
    Object? fromDeg = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? validTime = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
  }) {
    return WeatherContextWind(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as WeatherSectionStatus,
      asOf: asOf == const $CopyWithPlaceholder()
          ? _value.asOf
          // ignore: cast_nullable_to_non_nullable
          : asOf as DateTime?,
      statusText: statusText == const $CopyWithPlaceholder()
          ? _value.statusText
          // ignore: cast_nullable_to_non_nullable
          : statusText as String?,
      productId: productId == const $CopyWithPlaceholder()
          ? _value.productId
          // ignore: cast_nullable_to_non_nullable
          : productId as String?,
      speedMs: speedMs == const $CopyWithPlaceholder()
          ? _value.speedMs
          // ignore: cast_nullable_to_non_nullable
          : speedMs as num?,
      gustMs: gustMs == const $CopyWithPlaceholder()
          ? _value.gustMs
          // ignore: cast_nullable_to_non_nullable
          : gustMs as num?,
      fromDeg: fromDeg == const $CopyWithPlaceholder()
          ? _value.fromDeg
          // ignore: cast_nullable_to_non_nullable
          : fromDeg as int?,
      run: run == const $CopyWithPlaceholder()
          ? _value.run
          // ignore: cast_nullable_to_non_nullable
          : run as String?,
      validTime: validTime == const $CopyWithPlaceholder()
          ? _value.validTime
          // ignore: cast_nullable_to_non_nullable
          : validTime as DateTime?,
      step: step == const $CopyWithPlaceholder()
          ? _value.step
          // ignore: cast_nullable_to_non_nullable
          : step as int?,
    );
  }
}

extension $WeatherContextWindCopyWith on WeatherContextWind {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWeatherContextWind.copyWith(...)` or `instanceOfWeatherContextWind.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WeatherContextWindCWProxy get copyWith =>
      _$WeatherContextWindCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WeatherContextWind _$WeatherContextWindFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WeatherContextWind', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = WeatherContextWind(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$WeatherSectionStatusEnumMap, v),
        ),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        statusText: $checkedConvert('statusText', (v) => v as String?),
        productId: $checkedConvert('productId', (v) => v as String?),
        speedMs: $checkedConvert('speedMs', (v) => v as num?),
        gustMs: $checkedConvert('gustMs', (v) => v as num?),
        fromDeg: $checkedConvert('fromDeg', (v) => (v as num?)?.toInt()),
        run: $checkedConvert('run', (v) => v as String?),
        validTime: $checkedConvert(
          'validTime',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        step: $checkedConvert('step', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$WeatherContextWindToJson(WeatherContextWind instance) =>
    <String, dynamic>{
      'status': _$WeatherSectionStatusEnumMap[instance.status]!,
      'asOf': ?instance.asOf?.toIso8601String(),
      'statusText': ?instance.statusText,
      'productId': ?instance.productId,
      'speedMs': ?instance.speedMs,
      'gustMs': ?instance.gustMs,
      'fromDeg': ?instance.fromDeg,
      'run': ?instance.run,
      'validTime': ?instance.validTime?.toIso8601String(),
      'step': ?instance.step,
    };

const _$WeatherSectionStatusEnumMap = {
  WeatherSectionStatus.ok: 'ok',
  WeatherSectionStatus.stale: 'stale',
  WeatherSectionStatus.noCoverage: 'no-coverage',
  WeatherSectionStatus.unavailable: 'unavailable',
};
