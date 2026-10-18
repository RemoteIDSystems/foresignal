// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'felt_report_estimate_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FeltReportEstimatePayloadCWProxy {
  FeltReportEstimatePayload automated(bool automated);

  FeltReportEstimatePayload notAWarning(bool notAWarning);

  FeltReportEstimatePayload detector(DerivedPayloadCommonDetector detector);

  FeltReportEstimatePayload confidence(num? confidence);

  FeltReportEstimatePayload status(FeltReportEstimatePayloadStatusEnum status);

  FeltReportEstimatePayload evidence(List<DerivedEvidenceRef>? evidence);

  FeltReportEstimatePayload eventId(String eventId);

  FeltReportEstimatePayload placeId(String placeId);

  FeltReportEstimatePayload mmi(num mmi);

  FeltReportEstimatePayload distanceKm(num? distanceKm);

  FeltReportEstimatePayload feltLikely(bool? feltLikely);

  FeltReportEstimatePayload method(FeltReportEstimatePayloadMethodEnum method);

  FeltReportEstimatePayload coefficientSet(
    FeltReportEstimatePayloadCoefficientSetEnum? coefficientSet,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FeltReportEstimatePayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FeltReportEstimatePayload(...).copyWith(id: 12, name: "My name")
  /// ```
  FeltReportEstimatePayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    FeltReportEstimatePayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    String eventId,
    String placeId,
    num mmi,
    num? distanceKm,
    bool? feltLikely,
    FeltReportEstimatePayloadMethodEnum method,
    FeltReportEstimatePayloadCoefficientSetEnum? coefficientSet,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFeltReportEstimatePayload.copyWith(...)` or call `instanceOfFeltReportEstimatePayload.copyWith.fieldName(value)` for a single field.
class _$FeltReportEstimatePayloadCWProxyImpl
    implements _$FeltReportEstimatePayloadCWProxy {
  const _$FeltReportEstimatePayloadCWProxyImpl(this._value);

  final FeltReportEstimatePayload _value;

  @override
  FeltReportEstimatePayload automated(bool automated) =>
      call(automated: automated);

  @override
  FeltReportEstimatePayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  FeltReportEstimatePayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  FeltReportEstimatePayload confidence(num? confidence) =>
      call(confidence: confidence);

  @override
  FeltReportEstimatePayload status(
    FeltReportEstimatePayloadStatusEnum status,
  ) => call(status: status);

  @override
  FeltReportEstimatePayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  FeltReportEstimatePayload eventId(String eventId) => call(eventId: eventId);

  @override
  FeltReportEstimatePayload placeId(String placeId) => call(placeId: placeId);

  @override
  FeltReportEstimatePayload mmi(num mmi) => call(mmi: mmi);

  @override
  FeltReportEstimatePayload distanceKm(num? distanceKm) =>
      call(distanceKm: distanceKm);

  @override
  FeltReportEstimatePayload feltLikely(bool? feltLikely) =>
      call(feltLikely: feltLikely);

  @override
  FeltReportEstimatePayload method(
    FeltReportEstimatePayloadMethodEnum method,
  ) => call(method: method);

  @override
  FeltReportEstimatePayload coefficientSet(
    FeltReportEstimatePayloadCoefficientSetEnum? coefficientSet,
  ) => call(coefficientSet: coefficientSet);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FeltReportEstimatePayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FeltReportEstimatePayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FeltReportEstimatePayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? eventId = const $CopyWithPlaceholder(),
    Object? placeId = const $CopyWithPlaceholder(),
    Object? mmi = const $CopyWithPlaceholder(),
    Object? distanceKm = const $CopyWithPlaceholder(),
    Object? feltLikely = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? coefficientSet = const $CopyWithPlaceholder(),
  }) {
    return FeltReportEstimatePayload(
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
          : status as FeltReportEstimatePayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      eventId: eventId == const $CopyWithPlaceholder() || eventId == null
          ? _value.eventId
          // ignore: cast_nullable_to_non_nullable
          : eventId as String,
      placeId: placeId == const $CopyWithPlaceholder() || placeId == null
          ? _value.placeId
          // ignore: cast_nullable_to_non_nullable
          : placeId as String,
      mmi: mmi == const $CopyWithPlaceholder() || mmi == null
          ? _value.mmi
          // ignore: cast_nullable_to_non_nullable
          : mmi as num,
      distanceKm: distanceKm == const $CopyWithPlaceholder()
          ? _value.distanceKm
          // ignore: cast_nullable_to_non_nullable
          : distanceKm as num?,
      feltLikely: feltLikely == const $CopyWithPlaceholder()
          ? _value.feltLikely
          // ignore: cast_nullable_to_non_nullable
          : feltLikely as bool?,
      method: method == const $CopyWithPlaceholder() || method == null
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as FeltReportEstimatePayloadMethodEnum,
      coefficientSet: coefficientSet == const $CopyWithPlaceholder()
          ? _value.coefficientSet
          // ignore: cast_nullable_to_non_nullable
          : coefficientSet as FeltReportEstimatePayloadCoefficientSetEnum?,
    );
  }
}

extension $FeltReportEstimatePayloadCopyWith on FeltReportEstimatePayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFeltReportEstimatePayload.copyWith(...)` or `instanceOfFeltReportEstimatePayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FeltReportEstimatePayloadCWProxy get copyWith =>
      _$FeltReportEstimatePayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FeltReportEstimatePayload _$FeltReportEstimatePayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('FeltReportEstimatePayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'eventId',
      'placeId',
      'mmi',
      'method',
    ],
  );
  final val = FeltReportEstimatePayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$FeltReportEstimatePayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    eventId: $checkedConvert('eventId', (v) => v as String),
    placeId: $checkedConvert('placeId', (v) => v as String),
    mmi: $checkedConvert('mmi', (v) => v as num),
    distanceKm: $checkedConvert('distanceKm', (v) => v as num?),
    feltLikely: $checkedConvert('feltLikely', (v) => v as bool?),
    method: $checkedConvert(
      'method',
      (v) => $enumDecode(_$FeltReportEstimatePayloadMethodEnumEnumMap, v),
    ),
    coefficientSet: $checkedConvert(
      'coefficientSet',
      (v) => $enumDecodeNullable(
        _$FeltReportEstimatePayloadCoefficientSetEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$FeltReportEstimatePayloadToJson(
  FeltReportEstimatePayload instance,
) => <String, dynamic>{
  'automated': instance.automated,
  'notAWarning': instance.notAWarning,
  'detector': instance.detector.toJson(),
  'confidence': ?instance.confidence,
  'status': _$FeltReportEstimatePayloadStatusEnumEnumMap[instance.status]!,
  'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
  'eventId': instance.eventId,
  'placeId': instance.placeId,
  'mmi': instance.mmi,
  'distanceKm': ?instance.distanceKm,
  'feltLikely': ?instance.feltLikely,
  'method': _$FeltReportEstimatePayloadMethodEnumEnumMap[instance.method]!,
  'coefficientSet':
      ?_$FeltReportEstimatePayloadCoefficientSetEnumEnumMap[instance
          .coefficientSet],
};

const _$FeltReportEstimatePayloadStatusEnumEnumMap = {
  FeltReportEstimatePayloadStatusEnum.ok: 'ok',
  FeltReportEstimatePayloadStatusEnum.insufficientData: 'insufficient-data',
  FeltReportEstimatePayloadStatusEnum.noCoverage: 'no-coverage',
  FeltReportEstimatePayloadStatusEnum.degradedInput: 'degraded-input',
};

const _$FeltReportEstimatePayloadMethodEnumEnumMap = {
  FeltReportEstimatePayloadMethodEnum.shakemap: 'shakemap',
  FeltReportEstimatePayloadMethodEnum.ipeAw2007: 'ipe-aw2007',
};

const _$FeltReportEstimatePayloadCoefficientSetEnumEnumMap = {
  FeltReportEstimatePayloadCoefficientSetEnum.california: 'california',
  FeltReportEstimatePayloadCoefficientSetEnum.ceus: 'ceus',
};
