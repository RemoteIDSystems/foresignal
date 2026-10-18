// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'holdover_fire_risk_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HoldoverFireRiskPayloadCWProxy {
  HoldoverFireRiskPayload automated(bool automated);

  HoldoverFireRiskPayload notAWarning(bool notAWarning);

  HoldoverFireRiskPayload detector(DerivedPayloadCommonDetector detector);

  HoldoverFireRiskPayload confidence(num? confidence);

  HoldoverFireRiskPayload status(HoldoverFireRiskPayloadStatusEnum status);

  HoldoverFireRiskPayload evidence(List<DerivedEvidenceRef>? evidence);

  HoldoverFireRiskPayload cell(String cell);

  HoldoverFireRiskPayload utcDay(DateTime utcDay);

  HoldoverFireRiskPayload flashCount(int flashCount);

  HoldoverFireRiskPayload qpe72hMm(num? qpe72hMm);

  HoldoverFireRiskPayload precipSource(
    HoldoverFireRiskPayloadPrecipSourceEnum? precipSource,
  );

  HoldoverFireRiskPayload relativeHumidityPct(num? relativeHumidityPct);

  HoldoverFireRiskPayload ignitionConfirmed(bool ignitionConfirmed);

  HoldoverFireRiskPayload ignitionDelayH(num? ignitionDelayH);

  HoldoverFireRiskPayload watchUntil(DateTime? watchUntil);

  HoldoverFireRiskPayload severity(Severity? severity);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HoldoverFireRiskPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HoldoverFireRiskPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  HoldoverFireRiskPayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    HoldoverFireRiskPayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    String cell,
    DateTime utcDay,
    int flashCount,
    num? qpe72hMm,
    HoldoverFireRiskPayloadPrecipSourceEnum? precipSource,
    num? relativeHumidityPct,
    bool ignitionConfirmed,
    num? ignitionDelayH,
    DateTime? watchUntil,
    Severity? severity,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHoldoverFireRiskPayload.copyWith(...)` or call `instanceOfHoldoverFireRiskPayload.copyWith.fieldName(value)` for a single field.
class _$HoldoverFireRiskPayloadCWProxyImpl
    implements _$HoldoverFireRiskPayloadCWProxy {
  const _$HoldoverFireRiskPayloadCWProxyImpl(this._value);

  final HoldoverFireRiskPayload _value;

  @override
  HoldoverFireRiskPayload automated(bool automated) =>
      call(automated: automated);

  @override
  HoldoverFireRiskPayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  HoldoverFireRiskPayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  HoldoverFireRiskPayload confidence(num? confidence) =>
      call(confidence: confidence);

  @override
  HoldoverFireRiskPayload status(HoldoverFireRiskPayloadStatusEnum status) =>
      call(status: status);

  @override
  HoldoverFireRiskPayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  HoldoverFireRiskPayload cell(String cell) => call(cell: cell);

  @override
  HoldoverFireRiskPayload utcDay(DateTime utcDay) => call(utcDay: utcDay);

  @override
  HoldoverFireRiskPayload flashCount(int flashCount) =>
      call(flashCount: flashCount);

  @override
  HoldoverFireRiskPayload qpe72hMm(num? qpe72hMm) => call(qpe72hMm: qpe72hMm);

  @override
  HoldoverFireRiskPayload precipSource(
    HoldoverFireRiskPayloadPrecipSourceEnum? precipSource,
  ) => call(precipSource: precipSource);

  @override
  HoldoverFireRiskPayload relativeHumidityPct(num? relativeHumidityPct) =>
      call(relativeHumidityPct: relativeHumidityPct);

  @override
  HoldoverFireRiskPayload ignitionConfirmed(bool ignitionConfirmed) =>
      call(ignitionConfirmed: ignitionConfirmed);

  @override
  HoldoverFireRiskPayload ignitionDelayH(num? ignitionDelayH) =>
      call(ignitionDelayH: ignitionDelayH);

  @override
  HoldoverFireRiskPayload watchUntil(DateTime? watchUntil) =>
      call(watchUntil: watchUntil);

  @override
  HoldoverFireRiskPayload severity(Severity? severity) =>
      call(severity: severity);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HoldoverFireRiskPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HoldoverFireRiskPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HoldoverFireRiskPayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? cell = const $CopyWithPlaceholder(),
    Object? utcDay = const $CopyWithPlaceholder(),
    Object? flashCount = const $CopyWithPlaceholder(),
    Object? qpe72hMm = const $CopyWithPlaceholder(),
    Object? precipSource = const $CopyWithPlaceholder(),
    Object? relativeHumidityPct = const $CopyWithPlaceholder(),
    Object? ignitionConfirmed = const $CopyWithPlaceholder(),
    Object? ignitionDelayH = const $CopyWithPlaceholder(),
    Object? watchUntil = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
  }) {
    return HoldoverFireRiskPayload(
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
          : status as HoldoverFireRiskPayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      cell: cell == const $CopyWithPlaceholder() || cell == null
          ? _value.cell
          // ignore: cast_nullable_to_non_nullable
          : cell as String,
      utcDay: utcDay == const $CopyWithPlaceholder() || utcDay == null
          ? _value.utcDay
          // ignore: cast_nullable_to_non_nullable
          : utcDay as DateTime,
      flashCount:
          flashCount == const $CopyWithPlaceholder() || flashCount == null
          ? _value.flashCount
          // ignore: cast_nullable_to_non_nullable
          : flashCount as int,
      qpe72hMm: qpe72hMm == const $CopyWithPlaceholder()
          ? _value.qpe72hMm
          // ignore: cast_nullable_to_non_nullable
          : qpe72hMm as num?,
      precipSource: precipSource == const $CopyWithPlaceholder()
          ? _value.precipSource
          // ignore: cast_nullable_to_non_nullable
          : precipSource as HoldoverFireRiskPayloadPrecipSourceEnum?,
      relativeHumidityPct: relativeHumidityPct == const $CopyWithPlaceholder()
          ? _value.relativeHumidityPct
          // ignore: cast_nullable_to_non_nullable
          : relativeHumidityPct as num?,
      ignitionConfirmed:
          ignitionConfirmed == const $CopyWithPlaceholder() ||
              ignitionConfirmed == null
          ? _value.ignitionConfirmed
          // ignore: cast_nullable_to_non_nullable
          : ignitionConfirmed as bool,
      ignitionDelayH: ignitionDelayH == const $CopyWithPlaceholder()
          ? _value.ignitionDelayH
          // ignore: cast_nullable_to_non_nullable
          : ignitionDelayH as num?,
      watchUntil: watchUntil == const $CopyWithPlaceholder()
          ? _value.watchUntil
          // ignore: cast_nullable_to_non_nullable
          : watchUntil as DateTime?,
      severity: severity == const $CopyWithPlaceholder()
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as Severity?,
    );
  }
}

extension $HoldoverFireRiskPayloadCopyWith on HoldoverFireRiskPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHoldoverFireRiskPayload.copyWith(...)` or `instanceOfHoldoverFireRiskPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HoldoverFireRiskPayloadCWProxy get copyWith =>
      _$HoldoverFireRiskPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HoldoverFireRiskPayload _$HoldoverFireRiskPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HoldoverFireRiskPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'cell',
      'utcDay',
      'flashCount',
      'ignitionConfirmed',
    ],
  );
  final val = HoldoverFireRiskPayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$HoldoverFireRiskPayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    cell: $checkedConvert('cell', (v) => v as String),
    utcDay: $checkedConvert('utcDay', (v) => DateTime.parse(v as String)),
    flashCount: $checkedConvert('flashCount', (v) => (v as num).toInt()),
    qpe72hMm: $checkedConvert('qpe72hMm', (v) => v as num?),
    precipSource: $checkedConvert(
      'precipSource',
      (v) => $enumDecodeNullable(
        _$HoldoverFireRiskPayloadPrecipSourceEnumEnumMap,
        v,
      ),
    ),
    relativeHumidityPct: $checkedConvert(
      'relativeHumidityPct',
      (v) => v as num?,
    ),
    ignitionConfirmed: $checkedConvert('ignitionConfirmed', (v) => v as bool),
    ignitionDelayH: $checkedConvert('ignitionDelayH', (v) => v as num?),
    watchUntil: $checkedConvert(
      'watchUntil',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    severity: $checkedConvert(
      'severity',
      (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$HoldoverFireRiskPayloadToJson(
  HoldoverFireRiskPayload instance,
) => <String, dynamic>{
  'automated': instance.automated,
  'notAWarning': instance.notAWarning,
  'detector': instance.detector.toJson(),
  'confidence': ?instance.confidence,
  'status': _$HoldoverFireRiskPayloadStatusEnumEnumMap[instance.status]!,
  'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
  'cell': instance.cell,
  'utcDay': instance.utcDay.toIso8601String(),
  'flashCount': instance.flashCount,
  'qpe72hMm': ?instance.qpe72hMm,
  'precipSource':
      ?_$HoldoverFireRiskPayloadPrecipSourceEnumEnumMap[instance.precipSource],
  'relativeHumidityPct': ?instance.relativeHumidityPct,
  'ignitionConfirmed': instance.ignitionConfirmed,
  'ignitionDelayH': ?instance.ignitionDelayH,
  'watchUntil': ?instance.watchUntil?.toIso8601String(),
  'severity': ?_$SeverityEnumMap[instance.severity],
};

const _$HoldoverFireRiskPayloadStatusEnumEnumMap = {
  HoldoverFireRiskPayloadStatusEnum.ok: 'ok',
  HoldoverFireRiskPayloadStatusEnum.insufficientData: 'insufficient-data',
  HoldoverFireRiskPayloadStatusEnum.noCoverage: 'no-coverage',
  HoldoverFireRiskPayloadStatusEnum.degradedInput: 'degraded-input',
};

const _$HoldoverFireRiskPayloadPrecipSourceEnumEnumMap = {
  HoldoverFireRiskPayloadPrecipSourceEnum.mrms: 'mrms',
  HoldoverFireRiskPayloadPrecipSourceEnum.model: 'model',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
