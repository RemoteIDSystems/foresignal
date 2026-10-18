// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'aircraft_fusion_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AircraftFusionPayloadCWProxy {
  AircraftFusionPayload automated(bool automated);

  AircraftFusionPayload notAWarning(bool notAWarning);

  AircraftFusionPayload detector(DerivedPayloadCommonDetector detector);

  AircraftFusionPayload confidence(num? confidence);

  AircraftFusionPayload status(AircraftFusionPayloadStatusEnum status);

  AircraftFusionPayload evidence(List<DerivedEvidenceRef>? evidence);

  AircraftFusionPayload hex(String hex);

  AircraftFusionPayload sources(List<String> sources);

  AircraftFusionPayload primarySource(String primarySource);

  AircraftFusionPayload positionSource(
    AircraftFusionPayloadPositionSourceEnum? positionSource,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AircraftFusionPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AircraftFusionPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  AircraftFusionPayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    AircraftFusionPayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    String hex,
    List<String> sources,
    String primarySource,
    AircraftFusionPayloadPositionSourceEnum? positionSource,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAircraftFusionPayload.copyWith(...)` or call `instanceOfAircraftFusionPayload.copyWith.fieldName(value)` for a single field.
class _$AircraftFusionPayloadCWProxyImpl
    implements _$AircraftFusionPayloadCWProxy {
  const _$AircraftFusionPayloadCWProxyImpl(this._value);

  final AircraftFusionPayload _value;

  @override
  AircraftFusionPayload automated(bool automated) => call(automated: automated);

  @override
  AircraftFusionPayload notAWarning(bool notAWarning) =>
      call(notAWarning: notAWarning);

  @override
  AircraftFusionPayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  AircraftFusionPayload confidence(num? confidence) =>
      call(confidence: confidence);

  @override
  AircraftFusionPayload status(AircraftFusionPayloadStatusEnum status) =>
      call(status: status);

  @override
  AircraftFusionPayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  AircraftFusionPayload hex(String hex) => call(hex: hex);

  @override
  AircraftFusionPayload sources(List<String> sources) => call(sources: sources);

  @override
  AircraftFusionPayload primarySource(String primarySource) =>
      call(primarySource: primarySource);

  @override
  AircraftFusionPayload positionSource(
    AircraftFusionPayloadPositionSourceEnum? positionSource,
  ) => call(positionSource: positionSource);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AircraftFusionPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AircraftFusionPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AircraftFusionPayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? hex = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? primarySource = const $CopyWithPlaceholder(),
    Object? positionSource = const $CopyWithPlaceholder(),
  }) {
    return AircraftFusionPayload(
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
          : status as AircraftFusionPayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      hex: hex == const $CopyWithPlaceholder() || hex == null
          ? _value.hex
          // ignore: cast_nullable_to_non_nullable
          : hex as String,
      sources: sources == const $CopyWithPlaceholder() || sources == null
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as List<String>,
      primarySource:
          primarySource == const $CopyWithPlaceholder() || primarySource == null
          ? _value.primarySource
          // ignore: cast_nullable_to_non_nullable
          : primarySource as String,
      positionSource: positionSource == const $CopyWithPlaceholder()
          ? _value.positionSource
          // ignore: cast_nullable_to_non_nullable
          : positionSource as AircraftFusionPayloadPositionSourceEnum?,
    );
  }
}

extension $AircraftFusionPayloadCopyWith on AircraftFusionPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAircraftFusionPayload.copyWith(...)` or `instanceOfAircraftFusionPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AircraftFusionPayloadCWProxy get copyWith =>
      _$AircraftFusionPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AircraftFusionPayload _$AircraftFusionPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AircraftFusionPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'hex',
      'sources',
      'primarySource',
    ],
  );
  final val = AircraftFusionPayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$AircraftFusionPayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    hex: $checkedConvert('hex', (v) => v as String),
    sources: $checkedConvert(
      'sources',
      (v) => (v as List<dynamic>).map((e) => e as String).toList(),
    ),
    primarySource: $checkedConvert('primarySource', (v) => v as String),
    positionSource: $checkedConvert(
      'positionSource',
      (v) => $enumDecodeNullable(
        _$AircraftFusionPayloadPositionSourceEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$AircraftFusionPayloadToJson(
  AircraftFusionPayload instance,
) => <String, dynamic>{
  'automated': instance.automated,
  'notAWarning': instance.notAWarning,
  'detector': instance.detector.toJson(),
  'confidence': ?instance.confidence,
  'status': _$AircraftFusionPayloadStatusEnumEnumMap[instance.status]!,
  'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
  'hex': instance.hex,
  'sources': instance.sources,
  'primarySource': instance.primarySource,
  'positionSource':
      ?_$AircraftFusionPayloadPositionSourceEnumEnumMap[instance
          .positionSource],
};

const _$AircraftFusionPayloadStatusEnumEnumMap = {
  AircraftFusionPayloadStatusEnum.ok: 'ok',
  AircraftFusionPayloadStatusEnum.insufficientData: 'insufficient-data',
  AircraftFusionPayloadStatusEnum.noCoverage: 'no-coverage',
  AircraftFusionPayloadStatusEnum.degradedInput: 'degraded-input',
};

const _$AircraftFusionPayloadPositionSourceEnumEnumMap = {
  AircraftFusionPayloadPositionSourceEnum.adsb: 'adsb',
  AircraftFusionPayloadPositionSourceEnum.mlat: 'mlat',
  AircraftFusionPayloadPositionSourceEnum.tisb: 'tisb',
  AircraftFusionPayloadPositionSourceEnum.other: 'other',
};
