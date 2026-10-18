// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'port_call_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PortCallPayloadCWProxy {
  PortCallPayload automated(bool automated);

  PortCallPayload notAWarning(bool notAWarning);

  PortCallPayload detector(DerivedPayloadCommonDetector detector);

  PortCallPayload confidence(num? confidence);

  PortCallPayload status(PortCallPayloadStatusEnum status);

  PortCallPayload evidence(List<DerivedEvidenceRef>? evidence);

  PortCallPayload mmsi(String mmsi);

  PortCallPayload portNumber(String portNumber);

  PortCallPayload portName(String? portName);

  PortCallPayload arrival(DateTime arrival);

  PortCallPayload departure(DateTime? departure);

  PortCallPayload durationH(num? durationH);

  PortCallPayload departureUnknown(bool? departureUnknown);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PortCallPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PortCallPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  PortCallPayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    PortCallPayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    String mmsi,
    String portNumber,
    String? portName,
    DateTime arrival,
    DateTime? departure,
    num? durationH,
    bool? departureUnknown,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPortCallPayload.copyWith(...)` or call `instanceOfPortCallPayload.copyWith.fieldName(value)` for a single field.
class _$PortCallPayloadCWProxyImpl implements _$PortCallPayloadCWProxy {
  const _$PortCallPayloadCWProxyImpl(this._value);

  final PortCallPayload _value;

  @override
  PortCallPayload automated(bool automated) => call(automated: automated);

  @override
  PortCallPayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  PortCallPayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  PortCallPayload confidence(num? confidence) => call(confidence: confidence);

  @override
  PortCallPayload status(PortCallPayloadStatusEnum status) =>
      call(status: status);

  @override
  PortCallPayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  PortCallPayload mmsi(String mmsi) => call(mmsi: mmsi);

  @override
  PortCallPayload portNumber(String portNumber) => call(portNumber: portNumber);

  @override
  PortCallPayload portName(String? portName) => call(portName: portName);

  @override
  PortCallPayload arrival(DateTime arrival) => call(arrival: arrival);

  @override
  PortCallPayload departure(DateTime? departure) => call(departure: departure);

  @override
  PortCallPayload durationH(num? durationH) => call(durationH: durationH);

  @override
  PortCallPayload departureUnknown(bool? departureUnknown) =>
      call(departureUnknown: departureUnknown);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PortCallPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PortCallPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PortCallPayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? mmsi = const $CopyWithPlaceholder(),
    Object? portNumber = const $CopyWithPlaceholder(),
    Object? portName = const $CopyWithPlaceholder(),
    Object? arrival = const $CopyWithPlaceholder(),
    Object? departure = const $CopyWithPlaceholder(),
    Object? durationH = const $CopyWithPlaceholder(),
    Object? departureUnknown = const $CopyWithPlaceholder(),
  }) {
    return PortCallPayload(
      automated: automated == const $CopyWithPlaceholder() || automated == null
          ? _value.automated
          // ignore: cast_nullable_to_non_nullable
          : automated as bool,
      notAWarning:
          notAWarning == const $CopyWithPlaceholder() || notAWarning == null
          ? _value.notAWarning
          // ignore: cast_nullable_to_non_nullable
          : notAWarning as bool,
      detector: detector == const $CopyWithPlaceholder() || detector == null
          ? _value.detector
          // ignore: cast_nullable_to_non_nullable
          : detector as DerivedPayloadCommonDetector,
      confidence: confidence == const $CopyWithPlaceholder()
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as PortCallPayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      mmsi: mmsi == const $CopyWithPlaceholder() || mmsi == null
          ? _value.mmsi
          // ignore: cast_nullable_to_non_nullable
          : mmsi as String,
      portNumber:
          portNumber == const $CopyWithPlaceholder() || portNumber == null
          ? _value.portNumber
          // ignore: cast_nullable_to_non_nullable
          : portNumber as String,
      portName: portName == const $CopyWithPlaceholder()
          ? _value.portName
          // ignore: cast_nullable_to_non_nullable
          : portName as String?,
      arrival: arrival == const $CopyWithPlaceholder() || arrival == null
          ? _value.arrival
          // ignore: cast_nullable_to_non_nullable
          : arrival as DateTime,
      departure: departure == const $CopyWithPlaceholder()
          ? _value.departure
          // ignore: cast_nullable_to_non_nullable
          : departure as DateTime?,
      durationH: durationH == const $CopyWithPlaceholder()
          ? _value.durationH
          // ignore: cast_nullable_to_non_nullable
          : durationH as num?,
      departureUnknown: departureUnknown == const $CopyWithPlaceholder()
          ? _value.departureUnknown
          // ignore: cast_nullable_to_non_nullable
          : departureUnknown as bool?,
    );
  }
}

extension $PortCallPayloadCopyWith on PortCallPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPortCallPayload.copyWith(...)` or `instanceOfPortCallPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PortCallPayloadCWProxy get copyWith => _$PortCallPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PortCallPayload _$PortCallPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PortCallPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'mmsi',
      'portNumber',
      'arrival',
    ],
  );
  final val = PortCallPayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$PortCallPayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    mmsi: $checkedConvert('mmsi', (v) => v as String),
    portNumber: $checkedConvert('portNumber', (v) => v as String),
    portName: $checkedConvert('portName', (v) => v as String?),
    arrival: $checkedConvert('arrival', (v) => DateTime.parse(v as String)),
    departure: $checkedConvert(
      'departure',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    durationH: $checkedConvert('durationH', (v) => v as num?),
    departureUnknown: $checkedConvert('departureUnknown', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$PortCallPayloadToJson(PortCallPayload instance) =>
    <String, dynamic>{
      'automated': instance.automated,
      'notAWarning': instance.notAWarning,
      'detector': instance.detector.toJson(),
      'confidence': ?instance.confidence,
      'status': _$PortCallPayloadStatusEnumEnumMap[instance.status]!,
      'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
      'mmsi': instance.mmsi,
      'portNumber': instance.portNumber,
      'portName': ?instance.portName,
      'arrival': instance.arrival.toIso8601String(),
      'departure': ?instance.departure?.toIso8601String(),
      'durationH': ?instance.durationH,
      'departureUnknown': ?instance.departureUnknown,
    };

const _$PortCallPayloadStatusEnumEnumMap = {
  PortCallPayloadStatusEnum.ok: 'ok',
  PortCallPayloadStatusEnum.insufficientData: 'insufficient-data',
  PortCallPayloadStatusEnum.noCoverage: 'no-coverage',
  PortCallPayloadStatusEnum.degradedInput: 'degraded-input',
};
