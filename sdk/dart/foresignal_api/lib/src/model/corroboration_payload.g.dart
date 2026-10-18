// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'corroboration_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CorroborationPayloadCWProxy {
  CorroborationPayload automated(bool automated);

  CorroborationPayload notAWarning(bool notAWarning);

  CorroborationPayload detector(DerivedPayloadCommonDetector detector);

  CorroborationPayload confidence(num? confidence);

  CorroborationPayload status(CorroborationPayloadStatusEnum status);

  CorroborationPayload evidence(List<DerivedEvidenceRef>? evidence);

  CorroborationPayload kindFamily(
    CorroborationPayloadKindFamilyEnum kindFamily,
  );

  CorroborationPayload members(List<DerivedEvidenceRef> members);

  CorroborationPayload independentSources(int independentSources);

  CorroborationPayload credibility(int credibility);

  CorroborationPayload reliability(
    CorroborationPayloadReliabilityEnum? reliability,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorroborationPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorroborationPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  CorroborationPayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    CorroborationPayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    CorroborationPayloadKindFamilyEnum kindFamily,
    List<DerivedEvidenceRef> members,
    int independentSources,
    int credibility,
    CorroborationPayloadReliabilityEnum? reliability,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCorroborationPayload.copyWith(...)` or call `instanceOfCorroborationPayload.copyWith.fieldName(value)` for a single field.
class _$CorroborationPayloadCWProxyImpl
    implements _$CorroborationPayloadCWProxy {
  const _$CorroborationPayloadCWProxyImpl(this._value);

  final CorroborationPayload _value;

  @override
  CorroborationPayload automated(bool automated) => call(automated: automated);

  @override
  CorroborationPayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  CorroborationPayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  CorroborationPayload confidence(num? confidence) =>
      call(confidence: confidence);

  @override
  CorroborationPayload status(CorroborationPayloadStatusEnum status) =>
      call(status: status);

  @override
  CorroborationPayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  CorroborationPayload kindFamily(
    CorroborationPayloadKindFamilyEnum kindFamily,
  ) => call(kindFamily: kindFamily);

  @override
  CorroborationPayload members(List<DerivedEvidenceRef> members) =>
      call(members: members);

  @override
  CorroborationPayload independentSources(int independentSources) =>
      call(independentSources: independentSources);

  @override
  CorroborationPayload credibility(int credibility) =>
      call(credibility: credibility);

  @override
  CorroborationPayload reliability(
    CorroborationPayloadReliabilityEnum? reliability,
  ) => call(reliability: reliability);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CorroborationPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CorroborationPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CorroborationPayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? kindFamily = const $CopyWithPlaceholder(),
    Object? members = const $CopyWithPlaceholder(),
    Object? independentSources = const $CopyWithPlaceholder(),
    Object? credibility = const $CopyWithPlaceholder(),
    Object? reliability = const $CopyWithPlaceholder(),
  }) {
    return CorroborationPayload(
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
          : status as CorroborationPayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      kindFamily:
          kindFamily == const $CopyWithPlaceholder() || kindFamily == null
          ? _value.kindFamily
          // ignore: cast_nullable_to_non_nullable
          : kindFamily as CorroborationPayloadKindFamilyEnum,
      members: members == const $CopyWithPlaceholder() || members == null
          ? _value.members
          // ignore: cast_nullable_to_non_nullable
          : members as List<DerivedEvidenceRef>,
      independentSources:
          independentSources == const $CopyWithPlaceholder() ||
              independentSources == null
          ? _value.independentSources
          // ignore: cast_nullable_to_non_nullable
          : independentSources as int,
      credibility:
          credibility == const $CopyWithPlaceholder() || credibility == null
          ? _value.credibility
          // ignore: cast_nullable_to_non_nullable
          : credibility as int,
      reliability: reliability == const $CopyWithPlaceholder()
          ? _value.reliability
          // ignore: cast_nullable_to_non_nullable
          : reliability as CorroborationPayloadReliabilityEnum?,
    );
  }
}

extension $CorroborationPayloadCopyWith on CorroborationPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCorroborationPayload.copyWith(...)` or `instanceOfCorroborationPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CorroborationPayloadCWProxy get copyWith =>
      _$CorroborationPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CorroborationPayload _$CorroborationPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CorroborationPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'kindFamily',
      'members',
      'independentSources',
      'credibility',
    ],
  );
  final val = CorroborationPayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$CorroborationPayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    kindFamily: $checkedConvert(
      'kindFamily',
      (v) => $enumDecode(_$CorroborationPayloadKindFamilyEnumEnumMap, v),
    ),
    members: $checkedConvert(
      'members',
      (v) => (v as List<dynamic>)
          .map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    independentSources: $checkedConvert(
      'independentSources',
      (v) => (v as num).toInt(),
    ),
    credibility: $checkedConvert('credibility', (v) => (v as num).toInt()),
    reliability: $checkedConvert(
      'reliability',
      (v) =>
          $enumDecodeNullable(_$CorroborationPayloadReliabilityEnumEnumMap, v),
    ),
  );
  return val;
});

Map<String, dynamic> _$CorroborationPayloadToJson(
  CorroborationPayload instance,
) => <String, dynamic>{
  'automated': instance.automated,
  'notAWarning': instance.notAWarning,
  'detector': instance.detector.toJson(),
  'confidence': ?instance.confidence,
  'status': _$CorroborationPayloadStatusEnumEnumMap[instance.status]!,
  'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
  'kindFamily':
      _$CorroborationPayloadKindFamilyEnumEnumMap[instance.kindFamily]!,
  'members': instance.members.map((e) => e.toJson()).toList(),
  'independentSources': instance.independentSources,
  'credibility': instance.credibility,
  'reliability':
      ?_$CorroborationPayloadReliabilityEnumEnumMap[instance.reliability],
};

const _$CorroborationPayloadStatusEnumEnumMap = {
  CorroborationPayloadStatusEnum.ok: 'ok',
  CorroborationPayloadStatusEnum.insufficientData: 'insufficient-data',
  CorroborationPayloadStatusEnum.noCoverage: 'no-coverage',
  CorroborationPayloadStatusEnum.degradedInput: 'degraded-input',
};

const _$CorroborationPayloadKindFamilyEnumEnumMap = {
  CorroborationPayloadKindFamilyEnum.earthquake: 'earthquake',
  CorroborationPayloadKindFamilyEnum.fire: 'fire',
  CorroborationPayloadKindFamilyEnum.conflict: 'conflict',
  CorroborationPayloadKindFamilyEnum.outage: 'outage',
};

const _$CorroborationPayloadReliabilityEnumEnumMap = {
  CorroborationPayloadReliabilityEnum.A: 'A',
  CorroborationPayloadReliabilityEnum.B: 'B',
  CorroborationPayloadReliabilityEnum.C: 'C',
  CorroborationPayloadReliabilityEnum.D: 'D',
  CorroborationPayloadReliabilityEnum.E: 'E',
  CorroborationPayloadReliabilityEnum.F: 'F',
};
