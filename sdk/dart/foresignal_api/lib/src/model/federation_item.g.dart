// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'federation_item.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FederationItemCWProxy {
  FederationItem seq(int seq);

  FederationItem ref(String ref);

  FederationItem sourceId(String sourceId);

  FederationItem externalId(String externalId);

  FederationItem kind(String kind);

  FederationItem version(int version);

  FederationItem persistReason(FederationItemPersistReasonEnum? persistReason);

  FederationItem historyTier(FederationItemHistoryTierEnum? historyTier);

  FederationItem observedAt(DateTime observedAt);

  FederationItem eventTime(DateTime? eventTime);

  FederationItem validUntil(DateTime? validUntil);

  FederationItem tombstone(bool tombstone);

  FederationItem geometry(Geometry? geometry);

  FederationItem payload(Map<String, Object> payload);

  FederationItem quality(Quality? quality);

  FederationItem licence(String licence);

  FederationItem provenance(Provenance provenance);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FederationItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FederationItem(...).copyWith(id: 12, name: "My name")
  /// ```
  FederationItem call({
    int seq,
    String ref,
    String sourceId,
    String externalId,
    String kind,
    int version,
    FederationItemPersistReasonEnum? persistReason,
    FederationItemHistoryTierEnum? historyTier,
    DateTime observedAt,
    DateTime? eventTime,
    DateTime? validUntil,
    bool tombstone,
    Geometry? geometry,
    Map<String, Object> payload,
    Quality? quality,
    String licence,
    Provenance provenance,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFederationItem.copyWith(...)` or call `instanceOfFederationItem.copyWith.fieldName(value)` for a single field.
class _$FederationItemCWProxyImpl implements _$FederationItemCWProxy {
  const _$FederationItemCWProxyImpl(this._value);

  final FederationItem _value;

  @override
  FederationItem seq(int seq) => call(seq: seq);

  @override
  FederationItem ref(String ref) => call(ref: ref);

  @override
  FederationItem sourceId(String sourceId) => call(sourceId: sourceId);

  @override
  FederationItem externalId(String externalId) => call(externalId: externalId);

  @override
  FederationItem kind(String kind) => call(kind: kind);

  @override
  FederationItem version(int version) => call(version: version);

  @override
  FederationItem persistReason(
    FederationItemPersistReasonEnum? persistReason,
  ) => call(persistReason: persistReason);

  @override
  FederationItem historyTier(FederationItemHistoryTierEnum? historyTier) =>
      call(historyTier: historyTier);

  @override
  FederationItem observedAt(DateTime observedAt) =>
      call(observedAt: observedAt);

  @override
  FederationItem eventTime(DateTime? eventTime) => call(eventTime: eventTime);

  @override
  FederationItem validUntil(DateTime? validUntil) =>
      call(validUntil: validUntil);

  @override
  FederationItem tombstone(bool tombstone) => call(tombstone: tombstone);

  @override
  FederationItem geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  FederationItem payload(Map<String, Object> payload) => call(payload: payload);

  @override
  FederationItem quality(Quality? quality) => call(quality: quality);

  @override
  FederationItem licence(String licence) => call(licence: licence);

  @override
  FederationItem provenance(Provenance provenance) =>
      call(provenance: provenance);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FederationItem(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FederationItem(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FederationItem call({
    Object? seq = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? sourceId = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? persistReason = const $CopyWithPlaceholder(),
    Object? historyTier = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? eventTime = const $CopyWithPlaceholder(),
    Object? validUntil = const $CopyWithPlaceholder(),
    Object? tombstone = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? payload = const $CopyWithPlaceholder(),
    Object? quality = const $CopyWithPlaceholder(),
    Object? licence = const $CopyWithPlaceholder(),
    Object? provenance = const $CopyWithPlaceholder(),
  }) {
    return FederationItem(
      seq: seq == const $CopyWithPlaceholder() || seq == null
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      sourceId: sourceId == const $CopyWithPlaceholder() || sourceId == null
          ? _value.sourceId
          // ignore: cast_nullable_to_non_nullable
          : sourceId as String,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as String,
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      persistReason: persistReason == const $CopyWithPlaceholder()
          ? _value.persistReason
          // ignore: cast_nullable_to_non_nullable
          : persistReason as FederationItemPersistReasonEnum?,
      historyTier: historyTier == const $CopyWithPlaceholder()
          ? _value.historyTier
          // ignore: cast_nullable_to_non_nullable
          : historyTier as FederationItemHistoryTierEnum?,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
      eventTime: eventTime == const $CopyWithPlaceholder()
          ? _value.eventTime
          // ignore: cast_nullable_to_non_nullable
          : eventTime as DateTime?,
      validUntil: validUntil == const $CopyWithPlaceholder()
          ? _value.validUntil
          // ignore: cast_nullable_to_non_nullable
          : validUntil as DateTime?,
      tombstone: tombstone == const $CopyWithPlaceholder() || tombstone == null
          ? _value.tombstone
          // ignore: cast_nullable_to_non_nullable
          : tombstone as bool,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      payload: payload == const $CopyWithPlaceholder() || payload == null
          ? _value.payload
          // ignore: cast_nullable_to_non_nullable
          : payload as Map<String, Object>,
      quality: quality == const $CopyWithPlaceholder()
          ? _value.quality
          // ignore: cast_nullable_to_non_nullable
          : quality as Quality?,
      licence: licence == const $CopyWithPlaceholder() || licence == null
          ? _value.licence
          // ignore: cast_nullable_to_non_nullable
          : licence as String,
      provenance:
          provenance == const $CopyWithPlaceholder() || provenance == null
          ? _value.provenance
          // ignore: cast_nullable_to_non_nullable
          : provenance as Provenance,
    );
  }
}

extension $FederationItemCopyWith on FederationItem {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFederationItem.copyWith(...)` or `instanceOfFederationItem.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FederationItemCWProxy get copyWith => _$FederationItemCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FederationItem _$FederationItemFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FederationItem', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'seq',
          'ref',
          'sourceId',
          'externalId',
          'kind',
          'version',
          'observedAt',
          'tombstone',
          'payload',
          'licence',
          'provenance',
        ],
      );
      final val = FederationItem(
        seq: $checkedConvert('seq', (v) => (v as num).toInt()),
        ref: $checkedConvert('ref', (v) => v as String),
        sourceId: $checkedConvert('sourceId', (v) => v as String),
        externalId: $checkedConvert('externalId', (v) => v as String),
        kind: $checkedConvert('kind', (v) => v as String),
        version: $checkedConvert('version', (v) => (v as num).toInt()),
        persistReason: $checkedConvert(
          'persistReason',
          (v) =>
              $enumDecodeNullable(_$FederationItemPersistReasonEnumEnumMap, v),
        ),
        historyTier: $checkedConvert(
          'historyTier',
          (v) => $enumDecodeNullable(_$FederationItemHistoryTierEnumEnumMap, v),
        ),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => DateTime.parse(v as String),
        ),
        eventTime: $checkedConvert(
          'eventTime',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        validUntil: $checkedConvert(
          'validUntil',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        tombstone: $checkedConvert('tombstone', (v) => v as bool),
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        payload: $checkedConvert(
          'payload',
          (v) => (v as Map<String, dynamic>).map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        quality: $checkedConvert(
          'quality',
          (v) => v == null ? null : Quality.fromJson(v as Map<String, dynamic>),
        ),
        licence: $checkedConvert('licence', (v) => v as String),
        provenance: $checkedConvert(
          'provenance',
          (v) => Provenance.fromJson(v as Map<String, dynamic>),
        ),
      );
      return val;
    });

Map<String, dynamic> _$FederationItemToJson(
  FederationItem instance,
) => <String, dynamic>{
  'seq': instance.seq,
  'ref': instance.ref,
  'sourceId': instance.sourceId,
  'externalId': instance.externalId,
  'kind': instance.kind,
  'version': instance.version,
  'persistReason':
      ?_$FederationItemPersistReasonEnumEnumMap[instance.persistReason],
  'historyTier': ?_$FederationItemHistoryTierEnumEnumMap[instance.historyTier],
  'observedAt': instance.observedAt.toIso8601String(),
  'eventTime': ?instance.eventTime?.toIso8601String(),
  'validUntil': ?instance.validUntil?.toIso8601String(),
  'tombstone': instance.tombstone,
  'geometry': ?instance.geometry?.toJson(),
  'payload': instance.payload,
  'quality': ?instance.quality?.toJson(),
  'licence': instance.licence,
  'provenance': instance.provenance.toJson(),
};

const _$FederationItemPersistReasonEnumEnumMap = {
  FederationItemPersistReasonEnum.FIRST: 'FIRST',
  FederationItemPersistReasonEnum.REAPPEAR: 'REAPPEAR',
  FederationItemPersistReasonEnum.SIGNIFICANT: 'SIGNIFICANT',
  FederationItemPersistReasonEnum.TOMBSTONE: 'TOMBSTONE',
  FederationItemPersistReasonEnum.CHANGE: 'CHANGE',
  FederationItemPersistReasonEnum.SAMPLE: 'SAMPLE',
  FederationItemPersistReasonEnum.KEYFRAME: 'KEYFRAME',
  FederationItemPersistReasonEnum.HEARTBEAT: 'HEARTBEAT',
  FederationItemPersistReasonEnum.TRIGGER_EDGE: 'TRIGGER_EDGE',
  FederationItemPersistReasonEnum.SNAPSHOT: 'SNAPSHOT',
};

const _$FederationItemHistoryTierEnumEnumMap = {
  FederationItemHistoryTierEnum.STANDARD: 'STANDARD',
  FederationItemHistoryTierEnum.TRACK: 'TRACK',
};
