// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'deception_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DeceptionPayloadCWProxy {
  DeceptionPayload automated(bool automated);

  DeceptionPayload notAWarning(bool notAWarning);

  DeceptionPayload detector(DerivedPayloadCommonDetector detector);

  DeceptionPayload confidence(num? confidence);

  DeceptionPayload status(DeceptionPayloadStatusEnum status);

  DeceptionPayload evidence(List<DerivedEvidenceRef>? evidence);

  DeceptionPayload entity(String entity);

  DeceptionPayload checks(List<DeceptionPayloadAllOfChecks> checks);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeceptionPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeceptionPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  DeceptionPayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    DeceptionPayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    String entity,
    List<DeceptionPayloadAllOfChecks> checks,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDeceptionPayload.copyWith(...)` or call `instanceOfDeceptionPayload.copyWith.fieldName(value)` for a single field.
class _$DeceptionPayloadCWProxyImpl implements _$DeceptionPayloadCWProxy {
  const _$DeceptionPayloadCWProxyImpl(this._value);

  final DeceptionPayload _value;

  @override
  DeceptionPayload automated(bool automated) => call(automated: automated);

  @override
  DeceptionPayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  DeceptionPayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  DeceptionPayload confidence(num? confidence) => call(confidence: confidence);

  @override
  DeceptionPayload status(DeceptionPayloadStatusEnum status) =>
      call(status: status);

  @override
  DeceptionPayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  DeceptionPayload entity(String entity) => call(entity: entity);

  @override
  DeceptionPayload checks(List<DeceptionPayloadAllOfChecks> checks) =>
      call(checks: checks);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeceptionPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeceptionPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DeceptionPayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? entity = const $CopyWithPlaceholder(),
    Object? checks = const $CopyWithPlaceholder(),
  }) {
    return DeceptionPayload(
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
          : status as DeceptionPayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      entity: entity == const $CopyWithPlaceholder() || entity == null
          ? _value.entity
          // ignore: cast_nullable_to_non_nullable
          : entity as String,
      checks: checks == const $CopyWithPlaceholder() || checks == null
          ? _value.checks
          // ignore: cast_nullable_to_non_nullable
          : checks as List<DeceptionPayloadAllOfChecks>,
    );
  }
}

extension $DeceptionPayloadCopyWith on DeceptionPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDeceptionPayload.copyWith(...)` or `instanceOfDeceptionPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DeceptionPayloadCWProxy get copyWith => _$DeceptionPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DeceptionPayload _$DeceptionPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DeceptionPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'entity',
      'checks',
    ],
  );
  final val = DeceptionPayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$DeceptionPayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    entity: $checkedConvert('entity', (v) => v as String),
    checks: $checkedConvert(
      'checks',
      (v) => (v as List<dynamic>)
          .map(
            (e) =>
                DeceptionPayloadAllOfChecks.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$DeceptionPayloadToJson(DeceptionPayload instance) =>
    <String, dynamic>{
      'automated': instance.automated,
      'notAWarning': instance.notAWarning,
      'detector': instance.detector.toJson(),
      'confidence': ?instance.confidence,
      'status': _$DeceptionPayloadStatusEnumEnumMap[instance.status]!,
      'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
      'entity': instance.entity,
      'checks': instance.checks.map((e) => e.toJson()).toList(),
    };

const _$DeceptionPayloadStatusEnumEnumMap = {
  DeceptionPayloadStatusEnum.ok: 'ok',
  DeceptionPayloadStatusEnum.insufficientData: 'insufficient-data',
  DeceptionPayloadStatusEnum.noCoverage: 'no-coverage',
  DeceptionPayloadStatusEnum.degradedInput: 'degraded-input',
};
