// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'osm_infrastructure_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OsmInfrastructurePayloadCWProxy {
  OsmInfrastructurePayload name(String? name);

  OsmInfrastructurePayload operator_(String? operator_);

  OsmInfrastructurePayload voltageKv(num? voltageKv);

  OsmInfrastructurePayload capacityMw(num? capacityMw);

  OsmInfrastructurePayload beds(int? beds);

  OsmInfrastructurePayload emergency(String? emergency);

  OsmInfrastructurePayload iata(String? iata);

  OsmInfrastructurePayload icao(String? icao);

  OsmInfrastructurePayload ref(String? ref);

  OsmInfrastructurePayload osmType(
    OsmInfrastructurePayloadOsmTypeEnum? osmType,
  );

  OsmInfrastructurePayload osmVersion(int? osmVersion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OsmInfrastructurePayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OsmInfrastructurePayload(...).copyWith(id: 12, name: "My name")
  /// ```
  OsmInfrastructurePayload call({
    String? name,
    String? operator_,
    num? voltageKv,
    num? capacityMw,
    int? beds,
    String? emergency,
    String? iata,
    String? icao,
    String? ref,
    OsmInfrastructurePayloadOsmTypeEnum? osmType,
    int? osmVersion,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOsmInfrastructurePayload.copyWith(...)` or call `instanceOfOsmInfrastructurePayload.copyWith.fieldName(value)` for a single field.
class _$OsmInfrastructurePayloadCWProxyImpl
    implements _$OsmInfrastructurePayloadCWProxy {
  const _$OsmInfrastructurePayloadCWProxyImpl(this._value);

  final OsmInfrastructurePayload _value;

  @override
  OsmInfrastructurePayload name(String? name) => call(name: name);

  @override
  OsmInfrastructurePayload operator_(String? operator_) =>
      call(operator_: operator_);

  @override
  OsmInfrastructurePayload voltageKv(num? voltageKv) =>
      call(voltageKv: voltageKv);

  @override
  OsmInfrastructurePayload capacityMw(num? capacityMw) =>
      call(capacityMw: capacityMw);

  @override
  OsmInfrastructurePayload beds(int? beds) => call(beds: beds);

  @override
  OsmInfrastructurePayload emergency(String? emergency) =>
      call(emergency: emergency);

  @override
  OsmInfrastructurePayload iata(String? iata) => call(iata: iata);

  @override
  OsmInfrastructurePayload icao(String? icao) => call(icao: icao);

  @override
  OsmInfrastructurePayload ref(String? ref) => call(ref: ref);

  @override
  OsmInfrastructurePayload osmType(
    OsmInfrastructurePayloadOsmTypeEnum? osmType,
  ) => call(osmType: osmType);

  @override
  OsmInfrastructurePayload osmVersion(int? osmVersion) =>
      call(osmVersion: osmVersion);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OsmInfrastructurePayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OsmInfrastructurePayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OsmInfrastructurePayload call({
    Object? name = const $CopyWithPlaceholder(),
    Object? operator_ = const $CopyWithPlaceholder(),
    Object? voltageKv = const $CopyWithPlaceholder(),
    Object? capacityMw = const $CopyWithPlaceholder(),
    Object? beds = const $CopyWithPlaceholder(),
    Object? emergency = const $CopyWithPlaceholder(),
    Object? iata = const $CopyWithPlaceholder(),
    Object? icao = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? osmType = const $CopyWithPlaceholder(),
    Object? osmVersion = const $CopyWithPlaceholder(),
  }) {
    return OsmInfrastructurePayload(
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      operator_: operator_ == const $CopyWithPlaceholder()
          ? _value.operator_
          // ignore: cast_nullable_to_non_nullable
          : operator_ as String?,
      voltageKv: voltageKv == const $CopyWithPlaceholder()
          ? _value.voltageKv
          // ignore: cast_nullable_to_non_nullable
          : voltageKv as num?,
      capacityMw: capacityMw == const $CopyWithPlaceholder()
          ? _value.capacityMw
          // ignore: cast_nullable_to_non_nullable
          : capacityMw as num?,
      beds: beds == const $CopyWithPlaceholder()
          ? _value.beds
          // ignore: cast_nullable_to_non_nullable
          : beds as int?,
      emergency: emergency == const $CopyWithPlaceholder()
          ? _value.emergency
          // ignore: cast_nullable_to_non_nullable
          : emergency as String?,
      iata: iata == const $CopyWithPlaceholder()
          ? _value.iata
          // ignore: cast_nullable_to_non_nullable
          : iata as String?,
      icao: icao == const $CopyWithPlaceholder()
          ? _value.icao
          // ignore: cast_nullable_to_non_nullable
          : icao as String?,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      osmType: osmType == const $CopyWithPlaceholder()
          ? _value.osmType
          // ignore: cast_nullable_to_non_nullable
          : osmType as OsmInfrastructurePayloadOsmTypeEnum?,
      osmVersion: osmVersion == const $CopyWithPlaceholder()
          ? _value.osmVersion
          // ignore: cast_nullable_to_non_nullable
          : osmVersion as int?,
    );
  }
}

extension $OsmInfrastructurePayloadCopyWith on OsmInfrastructurePayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOsmInfrastructurePayload.copyWith(...)` or `instanceOfOsmInfrastructurePayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OsmInfrastructurePayloadCWProxy get copyWith =>
      _$OsmInfrastructurePayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OsmInfrastructurePayload _$OsmInfrastructurePayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OsmInfrastructurePayload', json, ($checkedConvert) {
  final val = OsmInfrastructurePayload(
    name: $checkedConvert('name', (v) => v as String?),
    operator_: $checkedConvert('operator', (v) => v as String?),
    voltageKv: $checkedConvert('voltageKv', (v) => v as num?),
    capacityMw: $checkedConvert('capacityMw', (v) => v as num?),
    beds: $checkedConvert('beds', (v) => (v as num?)?.toInt()),
    emergency: $checkedConvert('emergency', (v) => v as String?),
    iata: $checkedConvert('iata', (v) => v as String?),
    icao: $checkedConvert('icao', (v) => v as String?),
    ref: $checkedConvert('ref', (v) => v as String?),
    osmType: $checkedConvert(
      'osmType',
      (v) =>
          $enumDecodeNullable(_$OsmInfrastructurePayloadOsmTypeEnumEnumMap, v),
    ),
    osmVersion: $checkedConvert('osmVersion', (v) => (v as num?)?.toInt()),
  );
  return val;
}, fieldKeyMap: const {'operator_': 'operator'});

Map<String, dynamic> _$OsmInfrastructurePayloadToJson(
  OsmInfrastructurePayload instance,
) => <String, dynamic>{
  'name': ?instance.name,
  'operator': ?instance.operator_,
  'voltageKv': ?instance.voltageKv,
  'capacityMw': ?instance.capacityMw,
  'beds': ?instance.beds,
  'emergency': ?instance.emergency,
  'iata': ?instance.iata,
  'icao': ?instance.icao,
  'ref': ?instance.ref,
  'osmType': ?_$OsmInfrastructurePayloadOsmTypeEnumEnumMap[instance.osmType],
  'osmVersion': ?instance.osmVersion,
};

const _$OsmInfrastructurePayloadOsmTypeEnumEnumMap = {
  OsmInfrastructurePayloadOsmTypeEnum.node: 'node',
  OsmInfrastructurePayloadOsmTypeEnum.way: 'way',
  OsmInfrastructurePayloadOsmTypeEnum.relation: 'relation',
};
