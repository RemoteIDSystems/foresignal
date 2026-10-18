// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'sensor_all_of_capabilities.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SensorAllOfCapabilitiesCWProxy {
  SensorAllOfCapabilities operator_(String? operator_);

  SensorAllOfCapabilities sensorTypes(List<String>? sensorTypes);

  SensorAllOfCapabilities resolutionM(num? resolutionM);

  SensorAllOfCapabilities swathKm(num? swathKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SensorAllOfCapabilities(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SensorAllOfCapabilities(...).copyWith(id: 12, name: "My name")
  /// ```
  SensorAllOfCapabilities call({
    String? operator_,
    List<String>? sensorTypes,
    num? resolutionM,
    num? swathKm,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSensorAllOfCapabilities.copyWith(...)` or call `instanceOfSensorAllOfCapabilities.copyWith.fieldName(value)` for a single field.
class _$SensorAllOfCapabilitiesCWProxyImpl
    implements _$SensorAllOfCapabilitiesCWProxy {
  const _$SensorAllOfCapabilitiesCWProxyImpl(this._value);

  final SensorAllOfCapabilities _value;

  @override
  SensorAllOfCapabilities operator_(String? operator_) =>
      call(operator_: operator_);

  @override
  SensorAllOfCapabilities sensorTypes(List<String>? sensorTypes) =>
      call(sensorTypes: sensorTypes);

  @override
  SensorAllOfCapabilities resolutionM(num? resolutionM) =>
      call(resolutionM: resolutionM);

  @override
  SensorAllOfCapabilities swathKm(num? swathKm) => call(swathKm: swathKm);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SensorAllOfCapabilities(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SensorAllOfCapabilities(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SensorAllOfCapabilities call({
    Object? operator_ = const $CopyWithPlaceholder(),
    Object? sensorTypes = const $CopyWithPlaceholder(),
    Object? resolutionM = const $CopyWithPlaceholder(),
    Object? swathKm = const $CopyWithPlaceholder(),
  }) {
    return SensorAllOfCapabilities(
      operator_: operator_ == const $CopyWithPlaceholder()
          ? _value.operator_
          // ignore: cast_nullable_to_non_nullable
          : operator_ as String?,
      sensorTypes: sensorTypes == const $CopyWithPlaceholder()
          ? _value.sensorTypes
          // ignore: cast_nullable_to_non_nullable
          : sensorTypes as List<String>?,
      resolutionM: resolutionM == const $CopyWithPlaceholder()
          ? _value.resolutionM
          // ignore: cast_nullable_to_non_nullable
          : resolutionM as num?,
      swathKm: swathKm == const $CopyWithPlaceholder()
          ? _value.swathKm
          // ignore: cast_nullable_to_non_nullable
          : swathKm as num?,
    );
  }
}

extension $SensorAllOfCapabilitiesCopyWith on SensorAllOfCapabilities {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSensorAllOfCapabilities.copyWith(...)` or `instanceOfSensorAllOfCapabilities.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SensorAllOfCapabilitiesCWProxy get copyWith =>
      _$SensorAllOfCapabilitiesCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SensorAllOfCapabilities _$SensorAllOfCapabilitiesFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SensorAllOfCapabilities', json, ($checkedConvert) {
  final val = SensorAllOfCapabilities(
    operator_: $checkedConvert('operator', (v) => v as String?),
    sensorTypes: $checkedConvert(
      'sensorTypes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    resolutionM: $checkedConvert('resolutionM', (v) => v as num?),
    swathKm: $checkedConvert('swathKm', (v) => v as num?),
  );
  return val;
}, fieldKeyMap: const {'operator_': 'operator'});

Map<String, dynamic> _$SensorAllOfCapabilitiesToJson(
  SensorAllOfCapabilities instance,
) => <String, dynamic>{
  'operator': ?instance.operator_,
  'sensorTypes': ?instance.sensorTypes,
  'resolutionM': ?instance.resolutionM,
  'swathKm': ?instance.swathKm,
};
