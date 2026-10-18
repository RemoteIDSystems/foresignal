// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'loiter_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$LoiterPayloadCWProxy {
  LoiterPayload automated(bool automated);

  LoiterPayload notAWarning(bool notAWarning);

  LoiterPayload detector(DerivedPayloadCommonDetector detector);

  LoiterPayload confidence(num? confidence);

  LoiterPayload status(LoiterPayloadStatusEnum status);

  LoiterPayload evidence(List<DerivedEvidenceRef>? evidence);

  LoiterPayload entity(String entity);

  LoiterPayload fixes(int? fixes);

  LoiterPayload startedAt(DateTime startedAt);

  LoiterPayload durationMin(num? durationMin);

  LoiterPayload radiusNm(num? radiusNm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LoiterPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LoiterPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  LoiterPayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    LoiterPayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    String entity,
    int? fixes,
    DateTime startedAt,
    num? durationMin,
    num? radiusNm,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfLoiterPayload.copyWith(...)` or call `instanceOfLoiterPayload.copyWith.fieldName(value)` for a single field.
class _$LoiterPayloadCWProxyImpl implements _$LoiterPayloadCWProxy {
  const _$LoiterPayloadCWProxyImpl(this._value);

  final LoiterPayload _value;

  @override
  LoiterPayload automated(bool automated) => call(automated: automated);

  @override
  LoiterPayload notAWarning(bool notAWarning) => call(notAWarning: notAWarning);

  @override
  LoiterPayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  LoiterPayload confidence(num? confidence) => call(confidence: confidence);

  @override
  LoiterPayload status(LoiterPayloadStatusEnum status) => call(status: status);

  @override
  LoiterPayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  LoiterPayload entity(String entity) => call(entity: entity);

  @override
  LoiterPayload fixes(int? fixes) => call(fixes: fixes);

  @override
  LoiterPayload startedAt(DateTime startedAt) => call(startedAt: startedAt);

  @override
  LoiterPayload durationMin(num? durationMin) => call(durationMin: durationMin);

  @override
  LoiterPayload radiusNm(num? radiusNm) => call(radiusNm: radiusNm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `LoiterPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// LoiterPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  LoiterPayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? entity = const $CopyWithPlaceholder(),
    Object? fixes = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? durationMin = const $CopyWithPlaceholder(),
    Object? radiusNm = const $CopyWithPlaceholder(),
  }) {
    return LoiterPayload(
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
          : status as LoiterPayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      entity: entity == const $CopyWithPlaceholder() || entity == null
          ? _value.entity
          // ignore: cast_nullable_to_non_nullable
          : entity as String,
      fixes: fixes == const $CopyWithPlaceholder()
          ? _value.fixes
          // ignore: cast_nullable_to_non_nullable
          : fixes as int?,
      startedAt: startedAt == const $CopyWithPlaceholder() || startedAt == null
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime,
      durationMin: durationMin == const $CopyWithPlaceholder()
          ? _value.durationMin
          // ignore: cast_nullable_to_non_nullable
          : durationMin as num?,
      radiusNm: radiusNm == const $CopyWithPlaceholder()
          ? _value.radiusNm
          // ignore: cast_nullable_to_non_nullable
          : radiusNm as num?,
    );
  }
}

extension $LoiterPayloadCopyWith on LoiterPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfLoiterPayload.copyWith(...)` or `instanceOfLoiterPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$LoiterPayloadCWProxy get copyWith => _$LoiterPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

LoiterPayload _$LoiterPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('LoiterPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'entity',
      'startedAt',
    ],
  );
  final val = LoiterPayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$LoiterPayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    entity: $checkedConvert('entity', (v) => v as String),
    fixes: $checkedConvert('fixes', (v) => (v as num?)?.toInt()),
    startedAt: $checkedConvert('startedAt', (v) => DateTime.parse(v as String)),
    durationMin: $checkedConvert('durationMin', (v) => v as num?),
    radiusNm: $checkedConvert('radiusNm', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$LoiterPayloadToJson(LoiterPayload instance) =>
    <String, dynamic>{
      'automated': instance.automated,
      'notAWarning': instance.notAWarning,
      'detector': instance.detector.toJson(),
      'confidence': ?instance.confidence,
      'status': _$LoiterPayloadStatusEnumEnumMap[instance.status]!,
      'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
      'entity': instance.entity,
      'fixes': ?instance.fixes,
      'startedAt': instance.startedAt.toIso8601String(),
      'durationMin': ?instance.durationMin,
      'radiusNm': ?instance.radiusNm,
    };

const _$LoiterPayloadStatusEnumEnumMap = {
  LoiterPayloadStatusEnum.ok: 'ok',
  LoiterPayloadStatusEnum.insufficientData: 'insufficient-data',
  LoiterPayloadStatusEnum.noCoverage: 'no-coverage',
  LoiterPayloadStatusEnum.degradedInput: 'degraded-input',
};
