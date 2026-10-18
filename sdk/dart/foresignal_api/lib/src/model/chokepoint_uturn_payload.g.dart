// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'chokepoint_uturn_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChokepointUturnPayloadCWProxy {
  ChokepointUturnPayload automated(bool automated);

  ChokepointUturnPayload notAWarning(bool notAWarning);

  ChokepointUturnPayload detector(DerivedPayloadCommonDetector detector);

  ChokepointUturnPayload confidence(num? confidence);

  ChokepointUturnPayload status(ChokepointUturnPayloadStatusEnum status);

  ChokepointUturnPayload evidence(List<DerivedEvidenceRef>? evidence);

  ChokepointUturnPayload mmsi(String mmsi);

  ChokepointUturnPayload chokepointId(String chokepointId);

  ChokepointUturnPayload at(DateTime at);

  ChokepointUturnPayload manoeuvre(
    ChokepointUturnPayloadManoeuvreEnum manoeuvre,
  );

  ChokepointUturnPayload courseChangeDeg(num? courseChangeDeg);

  ChokepointUturnPayload speedDropPct(num? speedDropPct);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChokepointUturnPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChokepointUturnPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  ChokepointUturnPayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    ChokepointUturnPayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    String mmsi,
    String chokepointId,
    DateTime at,
    ChokepointUturnPayloadManoeuvreEnum manoeuvre,
    num? courseChangeDeg,
    num? speedDropPct,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChokepointUturnPayload.copyWith(...)` or call `instanceOfChokepointUturnPayload.copyWith.fieldName(value)` for a single field.
class _$ChokepointUturnPayloadCWProxyImpl
    implements _$ChokepointUturnPayloadCWProxy {
  const _$ChokepointUturnPayloadCWProxyImpl(this._value);

  final ChokepointUturnPayload _value;

  @override
  ChokepointUturnPayload automated(bool automated) =>
      call(automated: automated);

  @override
  ChokepointUturnPayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  ChokepointUturnPayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  ChokepointUturnPayload confidence(num? confidence) =>
      call(confidence: confidence);

  @override
  ChokepointUturnPayload status(ChokepointUturnPayloadStatusEnum status) =>
      call(status: status);

  @override
  ChokepointUturnPayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  ChokepointUturnPayload mmsi(String mmsi) => call(mmsi: mmsi);

  @override
  ChokepointUturnPayload chokepointId(String chokepointId) =>
      call(chokepointId: chokepointId);

  @override
  ChokepointUturnPayload at(DateTime at) => call(at: at);

  @override
  ChokepointUturnPayload manoeuvre(
    ChokepointUturnPayloadManoeuvreEnum manoeuvre,
  ) => call(manoeuvre: manoeuvre);

  @override
  ChokepointUturnPayload courseChangeDeg(num? courseChangeDeg) =>
      call(courseChangeDeg: courseChangeDeg);

  @override
  ChokepointUturnPayload speedDropPct(num? speedDropPct) =>
      call(speedDropPct: speedDropPct);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChokepointUturnPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChokepointUturnPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChokepointUturnPayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? mmsi = const $CopyWithPlaceholder(),
    Object? chokepointId = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
    Object? manoeuvre = const $CopyWithPlaceholder(),
    Object? courseChangeDeg = const $CopyWithPlaceholder(),
    Object? speedDropPct = const $CopyWithPlaceholder(),
  }) {
    return ChokepointUturnPayload(
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
          : status as ChokepointUturnPayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      mmsi: mmsi == const $CopyWithPlaceholder() || mmsi == null
          ? _value.mmsi
          // ignore: cast_nullable_to_non_nullable
          : mmsi as String,
      chokepointId:
          chokepointId == const $CopyWithPlaceholder() || chokepointId == null
          ? _value.chokepointId
          // ignore: cast_nullable_to_non_nullable
          : chokepointId as String,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
      manoeuvre: manoeuvre == const $CopyWithPlaceholder() || manoeuvre == null
          ? _value.manoeuvre
          // ignore: cast_nullable_to_non_nullable
          : manoeuvre as ChokepointUturnPayloadManoeuvreEnum,
      courseChangeDeg: courseChangeDeg == const $CopyWithPlaceholder()
          ? _value.courseChangeDeg
          // ignore: cast_nullable_to_non_nullable
          : courseChangeDeg as num?,
      speedDropPct: speedDropPct == const $CopyWithPlaceholder()
          ? _value.speedDropPct
          // ignore: cast_nullable_to_non_nullable
          : speedDropPct as num?,
    );
  }
}

extension $ChokepointUturnPayloadCopyWith on ChokepointUturnPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChokepointUturnPayload.copyWith(...)` or `instanceOfChokepointUturnPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChokepointUturnPayloadCWProxy get copyWith =>
      _$ChokepointUturnPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChokepointUturnPayload _$ChokepointUturnPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ChokepointUturnPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'mmsi',
      'chokepointId',
      'at',
      'manoeuvre',
    ],
  );
  final val = ChokepointUturnPayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$ChokepointUturnPayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    mmsi: $checkedConvert('mmsi', (v) => v as String),
    chokepointId: $checkedConvert('chokepointId', (v) => v as String),
    at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
    manoeuvre: $checkedConvert(
      'manoeuvre',
      (v) => $enumDecode(_$ChokepointUturnPayloadManoeuvreEnumEnumMap, v),
    ),
    courseChangeDeg: $checkedConvert('courseChangeDeg', (v) => v as num?),
    speedDropPct: $checkedConvert('speedDropPct', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$ChokepointUturnPayloadToJson(
  ChokepointUturnPayload instance,
) => <String, dynamic>{
  'automated': instance.automated,
  'notAWarning': instance.notAWarning,
  'detector': instance.detector.toJson(),
  'confidence': ?instance.confidence,
  'status': _$ChokepointUturnPayloadStatusEnumEnumMap[instance.status]!,
  'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
  'mmsi': instance.mmsi,
  'chokepointId': instance.chokepointId,
  'at': instance.at.toIso8601String(),
  'manoeuvre':
      _$ChokepointUturnPayloadManoeuvreEnumEnumMap[instance.manoeuvre]!,
  'courseChangeDeg': ?instance.courseChangeDeg,
  'speedDropPct': ?instance.speedDropPct,
};

const _$ChokepointUturnPayloadStatusEnumEnumMap = {
  ChokepointUturnPayloadStatusEnum.ok: 'ok',
  ChokepointUturnPayloadStatusEnum.insufficientData: 'insufficient-data',
  ChokepointUturnPayloadStatusEnum.noCoverage: 'no-coverage',
  ChokepointUturnPayloadStatusEnum.degradedInput: 'degraded-input',
};

const _$ChokepointUturnPayloadManoeuvreEnumEnumMap = {
  ChokepointUturnPayloadManoeuvreEnum.courseChange: 'course-change',
  ChokepointUturnPayloadManoeuvreEnum.uTurn: 'u-turn',
  ChokepointUturnPayloadManoeuvreEnum.speedDrop: 'speed-drop',
};
