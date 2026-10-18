// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'record_mapping.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RecordMappingCWProxy {
  RecordMapping version(RecordMappingVersionEnum version);

  RecordMapping select(String? select);

  RecordMapping externalId(MappingValueSpec externalId);

  RecordMapping kind(MappingValueSpec? kind);

  RecordMapping observedAt(MappingTimeSpec observedAt);

  RecordMapping eventTime(MappingTimeSpec? eventTime);

  RecordMapping validUntil(MappingTimeSpec? validUntil);

  RecordMapping geometry(RecordMappingGeometry? geometry);

  RecordMapping tombstone(RecordMappingTombstone? tombstone);

  RecordMapping quality(RecordMappingQuality? quality);

  RecordMapping fields(List<MappingField>? fields);

  RecordMapping pairs(RecordMappingPairs? pairs);

  RecordMapping drop(List<String>? drop);

  RecordMapping filter(List<RecordMappingFilterInner>? filter);

  RecordMapping spread(RecordMappingSpread? spread);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMapping(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMapping(...).copyWith(id: 12, name: "My name")
  /// ```
  RecordMapping call({
    RecordMappingVersionEnum version,
    String? select,
    MappingValueSpec externalId,
    MappingValueSpec? kind,
    MappingTimeSpec observedAt,
    MappingTimeSpec? eventTime,
    MappingTimeSpec? validUntil,
    RecordMappingGeometry? geometry,
    RecordMappingTombstone? tombstone,
    RecordMappingQuality? quality,
    List<MappingField>? fields,
    RecordMappingPairs? pairs,
    List<String>? drop,
    List<RecordMappingFilterInner>? filter,
    RecordMappingSpread? spread,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRecordMapping.copyWith(...)` or call `instanceOfRecordMapping.copyWith.fieldName(value)` for a single field.
class _$RecordMappingCWProxyImpl implements _$RecordMappingCWProxy {
  const _$RecordMappingCWProxyImpl(this._value);

  final RecordMapping _value;

  @override
  RecordMapping version(RecordMappingVersionEnum version) =>
      call(version: version);

  @override
  RecordMapping select(String? select) => call(select: select);

  @override
  RecordMapping externalId(MappingValueSpec externalId) =>
      call(externalId: externalId);

  @override
  RecordMapping kind(MappingValueSpec? kind) => call(kind: kind);

  @override
  RecordMapping observedAt(MappingTimeSpec observedAt) =>
      call(observedAt: observedAt);

  @override
  RecordMapping eventTime(MappingTimeSpec? eventTime) =>
      call(eventTime: eventTime);

  @override
  RecordMapping validUntil(MappingTimeSpec? validUntil) =>
      call(validUntil: validUntil);

  @override
  RecordMapping geometry(RecordMappingGeometry? geometry) =>
      call(geometry: geometry);

  @override
  RecordMapping tombstone(RecordMappingTombstone? tombstone) =>
      call(tombstone: tombstone);

  @override
  RecordMapping quality(RecordMappingQuality? quality) =>
      call(quality: quality);

  @override
  RecordMapping fields(List<MappingField>? fields) => call(fields: fields);

  @override
  RecordMapping pairs(RecordMappingPairs? pairs) => call(pairs: pairs);

  @override
  RecordMapping drop(List<String>? drop) => call(drop: drop);

  @override
  RecordMapping filter(List<RecordMappingFilterInner>? filter) =>
      call(filter: filter);

  @override
  RecordMapping spread(RecordMappingSpread? spread) => call(spread: spread);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RecordMapping(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RecordMapping(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RecordMapping call({
    Object? version = const $CopyWithPlaceholder(),
    Object? select = const $CopyWithPlaceholder(),
    Object? externalId = const $CopyWithPlaceholder(),
    Object? kind = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? eventTime = const $CopyWithPlaceholder(),
    Object? validUntil = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? tombstone = const $CopyWithPlaceholder(),
    Object? quality = const $CopyWithPlaceholder(),
    Object? fields = const $CopyWithPlaceholder(),
    Object? pairs = const $CopyWithPlaceholder(),
    Object? drop = const $CopyWithPlaceholder(),
    Object? filter = const $CopyWithPlaceholder(),
    Object? spread = const $CopyWithPlaceholder(),
  }) {
    return RecordMapping(
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as RecordMappingVersionEnum,
      select: select == const $CopyWithPlaceholder()
          ? _value.select
          // ignore: cast_nullable_to_non_nullable
          : select as String?,
      externalId:
          externalId == const $CopyWithPlaceholder() || externalId == null
          ? _value.externalId
          // ignore: cast_nullable_to_non_nullable
          : externalId as MappingValueSpec,
      kind: kind == const $CopyWithPlaceholder()
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as MappingValueSpec?,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as MappingTimeSpec,
      eventTime: eventTime == const $CopyWithPlaceholder()
          ? _value.eventTime
          // ignore: cast_nullable_to_non_nullable
          : eventTime as MappingTimeSpec?,
      validUntil: validUntil == const $CopyWithPlaceholder()
          ? _value.validUntil
          // ignore: cast_nullable_to_non_nullable
          : validUntil as MappingTimeSpec?,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as RecordMappingGeometry?,
      tombstone: tombstone == const $CopyWithPlaceholder()
          ? _value.tombstone
          // ignore: cast_nullable_to_non_nullable
          : tombstone as RecordMappingTombstone?,
      quality: quality == const $CopyWithPlaceholder()
          ? _value.quality
          // ignore: cast_nullable_to_non_nullable
          : quality as RecordMappingQuality?,
      fields: fields == const $CopyWithPlaceholder()
          ? _value.fields
          // ignore: cast_nullable_to_non_nullable
          : fields as List<MappingField>?,
      pairs: pairs == const $CopyWithPlaceholder()
          ? _value.pairs
          // ignore: cast_nullable_to_non_nullable
          : pairs as RecordMappingPairs?,
      drop: drop == const $CopyWithPlaceholder()
          ? _value.drop
          // ignore: cast_nullable_to_non_nullable
          : drop as List<String>?,
      filter: filter == const $CopyWithPlaceholder()
          ? _value.filter
          // ignore: cast_nullable_to_non_nullable
          : filter as List<RecordMappingFilterInner>?,
      spread: spread == const $CopyWithPlaceholder()
          ? _value.spread
          // ignore: cast_nullable_to_non_nullable
          : spread as RecordMappingSpread?,
    );
  }
}

extension $RecordMappingCopyWith on RecordMapping {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRecordMapping.copyWith(...)` or `instanceOfRecordMapping.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RecordMappingCWProxy get copyWith => _$RecordMappingCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RecordMapping _$RecordMappingFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RecordMapping', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['version', 'externalId', 'observedAt']);
  final val = RecordMapping(
    version: $checkedConvert(
      'version',
      (v) => $enumDecode(_$RecordMappingVersionEnumEnumMap, v),
    ),
    select: $checkedConvert('select', (v) => v as String?),
    externalId: $checkedConvert(
      'externalId',
      (v) => MappingValueSpec.fromJson(v as Map<String, dynamic>),
    ),
    kind: $checkedConvert(
      'kind',
      (v) => v == null
          ? null
          : MappingValueSpec.fromJson(v as Map<String, dynamic>),
    ),
    observedAt: $checkedConvert(
      'observedAt',
      (v) => MappingTimeSpec.fromJson(v as Map<String, dynamic>),
    ),
    eventTime: $checkedConvert(
      'eventTime',
      (v) => v == null
          ? null
          : MappingTimeSpec.fromJson(v as Map<String, dynamic>),
    ),
    validUntil: $checkedConvert(
      'validUntil',
      (v) => v == null
          ? null
          : MappingTimeSpec.fromJson(v as Map<String, dynamic>),
    ),
    geometry: $checkedConvert(
      'geometry',
      (v) => v == null
          ? null
          : RecordMappingGeometry.fromJson(v as Map<String, dynamic>),
    ),
    tombstone: $checkedConvert(
      'tombstone',
      (v) => v == null
          ? null
          : RecordMappingTombstone.fromJson(v as Map<String, dynamic>),
    ),
    quality: $checkedConvert(
      'quality',
      (v) => v == null
          ? null
          : RecordMappingQuality.fromJson(v as Map<String, dynamic>),
    ),
    fields: $checkedConvert(
      'fields',
      (v) => (v as List<dynamic>?)
          ?.map((e) => MappingField.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    pairs: $checkedConvert(
      'pairs',
      (v) => v == null
          ? null
          : RecordMappingPairs.fromJson(v as Map<String, dynamic>),
    ),
    drop: $checkedConvert(
      'drop',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    filter: $checkedConvert(
      'filter',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => RecordMappingFilterInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    spread: $checkedConvert(
      'spread',
      (v) => v == null
          ? null
          : RecordMappingSpread.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$RecordMappingToJson(RecordMapping instance) =>
    <String, dynamic>{
      'version': _$RecordMappingVersionEnumEnumMap[instance.version]!,
      'select': ?instance.select,
      'externalId': instance.externalId.toJson(),
      'kind': ?instance.kind?.toJson(),
      'observedAt': instance.observedAt.toJson(),
      'eventTime': ?instance.eventTime?.toJson(),
      'validUntil': ?instance.validUntil?.toJson(),
      'geometry': ?instance.geometry?.toJson(),
      'tombstone': ?instance.tombstone?.toJson(),
      'quality': ?instance.quality?.toJson(),
      'fields': ?instance.fields?.map((e) => e.toJson()).toList(),
      'pairs': ?instance.pairs?.toJson(),
      'drop': ?instance.drop,
      'filter': ?instance.filter?.map((e) => e.toJson()).toList(),
      'spread': ?instance.spread?.toJson(),
    };

const _$RecordMappingVersionEnumEnumMap = {RecordMappingVersionEnum.number1: 1};
