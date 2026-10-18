// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chokepoint_activity_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChokepointActivityPayloadCWProxy {
  ChokepointActivityPayload automated(bool automated);

  ChokepointActivityPayload notAWarning(bool notAWarning);

  ChokepointActivityPayload detector(DerivedPayloadCommonDetector detector);

  ChokepointActivityPayload confidence(num? confidence);

  ChokepointActivityPayload status(ChokepointActivityPayloadStatusEnum status);

  ChokepointActivityPayload evidence(List<DerivedEvidenceRef>? evidence);

  ChokepointActivityPayload chokepointId(String chokepointId);

  ChokepointActivityPayload name(String? name);

  ChokepointActivityPayload hourStart(DateTime hourStart);

  ChokepointActivityPayload transits(int? transits);

  ChokepointActivityPayload uTurns(int? uTurns);

  ChokepointActivityPayload stopped(int? stopped);

  ChokepointActivityPayload coverageStatus(
    ChokepointActivityPayloadCoverageStatusEnum? coverageStatus,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChokepointActivityPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChokepointActivityPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  ChokepointActivityPayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    ChokepointActivityPayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    String chokepointId,
    String? name,
    DateTime hourStart,
    int? transits,
    int? uTurns,
    int? stopped,
    ChokepointActivityPayloadCoverageStatusEnum? coverageStatus,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChokepointActivityPayload.copyWith(...)` or call `instanceOfChokepointActivityPayload.copyWith.fieldName(value)` for a single field.
class _$ChokepointActivityPayloadCWProxyImpl
    implements _$ChokepointActivityPayloadCWProxy {
  const _$ChokepointActivityPayloadCWProxyImpl(this._value);

  final ChokepointActivityPayload _value;

  @override
  ChokepointActivityPayload automated(bool automated) =>
      call(automated: automated);

  @override
  ChokepointActivityPayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  ChokepointActivityPayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  ChokepointActivityPayload confidence(num? confidence) =>
      call(confidence: confidence);

  @override
  ChokepointActivityPayload status(
    ChokepointActivityPayloadStatusEnum status,
  ) => call(status: status);

  @override
  ChokepointActivityPayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  ChokepointActivityPayload chokepointId(String chokepointId) =>
      call(chokepointId: chokepointId);

  @override
  ChokepointActivityPayload name(String? name) => call(name: name);

  @override
  ChokepointActivityPayload hourStart(DateTime hourStart) =>
      call(hourStart: hourStart);

  @override
  ChokepointActivityPayload transits(int? transits) => call(transits: transits);

  @override
  ChokepointActivityPayload uTurns(int? uTurns) => call(uTurns: uTurns);

  @override
  ChokepointActivityPayload stopped(int? stopped) => call(stopped: stopped);

  @override
  ChokepointActivityPayload coverageStatus(
    ChokepointActivityPayloadCoverageStatusEnum? coverageStatus,
  ) => call(coverageStatus: coverageStatus);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChokepointActivityPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChokepointActivityPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChokepointActivityPayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? chokepointId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? hourStart = const $CopyWithPlaceholder(),
    Object? transits = const $CopyWithPlaceholder(),
    Object? uTurns = const $CopyWithPlaceholder(),
    Object? stopped = const $CopyWithPlaceholder(),
    Object? coverageStatus = const $CopyWithPlaceholder(),
  }) {
    return ChokepointActivityPayload(
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
          : status as ChokepointActivityPayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      chokepointId:
          chokepointId == const $CopyWithPlaceholder() || chokepointId == null
          ? _value.chokepointId
          // ignore: cast_nullable_to_non_nullable
          : chokepointId as String,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      hourStart: hourStart == const $CopyWithPlaceholder() || hourStart == null
          ? _value.hourStart
          // ignore: cast_nullable_to_non_nullable
          : hourStart as DateTime,
      transits: transits == const $CopyWithPlaceholder()
          ? _value.transits
          // ignore: cast_nullable_to_non_nullable
          : transits as int?,
      uTurns: uTurns == const $CopyWithPlaceholder()
          ? _value.uTurns
          // ignore: cast_nullable_to_non_nullable
          : uTurns as int?,
      stopped: stopped == const $CopyWithPlaceholder()
          ? _value.stopped
          // ignore: cast_nullable_to_non_nullable
          : stopped as int?,
      coverageStatus: coverageStatus == const $CopyWithPlaceholder()
          ? _value.coverageStatus
          // ignore: cast_nullable_to_non_nullable
          : coverageStatus as ChokepointActivityPayloadCoverageStatusEnum?,
    );
  }
}

extension $ChokepointActivityPayloadCopyWith on ChokepointActivityPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChokepointActivityPayload.copyWith(...)` or `instanceOfChokepointActivityPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChokepointActivityPayloadCWProxy get copyWith =>
      _$ChokepointActivityPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChokepointActivityPayload _$ChokepointActivityPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChokepointActivityPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'chokepointId',
      'hourStart',
    ],
  );
  final val = ChokepointActivityPayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ChokepointActivityPayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    chokepointId: $checkedConvert('chokepointId', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String?),
    hourStart: $checkedConvert('hourStart', (v) => DateTime.parse(v as String)),
    transits: $checkedConvert('transits', (v) => (v as num?)?.toInt()),
    uTurns: $checkedConvert('uTurns', (v) => (v as num?)?.toInt()),
    stopped: $checkedConvert('stopped', (v) => (v as num?)?.toInt()),
    coverageStatus: $checkedConvert(
      'coverageStatus',
      (v) => $enumDecodeNullable(
        _$ChokepointActivityPayloadCoverageStatusEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$ChokepointActivityPayloadToJson(
  ChokepointActivityPayload instance,
) => <String, dynamic>{
  'automated': instance.automated,
  'notAWarning': instance.notAWarning,
  'detector': instance.detector.toJson(),
  'confidence': ?instance.confidence,
  'status': _$ChokepointActivityPayloadStatusEnumEnumMap[instance.status]!,
  'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
  'chokepointId': instance.chokepointId,
  'name': ?instance.name,
  'hourStart': instance.hourStart.toIso8601String(),
  'transits': ?instance.transits,
  'uTurns': ?instance.uTurns,
  'stopped': ?instance.stopped,
  'coverageStatus':
      ?_$ChokepointActivityPayloadCoverageStatusEnumEnumMap[instance
          .coverageStatus],
};

const _$ChokepointActivityPayloadStatusEnumEnumMap = {
  ChokepointActivityPayloadStatusEnum.ok: 'ok',
  ChokepointActivityPayloadStatusEnum.insufficientData: 'insufficient-data',
  ChokepointActivityPayloadStatusEnum.noCoverage: 'no-coverage',
  ChokepointActivityPayloadStatusEnum.degradedInput: 'degraded-input',
};

const _$ChokepointActivityPayloadCoverageStatusEnumEnumMap = {
  ChokepointActivityPayloadCoverageStatusEnum.ok: 'ok',
  ChokepointActivityPayloadCoverageStatusEnum.insufficientData:
      'insufficient-data',
  ChokepointActivityPayloadCoverageStatusEnum.noCoverage: 'no-coverage',
  ChokepointActivityPayloadCoverageStatusEnum.degradedInput: 'degraded-input',
};
