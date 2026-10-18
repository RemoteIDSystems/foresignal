// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ais_gap_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AisGapPayloadCWProxy {
  AisGapPayload automated(bool automated);

  AisGapPayload notAWarning(bool notAWarning);

  AisGapPayload detector(DerivedPayloadCommonDetector detector);

  AisGapPayload confidence(num? confidence);

  AisGapPayload status(AisGapPayloadStatusEnum status);

  AisGapPayload evidence(List<DerivedEvidenceRef>? evidence);

  AisGapPayload mmsi(String mmsi);

  AisGapPayload entityId(String? entityId);

  AisGapPayload gapStart(DateTime gapStart);

  AisGapPayload lastSog(num? lastSog);

  AisGapPayload lastCog(num? lastCog);

  AisGapPayload open(bool open);

  AisGapPayload gapEnd(DateTime? gapEnd);

  AisGapPayload gapMinutes(num? gapMinutes);

  AisGapPayload displacementKm(num? displacementKm);

  AisGapPayload impliedKn(num? impliedKn);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AisGapPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AisGapPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  AisGapPayload call({
    bool automated,
    bool notAWarning,
    DerivedPayloadCommonDetector detector,
    num? confidence,
    AisGapPayloadStatusEnum status,
    List<DerivedEvidenceRef>? evidence,
    String mmsi,
    String? entityId,
    DateTime gapStart,
    num? lastSog,
    num? lastCog,
    bool open,
    DateTime? gapEnd,
    num? gapMinutes,
    num? displacementKm,
    num? impliedKn,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAisGapPayload.copyWith(...)` or call `instanceOfAisGapPayload.copyWith.fieldName(value)` for a single field.
class _$AisGapPayloadCWProxyImpl implements _$AisGapPayloadCWProxy {
  const _$AisGapPayloadCWProxyImpl(this._value);

  final AisGapPayload _value;

  @override
  AisGapPayload automated(bool automated) => call(automated: automated);

  @override
  AisGapPayload notAWarning(bool notAWarning) => call(notAWarning: notAWarning);

  @override
  AisGapPayload detector(DerivedPayloadCommonDetector detector) =>
      call(detector: detector);

  @override
  AisGapPayload confidence(num? confidence) => call(confidence: confidence);

  @override
  AisGapPayload status(AisGapPayloadStatusEnum status) => call(status: status);

  @override
  AisGapPayload evidence(List<DerivedEvidenceRef>? evidence) =>
      call(evidence: evidence);

  @override
  AisGapPayload mmsi(String mmsi) => call(mmsi: mmsi);

  @override
  AisGapPayload entityId(String? entityId) => call(entityId: entityId);

  @override
  AisGapPayload gapStart(DateTime gapStart) => call(gapStart: gapStart);

  @override
  AisGapPayload lastSog(num? lastSog) => call(lastSog: lastSog);

  @override
  AisGapPayload lastCog(num? lastCog) => call(lastCog: lastCog);

  @override
  AisGapPayload open(bool open) => call(open: open);

  @override
  AisGapPayload gapEnd(DateTime? gapEnd) => call(gapEnd: gapEnd);

  @override
  AisGapPayload gapMinutes(num? gapMinutes) => call(gapMinutes: gapMinutes);

  @override
  AisGapPayload displacementKm(num? displacementKm) =>
      call(displacementKm: displacementKm);

  @override
  AisGapPayload impliedKn(num? impliedKn) => call(impliedKn: impliedKn);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AisGapPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AisGapPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AisGapPayload call({
    Object? automated = const $CopyWithPlaceholder(),
    Object? notAWarning = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? confidence = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? evidence = const $CopyWithPlaceholder(),
    Object? mmsi = const $CopyWithPlaceholder(),
    Object? entityId = const $CopyWithPlaceholder(),
    Object? gapStart = const $CopyWithPlaceholder(),
    Object? lastSog = const $CopyWithPlaceholder(),
    Object? lastCog = const $CopyWithPlaceholder(),
    Object? open = const $CopyWithPlaceholder(),
    Object? gapEnd = const $CopyWithPlaceholder(),
    Object? gapMinutes = const $CopyWithPlaceholder(),
    Object? displacementKm = const $CopyWithPlaceholder(),
    Object? impliedKn = const $CopyWithPlaceholder(),
  }) {
    return AisGapPayload(
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
          : status as AisGapPayloadStatusEnum,
      evidence: evidence == const $CopyWithPlaceholder()
          ? _value.evidence
          // ignore: cast_nullable_to_non_nullable
          : evidence as List<DerivedEvidenceRef>?,
      mmsi: mmsi == const $CopyWithPlaceholder() || mmsi == null
          ? _value.mmsi
          // ignore: cast_nullable_to_non_nullable
          : mmsi as String,
      entityId: entityId == const $CopyWithPlaceholder()
          ? _value.entityId
          // ignore: cast_nullable_to_non_nullable
          : entityId as String?,
      gapStart: gapStart == const $CopyWithPlaceholder() || gapStart == null
          ? _value.gapStart
          // ignore: cast_nullable_to_non_nullable
          : gapStart as DateTime,
      lastSog: lastSog == const $CopyWithPlaceholder()
          ? _value.lastSog
          // ignore: cast_nullable_to_non_nullable
          : lastSog as num?,
      lastCog: lastCog == const $CopyWithPlaceholder()
          ? _value.lastCog
          // ignore: cast_nullable_to_non_nullable
          : lastCog as num?,
      open: open == const $CopyWithPlaceholder() || open == null
          ? _value.open
          // ignore: cast_nullable_to_non_nullable
          : open as bool,
      gapEnd: gapEnd == const $CopyWithPlaceholder()
          ? _value.gapEnd
          // ignore: cast_nullable_to_non_nullable
          : gapEnd as DateTime?,
      gapMinutes: gapMinutes == const $CopyWithPlaceholder()
          ? _value.gapMinutes
          // ignore: cast_nullable_to_non_nullable
          : gapMinutes as num?,
      displacementKm: displacementKm == const $CopyWithPlaceholder()
          ? _value.displacementKm
          // ignore: cast_nullable_to_non_nullable
          : displacementKm as num?,
      impliedKn: impliedKn == const $CopyWithPlaceholder()
          ? _value.impliedKn
          // ignore: cast_nullable_to_non_nullable
          : impliedKn as num?,
    );
  }
}

extension $AisGapPayloadCopyWith on AisGapPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAisGapPayload.copyWith(...)` or `instanceOfAisGapPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AisGapPayloadCWProxy get copyWith => _$AisGapPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AisGapPayload _$AisGapPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AisGapPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'automated',
      'notAWarning',
      'detector',
      'status',
      'mmsi',
      'gapStart',
      'open',
    ],
  );
  final val = AisGapPayload(
    automated: $checkedConvert('automated', (v) => v as bool),
    notAWarning: $checkedConvert('notAWarning', (v) => v as bool),
    detector: $checkedConvert(
      'detector',
      (v) => DerivedPayloadCommonDetector.fromJson(v as Map<String, dynamic>),
    ),
    confidence: $checkedConvert('confidence', (v) => v as num?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$AisGapPayloadStatusEnumEnumMap, v),
    ),
    evidence: $checkedConvert(
      'evidence',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DerivedEvidenceRef.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    mmsi: $checkedConvert('mmsi', (v) => v as String),
    entityId: $checkedConvert('entityId', (v) => v as String?),
    gapStart: $checkedConvert('gapStart', (v) => DateTime.parse(v as String)),
    lastSog: $checkedConvert('lastSog', (v) => v as num?),
    lastCog: $checkedConvert('lastCog', (v) => v as num?),
    open: $checkedConvert('open', (v) => v as bool),
    gapEnd: $checkedConvert(
      'gapEnd',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    gapMinutes: $checkedConvert('gapMinutes', (v) => v as num?),
    displacementKm: $checkedConvert('displacementKm', (v) => v as num?),
    impliedKn: $checkedConvert('impliedKn', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$AisGapPayloadToJson(AisGapPayload instance) =>
    <String, dynamic>{
      'automated': instance.automated,
      'notAWarning': instance.notAWarning,
      'detector': instance.detector.toJson(),
      'confidence': ?instance.confidence,
      'status': _$AisGapPayloadStatusEnumEnumMap[instance.status]!,
      'evidence': ?instance.evidence?.map((e) => e.toJson()).toList(),
      'mmsi': instance.mmsi,
      'entityId': ?instance.entityId,
      'gapStart': instance.gapStart.toIso8601String(),
      'lastSog': ?instance.lastSog,
      'lastCog': ?instance.lastCog,
      'open': instance.open,
      'gapEnd': ?instance.gapEnd?.toIso8601String(),
      'gapMinutes': ?instance.gapMinutes,
      'displacementKm': ?instance.displacementKm,
      'impliedKn': ?instance.impliedKn,
    };

const _$AisGapPayloadStatusEnumEnumMap = {
  AisGapPayloadStatusEnum.ok: 'ok',
  AisGapPayloadStatusEnum.insufficientData: 'insufficient-data',
  AisGapPayloadStatusEnum.noCoverage: 'no-coverage',
  AisGapPayloadStatusEnum.degradedInput: 'degraded-input',
};
