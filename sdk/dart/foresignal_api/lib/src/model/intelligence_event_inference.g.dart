// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intelligence_event_inference.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelligenceEventInferenceCWProxy {
  IntelligenceEventInference confidence(num confidence);

  IntelligenceEventInference confidenceLabel(
    IntelligenceEventInferenceConfidenceLabelEnum confidenceLabel,
  );

  IntelligenceEventInference confidenceMethod(String confidenceMethod);

  IntelligenceEventInference calibrated(bool calibrated);

  IntelligenceEventInference evidenceQuality(
    IntelligenceEventInferenceEvidenceQualityEnum evidenceQuality,
  );

  IntelligenceEventInference startTime(DateTime startTime);

  IntelligenceEventInference endTime(DateTime? endTime);

  IntelligenceEventInference lastEvidenceAt(DateTime? lastEvidenceAt);

  IntelligenceEventInference geometry(Geometry? geometry);

  IntelligenceEventInference centroid(Geometry? centroid);

  IntelligenceEventInference regionTags(List<String>? regionTags);

  IntelligenceEventInference explanation(String explanation);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelligenceEventInference(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelligenceEventInference(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelligenceEventInference call({
    num confidence,
    IntelligenceEventInferenceConfidenceLabelEnum confidenceLabel,
    String confidenceMethod,
    bool calibrated,
    IntelligenceEventInferenceEvidenceQualityEnum evidenceQuality,
    DateTime startTime,
    DateTime? endTime,
    DateTime? lastEvidenceAt,
    Geometry? geometry,
    Geometry? centroid,
    List<String>? regionTags,
    String explanation,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelligenceEventInference.copyWith(...)` or call `instanceOfIntelligenceEventInference.copyWith.fieldName(value)` for a single field.
class _$IntelligenceEventInferenceCWProxyImpl
    implements _$IntelligenceEventInferenceCWProxy {
  const _$IntelligenceEventInferenceCWProxyImpl(this._value);

  final IntelligenceEventInference _value;

  @override
  IntelligenceEventInference confidence(num confidence) =>
      call(confidence: confidence);

  @override
  IntelligenceEventInference confidenceLabel(
    IntelligenceEventInferenceConfidenceLabelEnum confidenceLabel,
  ) => call(confidenceLabel: confidenceLabel);

  @override
  IntelligenceEventInference confidenceMethod(String confidenceMethod) =>
      call(confidenceMethod: confidenceMethod);

  @override
  IntelligenceEventInference calibrated(bool calibrated) =>
      call(calibrated: calibrated);

  @override
  IntelligenceEventInference evidenceQuality(
    IntelligenceEventInferenceEvidenceQualityEnum evidenceQuality,
  ) => call(evidenceQuality: evidenceQuality);

  @override
  IntelligenceEventInference startTime(DateTime startTime) =>
      call(startTime: startTime);

  @override
  IntelligenceEventInference endTime(DateTime? endTime) =>
      call(endTime: endTime);

  @override
  IntelligenceEventInference lastEvidenceAt(DateTime? lastEvidenceAt) =>
      call(lastEvidenceAt: lastEvidenceAt);

  @override
  IntelligenceEventInference geometry(Geometry? geometry) =>
      call(geometry: geometry);

  @override
  IntelligenceEventInference centroid(Geometry? centroid) =>
      call(centroid: centroid);

  @override
  IntelligenceEventInference regionTags(List<String>? regionTags) =>
      call(regionTags: regionTags);

  @override
  IntelligenceEventInference explanation(String explanation) =>
      call(explanation: explanation);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelligenceEventInference(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelligenceEventInference(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelligenceEventInference call({
    Object? confidence = const $CopyWithPlaceholder(),
    Object? confidenceLabel = const $CopyWithPlaceholder(),
    Object? confidenceMethod = const $CopyWithPlaceholder(),
    Object? calibrated = const $CopyWithPlaceholder(),
    Object? evidenceQuality = const $CopyWithPlaceholder(),
    Object? startTime = const $CopyWithPlaceholder(),
    Object? endTime = const $CopyWithPlaceholder(),
    Object? lastEvidenceAt = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? centroid = const $CopyWithPlaceholder(),
    Object? regionTags = const $CopyWithPlaceholder(),
    Object? explanation = const $CopyWithPlaceholder(),
  }) {
    return IntelligenceEventInference(
      confidence:
          confidence == const $CopyWithPlaceholder() || confidence == null
          ? _value.confidence
          // ignore: cast_nullable_to_non_nullable
          : confidence as num,
      confidenceLabel:
          confidenceLabel == const $CopyWithPlaceholder() ||
              confidenceLabel == null
          ? _value.confidenceLabel
          // ignore: cast_nullable_to_non_nullable
          : confidenceLabel as IntelligenceEventInferenceConfidenceLabelEnum,
      confidenceMethod:
          confidenceMethod == const $CopyWithPlaceholder() ||
              confidenceMethod == null
          ? _value.confidenceMethod
          // ignore: cast_nullable_to_non_nullable
          : confidenceMethod as String,
      calibrated:
          calibrated == const $CopyWithPlaceholder() || calibrated == null
          ? _value.calibrated
          // ignore: cast_nullable_to_non_nullable
          : calibrated as bool,
      evidenceQuality:
          evidenceQuality == const $CopyWithPlaceholder() ||
              evidenceQuality == null
          ? _value.evidenceQuality
          // ignore: cast_nullable_to_non_nullable
          : evidenceQuality as IntelligenceEventInferenceEvidenceQualityEnum,
      startTime: startTime == const $CopyWithPlaceholder() || startTime == null
          ? _value.startTime
          // ignore: cast_nullable_to_non_nullable
          : startTime as DateTime,
      endTime: endTime == const $CopyWithPlaceholder()
          ? _value.endTime
          // ignore: cast_nullable_to_non_nullable
          : endTime as DateTime?,
      lastEvidenceAt: lastEvidenceAt == const $CopyWithPlaceholder()
          ? _value.lastEvidenceAt
          // ignore: cast_nullable_to_non_nullable
          : lastEvidenceAt as DateTime?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      centroid: centroid == const $CopyWithPlaceholder()
          ? _value.centroid
          // ignore: cast_nullable_to_non_nullable
          : centroid as Geometry?,
      regionTags: regionTags == const $CopyWithPlaceholder()
          ? _value.regionTags
          // ignore: cast_nullable_to_non_nullable
          : regionTags as List<String>?,
      explanation:
          explanation == const $CopyWithPlaceholder() || explanation == null
          ? _value.explanation
          // ignore: cast_nullable_to_non_nullable
          : explanation as String,
    );
  }
}

extension $IntelligenceEventInferenceCopyWith on IntelligenceEventInference {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelligenceEventInference.copyWith(...)` or `instanceOfIntelligenceEventInference.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelligenceEventInferenceCWProxy get copyWith =>
      _$IntelligenceEventInferenceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelligenceEventInference _$IntelligenceEventInferenceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelligenceEventInference', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'confidence',
      'confidenceLabel',
      'confidenceMethod',
      'calibrated',
      'evidenceQuality',
      'startTime',
      'explanation',
    ],
  );
  final val = IntelligenceEventInference(
    confidence: $checkedConvert('confidence', (v) => v as num),
    confidenceLabel: $checkedConvert(
      'confidenceLabel',
      (v) => $enumDecode(
        _$IntelligenceEventInferenceConfidenceLabelEnumEnumMap,
        v,
      ),
    ),
    confidenceMethod: $checkedConvert('confidenceMethod', (v) => v as String),
    calibrated: $checkedConvert('calibrated', (v) => v as bool),
    evidenceQuality: $checkedConvert(
      'evidenceQuality',
      (v) => $enumDecode(
        _$IntelligenceEventInferenceEvidenceQualityEnumEnumMap,
        v,
      ),
    ),
    startTime: $checkedConvert('startTime', (v) => DateTime.parse(v as String)),
    endTime: $checkedConvert(
      'endTime',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastEvidenceAt: $checkedConvert(
      'lastEvidenceAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    geometry: $checkedConvert(
      'geometry',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    centroid: $checkedConvert(
      'centroid',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    regionTags: $checkedConvert(
      'regionTags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    explanation: $checkedConvert('explanation', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$IntelligenceEventInferenceToJson(
  IntelligenceEventInference instance,
) => <String, dynamic>{
  'confidence': instance.confidence,
  'confidenceLabel':
      _$IntelligenceEventInferenceConfidenceLabelEnumEnumMap[instance
          .confidenceLabel]!,
  'confidenceMethod': instance.confidenceMethod,
  'calibrated': instance.calibrated,
  'evidenceQuality':
      _$IntelligenceEventInferenceEvidenceQualityEnumEnumMap[instance
          .evidenceQuality]!,
  'startTime': instance.startTime.toIso8601String(),
  'endTime': ?instance.endTime?.toIso8601String(),
  'lastEvidenceAt': ?instance.lastEvidenceAt?.toIso8601String(),
  'geometry': ?instance.geometry?.toJson(),
  'centroid': ?instance.centroid?.toJson(),
  'regionTags': ?instance.regionTags,
  'explanation': instance.explanation,
};

const _$IntelligenceEventInferenceConfidenceLabelEnumEnumMap = {
  IntelligenceEventInferenceConfidenceLabelEnum.low: 'low',
  IntelligenceEventInferenceConfidenceLabelEnum.moderate: 'moderate',
  IntelligenceEventInferenceConfidenceLabelEnum.high: 'high',
};

const _$IntelligenceEventInferenceEvidenceQualityEnumEnumMap = {
  IntelligenceEventInferenceEvidenceQualityEnum.strong: 'strong',
  IntelligenceEventInferenceEvidenceQualityEnum.moderate: 'moderate',
  IntelligenceEventInferenceEvidenceQualityEnum.weak: 'weak',
  IntelligenceEventInferenceEvidenceQualityEnum.insufficient: 'insufficient',
};
