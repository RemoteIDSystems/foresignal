// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'emergency_squawk_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EmergencySquawkPayloadCWProxy {
  EmergencySquawkPayload automated(bool automated);

  EmergencySquawkPayload notAWarning(bool notAWarning);

  EmergencySquawkPayload detector(DerivedPayloadCommonDetector detector);

  EmergencySquawkPayload confidence(num? confidence);

  EmergencySquawkPayload status(EmergencySquawkPayloadStatusEnum status);

  EmergencySquawkPayload evidence(List<DerivedEvidenceRef>? evidence);

  EmergencySquawkPayload hex(String hex);

  EmergencySquawkPayload squawk(String? squawk);

  EmergencySquawkPayload emergency(String? emergency);

  EmergencySquawkPayload meaning(EmergencySquawkPayloadMeaningEnum meaning);

  EmergencySquawkPayload unverified(bool unverified);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EmergencySquawkPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EmergencySquawkPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  EmergencySquawkPayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    EmergencySquawkPayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    String hex,
    String? squawk,
    String? emergency,
    EmergencySquawkPayloadMeaningEnum meaning,
    bool unverified,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEmergencySquawkPayload.copyWith(...)` or call `instanceOfEmergencySquawkPayload.copyWith.fieldName(value)` for a single field.
class _$EmergencySquawkPayloadCWProxyImpl
    implements _$EmergencySquawkPayloadCWProxy {
  const _$EmergencySquawkPayloadCWProxyImpl(this._value);

  final EmergencySquawkPayload _value;

  @override
  EmergencySquawkPayload automated(bool automated) =>
      call(automated: automated);

  @override
  EmergencySquawkPayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  EmergencySquawkPayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  EmergencySquawkPayload confidence(num? confidence) =>
      call(confidence: confidence);

  @override
  EmergencySquawkPayload status(EmergencySquawkPayloadStatusEnum status) =>
      call(status: status);

  @override
  EmergencySquawkPayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  EmergencySquawkPayload hex(String hex) => call(hex: hex);

  @override
  EmergencySquawkPayload squawk(String? squawk) => call(squawk: squawk);

  @override
  EmergencySquawkPayload emergency(String? emergency) =>
      call(emergency: emergency);

  @override
  EmergencySquawkPayload meaning(EmergencySquawkPayloadMeaningEnum meaning) =>
      call(meaning: meaning);

  @override
  EmergencySquawkPayload unverified(bool unverified) =>
      call(unverified: unverified);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EmergencySquawkPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EmergencySquawkPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EmergencySquawkPayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? hex = const $CopyWithPlaceholder(),
    Object? squawk = const $CopyWithPlaceholder(),
    Object? emergency = const $CopyWithPlaceholder(),
    Object? meaning = const $CopyWithPlaceholder(),
    Object? unverified = const $CopyWithPlaceholder(),
  }) {
    return EmergencySquawkPayload(
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
          : status as EmergencySquawkPayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      hex: hex == const $CopyWithPlaceholder() || hex == null
          ? _value.hex
          // ignore: cast_nullable_to_non_nullable
          : hex as String,
      squawk: squawk == const $CopyWithPlaceholder()
          ? _value.squawk
          // ignore: cast_nullable_to_non_nullable
          : squawk as String?,
      emergency: emergency == const $CopyWithPlaceholder()
          ? _value.emergency
          // ignore: cast_nullable_to_non_nullable
          : emergency as String?,
      meaning: meaning == const $CopyWithPlaceholder() || meaning == null
          ? _value.meaning
          // ignore: cast_nullable_to_non_nullable
          : meaning as EmergencySquawkPayloadMeaningEnum,
      unverified:
          unverified == const $CopyWithPlaceholder() || unverified == null
          ? _value.unverified
          // ignore: cast_nullable_to_non_nullable
          : unverified as bool,
    );
  }
}

extension $EmergencySquawkPayloadCopyWith on EmergencySquawkPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEmergencySquawkPayload.copyWith(...)` or `instanceOfEmergencySquawkPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EmergencySquawkPayloadCWProxy get copyWith =>
      _$EmergencySquawkPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EmergencySquawkPayload _$EmergencySquawkPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EmergencySquawkPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'hex',
      'meaning',
      'unverified',
    ],
  );
  final val = EmergencySquawkPayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$EmergencySquawkPayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    hex: $checkedConvert('hex', (v) => v as String),
    squawk: $checkedConvert('squawk', (v) => v as String?),
    emergency: $checkedConvert('emergency', (v) => v as String?),
    meaning: $checkedConvert(
      'meaning',
      (v) => $enumDecode(_$EmergencySquawkPayloadMeaningEnumEnumMap, v),
    ),
    unverified: $checkedConvert('unverified', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$EmergencySquawkPayloadToJson(
  EmergencySquawkPayload instance,
) => <String, dynamic>{
  'automated': instance.automated,
  'notAWarning': instance.notAWarning,
  'detector': instance.detector.toJson(),
  'confidence': ?instance.confidence,
  'status': _$EmergencySquawkPayloadStatusEnumEnumMap[instance.status]!,
  'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
  'hex': instance.hex,
  'squawk': ?instance.squawk,
  'emergency': ?instance.emergency,
  'meaning': _$EmergencySquawkPayloadMeaningEnumEnumMap[instance.meaning]!,
  'unverified': instance.unverified,
};

const _$EmergencySquawkPayloadStatusEnumEnumMap = {
  EmergencySquawkPayloadStatusEnum.ok: 'ok',
  EmergencySquawkPayloadStatusEnum.insufficientData: 'insufficient-data',
  EmergencySquawkPayloadStatusEnum.noCoverage: 'no-coverage',
  EmergencySquawkPayloadStatusEnum.degradedInput: 'degraded-input',
};

const _$EmergencySquawkPayloadMeaningEnumEnumMap = {
  EmergencySquawkPayloadMeaningEnum.unlawfulInterference:
      'unlawful interference',
  EmergencySquawkPayloadMeaningEnum.radioFailure: 'radio failure',
  EmergencySquawkPayloadMeaningEnum.emergency: 'emergency',
};
