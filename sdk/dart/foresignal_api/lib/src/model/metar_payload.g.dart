// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'metar_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MetarPayloadCWProxy {
  MetarPayload raw(String raw);

  MetarPayload stationId(String stationId);

  MetarPayload metarType(MetarPayloadMetarTypeEnum? metarType);

  MetarPayload tempC(num? tempC);

  MetarPayload dewpointC(num? dewpointC);

  MetarPayload windDirDeg(int? windDirDeg);

  MetarPayload windVariable(bool? windVariable);

  MetarPayload windKt(int? windKt);

  MetarPayload gustKt(int? gustKt);

  MetarPayload visibilitySm(num? visibilitySm);

  MetarPayload visibilityPlus(bool? visibilityPlus);

  MetarPayload altimInHg(num? altimInHg);

  MetarPayload slpMb(num? slpMb);

  MetarPayload wx(String? wx);

  MetarPayload sky(List<MetarPayloadSkyInner>? sky);

  MetarPayload flightCategory(MetarPayloadFlightCategoryEnum? flightCategory);

  MetarPayload elevationM(num? elevationM);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MetarPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MetarPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  MetarPayload call({
    String raw,
    String stationId,
    MetarPayloadMetarTypeEnum? metarType,
    num? tempC,
    num? dewpointC,
    int? windDirDeg,
    bool? windVariable,
    int? windKt,
    int? gustKt,
    num? visibilitySm,
    bool? visibilityPlus,
    num? altimInHg,
    num? slpMb,
    String? wx,
    List<MetarPayloadSkyInner>? sky,
    MetarPayloadFlightCategoryEnum? flightCategory,
    num? elevationM,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMetarPayload.copyWith(...)` or call `instanceOfMetarPayload.copyWith.fieldName(value)` for a single field.
class _$MetarPayloadCWProxyImpl implements _$MetarPayloadCWProxy {
  const _$MetarPayloadCWProxyImpl(this._value);

  final MetarPayload _value;

  @override
  MetarPayload raw(String raw) => call(raw: raw);

  @override
  MetarPayload stationId(String stationId) => call(stationId: stationId);

  @override
  MetarPayload metarType(MetarPayloadMetarTypeEnum? metarType) =>
      call(metarType: metarType);

  @override
  MetarPayload tempC(num? tempC) => call(tempC: tempC);

  @override
  MetarPayload dewpointC(num? dewpointC) => call(dewpointC: dewpointC);

  @override
  MetarPayload windDirDeg(int? windDirDeg) => call(windDirDeg: windDirDeg);

  @override
  MetarPayload windVariable(bool? windVariable) =>
      call(windVariable: windVariable);

  @override
  MetarPayload windKt(int? windKt) => call(windKt: windKt);

  @override
  MetarPayload gustKt(int? gustKt) => call(gustKt: gustKt);

  @override
  MetarPayload visibilitySm(num? visibilitySm) =>
      call(visibilitySm: visibilitySm);

  @override
  MetarPayload visibilityPlus(bool? visibilityPlus) =>
      call(visibilityPlus: visibilityPlus);

  @override
  MetarPayload altimInHg(num? altimInHg) => call(altimInHg: altimInHg);

  @override
  MetarPayload slpMb(num? slpMb) => call(slpMb: slpMb);

  @override
  MetarPayload wx(String? wx) => call(wx: wx);

  @override
  MetarPayload sky(List<MetarPayloadSkyInner>? sky) => call(sky: sky);

  @override
  MetarPayload flightCategory(MetarPayloadFlightCategoryEnum? flightCategory) =>
      call(flightCategory: flightCategory);

  @override
  MetarPayload elevationM(num? elevationM) => call(elevationM: elevationM);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MetarPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MetarPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MetarPayload call({
    Object? raw = const $CopyWithPlaceholder(),
    Object? stationId = const $CopyWithPlaceholder(),
    Object? metarType = const $CopyWithPlaceholder(),
    Object? tempC = const $CopyWithPlaceholder(),
    Object? dewpointC = const $CopyWithPlaceholder(),
    Object? windDirDeg = const $CopyWithPlaceholder(),
    Object? windVariable = const $CopyWithPlaceholder(),
    Object? windKt = const $CopyWithPlaceholder(),
    Object? gustKt = const $CopyWithPlaceholder(),
    Object? visibilitySm = const $CopyWithPlaceholder(),
    Object? visibilityPlus = const $CopyWithPlaceholder(),
    Object? altimInHg = const $CopyWithPlaceholder(),
    Object? slpMb = const $CopyWithPlaceholder(),
    Object? wx = const $CopyWithPlaceholder(),
    Object? sky = const $CopyWithPlaceholder(),
    Object? flightCategory = const $CopyWithPlaceholder(),
    Object? elevationM = const $CopyWithPlaceholder(),
  }) {
    return MetarPayload(
      raw: raw == const $CopyWithPlaceholder() || raw == null
          ? _value.raw
          // ignore: cast_nullable_to_non_nullable
          : raw as String,
      stationId: stationId == const $CopyWithPlaceholder() || stationId == null
          ? _value.stationId
          // ignore: cast_nullable_to_non_nullable
          : stationId as String,
      metarType: metarType == const $CopyWithPlaceholder()
          ? _value.metarType
          // ignore: cast_nullable_to_non_nullable
          : metarType as MetarPayloadMetarTypeEnum?,
      tempC: tempC == const $CopyWithPlaceholder()
          ? _value.tempC
          // ignore: cast_nullable_to_non_nullable
          : tempC as num?,
      dewpointC: dewpointC == const $CopyWithPlaceholder()
          ? _value.dewpointC
          // ignore: cast_nullable_to_non_nullable
          : dewpointC as num?,
      windDirDeg: windDirDeg == const $CopyWithPlaceholder()
          ? _value.windDirDeg
          // ignore: cast_nullable_to_non_nullable
          : windDirDeg as int?,
      windVariable: windVariable == const $CopyWithPlaceholder()
          ? _value.windVariable
          // ignore: cast_nullable_to_non_nullable
          : windVariable as bool?,
      windKt: windKt == const $CopyWithPlaceholder()
          ? _value.windKt
          // ignore: cast_nullable_to_non_nullable
          : windKt as int?,
      gustKt: gustKt == const $CopyWithPlaceholder()
          ? _value.gustKt
          // ignore: cast_nullable_to_non_nullable
          : gustKt as int?,
      visibilitySm: visibilitySm == const $CopyWithPlaceholder()
          ? _value.visibilitySm
          // ignore: cast_nullable_to_non_nullable
          : visibilitySm as num?,
      visibilityPlus: visibilityPlus == const $CopyWithPlaceholder()
          ? _value.visibilityPlus
          // ignore: cast_nullable_to_non_nullable
          : visibilityPlus as bool?,
      altimInHg: altimInHg == const $CopyWithPlaceholder()
          ? _value.altimInHg
          // ignore: cast_nullable_to_non_nullable
          : altimInHg as num?,
      slpMb: slpMb == const $CopyWithPlaceholder()
          ? _value.slpMb
          // ignore: cast_nullable_to_non_nullable
          : slpMb as num?,
      wx: wx == const $CopyWithPlaceholder()
          ? _value.wx
          // ignore: cast_nullable_to_non_nullable
          : wx as String?,
      sky: sky == const $CopyWithPlaceholder()
          ? _value.sky
          // ignore: cast_nullable_to_non_nullable
          : sky as List<MetarPayloadSkyInner>?,
      flightCategory: flightCategory == const $CopyWithPlaceholder()
          ? _value.flightCategory
          // ignore: cast_nullable_to_non_nullable
          : flightCategory as MetarPayloadFlightCategoryEnum?,
      elevationM: elevationM == const $CopyWithPlaceholder()
          ? _value.elevationM
          // ignore: cast_nullable_to_non_nullable
          : elevationM as num?,
    );
  }
}

extension $MetarPayloadCopyWith on MetarPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMetarPayload.copyWith(...)` or `instanceOfMetarPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MetarPayloadCWProxy get copyWith => _$MetarPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MetarPayload _$MetarPayloadFromJson(Map<String, dynamic> json) =>
    $checkedCreate('MetarPayload', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['raw', 'stationId']);
      final val = MetarPayload(
        raw: $checkedConvert('raw', (v) => v as String),
        stationId: $checkedConvert('stationId', (v) => v as String),
        metarType: $checkedConvert(
          'metarType',
          (v) => $enumDecodeNullable(_$MetarPayloadMetarTypeEnumEnumMap, v),
        ),
        tempC: $checkedConvert('tempC', (v) => v as num?),
        dewpointC: $checkedConvert('dewpointC', (v) => v as num?),
        windDirDeg: $checkedConvert('windDirDeg', (v) => (v as num?)?.toInt()),
        windVariable: $checkedConvert('windVariable', (v) => v as bool?),
        windKt: $checkedConvert('windKt', (v) => (v as num?)?.toInt()),
        gustKt: $checkedConvert('gustKt', (v) => (v as num?)?.toInt()),
        visibilitySm: $checkedConvert('visibilitySm', (v) => v as num?),
        visibilityPlus: $checkedConvert('visibilityPlus', (v) => v as bool?),
        altimInHg: $checkedConvert('altimInHg', (v) => v as num?),
        slpMb: $checkedConvert('slpMb', (v) => v as num?),
        wx: $checkedConvert('wx', (v) => v as String?),
        sky: $checkedConvert(
          'sky',
          (v) => (v as List<dynamic>?)
              ?.map(
                (e) => MetarPayloadSkyInner.fromJson(e as Map<String, dynamic>),
              )
              .toList(),
        ),
        flightCategory: $checkedConvert(
          'flightCategory',
          (v) =>
              $enumDecodeNullable(_$MetarPayloadFlightCategoryEnumEnumMap, v),
        ),
        elevationM: $checkedConvert('elevationM', (v) => v as num?),
      );
      return val;
    });

Map<String, dynamic> _$MetarPayloadToJson(MetarPayload instance) =>
    <String, dynamic>{
      'raw': instance.raw,
      'stationId': instance.stationId,
      'metarType': ?_$MetarPayloadMetarTypeEnumEnumMap[instance.metarType],
      'tempC': ?instance.tempC,
      'dewpointC': ?instance.dewpointC,
      'windDirDeg': ?instance.windDirDeg,
      'windVariable': ?instance.windVariable,
      'windKt': ?instance.windKt,
      'gustKt': ?instance.gustKt,
      'visibilitySm': ?instance.visibilitySm,
      'visibilityPlus': ?instance.visibilityPlus,
      'altimInHg': ?instance.altimInHg,
      'slpMb': ?instance.slpMb,
      'wx': ?instance.wx,
      'sky': ?instance.sky?.map((e) => e.toJson()).toList(),
      'flightCategory':
          ?_$MetarPayloadFlightCategoryEnumEnumMap[instance.flightCategory],
      'elevationM': ?instance.elevationM,
    };

const _$MetarPayloadMetarTypeEnumEnumMap = {
  MetarPayloadMetarTypeEnum.METAR: 'METAR',
  MetarPayloadMetarTypeEnum.SPECI: 'SPECI',
};

const _$MetarPayloadFlightCategoryEnumEnumMap = {
  MetarPayloadFlightCategoryEnum.VFR: 'VFR',
  MetarPayloadFlightCategoryEnum.MVFR: 'MVFR',
  MetarPayloadFlightCategoryEnum.IFR: 'IFR',
  MetarPayloadFlightCategoryEnum.LIFR: 'LIFR',
};
