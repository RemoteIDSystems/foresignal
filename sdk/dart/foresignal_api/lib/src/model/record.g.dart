// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'record.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecordCWProxy {
  Record id(String id);

  Record source_(String source_);

  Record externalId(String externalId);

  Record kind(String kind);

  Record version(int version);

  Record seq(int seq);

  Record observedAt(DateTime observedAt);

  Record ingestedAt(DateTime? ingestedAt);

  Record validUntil(DateTime? validUntil);

  Record tombstone(bool? tombstone);

  Record geometry(Geometry? geometry);

  Record payload(Map<String, Object> payload);

  Record attribution(String? attribution);

  Record title(String? title);

  Record persistedObservedAt(DateTime? persistedObservedAt);

  Record eventTime(DateTime? eventTime);

  Record validFrom(DateTime? validFrom);

  Record quality(Quality? quality);

  Record live(bool? live);

  Record qaFlags(int? qaFlags);

  Record distanceKm(num? distanceKm);

  Record observer(List<Observer>? observer);

  Record region(RegionTags? region);

  Record entityId(String? entityId);

  Record provenance(Provenance? provenance);

  Record globalRef(String? globalRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Record(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Record(...).copyWith(id: 12, name: "My name")
  /// ```
  Record call({
    String id,
    String source_,
    String externalId,
    String kind,
    int version,
    int seq,
    DateTime observedAt,
    DateTime? ingestedAt,
    DateTime? validUntil,
    bool? tombstone,
    Geometry? geometry,
    Map<String, Object> payload,
    String? attribution,
    String? title,
    DateTime? persistedObservedAt,
    DateTime? eventTime,
    DateTime? validFrom,
    Quality? quality,
    bool? live,
    int? qaFlags,
    num? distanceKm,
    List<Observer>? observer,
    RegionTags? region,
    String? entityId,
    Provenance? provenance,
    String? globalRef,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecord.copyWith(...)` or call `instanceOfRecord.copyWith.fieldName(value)` for a single field.
class _$RecordCWProxyImpl implements _$RecordCWProxy {
  const _$RecordCWProxyImpl(this._value);

  final Record _value;

  @override
  Record id(String id) => call(id: id);

  @override
  Record source_(String source_) => call(source_: source_);

  @override
  Record externalId(String externalId) => call(externalId: externalId);

  @override
  Record kind(String kind) => call(kind: kind);

  @override
  Record version(int version) => call(version: version);

  @override
  Record seq(int seq) => call(seq: seq);

  @override
  Record observedAt(DateTime observedAt) => call(observedAt: observedAt);

  @override
  Record ingestedAt(DateTime? ingestedAt) => call(ingestedAt: ingestedAt);

  @override
  Record validUntil(DateTime? validUntil) => call(validUntil: validUntil);

  @override
  Record tombstone(bool? tombstone) => call(tombstone: tombstone);

  @override
  Record geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  Record payload(Map<String, Object> payload) => call(payload: payload);

  @override
  Record attribution(String? attribution) => call(attribution: attribution);

  @override
  Record title(String? title) => call(title: title);

  @override
  Record persistedObservedAt(DateTime? persistedObservedAt) =>
      call(persistedObservedAt: persistedObservedAt);

  @override
  Record eventTime(DateTime? eventTime) => call(eventTime: eventTime);

  @override
  Record validFrom(DateTime? validFrom) => call(validFrom: validFrom);

  @override
  Record quality(Quality? quality) => call(quality: quality);

  @override
  Record live(bool? live) => call(live: live);

  @override
  Record qaFlags(int? qaFlags) => call(qaFlags: qaFlags);

  @override
  Record distanceKm(num? distanceKm) => call(distanceKm: distanceKm);

  @override
  Record observer(List<Observer>? observer) => call(observer: observer);

  @override
  Record region(RegionTags? region) => call(region: region);

  @override
  Record entityId(String? entityId) => call(entityId: entityId);

  @override
  Record provenance(Provenance? provenance) => call(provenance: provenance);

  @override
  Record globalRef(String? globalRef) => call(globalRef: globalRef);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Record(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Record(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Record call({
    Object? id = const $CopyWithPlaceholder(),
    Object? source_ = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? seq = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? ingestedAt = const $CopyWithPlaceholder(),
    Object? validUntil = const $CopyWithPlaceholder(),
    Object? tombstone = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? payload = const $CopyWithPlaceholder(),
    Object? attribution = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? persistedObservedAt = const $CopyWithPlaceholder(),
    Object? eventTime = const $CopyWithPlaceholder(),
    Object? validFrom = const $CopyWithPlaceholder(),
    Object? quality = const $CopyWithPlaceholder(),
    Object? live = const $CopyWithPlaceholder(),
    Object? qaFlags = const $CopyWithPlaceholder(),
    Object? distanceKm = const $CopyWithPlaceholder(),
    Object? observer = const $CopyWithPlaceholder(),
    Object? region = const $CopyWithPlaceholder(),
    Object? entityId = const $CopyWithPlaceholder(),
    Object? provenance = const $CopyWithPlaceholder(),
    Object? globalRef = const $CopyWithPlaceholder(),
  }) {
    return Record(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
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
      seq: seq == const $CopyWithPlaceholder() || seq == null
          ? _value.seq
          // ignore: cast_nullable_to_non_nullable
          : seq as int,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
      ingestedAt: ingestedAt == const $CopyWithPlaceholder()
          ? _value.ingestedAt
          // ignore: cast_nullable_to_non_nullable
          : ingestedAt as DateTime?,
      validUntil: validUntil == const $CopyWithPlaceholder()
          ? _value.validUntil
          // ignore: cast_nullable_to_non_nullable
          : validUntil as DateTime?,
      tombstone: tombstone == const $CopyWithPlaceholder()
          ? _value.tombstone
          // ignore: cast_nullable_to_non_nullable
          : tombstone as bool?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      payload: payload == const $CopyWithPlaceholder() || payload == null
          ? _value.payload
          // ignore: cast_nullable_to_non_nullable
          : payload as Map<String, Object>,
      attribution: attribution == const $CopyWithPlaceholder()
          ? _value.attribution
          // ignore: cast_nullable_to_non_nullable
          : attribution as String?,
      title: title == const $CopyWithPlaceholder()
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String?,
      persistedObservedAt: persistedObservedAt == const $CopyWithPlaceholder()
          ? _value.persistedObservedAt
          // ignore: cast_nullable_to_non_nullable
          : persistedObservedAt as DateTime?,
      eventTime: eventTime == const $CopyWithPlaceholder()
          ? _value.eventTime
          // ignore: cast_nullable_to_non_nullable
          : eventTime as DateTime?,
      validFrom: validFrom == const $CopyWithPlaceholder()
          ? _value.validFrom
          // ignore: cast_nullable_to_non_nullable
          : validFrom as DateTime?,
      quality: quality == const $CopyWithPlaceholder()
          ? _value.quality
          // ignore: cast_nullable_to_non_nullable
          : quality as Quality?,
      live: live == const $CopyWithPlaceholder()
          ? _value.live
          // ignore: cast_nullable_to_non_nullable
          : live as bool?,
      qaFlags: qaFlags == const $CopyWithPlaceholder()
          ? _value.qaFlags
          // ignore: cast_nullable_to_non_nullable
          : qaFlags as int?,
      distanceKm: distanceKm == const $CopyWithPlaceholder()
          ? _value.distanceKm
          // ignore: cast_nullable_to_non_nullable
          : distanceKm as num?,
      observer: observer == const $CopyWithPlaceholder()
          ? _value.observer
          // ignore: cast_nullable_to_non_nullable
          : observer as List<Observer>?,
      region: region == const $CopyWithPlaceholder()
          ? _value.region
          // ignore: cast_nullable_to_non_nullable
          : region as RegionTags?,
      entityId: entityId == const $CopyWithPlaceholder()
          ? _value.entityId
          // ignore: cast_nullable_to_non_nullable
          : entityId as String?,
      provenance: provenance == const $CopyWithPlaceholder()
          ? _value.provenance
          // ignore: cast_nullable_to_non_nullable
          : provenance as Provenance?,
      globalRef: globalRef == const $CopyWithPlaceholder()
          ? _value.globalRef
          // ignore: cast_nullable_to_non_nullable
          : globalRef as String?,
    );
  }
}

extension $RecordCopyWith on Record {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecord.copyWith(...)` or `instanceOfRecord.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecordCWProxy get copyWith => _$RecordCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Record _$RecordFromJson(Map<String, dynamic> json) => $checkedCreate(
  'Record',
  json,
  ($checkedConvert) {
    $checkKeys(
      json,
      requiredKeys: const [
        'id',
        'source',
        'externalId',
        'kind',
        'version',
        'seq',
        'observedAt',
        'payload',
      ],
    );
    final val = Record(
      id: $checkedConvert('id', (v) => v as String),
      source_: $checkedConvert('source', (v) => v as String),
      externalId: $checkedConvert('externalId', (v) => v as String),
      kind: $checkedConvert('kind', (v) => v as String),
      version: $checkedConvert('version', (v) => (v as num).toInt()),
      seq: $checkedConvert('seq', (v) => (v as num).toInt()),
      observedAt: $checkedConvert(
        'observedAt',
        (v) => DateTime.parse(v as String),
      ),
      ingestedAt: $checkedConvert(
        'ingestedAt',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      validUntil: $checkedConvert(
        'validUntil',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      tombstone: $checkedConvert('tombstone', (v) => v as bool?),
      geometry: $checkedConvert(
        'geometry',
        (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
      ),
      payload: $checkedConvert(
        'payload',
        (v) =>
            (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
      ),
      attribution: $checkedConvert('attribution', (v) => v as String?),
      title: $checkedConvert('title', (v) => v as String?),
      persistedObservedAt: $checkedConvert(
        'persistedObservedAt',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      eventTime: $checkedConvert(
        'eventTime',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      validFrom: $checkedConvert(
        'validFrom',
        (v) => v == null ? null : DateTime.parse(v as String),
      ),
      quality: $checkedConvert(
        'quality',
        (v) => v == null ? null : Quality.fromJson(v as Map<String, dynamic>),
      ),
      live: $checkedConvert('live', (v) => v as bool?),
      qaFlags: $checkedConvert('qaFlags', (v) => (v as num?)?.toInt()),
      distanceKm: $checkedConvert('distanceKm', (v) => v as num?),
      observer: $checkedConvert(
        'observer',
        (v) => (v as List<dynamic>?)
            ?.map((e) => Observer.fromJson(e as Map<String, dynamic>))
            .toList(),
      ),
      region: $checkedConvert(
        'region',
        (v) =>
            v == null ? null : RegionTags.fromJson(v as Map<String, dynamic>),
      ),
      entityId: $checkedConvert('entityId', (v) => v as String?),
      provenance: $checkedConvert(
        'provenance',
        (v) =>
            v == null ? null : Provenance.fromJson(v as Map<String, dynamic>),
      ),
      globalRef: $checkedConvert('globalRef', (v) => v as String?),
    );
    return val;
  },
  fieldKeyMap: const {'source_': 'source'},
);

Map<String, dynamic> _$RecordToJson(Record instance) => <String, dynamic>{
  'id': instance.id,
  'source': instance.source_,
  'externalId': instance.externalId,
  'kind': instance.kind,
  'version': instance.version,
  'seq': instance.seq,
  'observedAt': instance.observedAt.toIso8601String(),
  'ingestedAt': ?instance.ingestedAt?.toIso8601String(),
  'validUntil': ?instance.validUntil?.toIso8601String(),
  'tombstone': ?instance.tombstone,
  'geometry': ?instance.geometry?.toJson(),
  'payload': instance.payload,
  'attribution': ?instance.attribution,
  'title': ?instance.title,
  'persistedObservedAt': ?instance.persistedObservedAt?.toIso8601String(),
  'eventTime': ?instance.eventTime?.toIso8601String(),
  'validFrom': ?instance.validFrom?.toIso8601String(),
  'quality': ?instance.quality?.toJson(),
  'live': ?instance.live,
  'qaFlags': ?instance.qaFlags,
  'distanceKm': ?instance.distanceKm,
  'observer': ?instance.observer?.map((e) => e.toJson()).toList(),
  'region': ?instance.region?.toJson(),
  'entityId': ?instance.entityId,
  'provenance': ?instance.provenance?.toJson(),
  'globalRef': ?instance.globalRef,
};
