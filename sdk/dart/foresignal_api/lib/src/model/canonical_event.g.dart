// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'canonical_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CanonicalEventCWProxy {
  CanonicalEvent id(String id);

  CanonicalEvent ref(String ref);

  CanonicalEvent family(String family);

  CanonicalEvent eventTime(DateTime eventTime);

  CanonicalEvent geometry(Geometry? geometry);

  CanonicalEvent regionCodes(List<String>? regionCodes);

  CanonicalEvent firstObservedAt(DateTime? firstObservedAt);

  CanonicalEvent firstObservationRef(String? firstObservationRef);

  CanonicalEvent observationCount(int observationCount);

  CanonicalEvent evidenceGroupCount(int evidenceGroupCount);

  CanonicalEvent qualityFlags(
    List<CanonicalEventQualityFlagsEnum> qualityFlags,
  );

  CanonicalEvent status(CanonicalEventStatusEnum status);

  CanonicalEvent mergedInto(String? mergedInto);

  CanonicalEvent resolverVersion(String resolverVersion);

  CanonicalEvent createdAt(DateTime createdAt);

  CanonicalEvent updatedAt(DateTime updatedAt);

  CanonicalEvent observations(List<ObservationLink>? observations);

  CanonicalEvent evidenceGroups(List<EvidenceGroup>? evidenceGroups);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CanonicalEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CanonicalEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  CanonicalEvent call({
    String id,
    String ref,
    String family,
    DateTime eventTime,
    Geometry? geometry,
    List<String>? regionCodes,
    DateTime? firstObservedAt,
    String? firstObservationRef,
    int observationCount,
    int evidenceGroupCount,
    List<CanonicalEventQualityFlagsEnum> qualityFlags,
    CanonicalEventStatusEnum status,
    String? mergedInto,
    String resolverVersion,
    DateTime createdAt,
    DateTime updatedAt,
    List<ObservationLink>? observations,
    List<EvidenceGroup>? evidenceGroups,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCanonicalEvent.copyWith(...)` or call `instanceOfCanonicalEvent.copyWith.fieldName(value)` for a single field.
class _$CanonicalEventCWProxyImpl implements _$CanonicalEventCWProxy {
  const _$CanonicalEventCWProxyImpl(this._value);

  final CanonicalEvent _value;

  @override
  CanonicalEvent id(String id) => call(id: id);

  @override
  CanonicalEvent ref(String ref) => call(ref: ref);

  @override
  CanonicalEvent family(String family) => call(family: family);

  @override
  CanonicalEvent eventTime(DateTime eventTime) => call(eventTime: eventTime);

  @override
  CanonicalEvent geometry(Geometry? geometry) => call(geometry: geometry);

  @override
  CanonicalEvent regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  CanonicalEvent firstObservedAt(DateTime? firstObservedAt) =>
      call(firstObservedAt: firstObservedAt);

  @override
  CanonicalEvent firstObservationRef(String? firstObservationRef) =>
      call(firstObservationRef: firstObservationRef);

  @override
  CanonicalEvent observationCount(int observationCount) =>
      call(observationCount: observationCount);

  @override
  CanonicalEvent evidenceGroupCount(int evidenceGroupCount) =>
      call(evidenceGroupCount: evidenceGroupCount);

  @override
  CanonicalEvent qualityFlags(
    List<CanonicalEventQualityFlagsEnum> qualityFlags,
  ) => call(qualityFlags: qualityFlags);

  @override
  CanonicalEvent status(CanonicalEventStatusEnum status) =>
      call(status: status);

  @override
  CanonicalEvent mergedInto(String? mergedInto) => call(mergedInto: mergedInto);

  @override
  CanonicalEvent resolverVersion(String resolverVersion) =>
      call(resolverVersion: resolverVersion);

  @override
  CanonicalEvent createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  CanonicalEvent updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  @override
  CanonicalEvent observations(List<ObservationLink>? observations) =>
      call(observations: observations);

  @override
  CanonicalEvent evidenceGroups(List<EvidenceGroup>? evidenceGroups) =>
      call(evidenceGroups: evidenceGroups);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CanonicalEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CanonicalEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CanonicalEvent call({
    Object? id = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? family = const $CopyWithPlaceholder(),
    Object? eventTime = const $CopyWithPlaceholder(),
    Object? geometry = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? firstObservedAt = const $CopyWithPlaceholder(),
    Object? firstObservationRef = const $CopyWithPlaceholder(),
    Object? observationCount = const $CopyWithPlaceholder(),
    Object? evidenceGroupCount = const $CopyWithPlaceholder(),
    Object? qualityFlags = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? mergedInto = const $CopyWithPlaceholder(),
    Object? resolverVersion = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
    Object? observations = const $CopyWithPlaceholder(),
    Object? evidenceGroups = const $CopyWithPlaceholder(),
  }) {
    return CanonicalEvent(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      ref: ref == const $CopyWithPlaceholder() || ref == null
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String,
      family: family == const $CopyWithPlaceholder() || family == null
          ? _value.family
          // ignore: cast_nullable_to_non_nullable
          : family as String,
      eventTime: eventTime == const $CopyWithPlaceholder() || eventTime == null
          ? _value.eventTime
          // ignore: cast_nullable_to_non_nullable
          : eventTime as DateTime,
      geometry: geometry == const $CopyWithPlaceholder()
          ? _value.geometry
          // ignore: cast_nullable_to_non_nullable
          : geometry as Geometry?,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      firstObservedAt: firstObservedAt == const $CopyWithPlaceholder()
          ? _value.firstObservedAt
          // ignore: cast_nullable_to_non_nullable
          : firstObservedAt as DateTime?,
      firstObservationRef: firstObservationRef == const $CopyWithPlaceholder()
          ? _value.firstObservationRef
          // ignore: cast_nullable_to_non_nullable
          : firstObservationRef as String?,
      observationCount:
          observationCount == const $CopyWithPlaceholder() ||
              observationCount == null
          ? _value.observationCount
          // ignore: cast_nullable_to_non_nullable
          : observationCount as int,
      evidenceGroupCount:
          evidenceGroupCount == const $CopyWithPlaceholder() ||
              evidenceGroupCount == null
          ? _value.evidenceGroupCount
          // ignore: cast_nullable_to_non_nullable
          : evidenceGroupCount as int,
      qualityFlags:
          qualityFlags == const $CopyWithPlaceholder() || qualityFlags == null
          ? _value.qualityFlags
          // ignore: cast_nullable_to_non_nullable
          : qualityFlags as List<CanonicalEventQualityFlagsEnum>,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as CanonicalEventStatusEnum,
      mergedInto: mergedInto == const $CopyWithPlaceholder()
          ? _value.mergedInto
          // ignore: cast_nullable_to_non_nullable
          : mergedInto as String?,
      resolverVersion:
          resolverVersion == const $CopyWithPlaceholder() ||
              resolverVersion == null
          ? _value.resolverVersion
          // ignore: cast_nullable_to_non_nullable
          : resolverVersion as String,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
      observations: observations == const $CopyWithPlaceholder()
          ? _value.observations
          // ignore: cast_nullable_to_non_nullable
          : observations as List<ObservationLink>?,
      evidenceGroups: evidenceGroups == const $CopyWithPlaceholder()
          ? _value.evidenceGroups
          // ignore: cast_nullable_to_non_nullable
          : evidenceGroups as List<EvidenceGroup>?,
    );
  }
}

extension $CanonicalEventCopyWith on CanonicalEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCanonicalEvent.copyWith(...)` or `instanceOfCanonicalEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CanonicalEventCWProxy get copyWith => _$CanonicalEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CanonicalEvent _$CanonicalEventFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CanonicalEvent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'ref',
      'family',
      'eventTime',
      'observationCount',
      'evidenceGroupCount',
      'qualityFlags',
      'status',
      'resolverVersion',
      'createdAt',
      'updatedAt',
    ],
  );
  final val = CanonicalEvent(
    id: $checkedConvert('id', (v) => v as String),
    ref: $checkedConvert('ref', (v) => v as String),
    family: $checkedConvert('family', (v) => v as String),
    eventTime: $checkedConvert('eventTime', (v) => DateTime.parse(v as String)),
    geometry: $checkedConvert(
      'geometry',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    regionCodes: $checkedConvert(
      'regionCodes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    firstObservedAt: $checkedConvert(
      'firstObservedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    firstObservationRef: $checkedConvert(
      'firstObservationRef',
      (v) => v as String?,
    ),
    observationCount: $checkedConvert(
      'observationCount',
      (v) => (v as num).toInt(),
    ),
    evidenceGroupCount: $checkedConvert(
      'evidenceGroupCount',
      (v) => (v as num).toInt(),
    ),
    qualityFlags: $checkedConvert(
      'qualityFlags',
      (v) => (v as List<dynamic>)
          .map((e) => $enumDecode(_$CanonicalEventQualityFlagsEnumEnumMap, e))
          .toList(),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$CanonicalEventStatusEnumEnumMap, v),
    ),
    mergedInto: $checkedConvert('mergedInto', (v) => v as String?),
    resolverVersion: $checkedConvert('resolverVersion', (v) => v as String),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
    observations: $checkedConvert(
      'observations',
      (v) => (v as List<dynamic>?)
          ?.map((e) => ObservationLink.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    evidenceGroups: $checkedConvert(
      'evidenceGroups',
      (v) => (v as List<dynamic>?)
          ?.map((e) => EvidenceGroup.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$CanonicalEventToJson(
  CanonicalEvent instance,
) => <String, dynamic>{
  'id': instance.id,
  'ref': instance.ref,
  'family': instance.family,
  'eventTime': instance.eventTime.toIso8601String(),
  'geometry': ?instance.geometry?.toJson(),
  'regionCodes': ?instance.regionCodes,
  'firstObservedAt': ?instance.firstObservedAt?.toIso8601String(),
  'firstObservationRef': ?instance.firstObservationRef,
  'observationCount': instance.observationCount,
  'evidenceGroupCount': instance.evidenceGroupCount,
  'qualityFlags': instance.qualityFlags
      .map((e) => _$CanonicalEventQualityFlagsEnumEnumMap[e]!)
      .toList(),
  'status': _$CanonicalEventStatusEnumEnumMap[instance.status]!,
  'mergedInto': ?instance.mergedInto,
  'resolverVersion': instance.resolverVersion,
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': instance.updatedAt.toIso8601String(),
  'observations': ?instance.observations?.map((e) => e.toJson()).toList(),
  'evidenceGroups': ?instance.evidenceGroups?.map((e) => e.toJson()).toList(),
};

const _$CanonicalEventQualityFlagsEnumEnumMap = {
  CanonicalEventQualityFlagsEnum.ECHO: 'ECHO',
  CanonicalEventQualityFlagsEnum.TIMESTAMP_CONFLICT: 'TIMESTAMP_CONFLICT',
  CanonicalEventQualityFlagsEnum.SINGLE_ORIGINATOR: 'SINGLE_ORIGINATOR',
  CanonicalEventQualityFlagsEnum.LATE_REVISION: 'LATE_REVISION',
};

const _$CanonicalEventStatusEnumEnumMap = {
  CanonicalEventStatusEnum.open: 'open',
  CanonicalEventStatusEnum.merged: 'merged',
  CanonicalEventStatusEnum.closed: 'closed',
};
