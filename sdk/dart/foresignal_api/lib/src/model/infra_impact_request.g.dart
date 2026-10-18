// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'infra_impact_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$InfraImpactRequestCWProxy {
  InfraImpactRequest geometry(Geometry? geometry);

  InfraImpactRequest eventId(String? eventId);

  InfraImpactRequest record(InfraImpactRequestRecord? record);

  InfraImpactRequest depth(int? depth);

  InfraImpactRequest minLabel(InfraImpactRequestMinLabelEnum? minLabel);

  InfraImpactRequest types(List<InfraImpactRequestTypesEnum>? types);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraImpactRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraImpactRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  InfraImpactRequest call({
    Geometry? geometry,
    String? eventId,
    InfraImpactRequestRecord? record,
    int? depth,
    InfraImpactRequestMinLabelEnum? minLabel,
    List<InfraImpactRequestTypesEnum>? types,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfInfraImpactRequest.copyWith(...)` or call `instanceOfInfraImpactRequest.copyWith.fieldName(value)` for a single field.
class _$InfraImpactRequestCWProxyImpl implements _$InfraImpactRequestCWProxy {
  const _$InfraImpactRequestCWProxyImpl(this._value);

  final InfraImpactRequest _value;

  @override
  InfraImpactRequest geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  InfraImpactRequest eventId(String? eventId) => call(eventId: eventId);

  @override
  InfraImpactRequest record(InfraImpactRequestRecord? record) =>
      call(record: record);

  @override
  InfraImpactRequest depth(int? depth) => call(depth: depth);

  @override
  InfraImpactRequest minLabel(InfraImpactRequestMinLabelEnum? minLabel) =>
      call(minLabel: minLabel);

  @override
  InfraImpactRequest types(List<InfraImpactRequestTypesEnum>? types) =>
      call(types: types);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `InfraImpactRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// InfraImpactRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  InfraImpactRequest call({
    Object? geometry = const $CopyWithPlaceholder(),
    Object? eventId = const $CopyWithPlaceholder(),
    Object? record = const $CopyWithPlaceholder(),
    Object? depth = const $CopyWithPlaceholder(),
    Object? minLabel = const $CopyWithPlaceholder(),
    Object? types = const $CopyWithPlaceholder(),
  }) {
    return InfraImpactRequest(
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      eventId: eventId == const $CopyWithPlaceholder()
          ? _value.eventId
          // ignore: cast_nullable_to_non_nullable
          : eventId as String?,
      record: record == const $CopyWithPlaceholder()
          ? _value.record
          // ignore: cast_nullable_to_non_nullable
          : record as InfraImpactRequestRecord?,
      depth: depth == const $CopyWithPlaceholder()
          ? _value.depth
          // ignore: cast_nullable_to_non_nullable
          : depth as int?,
      minLabel: minLabel == const $CopyWithPlaceholder()
          ? _value.minLabel
          // ignore: cast_nullable_to_non_nullable
          : minLabel as InfraImpactRequestMinLabelEnum?,
      types: types == const $CopyWithPlaceholder()
          ? _value.types
          // ignore: cast_nullable_to_non_nullable
          : types as List<InfraImpactRequestTypesEnum>?,
    );
  }
}

extension $InfraImpactRequestCopyWith on InfraImpactRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfInfraImpactRequest.copyWith(...)` or `instanceOfInfraImpactRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$InfraImpactRequestCWProxy get copyWith =>
      _$InfraImpactRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

InfraImpactRequest _$InfraImpactRequestFromJson(Map<String, dynamic> json) =>
    $checkedCreate('InfraImpactRequest', json, ($checkedConvert) {
      final val = InfraImpactRequest(
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        eventId: $checkedConvert('eventId', (v) => v as String?),
        record: $checkedConvert(
          'record',
          (v) => v == null
              ? null
              : InfraImpactRequestRecord.fromJson(v as Map<String, dynamic>),
        ),
        depth: $checkedConvert('depth', (v) => (v as num?)?.toInt() ?? 2),
        minLabel: $checkedConvert(
          'minLabel',
          (v) =>
              $enumDecodeNullable(_$InfraImpactRequestMinLabelEnumEnumMap, v),
        ),
        types: $checkedConvert(
          'types',
          (v) => (v as List<dynamic>?)
              ?.map((e) => $enumDecode(_$InfraImpactRequestTypesEnumEnumMap, e))
              .toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$InfraImpactRequestToJson(InfraImpactRequest instance) =>
    <String, dynamic>{
      'geometry': ?instance.geometry?.toJson(),
      'eventId': ?instance.eventId,
      'record': ?instance.record?.toJson(),
      'depth': ?instance.depth,
      'minLabel': ?_$InfraImpactRequestMinLabelEnumEnumMap[instance.minLabel],
      'types': ?instance.types
          ?.map((e) => _$InfraImpactRequestTypesEnumEnumMap[e]!)
          .toList(),
    };

const _$InfraImpactRequestMinLabelEnumEnumMap = {
  InfraImpactRequestMinLabelEnum.confirmed: 'confirmed',
  InfraImpactRequestMinLabelEnum.likely: 'likely',
  InfraImpactRequestMinLabelEnum.possible: 'possible',
  InfraImpactRequestMinLabelEnum.speculative: 'speculative',
};

const _$InfraImpactRequestTypesEnumEnumMap = {
  InfraImpactRequestTypesEnum.powerPeriodGeneration: 'power.generation',
  InfraImpactRequestTypesEnum.powerPeriodNuclear: 'power.nuclear',
  InfraImpactRequestTypesEnum.powerPeriodSubstation: 'power.substation',
  InfraImpactRequestTypesEnum.powerPeriodLine: 'power.line',
  InfraImpactRequestTypesEnum.telecomPeriodDatacentre: 'telecom.datacentre',
  InfraImpactRequestTypesEnum.telecomPeriodIxp: 'telecom.ixp',
  InfraImpactRequestTypesEnum.telecomPeriodExchange: 'telecom.exchange',
  InfraImpactRequestTypesEnum.telecomPeriodCable: 'telecom.cable',
  InfraImpactRequestTypesEnum.telecomPeriodLanding: 'telecom.landing',
  InfraImpactRequestTypesEnum.transportPeriodAirport: 'transport.airport',
  InfraImpactRequestTypesEnum.transportPeriodPort: 'transport.port',
  InfraImpactRequestTypesEnum.healthPeriodHospital: 'health.hospital',
  InfraImpactRequestTypesEnum.waterPeriodTreatment: 'water.treatment',
  InfraImpactRequestTypesEnum.spacePeriodSpaceport: 'space.spaceport',
};
