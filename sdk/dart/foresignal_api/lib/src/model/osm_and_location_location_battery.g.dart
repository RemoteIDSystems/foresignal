// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'osm_and_location_location_battery.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$OsmAndLocationLocationBatteryCWProxy {
  OsmAndLocationLocationBattery level(num? level);

  OsmAndLocationLocationBattery isCharging(bool? isCharging);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OsmAndLocationLocationBattery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OsmAndLocationLocationBattery(...).copyWith(id: 12, name: "My name")
  /// ```
  OsmAndLocationLocationBattery call({num? level, bool? isCharging});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfOsmAndLocationLocationBattery.copyWith(...)` or call `instanceOfOsmAndLocationLocationBattery.copyWith.fieldName(value)` for a single field.
class _$OsmAndLocationLocationBatteryCWProxyImpl
    implements _$OsmAndLocationLocationBatteryCWProxy {
  const _$OsmAndLocationLocationBatteryCWProxyImpl(this._value);

  final OsmAndLocationLocationBattery _value;

  @override
  OsmAndLocationLocationBattery level(num? level) => call(level: level);

  @override
  OsmAndLocationLocationBattery isCharging(bool? isCharging) =>
      call(isCharging: isCharging);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `OsmAndLocationLocationBattery(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// OsmAndLocationLocationBattery(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  OsmAndLocationLocationBattery call({
    Object? level = const $CopyWithPlaceholder(),
    Object? isCharging = const $CopyWithPlaceholder(),
  }) {
    return OsmAndLocationLocationBattery(
      level: level == const $CopyWithPlaceholder()
          ? _value.level
          // ignore: cast_nullable_to_non_nullable
          : level as num?,
      isCharging: isCharging == const $CopyWithPlaceholder()
          ? _value.isCharging
          // ignore: cast_nullable_to_non_nullable
          : isCharging as bool?,
    );
  }
}

extension $OsmAndLocationLocationBatteryCopyWith
    on OsmAndLocationLocationBattery {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfOsmAndLocationLocationBattery.copyWith(...)` or `instanceOfOsmAndLocationLocationBattery.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$OsmAndLocationLocationBatteryCWProxy get copyWith =>
      _$OsmAndLocationLocationBatteryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

OsmAndLocationLocationBattery _$OsmAndLocationLocationBatteryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('OsmAndLocationLocationBattery', json, ($checkedConvert) {
  final val = OsmAndLocationLocationBattery(
    level: $checkedConvert('level', (v) => v as num?),
    isCharging: $checkedConvert('is_charging', (v) => v as bool?),
  );
  return val;
}, fieldKeyMap: const {'isCharging': 'is_charging'});

Map<String, dynamic> _$OsmAndLocationLocationBatteryToJson(
  OsmAndLocationLocationBattery instance,
) => <String, dynamic>{
  'level': ?instance.level,
  'is_charging': ?instance.isCharging,
};
