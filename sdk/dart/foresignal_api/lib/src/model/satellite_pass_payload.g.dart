// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'satellite_pass_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SatellitePassPayloadCWProxy {
  SatellitePassPayload status(SatellitePassPayloadStatusEnum status);

  SatellitePassPayload satellite(String satellite);

  SatellitePassPayload satKey(String satKey);

  SatellitePassPayload norad(int? norad);

  SatellitePassPayload pipelineId(String? pipelineId);

  SatellitePassPayload frequencyHz(int? frequencyHz);

  SatellitePassPayload aos(DateTime aos);

  SatellitePassPayload los(DateTime? los);

  SatellitePassPayload maxElevationDeg(num? maxElevationDeg);

  SatellitePassPayload rf(SatellitePassPayloadRf? rf);

  SatellitePassPayload instruments(List<String>? instruments);

  SatellitePassPayload products(
    List<SatellitePassPayloadProductsInner>? products,
  );

  SatellitePassPayload tle(String? tle);

  SatellitePassPayload satdumpVersion(String? satdumpVersion);

  SatellitePassPayload title(String title);

  SatellitePassPayload attribution(Attribution? attribution);

  SatellitePassPayload geometrySource(
    SatellitePassPayloadGeometrySourceEnum? geometrySource,
  );

  SatellitePassPayload tca(DateTime? tca);

  SatellitePassPayload aosAzimuthDeg(num? aosAzimuthDeg);

  SatellitePassPayload losAzimuthDeg(num? losAzimuthDeg);

  SatellitePassPayload place(String? place);

  SatellitePassPayload imminent(bool? imminent);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SatellitePassPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SatellitePassPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  SatellitePassPayload call({
    SatellitePassPayloadStatusEnum status,
    String satellite,
    String satKey,
    int? norad,
    String? pipelineId,
    int? frequencyHz,
    DateTime aos,
    DateTime? los,
    num? maxElevationDeg,
    SatellitePassPayloadRf? rf,
    List<String>? instruments,
    List<SatellitePassPayloadProductsInner>? products,
    String? tle,
    String? satdumpVersion,
    String title,
    Attribution? attribution,
    SatellitePassPayloadGeometrySourceEnum? geometrySource,
    DateTime? tca,
    num? aosAzimuthDeg,
    num? losAzimuthDeg,
    String? place,
    bool? imminent,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSatellitePassPayload.copyWith(...)` or call `instanceOfSatellitePassPayload.copyWith.fieldName(value)` for a single field.
class _$SatellitePassPayloadCWProxyImpl
    implements _$SatellitePassPayloadCWProxy {
  const _$SatellitePassPayloadCWProxyImpl(this._value);

  final SatellitePassPayload _value;

  @override
  SatellitePassPayload status(SatellitePassPayloadStatusEnum status) =>
      call(status: status);

  @override
  SatellitePassPayload satellite(String satellite) =>
      call(satellite: satellite);

  @override
  SatellitePassPayload satKey(String satKey) => call(satKey: satKey);

  @override
  SatellitePassPayload norad(int? norad) => call(norad: norad);

  @override
  SatellitePassPayload pipelineId(String? pipelineId) =>
      call(pipelineId: pipelineId);

  @override
  SatellitePassPayload frequencyHz(int? frequencyHz) =>
      call(frequencyHz: frequencyHz);

  @override
  SatellitePassPayload aos(DateTime aos) => call(aos: aos);

  @override
  SatellitePassPayload los(DateTime? los) => call(los: los);

  @override
  SatellitePassPayload maxElevationDeg(num? maxElevationDeg) =>
      call(maxElevationDeg: maxElevationDeg);

  @override
  SatellitePassPayload rf(SatellitePassPayloadRf? rf) => call(rf: rf);

  @override
  SatellitePassPayload instruments(List<String>? instruments) =>
      call(instruments: instruments);

  @override
  SatellitePassPayload products(
    List<SatellitePassPayloadProductsInner>? products,
  ) => call(products: products);

  @override
  SatellitePassPayload tle(String? tle) => call(tle: tle);

  @override
  SatellitePassPayload satdumpVersion(String? satdumpVersion) =>
      call(satdumpVersion: satdumpVersion);

  @override
  SatellitePassPayload title(String title) => call(title: title);

  @override
  SatellitePassPayload attribution(Attribution? attribution) =>
      call(attribution: attribution);

  @override
  SatellitePassPayload geometrySource(
    SatellitePassPayloadGeometrySourceEnum? geometrySource,
  ) => call(geometrySource: geometrySource);

  @override
  SatellitePassPayload tca(DateTime? tca) => call(tca: tca);

  @override
  SatellitePassPayload aosAzimuthDeg(num? aosAzimuthDeg) =>
      call(aosAzimuthDeg: aosAzimuthDeg);

  @override
  SatellitePassPayload losAzimuthDeg(num? losAzimuthDeg) =>
      call(losAzimuthDeg: losAzimuthDeg);

  @override
  SatellitePassPayload place(String? place) => call(place: place);

  @override
  SatellitePassPayload imminent(bool? imminent) => call(imminent: imminent);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SatellitePassPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SatellitePassPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SatellitePassPayload call({
    Object? status = const $CopyWithPlaceholder(),
    Object? satellite = const $CopyWithPlaceholder(),
    Object? satKey = const $CopyWithPlaceholder(),
    Object? norad = const $CopyWithPlaceholder(),
    Object? pipelineId = const $CopyWithPlaceholder(),
    Object? frequencyHz = const $CopyWithPlaceholder(),
    Object? aos = const $CopyWithPlaceholder(),
    Object? los = const $CopyWithPlaceholder(),
    Object? maxElevationDeg = const $CopyWithPlaceholder(),
    Object? rf = const $CopyWithPlaceholder(),
    Object? instruments = const $CopyWithPlaceholder(),
    Object? products = const $CopyWithPlaceholder(),
    Object? tle = const $CopyWithPlaceholder(),
    Object? satdumpVersion = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? geometrySource = const $CopyWithPlaceholder(),
    Object? tca = const $CopyWithPlaceholder(),
    Object? aosAzimuthDeg = const $CopyWithPlaceholder(),
    Object? losAzimuthDeg = const $CopyWithPlaceholder(),
    Object? place = const $CopyWithPlaceholder(),
    Object? imminent = const $CopyWithPlaceholder(),
  }) {
    return SatellitePassPayload(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as SatellitePassPayloadStatusEnum,
      satellite: satellite == const $CopyWithPlaceholder() || satellite == null
          ? _value.satellite
          // ignore: cast_nullable_to_non_nullable
          : satellite as String,
      satKey: satKey == const $CopyWithPlaceholder() || satKey == null
          ? _value.satKey
          // ignore: cast_nullable_to_non_nullable
          : satKey as String,
      norad: norad == const $CopyWithPlaceholder()
          ? _value.norad
          // ignore: cast_nullable_to_non_nullable
          : norad as int?,
      pipelineId: pipelineId == const $CopyWithPlaceholder()
          ? _value.pipelineId
          // ignore: cast_nullable_to_non_nullable
          : pipelineId as String?,
      frequencyHz: frequencyHz == const $CopyWithPlaceholder()
          ? _value.frequencyHz
          // ignore: cast_nullable_to_non_nullable
          : frequencyHz as int?,
      aos: aos == const $CopyWithPlaceholder() || aos == null
          ? _value.aos
          // ignore: cast_nullable_to_non_nullable
          : aos as DateTime,
      los: los == const $CopyWithPlaceholder()
          ? _value.los
          // ignore: cast_nullable_to_non_nullable
          : los as DateTime?,
      maxElevationDeg: maxElevationDeg == const $CopyWithPlaceholder()
          ? _value.maxElevationDeg
          // ignore: cast_nullable_to_non_nullable
          : maxElevationDeg as num?,
      rf: rf == const $CopyWithPlaceholder()
          ? _value.rf
          // ignore: cast_nullable_to_non_nullable
          : rf as SatellitePassPayloadRf?,
      instruments: instruments == const $CopyWithPlaceholder()
          ? _value.instruments
          // ignore: cast_nullable_to_non_nullable
          : instruments as List<String>?,
      products: products == const $CopyWithPlaceholder()
          ? _value.products
          // ignore: cast_nullable_to_non_nullable
          : products as List<SatellitePassPayloadProductsInner>?,
      tle: tle == const $CopyWithPlaceholder()
          ? _value.tle
          // ignore: cast_nullable_to_non_nullable
          : tle as String?,
      satdumpVersion: satdumpVersion == const $CopyWithPlaceholder()
          ? _value.satdumpVersion
          // ignore: cast_nullable_to_non_nullable
          : satdumpVersion as String?,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as Attribution?,
      geometrySource: geometrySource == const $CopyWithPlaceholder()
          ? _value.geometrySource
          // ignore: cast_nullable_to_non_nullable
          : geometrySource as SatellitePassPayloadGeometrySourceEnum?,
      tca: tca == const $CopyWithPlaceholder()
          ? _value.tca
          // ignore: cast_nullable_to_non_nullable
          : tca as DateTime?,
      aosAzimuthDeg: aosAzimuthDeg == const $CopyWithPlaceholder()
          ? _value.aosAzimuthDeg
          // ignore: cast_nullable_to_non_nullable
          : aosAzimuthDeg as num?,
      losAzimuthDeg: losAzimuthDeg == const $CopyWithPlaceholder()
          ? _value.losAzimuthDeg
          // ignore: cast_nullable_to_non_nullable
          : losAzimuthDeg as num?,
      place: place == const $CopyWithPlaceholder()
          ? _value.place
          // ignore: cast_nullable_to_non_nullable
          : place as String?,
      imminent: imminent == const $CopyWithPlaceholder()
          ? _value.imminent
          // ignore: cast_nullable_to_non_nullable
          : imminent as bool?,
    );
  }
}

extension $SatellitePassPayloadCopyWith on SatellitePassPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSatellitePassPayload.copyWith(...)` or `instanceOfSatellitePassPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SatellitePassPayloadCWProxy get copyWith =>
      _$SatellitePassPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SatellitePassPayload _$SatellitePassPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SatellitePassPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['status', 'satellite', 'satKey', 'aos', 'title'],
  );
  final val = SatellitePassPayload(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$SatellitePassPayloadStatusEnumEnumMap, v),
    ),
    satellite: $checkedConvert('satellite', (v) => v as String),
    satKey: $checkedConvert('satKey', (v) => v as String),
    norad: $checkedConvert('norad', (v) => (v as num?)?.toInt()),
    pipelineId: $checkedConvert('pipelineId', (v) => v as String?),
    frequencyHz: $checkedConvert('frequencyHz', (v) => (v as num?)?.toInt()),
    aos: $checkedConvert('aos', (v) => DateTime.parse(v as String)),
    los: $checkedConvert(
      'los',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    maxElevationDeg: $checkedConvert('maxElevationDeg', (v) => v as num?),
    rf: $checkedConvert(
      'rf',
      (v) => v == null
          ? null
          : SatellitePassPayloadRf.fromJson(v as Map<String, dynamic>),
    ),
    instruments: $checkedConvert(
      'instruments',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    products: $checkedConvert(
      'products',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => SatellitePassPayloadProductsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    tle: $checkedConvert('tle', (v) => v as String?),
    satdumpVersion: $checkedConvert('satdumpVersion', (v) => v as String?),
    title: $checkedConvert('title', (v) => v as String),
    attribution: $checkedConvert(
      'attribution',
      (v) => v == null ? null : Attribution.fromJson(v as Map<String, dynamic>),
    ),
    geometrySource: $checkedConvert(
      'geometrySource',
      (v) => $enumDecodeNullable(
        _$SatellitePassPayloadGeometrySourceEnumEnumMap,
        v,
      ),
    ),
    tca: $checkedConvert(
      'tca',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    aosAzimuthDeg: $checkedConvert('aosAzimuthDeg', (v) => v as num?),
    losAzimuthDeg: $checkedConvert('losAzimuthDeg', (v) => v as num?),
    place: $checkedConvert('place', (v) => v as String?),
    imminent: $checkedConvert('imminent', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$SatellitePassPayloadToJson(
  SatellitePassPayload instance,
) => <String, dynamic>{
  'status': _$SatellitePassPayloadStatusEnumEnumMap[instance.status]!,
  'satellite': instance.satellite,
  'satKey': instance.satKey,
  'norad': ?instance.norad,
  'pipelineId': ?instance.pipelineId,
  'frequencyHz': ?instance.frequencyHz,
  'aos': instance.aos.toIso8601String(),
  'los': ?instance.los?.toIso8601String(),
  'maxElevationDeg': ?instance.maxElevationDeg,
  'rf': ?instance.rf?.toJson(),
  'instruments': ?instance.instruments,
  'products': ?instance.products?.map((e) => e.toJson()).toList(),
  'tle': ?instance.tle,
  'satdumpVersion': ?instance.satdumpVersion,
  'title': instance.title,
  'attribution': ?instance.attribution?.toJson(),
  'geometrySource':
      ?_$SatellitePassPayloadGeometrySourceEnumEnumMap[instance.geometrySource],
  'tca': ?instance.tca?.toIso8601String(),
  'aosAzimuthDeg': ?instance.aosAzimuthDeg,
  'losAzimuthDeg': ?instance.losAzimuthDeg,
  'place': ?instance.place,
  'imminent': ?instance.imminent,
};

const _$SatellitePassPayloadStatusEnumEnumMap = {
  SatellitePassPayloadStatusEnum.predicted: 'predicted',
  SatellitePassPayloadStatusEnum.receiving: 'receiving',
  SatellitePassPayloadStatusEnum.recorded: 'recorded',
  SatellitePassPayloadStatusEnum.decoded: 'decoded',
  SatellitePassPayloadStatusEnum.failed: 'failed',
  SatellitePassPayloadStatusEnum.missed: 'missed',
};

const _$SatellitePassPayloadGeometrySourceEnumEnumMap = {
  SatellitePassPayloadGeometrySourceEnum.footprint: 'footprint',
  SatellitePassPayloadGeometrySourceEnum.swath: 'swath',
};
