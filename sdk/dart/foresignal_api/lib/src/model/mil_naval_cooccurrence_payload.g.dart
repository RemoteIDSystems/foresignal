// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'mil_naval_cooccurrence_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MilNavalCooccurrencePayloadCWProxy {
  MilNavalCooccurrencePayload automated(bool automated);

  MilNavalCooccurrencePayload notAWarning(bool notAWarning);

  MilNavalCooccurrencePayload detector(DerivedPayloadCommonDetector detector);

  MilNavalCooccurrencePayload confidence(num? confidence);

  MilNavalCooccurrencePayload status(
    MilNavalCooccurrencePayloadStatusEnum status,
  );

  MilNavalCooccurrencePayload evidence(List<DerivedEvidenceRef>? evidence);

  MilNavalCooccurrencePayload cell(String cell);

  MilNavalCooccurrencePayload aircraftCount(int aircraftCount);

  MilNavalCooccurrencePayload vesselCount(int vesselCount);

  MilNavalCooccurrencePayload aircraft(List<String>? aircraft);

  MilNavalCooccurrencePayload vessels(List<String>? vessels);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MilNavalCooccurrencePayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MilNavalCooccurrencePayload(...).copyWith(id: 12, name: "My name")
  /// ```
  MilNavalCooccurrencePayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    MilNavalCooccurrencePayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    String cell,
    int aircraftCount,
    int vesselCount,
    List<String>? aircraft,
    List<String>? vessels,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMilNavalCooccurrencePayload.copyWith(...)` or call `instanceOfMilNavalCooccurrencePayload.copyWith.fieldName(value)` for a single field.
class _$MilNavalCooccurrencePayloadCWProxyImpl
    implements _$MilNavalCooccurrencePayloadCWProxy {
  const _$MilNavalCooccurrencePayloadCWProxyImpl(this._value);

  final MilNavalCooccurrencePayload _value;

  @override
  MilNavalCooccurrencePayload automated(bool automated) =>
      call(automated: automated);

  @override
  MilNavalCooccurrencePayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  MilNavalCooccurrencePayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  MilNavalCooccurrencePayload confidence(num? confidence) =>
      call(confidence: confidence);

  @override
  MilNavalCooccurrencePayload status(
    MilNavalCooccurrencePayloadStatusEnum status,
  ) => call(status: status);

  @override
  MilNavalCooccurrencePayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  MilNavalCooccurrencePayload cell(String cell) => call(cell: cell);

  @override
  MilNavalCooccurrencePayload aircraftCount(int aircraftCount) =>
      call(aircraftCount: aircraftCount);

  @override
  MilNavalCooccurrencePayload vesselCount(int vesselCount) =>
      call(vesselCount: vesselCount);

  @override
  MilNavalCooccurrencePayload aircraft(List<String>? aircraft) =>
      call(aircraft: aircraft);

  @override
  MilNavalCooccurrencePayload vessels(List<String>? vessels) =>
      call(vessels: vessels);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MilNavalCooccurrencePayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MilNavalCooccurrencePayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MilNavalCooccurrencePayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? cell = const $CopyWithPlaceholder(),
    Object? aircraftCount = const $CopyWithPlaceholder(),
    Object? vesselCount = const $CopyWithPlaceholder(),
    Object? aircraft = const $CopyWithPlaceholder(),
    Object? vessels = const $CopyWithPlaceholder(),
  }) {
    return MilNavalCooccurrencePayload(
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
          : status as MilNavalCooccurrencePayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      cell: cell == const $CopyWithPlaceholder() || cell == null
          ? _value.cell
          // ignore: cast_nullable_to_non_nullable
          : cell as String,
      aircraftCount:
          aircraftCount == const $CopyWithPlaceholder() || aircraftCount == null
          ? _value.aircraftCount
          // ignore: cast_nullable_to_non_nullable
          : aircraftCount as int,
      vesselCount:
          vesselCount == const $CopyWithPlaceholder() || vesselCount == null
          ? _value.vesselCount
          // ignore: cast_nullable_to_non_nullable
          : vesselCount as int,
      aircraft: aircraft == const $CopyWithPlaceholder()
          ? _value.aircraft
          // ignore: cast_nullable_to_non_nullable
          : aircraft as List<String>?,
      vessels: vessels == const $CopyWithPlaceholder()
          ? _value.vessels
          // ignore: cast_nullable_to_non_nullable
          : vessels as List<String>?,
    );
  }
}

extension $MilNavalCooccurrencePayloadCopyWith on MilNavalCooccurrencePayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMilNavalCooccurrencePayload.copyWith(...)` or `instanceOfMilNavalCooccurrencePayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MilNavalCooccurrencePayloadCWProxy get copyWith =>
      _$MilNavalCooccurrencePayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MilNavalCooccurrencePayload _$MilNavalCooccurrencePayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MilNavalCooccurrencePayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'cell',
      'aircraftCount',
      'vesselCount',
    ],
  );
  final val = MilNavalCooccurrencePayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$MilNavalCooccurrencePayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    cell: $checkedConvert('cell', (v) => v as String),
    aircraftCount: $checkedConvert('aircraftCount', (v) => (v as num).toInt()),
    vesselCount: $checkedConvert('vesselCount', (v) => (v as num).toInt()),
    aircraft: $checkedConvert(
      'aircraft',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    vessels: $checkedConvert(
      'vessels',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$MilNavalCooccurrencePayloadToJson(
  MilNavalCooccurrencePayload instance,
) => <String, dynamic>{
  'automated': instance.automated,
  'notAWarning': instance.notAWarning,
  'detector': instance.detector.toJson(),
  'confidence': ?instance.confidence,
  'status': _$MilNavalCooccurrencePayloadStatusEnumEnumMap[instance.status]!,
  'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
  'cell': instance.cell,
  'aircraftCount': instance.aircraftCount,
  'vesselCount': instance.vesselCount,
  'aircraft': ?instance.aircraft,
  'vessels': ?instance.vessels,
};

const _$MilNavalCooccurrencePayloadStatusEnumEnumMap = {
  MilNavalCooccurrencePayloadStatusEnum.ok: 'ok',
  MilNavalCooccurrencePayloadStatusEnum.insufficientData: 'insufficient-data',
  MilNavalCooccurrencePayloadStatusEnum.noCoverage: 'no-coverage',
  MilNavalCooccurrencePayloadStatusEnum.degradedInput: 'degraded-input',
};
