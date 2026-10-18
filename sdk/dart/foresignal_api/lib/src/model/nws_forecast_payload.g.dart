// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'nws_forecast_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NwsForecastPayloadCWProxy {
  NwsForecastPayload place(String place);

  NwsForecastPayload wfo(String wfo);

  NwsForecastPayload gridX(int gridX);

  NwsForecastPayload gridY(int gridY);

  NwsForecastPayload popMax24h(int? popMax24h);

  NwsForecastPayload qpf24hMm(num? qpf24hMm);

  NwsForecastPayload tMaxC24h(num? tMaxC24h);

  NwsForecastPayload tMinC24h(num? tMinC24h);

  NwsForecastPayload gustMaxMs24h(num? gustMaxMs24h);

  NwsForecastPayload hazards(List<NwsForecastPayloadHazardsInner>? hazards);

  NwsForecastPayload forecastUpdated(DateTime forecastUpdated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NwsForecastPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NwsForecastPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  NwsForecastPayload call({
    String place,
    String wfo,
    int gridX,
    int gridY,
    int? popMax24h,
    num? qpf24hMm,
    num? tMaxC24h,
    num? tMinC24h,
    num? gustMaxMs24h,
    List<NwsForecastPayloadHazardsInner>? hazards,
    DateTime forecastUpdated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNwsForecastPayload.copyWith(...)` or call `instanceOfNwsForecastPayload.copyWith.fieldName(value)` for a single field.
class _$NwsForecastPayloadCWProxyImpl implements _$NwsForecastPayloadCWProxy {
  const _$NwsForecastPayloadCWProxyImpl(this._value);

  final NwsForecastPayload _value;

  @override
  NwsForecastPayload place(String place) => call(place: place);

  @override
  NwsForecastPayload wfo(String wfo) => call(wfo: wfo);

  @override
  NwsForecastPayload gridX(int gridX) => call(gridX: gridX);

  @override
  NwsForecastPayload gridY(int gridY) => call(gridY: gridY);

  @override
  NwsForecastPayload popMax24h(int? popMax24h) => call(popMax24h: popMax24h);

  @override
  NwsForecastPayload qpf24hMm(num? qpf24hMm) => call(qpf24hMm: qpf24hMm);

  @override
  NwsForecastPayload tMaxC24h(num? tMaxC24h) => call(tMaxC24h: tMaxC24h);

  @override
  NwsForecastPayload tMinC24h(num? tMinC24h) => call(tMinC24h: tMinC24h);

  @override
  NwsForecastPayload gustMaxMs24h(num? gustMaxMs24h) =>
      call(gustMaxMs24h: gustMaxMs24h);

  @override
  NwsForecastPayload hazards(List<NwsForecastPayloadHazardsInner>? hazards) =>
      call(hazards: hazards);

  @override
  NwsForecastPayload forecastUpdated(DateTime forecastUpdated) =>
      call(forecastUpdated: forecastUpdated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NwsForecastPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NwsForecastPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NwsForecastPayload call({
    Object? place = const $CopyWithPlaceholder(),
    Object? wfo = const $CopyWithPlaceholder(),
    Object? gridX = const $CopyWithPlaceholder(),
    Object? gridY = const $CopyWithPlaceholder(),
    Object? popMax24h = const $CopyWithPlaceholder(),
    Object? qpf24hMm = const $CopyWithPlaceholder(),
    Object? tMaxC24h = const $CopyWithPlaceholder(),
    Object? tMinC24h = const $CopyWithPlaceholder(),
    Object? gustMaxMs24h = const $CopyWithPlaceholder(),
    Object? hazards = const $CopyWithPlaceholder(),
    Object? forecastUpdated = const $CopyWithPlaceholder(),
  }) {
    return NwsForecastPayload(
      place: place == const $CopyWithPlaceholder() || place == null
          ? _value.place
          // ignore: cast_nullable_to_non_nullable
          : place as String,
      wfo: wfo == const $CopyWithPlaceholder() || wfo == null
          ? _value.wfo
          // ignore: cast_nullable_to_non_nullable
          : wfo as String,
      gridX: gridX == const $CopyWithPlaceholder() || gridX == null
          ? _value.gridX
          // ignore: cast_nullable_to_non_nullable
          : gridX as int,
      gridY: gridY == const $CopyWithPlaceholder() || gridY == null
          ? _value.gridY
          // ignore: cast_nullable_to_non_nullable
          : gridY as int,
      popMax24h: popMax24h == const $CopyWithPlaceholder()
          ? _value.popMax24h
          // ignore: cast_nullable_to_non_nullable
          : popMax24h as int?,
      qpf24hMm: qpf24hMm == const $CopyWithPlaceholder()
          ? _value.qpf24hMm
          // ignore: cast_nullable_to_non_nullable
          : qpf24hMm as num?,
      tMaxC24h: tMaxC24h == const $CopyWithPlaceholder()
          ? _value.tMaxC24h
          // ignore: cast_nullable_to_non_nullable
          : tMaxC24h as num?,
      tMinC24h: tMinC24h == const $CopyWithPlaceholder()
          ? _value.tMinC24h
          // ignore: cast_nullable_to_non_nullable
          : tMinC24h as num?,
      gustMaxMs24h: gustMaxMs24h == const $CopyWithPlaceholder()
          ? _value.gustMaxMs24h
          // ignore: cast_nullable_to_non_nullable
          : gustMaxMs24h as num?,
      hazards: hazards == const $CopyWithPlaceholder()
          ? _value.hazards
          // ignore: cast_nullable_to_non_nullable
          : hazards as List<NwsForecastPayloadHazardsInner>?,
      forecastUpdated:
          forecastUpdated == const $CopyWithPlaceholder() ||
              forecastUpdated == null
          ? _value.forecastUpdated
          // ignore: cast_nullable_to_non_nullable
          : forecastUpdated as DateTime,
    );
  }
}

extension $NwsForecastPayloadCopyWith on NwsForecastPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNwsForecastPayload.copyWith(...)` or `instanceOfNwsForecastPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NwsForecastPayloadCWProxy get copyWith =>
      _$NwsForecastPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NwsForecastPayload _$NwsForecastPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NwsForecastPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['place', 'wfo', 'gridX', 'gridY', 'forecastUpdated'],
  );
  final val = NwsForecastPayload(
    place: $checkedConvert('place', (v) => v as String),
    wfo: $checkedConvert('wfo', (v) => v as String),
    gridX: $checkedConvert('gridX', (v) => (v as num).toInt()),
    gridY: $checkedConvert('gridY', (v) => (v as num).toInt()),
    popMax24h: $checkedConvert('popMax24h', (v) => (v as num?)?.toInt()),
    qpf24hMm: $checkedConvert('qpf24hMm', (v) => v as num?),
    tMaxC24h: $checkedConvert('tMaxC24h', (v) => v as num?),
    tMinC24h: $checkedConvert('tMinC24h', (v) => v as num?),
    gustMaxMs24h: $checkedConvert('gustMaxMs24h', (v) => v as num?),
    hazards: $checkedConvert(
      'hazards',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => NwsForecastPayloadHazardsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    forecastUpdated: $checkedConvert(
      'forecastUpdated',
      (v) => DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$NwsForecastPayloadToJson(NwsForecastPayload instance) =>
    <String, dynamic>{
      'place': instance.place,
      'wfo': instance.wfo,
      'gridX': instance.gridX,
      'gridY': instance.gridY,
      'popMax24h': ?instance.popMax24h,
      'qpf24hMm': ?instance.qpf24hMm,
      'tMaxC24h': ?instance.tMaxC24h,
      'tMinC24h': ?instance.tMinC24h,
      'gustMaxMs24h': ?instance.gustMaxMs24h,
      'hazards': ?instance.hazards?.map((e) => e.toJson()).toList(),
      'forecastUpdated': instance.forecastUpdated.toIso8601String(),
    };
