// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trip_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TripInputCWProxy {
  TripInput name(String name);

  TripInput routePlaceId(String? routePlaceId);

  TripInput route(Geometry? route);

  TripInput bufferMeters(int? bufferMeters);

  TripInput schedule(TripSchedule schedule);

  TripInput expectedDuration(String expectedDuration);

  TripInput leadTime(String? leadTime);

  TripInput hazardKinds(List<String>? hazardKinds);

  TripInput minSeverity(Severity? minSeverity);

  TripInput channelIds(List<String>? channelIds);

  TripInput anchor(String? anchor);

  TripInput notifyWhenClear(bool? notifyWhenClear);

  TripInput enabled(bool? enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TripInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TripInput(...).copyWith(id: 12, name: "My name")
  /// ```
  TripInput call({
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
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTripInput.copyWith(...)` or call `instanceOfTripInput.copyWith.fieldName(value)` for a single field.
class _$TripInputCWProxyImpl implements _$TripInputCWProxy {
  const _$TripInputCWProxyImpl(this._value);

  final TripInput _value;

  @override
  TripInput name(String name) => call(name: name);

  @override
  TripInput routePlaceId(String? routePlaceId) =>
      call(routePlaceId: routePlaceId);

  @override
  TripInput route(Geometry? route) => call(route: route);

  @override
  TripInput bufferMeters(int? bufferMeters) => call(bufferMeters: bufferMeters);

  @override
  TripInput schedule(TripSchedule schedule) => call(schedule: schedule);

  @override
  TripInput expectedDuration(String expectedDuration) =>
      call(expectedDuration: expectedDuration);

  @override
  TripInput leadTime(String? leadTime) => call(leadTime: leadTime);

  @override
  TripInput hazardKinds(List<String>? hazardKinds) =>
      call(hazardKinds: hazardKinds);

  @override
  TripInput minSeverity(Severity? minSeverity) =>
      call(minSeverity: minSeverity);

  @override
  TripInput channelIds(List<String>? channelIds) =>
      call(channelIds: channelIds);

  @override
  TripInput anchor(String? anchor) => call(anchor: anchor);

  @override
  TripInput notifyWhenClear(bool? notifyWhenClear) =>
      call(notifyWhenClear: notifyWhenClear);

  @override
  TripInput enabled(bool? enabled) => call(enabled: enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TripInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TripInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TripInput call({
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
  }) {
    return TripInput(
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
    );
  }
}

extension $TripInputCopyWith on TripInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTripInput.copyWith(...)` or `instanceOfTripInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TripInputCWProxy get copyWith => _$TripInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TripInput _$TripInputFromJson(Map<String, dynamic> json) => $checkedCreate(
  'TripInput',
  json,
  ($checkedConvert) {
    $checkKeys(
      json,
      requiredKeys: const ['name', 'schedule', 'expectedDuration'],
    );
    final val = TripInput(
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
    );
    return val;
  },
);

Map<String, dynamic> _$TripInputToJson(TripInput instance) => <String, dynamic>{
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
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
