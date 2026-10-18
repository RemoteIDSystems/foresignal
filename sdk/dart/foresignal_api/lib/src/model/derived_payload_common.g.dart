// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'derived_payload_common.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DerivedPayloadCommonCWProxy {
  DerivedPayloadCommon automated(bool automated);

  DerivedPayloadCommon notAWarning(bool notAWarning);

  DerivedPayloadCommon detector(DerivedPayloadCommonDetector detector);

  DerivedPayloadCommon confidence(num? confidence);

  DerivedPayloadCommon status(DerivedPayloadCommonStatusEnum status);

  DerivedPayloadCommon evidence(List<DerivedEvidenceRef>? evidence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DerivedPayloadCommon(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DerivedPayloadCommon(...).copyWith(id: 12, name: "My name")
  /// ```
  DerivedPayloadCommon call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    DerivedPayloadCommonStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDerivedPayloadCommon.copyWith(...)` or call `instanceOfDerivedPayloadCommon.copyWith.fieldName(value)` for a single field.
class _$DerivedPayloadCommonCWProxyImpl
    implements _$DerivedPayloadCommonCWProxy {
  const _$DerivedPayloadCommonCWProxyImpl(this._value);

  final DerivedPayloadCommon _value;

  @override
  DerivedPayloadCommon automated(bool automated) => call(automated: automated);

  @override
  DerivedPayloadCommon notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  DerivedPayloadCommon detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  DerivedPayloadCommon confidence(num? confidence) =>
      call(confidence: confidence);

  @override
  DerivedPayloadCommon status(DerivedPayloadCommonStatusEnum status) =>
      call(status: status);

  @override
  DerivedPayloadCommon evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DerivedPayloadCommon(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DerivedPayloadCommon(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DerivedPayloadCommon call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
  }) {
    return DerivedPayloadCommon(
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
          : status as DerivedPayloadCommonStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
    );
  }
}

extension $DerivedPayloadCommonCopyWith on DerivedPayloadCommon {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDerivedPayloadCommon.copyWith(...)` or `instanceOfDerivedPayloadCommon.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DerivedPayloadCommonCWProxy get copyWith =>
      _$DerivedPayloadCommonCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DerivedPayloadCommon _$DerivedPayloadCommonFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DerivedPayloadCommon', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['automated', 'notAWarning', 'detector', 'status'],
  );
  final val = DerivedPayloadCommon(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$DerivedPayloadCommonStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$DerivedPayloadCommonToJson(
  DerivedPayloadCommon instance,
) => <String, dynamic>{
  'automated': instance.automated,
  'notAWarning': instance.notAWarning,
  'detector': instance.detector.toJson(),
  'confidence': ?instance.confidence,
  'status': _$DerivedPayloadCommonStatusEnumEnumMap[instance.status]!,
  'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
};

const _$DerivedPayloadCommonStatusEnumEnumMap = {
  DerivedPayloadCommonStatusEnum.ok: 'ok',
  DerivedPayloadCommonStatusEnum.insufficientData: 'insufficient-data',
  DerivedPayloadCommonStatusEnum.noCoverage: 'no-coverage',
  DerivedPayloadCommonStatusEnum.degradedInput: 'degraded-input',
};
