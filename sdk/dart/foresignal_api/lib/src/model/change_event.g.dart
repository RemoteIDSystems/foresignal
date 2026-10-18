// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'change_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ChangeEventCWProxy {
  ChangeEvent seq(int seq);

  ChangeEvent source_(String source_);

  ChangeEvent externalId(String externalId);

  ChangeEvent kind(String kind);

  ChangeEvent version(int version);

  ChangeEvent observedAt(DateTime observedAt);

  ChangeEvent tombstone(bool tombstone);

  ChangeEvent geometry(Geometry? geometry);

  ChangeEvent payload(Map<String, Object>? payload);

  ChangeEvent prevSeq(int? prevSeq);

  ChangeEvent historyTier(HistoryTier? historyTier);

  ChangeEvent persistReason(PersistReason? persistReason);

  ChangeEvent eventTime(DateTime? eventTime);

  ChangeEvent validUntil(DateTime? validUntil);

  ChangeEvent qaFlags(int? qaFlags);

  ChangeEvent bbox(List<num>? bbox);

  ChangeEvent prevPersistedBbox(List<num>? prevPersistedBbox);

  ChangeEvent prevLiveBbox(List<num>? prevLiveBbox);

  ChangeEvent leftFilter(bool? leftFilter);

  ChangeEvent purged(bool? purged);

  ChangeEvent validFrom(DateTime? validFrom);

  ChangeEvent globalRef(String? globalRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChangeEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChangeEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  ChangeEvent call({
    int seq,
    String source_,
    String externalId,
    String kind,
    int version,
    DateTime observedAt,
    bool tombstone,
    Geometry? geometry,
    Map<String, Object>? payload,
    int? prevSeq,
    HistoryTier? historyTier,
    PersistReason? persistReason,
    DateTime? eventTime,
    DateTime? validUntil,
    int? qaFlags,
    List<num>? bbox,
    List<num>? prevPersistedBbox,
    List<num>? prevLiveBbox,
    bool? leftFilter,
    bool? purged,
    DateTime? validFrom,
    String? globalRef,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfChangeEvent.copyWith(...)` or call `instanceOfChangeEvent.copyWith.fieldName(value)` for a single field.
class _$ChangeEventCWProxyImpl implements _$ChangeEventCWProxy {
  const _$ChangeEventCWProxyImpl(this._value);

  final ChangeEvent _value;

  @override
  ChangeEvent seq(int seq) => call(seq: seq);

  @override
  ChangeEvent source_(String source_) => call(source_: source_);

  @override
  ChangeEvent externalId(String externalId) => call(externalId: externalId);

  @override
  ChangeEvent kind(String kind) => call(kind: kind);

  @override
  ChangeEvent version(int version) => call(version: version);

  @override
  ChangeEvent observedAt(DateTime observedAt) => call(observedAt: observedAt);

  @override
  ChangeEvent tombstone(bool tombstone) => call(tombstone: tombstone);

  @override
  ChangeEvent geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  ChangeEvent payload(Map<String, Object>? payload) => call(payload: payload);

  @override
  ChangeEvent prevSeq(int? prevSeq) => call(prevSeq: prevSeq);

  @override
  ChangeEvent historyTier(HistoryTier? historyTier) =>
      call(historyTier: historyTier);

  @override
  ChangeEvent persistReason(PersistReason? persistReason) =>
      call(persistReason: persistReason);

  @override
  ChangeEvent eventTime(DateTime? eventTime) => call(eventTime: eventTime);

  @override
  ChangeEvent validUntil(DateTime? validUntil) => call(validUntil: validUntil);

  @override
  ChangeEvent qaFlags(int? qaFlags) => call(qaFlags: qaFlags);

  @override
  ChangeEvent bbox(List<num>? bbox) => call(bbox: bbox);

  @override
  ChangeEvent prevPersistedBbox(List<num>? prevPersistedBbox) =>
      call(prevPersistedBbox: prevPersistedBbox);

  @override
  ChangeEvent prevLiveBbox(List<num>? prevLiveBbox) =>
      call(prevLiveBbox: prevLiveBbox);

  @override
  ChangeEvent leftFilter(bool? leftFilter) => call(leftFilter: leftFilter);

  @override
  ChangeEvent purged(bool? purged) => call(purged: purged);

  @override
  ChangeEvent validFrom(DateTime? validFrom) => call(validFrom: validFrom);

  @override
  ChangeEvent globalRef(String? globalRef) => call(globalRef: globalRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ChangeEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ChangeEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ChangeEvent call({
    Object? seq = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? tombstone = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? payload = const $CopyWithPlaceholder(),
    Object? prevSeq = const $CopyWithPlaceholder(),
    Object? historyTier = const $CopyWithPlaceholder(),
    Object? persistReason = const $CopyWithPlaceholder(),
    Object? eventTime = const $CopyWithPlaceholder(),
    Object? validUntil = const $CopyWithPlaceholder(),
    Object? qaFlags = const $CopyWithPlaceholder(),
    Object? bbox = const $CopyWithPlaceholder(),
    Object? prevPersistedBbox = const $CopyWithPlaceholder(),
    Object? prevLiveBbox = const $CopyWithPlaceholder(),
    Object? leftFilter = const $CopyWithPlaceholder(),
    Object? purged = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? globalRef = const $CopyWithPlaceholder(),
  }) {
    return ChangeEvent(
      seq: seq == const $CopyWithPlaceholder() || seq == null
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int,
      source_: source_ == const $CopyWithPlaceholder() || source_ == null
          ? _value.source_
          // ignore: cast_nullable_to_non_nullable
          : source_ as String,
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
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
      tombstone: tombstone == const $CopyWithPlaceholder() || tombstone == null
          ? _value.tombstone
          // ignore: cast_nullable_to_non_nullable
          : tombstone as bool,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      payload: payload == const $CopyWithPlaceholder()
          ? _value.payload
          // ignore: cast_nullable_to_non_nullable
          : payload as Map<String, Object>?,
      prevSeq: prevSeq == const $CopyWithPlaceholder()
          ? _value.prevSeq
          // ignore: cast_nullable_to_non_nullable
          : prevSeq as int?,
      historyTier: historyTier == const $CopyWithPlaceholder()
          ? _value.historyTier
          // ignore: cast_nullable_to_non_nullable
          : historyTier as HistoryTier?,
      persistReason: persistReason == const $CopyWithPlaceholder()
          ? _value.persistReason
          // ignore: cast_nullable_to_non_nullable
          : persistReason as PersistReason?,
      eventTime: eventTime == const $CopyWithPlaceholder()
          ? _value.eventTime
          // ignore: cast_nullable_to_non_nullable
          : eventTime as DateTime?,
      validUntil: validUntil == const $CopyWithPlaceholder()
          ? _value.validUntil
          // ignore: cast_nullable_to_non_nullable
          : validUntil as DateTime?,
      qaFlags: qaFlags == const $CopyWithPlaceholder()
          ? _value.qaFlags
          // ignore: cast_nullable_to_non_nullable
          : qaFlags as int?,
      bbox: bbox == const $CopyWithPlaceholder()
          ? _value.bbox
          // ignore: cast_nullable_to_non_nullable
          : bbox as List<num>?,
      prevPersistedBbox: prevPersistedBbox == const $CopyWithPlaceholder()
          ? _value.prevPersistedBbox
          // ignore: cast_nullable_to_non_nullable
          : prevPersistedBbox as List<num>?,
      prevLiveBbox: prevLiveBbox == const $CopyWithPlaceholder()
          ? _value.prevLiveBbox
          // ignore: cast_nullable_to_non_nullable
          : prevLiveBbox as List<num>?,
      leftFilter: leftFilter == const $CopyWithPlaceholder()
          ? _value.leftFilter
          // ignore: cast_nullable_to_non_nullable
          : leftFilter as bool?,
      purged: purged == const $CopyWithPlaceholder()
          ? _value.purged
          // ignore: cast_nullable_to_non_nullable
          : purged as bool?,
      validFrom: validFrom == const $CopyWithPlaceholder()
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime?,
      globalRef: globalRef == const $CopyWithPlaceholder()
          ? _value.globalRef
          // ignore: cast_nullable_to_non_nullable
          : globalRef as String?,
    );
  }
}

extension $ChangeEventCopyWith on ChangeEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfChangeEvent.copyWith(...)` or `instanceOfChangeEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ChangeEventCWProxy get copyWith => _$ChangeEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ChangeEvent _$ChangeEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('ChangeEvent', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'seq',
          'source',
          'externalId',
          'kind',
          'version',
          'observedAt',
          'tombstone',
        ],
      );
      final val = ChangeEvent(
        seq: $checkedConvert('seq', (v) => (v as num).toInt()),
        source_: $checkedConvert('source', (v) => v as String),
        externalId: $checkedConvert('externalId', (v) => v as String),
        kind: $checkedConvert('kind', (v) => v as String),
        version: $checkedConvert('version', (v) => (v as num).toInt()),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => DateTime.parse(v as String),
        ),
        tombstone: $checkedConvert('tombstone', (v) => v as bool),
        geometry: $checkedConvert(
          'geometry',
          (v) =>
              v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
        ),
        payload: $checkedConvert(
          'payload',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
        ),
        prevSeq: $checkedConvert('prevSeq', (v) => (v as num?)?.toInt()),
        historyTier: $checkedConvert(
          'historyTier',
          (v) => $enumDecodeNullable(_$HistoryTierEnumMap, v),
        ),
        persistReason: $checkedConvert(
          'persistReason',
          (v) => $enumDecodeNullable(_$PersistReasonEnumMap, v),
        ),
        eventTime: $checkedConvert(
          'eventTime',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        validUntil: $checkedConvert(
          'validUntil',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        qaFlags: $checkedConvert('qaFlags', (v) => (v as num?)?.toInt()),
        bbox: $checkedConvert(
          'bbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        prevPersistedBbox: $checkedConvert(
          'prevPersistedBbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        prevLiveBbox: $checkedConvert(
          'prevLiveBbox',
          (v) => (v as List<dynamic>?)?.map((e) => e as num).toList(),
        ),
        leftFilter: $checkedConvert('leftFilter', (v) => v as bool? ?? false),
        purged: $checkedConvert('purged', (v) => v as bool?),
        validFrom: $checkedConvert(
          'validFrom',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        globalRef: $checkedConvert('globalRef', (v) => v as String?),
      );
      return val;
    }, fieldKeyMap: const {'source_': 'source'});

Map<String, dynamic> _$ChangeEventToJson(ChangeEvent instance) =>
    <String, dynamic>{
      'seq': instance.seq,
      'source': instance.source_,
      'externalId': instance.externalId,
      'kind': instance.kind,
      'version': instance.version,
      'observedAt': instance.observedAt.toIso8601String(),
      'tombstone': instance.tombstone,
      'geometry': ?instance.geometry?.toJson(),
      'payload': ?instance.payload,
      'prevSeq': ?instance.prevSeq,
      'historyTier': ?_$HistoryTierEnumMap[instance.historyTier],
      'persistReason': ?_$PersistReasonEnumMap[instance.persistReason],
      'eventTime': ?instance.eventTime?.toIso8601String(),
      'validUntil': ?instance.validUntil?.toIso8601String(),
      'qaFlags': ?instance.qaFlags,
      'bbox': ?instance.bbox,
      'prevPersistedBbox': ?instance.prevPersistedBbox,
      'prevLiveBbox': ?instance.prevLiveBbox,
      'leftFilter': ?instance.leftFilter,
      'purged': ?instance.purged,
      'validFrom': ?instance.validFrom?.toIso8601String(),
      'globalRef': ?instance.globalRef,
    };

const _$HistoryTierEnumMap = {
  HistoryTier.STANDARD: 'STANDARD',
  HistoryTier.TRACK: 'TRACK',
};

const _$PersistReasonEnumMap = {
  PersistReason.FIRST: 'FIRST',
  PersistReason.REAPPEAR: 'REAPPEAR',
  PersistReason.SIGNIFICANT: 'SIGNIFICANT',
  PersistReason.TOMBSTONE: 'TOMBSTONE',
  PersistReason.CHANGE: 'CHANGE',
  PersistReason.SAMPLE: 'SAMPLE',
  PersistReason.KEYFRAME: 'KEYFRAME',
  PersistReason.HEARTBEAT: 'HEARTBEAT',
  PersistReason.TRIGGER_EDGE: 'TRIGGER_EDGE',
};
