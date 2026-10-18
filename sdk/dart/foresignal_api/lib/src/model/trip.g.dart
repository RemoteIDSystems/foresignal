// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trip.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TripCWProxy {
  Trip name(String name);

  Trip routePlaceId(String? routePlaceId);

  Trip route(Geometry? route);

  Trip bufferMeters(int? bufferMeters);

  Trip schedule(TripSchedule schedule);

  Trip expectedDuration(String expectedDuration);

  Trip leadTime(String? leadTime);

  Trip hazardKinds(List<String>? hazardKinds);

  Trip minSeverity(Severity? minSeverity);

  Trip channelIds(List<String>? channelIds);

  Trip anchor(String? anchor);

  Trip notifyWhenClear(bool? notifyWhenClear);

  Trip enabled(bool? enabled);

  Trip id(String id);

  Trip corridor(Geometry corridor);

  Trip nextOccurrence(DateTime? nextOccurrence);

  Trip createdBy(String? createdBy);

  Trip createdAt(DateTime createdAt);

  Trip updatedAt(DateTime updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Trip(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Trip(...).copyWith(id: 12, name: "My name")
  /// ```
  Trip call({
    String name,
    String? routePlaceId,
    Geometry? route,
    int? bufferMeters,
    TripSchedule schedule,
    String expectedDuration,
    String? leadTime,
    List<String>? hazardKinds,
    Severity? minSeverity,
    List<String>? channelIds,
    String? anchor,
    bool? notifyWhenClear,
    bool? enabled,
    String id,
    Geometry corridor,
    DateTime? nextOccurrence,
    String? createdBy,
    DateTime createdAt,
    DateTime updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTrip.copyWith(...)` or call `instanceOfTrip.copyWith.fieldName(value)` for a single field.
class _$TripCWProxyImpl implements _$TripCWProxy {
  const _$TripCWProxyImpl(this._value);

  final Trip _value;

  @override
  Trip name(String name) => call(name: name);

  @override
  Trip routePlaceId(String? routePlaceId) => call(routePlaceId: routePlaceId);

  @override
  Trip route(Geometry? route) => call(route: route);

  @override
  Trip bufferMeters(int? bufferMeters) => call(bufferMeters: bufferMeters);

  @override
  Trip schedule(TripSchedule schedule) => call(schedule: schedule);

  @override
  Trip expectedDuration(String expectedDuration) =>
      call(expectedDuration: expectedDuration);

  @override
  Trip leadTime(String? leadTime) => call(leadTime: leadTime);

  @override
  Trip hazardKinds(List<String>? hazardKinds) => call(hazardKinds: hazardKinds);

  @override
  Trip minSeverity(Severity? minSeverity) => call(minSeverity: minSeverity);

  @override
  Trip channelIds(List<String>? channelIds) => call(channelIds: channelIds);

  @override
  Trip anchor(String? anchor) => call(anchor: anchor);

  @override
  Trip notifyWhenClear(bool? notifyWhenClear) =>
      call(notifyWhenClear: notifyWhenClear);

  @override
  Trip enabled(bool? enabled) => call(enabled: enabled);

  @override
  Trip id(String id) => call(id: id);

  @override
  Trip corridor(Geometry corridor) => call(corridor: corridor);

  @override
  Trip nextOccurrence(DateTime? nextOccurrence) =>
      call(nextOccurrence: nextOccurrence);

  @override
  Trip createdBy(String? createdBy) => call(createdBy: createdBy);

  @override
  Trip createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Trip updatedAt(DateTime updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Trip(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Trip(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Trip call({
    Object? name = const $CopyWithPlaceholder(),
    Object? routePlaceId = const $CopyWithPlaceholder(),
    Object? route = const $CopyWithPlaceholder(),
    Object? bufferMeters = const $CopyWithPlaceholder(),
    Object? schedule = const $CopyWithPlaceholder(),
    Object? expectedDuration = const $CopyWithPlaceholder(),
    Object? leadTime = const $CopyWithPlaceholder(),
    Object? hazardKinds = const $CopyWithPlaceholder(),
    Object? minSeverity = const $CopyWithPlaceholder(),
    Object? channelIds = const $CopyWithPlaceholder(),
    Object? anchor = const $CopyWithPlaceholder(),
    Object? notifyWhenClear = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? corridor = const $CopyWithPlaceholder(),
    Object? nextOccurrence = const $CopyWithPlaceholder(),
    Object? createdBy = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return Trip(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      routePlaceId: routePlaceId == const $CopyWithPlaceholder()
          ? _value.routePlaceId
          // ignore: cast_nullable_to_non_nullable
          : routePlaceId as String?,
      route: route == const $CopyWithPlaceholder()
          ? _value.route
          // ignore: cast_nullable_to_non_nullable
          : route as Geometry?,
      bufferMeters: bufferMeters == const $CopyWithPlaceholder()
          ? _value.bufferMeters
          // ignore: cast_nullable_to_non_nullable
          : bufferMeters as int?,
      schedule: schedule == const $CopyWithPlaceholder() || schedule == null
          ? _value.schedule
          // ignore: cast_nullable_to_non_nullable
          : schedule as TripSchedule,
      expectedDuration:
          expectedDuration == const $CopyWithPlaceholder() ||
              expectedDuration == null
          ? _value.expectedDuration
          // ignore: cast_nullable_to_non_nullable
          : expectedDuration as String,
      leadTime: leadTime == const $CopyWithPlaceholder()
          ? _value.leadTime
          // ignore: cast_nullable_to_non_nullable
          : leadTime as String?,
      hazardKinds: hazardKinds == const $CopyWithPlaceholder()
          ? _value.hazardKinds
          // ignore: cast_nullable_to_non_nullable
          : hazardKinds as List<String>?,
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as Severity?,
      channelIds: channelIds == const $CopyWithPlaceholder()
          ? _value.channelIds
          // ignore: cast_nullable_to_non_nullable
          : channelIds as List<String>?,
      anchor: anchor == const $CopyWithPlaceholder()
          ? _value.anchor
          // ignore: cast_nullable_to_non_nullable
          : anchor as String?,
      notifyWhenClear: notifyWhenClear == const $CopyWithPlaceholder()
          ? _value.notifyWhenClear
          // ignore: cast_nullable_to_non_nullable
          : notifyWhenClear as bool?,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      corridor: corridor == const $CopyWithPlaceholder() || corridor == null
          ? _value.corridor
          // ignore: cast_nullable_to_non_nullable
          : corridor as Geometry,
      nextOccurrence: nextOccurrence == const $CopyWithPlaceholder()
          ? _value.nextOccurrence
          // ignore: cast_nullable_to_non_nullable
          : nextOccurrence as DateTime?,
      createdBy: createdBy == const $CopyWithPlaceholder()
          ? _value.createdBy
          // ignore: cast_nullable_to_non_nullable
          : createdBy as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder() || updatedAt == null
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime,
    );
  }
}

extension $TripCopyWith on Trip {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTrip.copyWith(...)` or `instanceOfTrip.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TripCWProxy get copyWith => _$TripCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Trip _$TripFromJson(Map<String, dynamic> json) => $checkedCreate('Trip', json, (
  $checkedConvert,
) {
  $checkKeys(
    json,
    requiredKeys: const [
      'name',
      'schedule',
      'expectedDuration',
      'id',
      'corridor',
      'createdAt',
      'updatedAt',
    ],
  );
  final val = Trip(
    name: $checkedConvert('name', (v) => v as String),
    routePlaceId: $checkedConvert('routePlaceId', (v) => v as String?),
    route: $checkedConvert(
      'route',
      (v) => v == null ? null : Geometry.fromJson(v as Map<String, dynamic>),
    ),
    bufferMeters: $checkedConvert(
      'bufferMeters',
      (v) => (v as num?)?.toInt() ?? 2000,
    ),
    schedule: $checkedConvert(
      'schedule',
      (v) => TripSchedule.fromJson(v as Map<String, dynamic>),
    ),
    expectedDuration: $checkedConvert('expectedDuration', (v) => v as String),
    leadTime: $checkedConvert('leadTime', (v) => v as String? ?? '60m'),
    hazardKinds: $checkedConvert(
      'hazardKinds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    minSeverity: $checkedConvert(
      'minSeverity',
      (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
    ),
    channelIds: $checkedConvert(
      'channelIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    anchor: $checkedConvert('anchor', (v) => v as String?),
    notifyWhenClear: $checkedConvert(
      'notifyWhenClear',
      (v) => v as bool? ?? true,
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
    id: $checkedConvert('id', (v) => v as String),
    corridor: $checkedConvert(
      'corridor',
      (v) => Geometry.fromJson(v as Map<String, dynamic>),
    ),
    nextOccurrence: $checkedConvert(
      'nextOccurrence',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    createdBy: $checkedConvert('createdBy', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert('updatedAt', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$TripToJson(Trip instance) => <String, dynamic>{
  'name': instance.name,
  'routePlaceId': ?instance.routePlaceId,
  'route': ?instance.route?.toJson(),
  'bufferMeters': ?instance.bufferMeters,
  'schedule': instance.schedule.toJson(),
  'expectedDuration': instance.expectedDuration,
  'leadTime': ?instance.leadTime,
  'hazardKinds': ?instance.hazardKinds,
  'minSeverity': ?_$SeverityEnumMap[instance.minSeverity],
  'channelIds': ?instance.channelIds,
  'anchor': ?instance.anchor,
  'notifyWhenClear': ?instance.notifyWhenClear,
  'enabled': ?instance.enabled,
  'id': instance.id,
  'corridor': instance.corridor.toJson(),
  'nextOccurrence': ?instance.nextOccurrence?.toIso8601String(),
  'createdBy': ?instance.createdBy,
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': instance.updatedAt.toIso8601String(),
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
