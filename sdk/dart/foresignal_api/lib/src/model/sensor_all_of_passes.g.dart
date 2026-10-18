// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sensor_all_of_passes.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SensorAllOfPassesCWProxy {
  SensorAllOfPasses aos(DateTime aos);

  SensorAllOfPasses tca(DateTime? tca);

  SensorAllOfPasses los(DateTime los);

  SensorAllOfPasses maxElevationDeg(num maxElevationDeg);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SensorAllOfPasses(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SensorAllOfPasses(...).copyWith(id: 12, name: "My name")
  /// ```
  SensorAllOfPasses call({
    DateTime aos,
    DateTime? tca,
    DateTime los,
    num maxElevationDeg,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSensorAllOfPasses.copyWith(...)` or call `instanceOfSensorAllOfPasses.copyWith.fieldName(value)` for a single field.
class _$SensorAllOfPassesCWProxyImpl implements _$SensorAllOfPassesCWProxy {
  const _$SensorAllOfPassesCWProxyImpl(this._value);

  final SensorAllOfPasses _value;

  @override
  SensorAllOfPasses aos(DateTime aos) => call(aos: aos);

  @override
  SensorAllOfPasses tca(DateTime? tca) => call(tca: tca);

  @override
  SensorAllOfPasses los(DateTime los) => call(los: los);

  @override
  SensorAllOfPasses maxElevationDeg(num maxElevationDeg) =>
      call(maxElevationDeg: maxElevationDeg);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SensorAllOfPasses(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SensorAllOfPasses(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SensorAllOfPasses call({
    Object? aos = const $CopyWithPlaceholder(),
    Object? tca = const $CopyWithPlaceholder(),
    Object? los = const $CopyWithPlaceholder(),
    Object? maxElevationDeg = const $CopyWithPlaceholder(),
  }) {
    return SensorAllOfPasses(
      aos: aos == const $CopyWithPlaceholder() || aos == null
          ? _value.aos
          // ignore: cast_nullable_to_non_nullable
          : aos as DateTime,
      tca: tca == const $CopyWithPlaceholder()
          ? _value.tca
          // ignore: cast_nullable_to_non_nullable
          : tca as DateTime?,
      los: los == const $CopyWithPlaceholder() || los == null
          ? _value.los
          // ignore: cast_nullable_to_non_nullable
          : los as DateTime,
      maxElevationDeg:
          maxElevationDeg == const $CopyWithPlaceholder() ||
              maxElevationDeg == null
          ? _value.maxElevationDeg
          // ignore: cast_nullable_to_non_nullable
          : maxElevationDeg as num,
    );
  }
}

extension $SensorAllOfPassesCopyWith on SensorAllOfPasses {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSensorAllOfPasses.copyWith(...)` or `instanceOfSensorAllOfPasses.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SensorAllOfPassesCWProxy get copyWith =>
      _$SensorAllOfPassesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SensorAllOfPasses _$SensorAllOfPassesFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SensorAllOfPasses', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['aos', 'los', 'maxElevationDeg']);
      final val = SensorAllOfPasses(
        aos: $checkedConvert('aos', (v) => DateTime.parse(v as String)),
        tca: $checkedConvert(
          'tca',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        los: $checkedConvert('los', (v) => DateTime.parse(v as String)),
        maxElevationDeg: $checkedConvert('maxElevationDeg', (v) => v as num),
      );
      return val;
    });

Map<String, dynamic> _$SensorAllOfPassesToJson(SensorAllOfPasses instance) =>
    <String, dynamic>{
      'aos': instance.aos.toIso8601String(),
      'tca': ?instance.tca?.toIso8601String(),
      'los': instance.los.toIso8601String(),
      'maxElevationDeg': instance.maxElevationDeg,
    };
