// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'overland_batch_locations_inner_properties.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OverlandBatchLocationsInnerPropertiesCWProxy {
  OverlandBatchLocationsInnerProperties timestamp(DateTime? timestamp);

  OverlandBatchLocationsInnerProperties horizontalAccuracy(
    num? horizontalAccuracy,
  );

  OverlandBatchLocationsInnerProperties altitude(num? altitude);

  OverlandBatchLocationsInnerProperties speed(num? speed);

  OverlandBatchLocationsInnerProperties course(num? course);

  OverlandBatchLocationsInnerProperties motion(List<String>? motion);

  OverlandBatchLocationsInnerProperties batteryLevel(num? batteryLevel);

  OverlandBatchLocationsInnerProperties batteryState(String? batteryState);

  OverlandBatchLocationsInnerProperties wifi(String? wifi);

  OverlandBatchLocationsInnerProperties deviceId(String? deviceId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OverlandBatchLocationsInnerProperties(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OverlandBatchLocationsInnerProperties(...).copyWith(id: 12, name: "My name")
  /// ```
  OverlandBatchLocationsInnerProperties call({
    DateTime? timestamp,
    num? horizontalAccuracy,
    num? altitude,
    num? speed,
    num? course,
    List<String>? motion,
    num? batteryLevel,
    String? batteryState,
    String? wifi,
    String? deviceId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOverlandBatchLocationsInnerProperties.copyWith(...)` or call `instanceOfOverlandBatchLocationsInnerProperties.copyWith.fieldName(value)` for a single field.
class _$OverlandBatchLocationsInnerPropertiesCWProxyImpl
    implements _$OverlandBatchLocationsInnerPropertiesCWProxy {
  const _$OverlandBatchLocationsInnerPropertiesCWProxyImpl(this._value);

  final OverlandBatchLocationsInnerProperties _value;

  @override
  OverlandBatchLocationsInnerProperties timestamp(DateTime? timestamp) =>
      call(timestamp: timestamp);

  @override
  OverlandBatchLocationsInnerProperties horizontalAccuracy(
    num? horizontalAccuracy,
  ) => call(horizontalAccuracy: horizontalAccuracy);

  @override
  OverlandBatchLocationsInnerProperties altitude(num? altitude) =>
      call(altitude: altitude);

  @override
  OverlandBatchLocationsInnerProperties speed(num? speed) => call(speed: speed);

  @override
  OverlandBatchLocationsInnerProperties course(num? course) =>
      call(course: course);

  @override
  OverlandBatchLocationsInnerProperties motion(List<String>? motion) =>
      call(motion: motion);

  @override
  OverlandBatchLocationsInnerProperties batteryLevel(num? batteryLevel) =>
      call(batteryLevel: batteryLevel);

  @override
  OverlandBatchLocationsInnerProperties batteryState(String? batteryState) =>
      call(batteryState: batteryState);

  @override
  OverlandBatchLocationsInnerProperties wifi(String? wifi) => call(wifi: wifi);

  @override
  OverlandBatchLocationsInnerProperties deviceId(String? deviceId) =>
      call(deviceId: deviceId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OverlandBatchLocationsInnerProperties(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OverlandBatchLocationsInnerProperties(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OverlandBatchLocationsInnerProperties call({
    Object? timestamp = const $CopyWithPlaceholder(),
    Object? horizontalAccuracy = const $CopyWithPlaceholder(),
    Object? altitude = const $CopyWithPlaceholder(),
    Object? speed = const $CopyWithPlaceholder(),
    Object? course = const $CopyWithPlaceholder(),
    Object? motion = const $CopyWithPlaceholder(),
    Object? batteryLevel = const $CopyWithPlaceholder(),
    Object? batteryState = const $CopyWithPlaceholder(),
    Object? wifi = const $CopyWithPlaceholder(),
    Object? deviceId = const $CopyWithPlaceholder(),
  }) {
    return OverlandBatchLocationsInnerProperties(
      timestamp: timestamp == const $CopyWithPlaceholder()
          ? _value.timestamp
          // ignore: cast_nullable_to_non_nullable
          : timestamp as DateTime?,
      horizontalAccuracy: horizontalAccuracy == const $CopyWithPlaceholder()
          ? _value.horizontalAccuracy
          // ignore: cast_nullable_to_non_nullable
          : horizontalAccuracy as num?,
      altitude: altitude == const $CopyWithPlaceholder()
          ? _value.altitude
          // ignore: cast_nullable_to_non_nullable
          : altitude as num?,
      speed: speed == const $CopyWithPlaceholder()
          ? _value.speed
          // ignore: cast_nullable_to_non_nullable
          : speed as num?,
      course: course == const $CopyWithPlaceholder()
          ? _value.course
          // ignore: cast_nullable_to_non_nullable
          : course as num?,
      motion: motion == const $CopyWithPlaceholder()
          ? _value.motion
          // ignore: cast_nullable_to_non_nullable
          : motion as List<String>?,
      batteryLevel: batteryLevel == const $CopyWithPlaceholder()
          ? _value.batteryLevel
          // ignore: cast_nullable_to_non_nullable
          : batteryLevel as num?,
      batteryState: batteryState == const $CopyWithPlaceholder()
          ? _value.batteryState
          // ignore: cast_nullable_to_non_nullable
          : batteryState as String?,
      wifi: wifi == const $CopyWithPlaceholder()
          ? _value.wifi
          // ignore: cast_nullable_to_non_nullable
          : wifi as String?,
      deviceId: deviceId == const $CopyWithPlaceholder()
          ? _value.deviceId
          // ignore: cast_nullable_to_non_nullable
          : deviceId as String?,
    );
  }
}

extension $OverlandBatchLocationsInnerPropertiesCopyWith
    on OverlandBatchLocationsInnerProperties {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOverlandBatchLocationsInnerProperties.copyWith(...)` or `instanceOfOverlandBatchLocationsInnerProperties.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OverlandBatchLocationsInnerPropertiesCWProxy get copyWith =>
      _$OverlandBatchLocationsInnerPropertiesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OverlandBatchLocationsInnerProperties
_$OverlandBatchLocationsInnerPropertiesFromJson(Map<String, dynamic> json) =>
    $checkedCreate(
      'OverlandBatchLocationsInnerProperties',
      json,
      ($checkedConvert) {
        final val = OverlandBatchLocationsInnerProperties(
          timestamp: $checkedConvert(
            'timestamp',
            (v) => v == null ? null : DateTime.parse(v as String),
          ),
          horizontalAccuracy: $checkedConvert(
            'horizontal_accuracy',
            (v) => v as num?,
          ),
          altitude: $checkedConvert('altitude', (v) => v as num?),
          speed: $checkedConvert('speed', (v) => v as num?),
          course: $checkedConvert('course', (v) => v as num?),
          motion: $checkedConvert(
            'motion',
            (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
          ),
          batteryLevel: $checkedConvert('battery_level', (v) => v as num?),
          batteryState: $checkedConvert('battery_state', (v) => v as String?),
          wifi: $checkedConvert('wifi', (v) => v as String?),
          deviceId: $checkedConvert('device_id', (v) => v as String?),
        );
        return val;
      },
      fieldKeyMap: const {
        'horizontalAccuracy': 'horizontal_accuracy',
        'batteryLevel': 'battery_level',
        'batteryState': 'battery_state',
        'deviceId': 'device_id',
      },
    );

Map<String, dynamic> _$OverlandBatchLocationsInnerPropertiesToJson(
  OverlandBatchLocationsInnerProperties instance,
) => <String, dynamic>{
  'timestamp': ?instance.timestamp?.toIso8601String(),
  'horizontal_accuracy': ?instance.horizontalAccuracy,
  'altitude': ?instance.altitude,
  'speed': ?instance.speed,
  'course': ?instance.course,
  'motion': ?instance.motion,
  'battery_level': ?instance.batteryLevel,
  'battery_state': ?instance.batteryState,
  'wifi': ?instance.wifi,
  'device_id': ?instance.deviceId,
};
