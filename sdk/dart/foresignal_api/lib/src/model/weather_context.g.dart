// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'weather_context.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$WeatherContextCWProxy {
  WeatherContext status(WeatherContextStatusEnum status);

  WeatherContext at(DateTime? at);

  WeatherContext computedAt(DateTime? computedAt);

  WeatherContext radar(WeatherContextRadar? radar);

  WeatherContext lightning(WeatherContextLightning? lightning);

  WeatherContext precip(WeatherContextPrecip? precip);

  WeatherContext warnings(WeatherContextWarnings? warnings);

  WeatherContext wind(WeatherContextWind? wind);

  WeatherContext smoke(WeatherContextSmoke? smoke);

  WeatherContext summary(String? summary);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContext(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContext(...).copyWith(id: 12, name: "My name")
  /// ```
  WeatherContext call({
    WeatherContextStatusEnum status,
    DateTime? at,
    DateTime? computedAt,
    WeatherContextRadar? radar,
    WeatherContextLightning? lightning,
    WeatherContextPrecip? precip,
    WeatherContextWarnings? warnings,
    WeatherContextWind? wind,
    WeatherContextSmoke? smoke,
    String? summary,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfWeatherContext.copyWith(...)` or call `instanceOfWeatherContext.copyWith.fieldName(value)` for a single field.
class _$WeatherContextCWProxyImpl implements _$WeatherContextCWProxy {
  const _$WeatherContextCWProxyImpl(this._value);

  final WeatherContext _value;

  @override
  WeatherContext status(WeatherContextStatusEnum status) =>
      call(status: status);

  @override
  WeatherContext at(DateTime? at) => call(at: at);

  @override
  WeatherContext computedAt(DateTime? computedAt) =>
      call(computedAt: computedAt);

  @override
  WeatherContext radar(WeatherContextRadar? radar) => call(radar: radar);

  @override
  WeatherContext lightning(WeatherContextLightning? lightning) =>
      call(lightning: lightning);

  @override
  WeatherContext precip(WeatherContextPrecip? precip) => call(precip: precip);

  @override
  WeatherContext warnings(WeatherContextWarnings? warnings) =>
      call(warnings: warnings);

  @override
  WeatherContext wind(WeatherContextWind? wind) => call(wind: wind);

  @override
  WeatherContext smoke(WeatherContextSmoke? smoke) => call(smoke: smoke);

  @override
  WeatherContext summary(String? summary) => call(summary: summary);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `WeatherContext(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// WeatherContext(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  WeatherContext call({
    Object? status = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? computedAt = const $CopyWithPlaceholder(),
    Object? radar = const $CopyWithPlaceholder(),
    Object? lightning = const $CopyWithPlaceholder(),
    Object? precip = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
    Object? wind = const $CopyWithPlaceholder(),
    Object? smoke = const $CopyWithPlaceholder(),
    Object? summary = const $CopyWithPlaceholder(),
  }) {
    return WeatherContext(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as WeatherContextStatusEnum,
      at: at == const $CopyWithPlaceholder()
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime?,
      computedAt: computedAt == const $CopyWithPlaceholder()
          ? _value.computedAt
          // ignore: cast_nullable_to_non_nullable
          : computedAt as DateTime?,
      radar: radar == const $CopyWithPlaceholder()
          ? _value.radar
          // ignore: cast_nullable_to_non_nullable
          : radar as WeatherContextRadar?,
      lightning: lightning == const $CopyWithPlaceholder()
          ? _value.lightning
          // ignore: cast_nullable_to_non_nullable
          : lightning as WeatherContextLightning?,
      precip: precip == const $CopyWithPlaceholder()
          ? _value.precip
          // ignore: cast_nullable_to_non_nullable
          : precip as WeatherContextPrecip?,
      warnings: warnings == const $CopyWithPlaceholder()
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as WeatherContextWarnings?,
      wind: wind == const $CopyWithPlaceholder()
          ? _value.wind
          // ignore: cast_nullable_to_non_nullable
          : wind as WeatherContextWind?,
      smoke: smoke == const $CopyWithPlaceholder()
          ? _value.smoke
          // ignore: cast_nullable_to_non_nullable
          : smoke as WeatherContextSmoke?,
      summary: summary == const $CopyWithPlaceholder()
          ? _value.summary
          // ignore: cast_nullable_to_non_nullable
          : summary as String?,
    );
  }
}

extension $WeatherContextCopyWith on WeatherContext {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfWeatherContext.copyWith(...)` or `instanceOfWeatherContext.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$WeatherContextCWProxy get copyWith => _$WeatherContextCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

WeatherContext _$WeatherContextFromJson(Map<String, dynamic> json) =>
    $checkedCreate('WeatherContext', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['status']);
      final val = WeatherContext(
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$WeatherContextStatusEnumEnumMap, v),
        ),
        at: $checkedConvert(
          'at',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        computedAt: $checkedConvert(
          'computedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        radar: $checkedConvert(
          'radar',
          (v) => v == null
              ? null
              : WeatherContextRadar.fromJson(v as Map<String, dynamic>),
        ),
        lightning: $checkedConvert(
          'lightning',
          (v) => v == null
              ? null
              : WeatherContextLightning.fromJson(v as Map<String, dynamic>),
        ),
        precip: $checkedConvert(
          'precip',
          (v) => v == null
              ? null
              : WeatherContextPrecip.fromJson(v as Map<String, dynamic>),
        ),
        warnings: $checkedConvert(
          'warnings',
          (v) => v == null
              ? null
              : WeatherContextWarnings.fromJson(v as Map<String, dynamic>),
        ),
        wind: $checkedConvert(
          'wind',
          (v) => v == null
              ? null
              : WeatherContextWind.fromJson(v as Map<String, dynamic>),
        ),
        smoke: $checkedConvert(
          'smoke',
          (v) => v == null
              ? null
              : WeatherContextSmoke.fromJson(v as Map<String, dynamic>),
        ),
        summary: $checkedConvert('summary', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$WeatherContextToJson(WeatherContext instance) =>
    <String, dynamic>{
      'status': _$WeatherContextStatusEnumEnumMap[instance.status]!,
      'at': ?instance.at?.toIso8601String(),
      'computedAt': ?instance.computedAt?.toIso8601String(),
      'radar': ?instance.radar?.toJson(),
      'lightning': ?instance.lightning?.toJson(),
      'precip': ?instance.precip?.toJson(),
      'warnings': ?instance.warnings?.toJson(),
      'wind': ?instance.wind?.toJson(),
      'smoke': ?instance.smoke?.toJson(),
      'summary': ?instance.summary,
    };

const _$WeatherContextStatusEnumEnumMap = {
  WeatherContextStatusEnum.ok: 'ok',
  WeatherContextStatusEnum.unavailable: 'unavailable',
};
