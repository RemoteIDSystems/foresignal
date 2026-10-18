// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'weather_context_smoke.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WeatherContextSmokeCWProxy {
  WeatherContextSmoke status(WeatherSectionStatus status);

  WeatherContextSmoke asOf(DateTime? asOf);

  WeatherContextSmoke statusText(String? statusText);

  WeatherContextSmoke pm25UgM3(num? pm25UgM3);

  WeatherContextSmoke run(String? run);

  WeatherContextSmoke validTime(DateTime? validTime);

  WeatherContextSmoke step(int? step);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextSmoke(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextSmoke(...).copyWith(id: 12, name: "My name")
  /// ```
  WeatherContextSmoke call({
    WeatherSectionStatus status,
    DateTime? asOf,
    String? statusText,
    num? pm25UgM3,
    String? run,
    DateTime? validTime,
    int? step,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWeatherContextSmoke.copyWith(...)` or call `instanceOfWeatherContextSmoke.copyWith.fieldName(value)` for a single field.
class _$WeatherContextSmokeCWProxyImpl implements _$WeatherContextSmokeCWProxy {
  const _$WeatherContextSmokeCWProxyImpl(this._value);

  final WeatherContextSmoke _value;

  @override
  WeatherContextSmoke status(WeatherSectionStatus status) =>
      call(status: status);

  @override
  WeatherContextSmoke asOf(DateTime? asOf) => call(asOf: asOf);

  @override
  WeatherContextSmoke statusText(String? statusText) =>
      call(statusText: statusText);

  @override
  WeatherContextSmoke pm25UgM3(num? pm25UgM3) => call(pm25UgM3: pm25UgM3);

  @override
  WeatherContextSmoke run(String? run) => call(run: run);

  @override
  WeatherContextSmoke validTime(DateTime? validTime) =>
      call(validTime: validTime);

  @override
  WeatherContextSmoke step(int? step) => call(step: step);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContextSmoke(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContextSmoke(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WeatherContextSmoke call({
    Object? status = const $CopyWithPlaceholder(),
    Object? asOf = const $CopyWithPlaceholder(),
    Object? statusText = const $CopyWithPlaceholder(),
    Object? pm25UgM3 = const $CopyWithPlaceholder(),
    Object? run = const $CopyWithPlaceholder(),
    Object? validTime = const $CopyWithPlaceholder(),
    Object? step = const $CopyWithPlaceholder(),
  }) {
    return WeatherContextSmoke(
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
      pm25UgM3: pm25UgM3 == const $CopyWithPlaceholder()
          ? _value.pm25UgM3
          // ignore: cast_nullable_to_non_nullable
          : pm25UgM3 as num?,
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

extension $WeatherContextSmokeCopyWith on WeatherContextSmoke {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWeatherContextSmoke.copyWith(...)` or `instanceOfWeatherContextSmoke.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WeatherContextSmokeCWProxy get copyWith =>
      _$WeatherContextSmokeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WeatherContextSmoke _$WeatherContextSmokeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WeatherContextSmoke', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = WeatherContextSmoke(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$WeatherSectionStatusEnumMap, v),
        ),
        asOf: $checkedConvert(
          'asOf',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        statusText: $checkedConvert('statusText', (v) => v as String?),
        pm25UgM3: $checkedConvert('pm25UgM3', (v) => v as num?),
        run: $checkedConvert('run', (v) => v as String?),
        validTime: $checkedConvert(
          'validTime',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        step: $checkedConvert('step', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$WeatherContextSmokeToJson(
  WeatherContextSmoke instance,
) => <String, dynamic>{
  'status': _$WeatherSectionStatusEnumMap[instance.status]!,
  'asOf': ?instance.asOf?.toIso8601String(),
  'statusText': ?instance.statusText,
  'pm25UgM3': ?instance.pm25UgM3,
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
