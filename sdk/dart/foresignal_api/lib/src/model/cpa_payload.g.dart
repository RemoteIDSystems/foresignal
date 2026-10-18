// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cpa_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CpaPayloadCWProxy {
  CpaPayload automated(bool automated);

  CpaPayload notAWarning(bool notAWarning);

  CpaPayload detector(DerivedPayloadCommonDetector detector);

  CpaPayload confidence(num? confidence);

  CpaPayload status(CpaPayloadStatusEnum status);

  CpaPayload evidence(List<DerivedEvidenceRef>? evidence);

  CpaPayload vessels(List<String> vessels);

  CpaPayload cpaNm(num cpaNm);

  CpaPayload tcpaMin(num tcpaMin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CpaPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CpaPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  CpaPayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    CpaPayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    List<String> vessels,
    num cpaNm,
    num tcpaMin,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCpaPayload.copyWith(...)` or call `instanceOfCpaPayload.copyWith.fieldName(value)` for a single field.
class _$CpaPayloadCWProxyImpl implements _$CpaPayloadCWProxy {
  const _$CpaPayloadCWProxyImpl(this._value);

  final CpaPayload _value;

  @override
  CpaPayload automated(bool automated) => call(automated: automated);

  @override
  CpaPayload notAWarning(bool notAWarning) => call(notAWarning: notAWarning);

  @override
  CpaPayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  CpaPayload confidence(num? confidence) => call(confidence: confidence);

  @override
  CpaPayload status(CpaPayloadStatusEnum status) => call(status: status);

  @override
  CpaPayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  CpaPayload vessels(List<String> vessels) => call(vessels: vessels);

  @override
  CpaPayload cpaNm(num cpaNm) => call(cpaNm: cpaNm);

  @override
  CpaPayload tcpaMin(num tcpaMin) => call(tcpaMin: tcpaMin);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CpaPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CpaPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CpaPayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? vessels = const $CopyWithPlaceholder(),
    Object? cpaNm = const $CopyWithPlaceholder(),
    Object? tcpaMin = const $CopyWithPlaceholder(),
  }) {
    return CpaPayload(
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
          : status as CpaPayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      vessels: vessels == const $CopyWithPlaceholder() || vessels == null
          ? _value.vessels
          // ignore: cast_nullable_to_non_nullable
          : vessels as List<String>,
      cpaNm: cpaNm == const $CopyWithPlaceholder() || cpaNm == null
          ? _value.cpaNm
          // ignore: cast_nullable_to_non_nullable
          : cpaNm as num,
      tcpaMin: tcpaMin == const $CopyWithPlaceholder() || tcpaMin == null
          ? _value.tcpaMin
          // ignore: cast_nullable_to_non_nullable
          : tcpaMin as num,
    );
  }
}

extension $CpaPayloadCopyWith on CpaPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCpaPayload.copyWith(...)` or `instanceOfCpaPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CpaPayloadCWProxy get copyWith => _$CpaPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CpaPayload _$CpaPayloadFromJson(Map<String, dynamic> json) => $checkedCreate(
  'CpaPayload',
  json,
  ($checkedConvert) {
    $checkKeys(
      json,
      requiredKeys: const [
        'automated',
        'notAWarning',
        'detector',
        'status',
        'vessels',
        'cpaNm',
        'tcpaMin',
      ],
    );
    final val = CpaPayload(
      automated: $checkedConvert('automated', (v) => v as bool),
      notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
      detector: $checkedConvert(
        'detector',
        (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
      ),
      confidence: $checkedConvert('confidence', (v) => v as num?),
      status: $checkedConvert(
        'status',
        (v) => $enumDecode(_$CpaPayloadStatusEnumEnumMap, v),
      ),
      evidence: $checkedConvert(
        'evidence',
        (v) => (v as List<dynamic>?)
            ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      vessels: $checkedConvert(
        'vessels',
        (v) => (v as List<dynamic>).map((e) => e as String).toList(),
      ),
      cpaNm: $checkedConvert('cpaNm', (v) => v as num),
      tcpaMin: $checkedConvert('tcpaMin', (v) => v as num),
    );
    return val;
  },
);

Map<String, dynamic> _$CpaPayloadToJson(CpaPayload instance) =>
    <String, dynamic>{
      'automated': instance.automated,
      'notAWarning': instance.notAWarning,
      'detector': instance.detector.toJson(),
      'confidence': ?instance.confidence,
      'status': _$CpaPayloadStatusEnumEnumMap[instance.status]!,
      'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
      'vessels': instance.vessels,
      'cpaNm': instance.cpaNm,
      'tcpaMin': instance.tcpaMin,
    };

const _$CpaPayloadStatusEnumEnumMap = {
  CpaPayloadStatusEnum.ok: 'ok',
  CpaPayloadStatusEnum.insufficientData: 'insufficient-data',
  CpaPayloadStatusEnum.noCoverage: 'no-coverage',
  CpaPayloadStatusEnum.degradedInput: 'degraded-input',
};
