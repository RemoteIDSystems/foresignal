// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sample_position.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SamplePositionCWProxy {
  SamplePosition lon(num lon);

  SamplePosition lat(num lat);

  SamplePosition observedAt(DateTime observedAt);

  SamplePosition ageSeconds(int ageSeconds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SamplePosition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SamplePosition(...).copyWith(id: 12, name: "My name")
  /// ```
  SamplePosition call({num lon, num lat, DateTime observedAt, int ageSeconds});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSamplePosition.copyWith(...)` or call `instanceOfSamplePosition.copyWith.fieldName(value)` for a single field.
class _$SamplePositionCWProxyImpl implements _$SamplePositionCWProxy {
  const _$SamplePositionCWProxyImpl(this._value);

  final SamplePosition _value;

  @override
  SamplePosition lon(num lon) => call(lon: lon);

  @override
  SamplePosition lat(num lat) => call(lat: lat);

  @override
  SamplePosition observedAt(DateTime observedAt) =>
      call(observedAt: observedAt);

  @override
  SamplePosition ageSeconds(int ageSeconds) => call(ageSeconds: ageSeconds);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SamplePosition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SamplePosition(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SamplePosition call({
    Object? lon = const $CopyWithPlaceholder(),
    Object? lat = const $CopyWithPlaceholder(),
    Object? observedAt = const $CopyWithPlaceholder(),
    Object? ageSeconds = const $CopyWithPlaceholder(),
  }) {
    return SamplePosition(
      lon: lon == const $CopyWithPlaceholder() || lon == null
          ? _value.lon
          // ignore: cast_nullable_to_non_nullable
          : lon as num,
      lat: lat == const $CopyWithPlaceholder() || lat == null
          ? _value.lat
          // ignore: cast_nullable_to_non_nullable
          : lat as num,
      observedAt:
          observedAt == const $CopyWithPlaceholder() || observedAt == null
          ? _value.observedAt
          // ignore: cast_nullable_to_non_nullable
          : observedAt as DateTime,
      ageSeconds:
          ageSeconds == const $CopyWithPlaceholder() || ageSeconds == null
          ? _value.ageSeconds
          // ignore: cast_nullable_to_non_nullable
          : ageSeconds as int,
    );
  }
}

extension $SamplePositionCopyWith on SamplePosition {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSamplePosition.copyWith(...)` or `instanceOfSamplePosition.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SamplePositionCWProxy get copyWith => _$SamplePositionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SamplePosition _$SamplePositionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SamplePosition', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['lon', 'lat', 'observedAt', 'ageSeconds'],
      );
      final val = SamplePosition(
        lon: $checkedConvert('lon', (v) => v as num),
        lat: $checkedConvert('lat', (v) => v as num),
        observedAt: $checkedConvert(
          'observedAt',
          (v) => DateTime.parse(v as String),
        ),
        ageSeconds: $checkedConvert('ageSeconds', (v) => (v as num).toInt()),
      );
      return val;
    });

Map<String, dynamic> _$SamplePositionToJson(SamplePosition instance) =>
    <String, dynamic>{
      'lon': instance.lon,
      'lat': instance.lat,
      'observedAt': instance.observedAt.toIso8601String(),
      'ageSeconds': instance.ageSeconds,
    };
