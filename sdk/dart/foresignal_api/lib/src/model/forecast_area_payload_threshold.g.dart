// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_area_payload_threshold.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastAreaPayloadThresholdCWProxy {
  ForecastAreaPayloadThreshold op(ForecastAreaPayloadThresholdOpEnum? op);

  ForecastAreaPayloadThreshold value(num? value);

  ForecastAreaPayloadThreshold units(String? units);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastAreaPayloadThreshold(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastAreaPayloadThreshold(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastAreaPayloadThreshold call({
    ForecastAreaPayloadThresholdOpEnum? op,
    num? value,
    String? units,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastAreaPayloadThreshold.copyWith(...)` or call `instanceOfForecastAreaPayloadThreshold.copyWith.fieldName(value)` for a single field.
class _$ForecastAreaPayloadThresholdCWProxyImpl
    implements _$ForecastAreaPayloadThresholdCWProxy {
  const _$ForecastAreaPayloadThresholdCWProxyImpl(this._value);

  final ForecastAreaPayloadThreshold _value;

  @override
  ForecastAreaPayloadThreshold op(ForecastAreaPayloadThresholdOpEnum? op) =>
      call(op: op);

  @override
  ForecastAreaPayloadThreshold value(num? value) => call(value: value);

  @override
  ForecastAreaPayloadThreshold units(String? units) => call(units: units);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastAreaPayloadThreshold(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastAreaPayloadThreshold(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastAreaPayloadThreshold call({
    Object? op = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? units = const $CopyWithPlaceholder(),
  }) {
    return ForecastAreaPayloadThreshold(
      op: op == const $CopyWithPlaceholder()
          ? _value.op
          // ignore: cast_nullable_to_non_nullable
          : op as ForecastAreaPayloadThresholdOpEnum?,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num?,
      units: units == const $CopyWithPlaceholder()
          ? _value.units
          // ignore: cast_nullable_to_non_nullable
          : units as String?,
    );
  }
}

extension $ForecastAreaPayloadThresholdCopyWith
    on ForecastAreaPayloadThreshold {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastAreaPayloadThreshold.copyWith(...)` or `instanceOfForecastAreaPayloadThreshold.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastAreaPayloadThresholdCWProxy get copyWith =>
      _$ForecastAreaPayloadThresholdCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastAreaPayloadThreshold _$ForecastAreaPayloadThresholdFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastAreaPayloadThreshold', json, ($checkedConvert) {
  final val = ForecastAreaPayloadThreshold(
    op: $checkedConvert(
      'op',
      (v) =>
          $enumDecodeNullable(_$ForecastAreaPayloadThresholdOpEnumEnumMap, v),
    ),
    value: $checkedConvert('value', (v) => v as num?),
    units: $checkedConvert('units', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$ForecastAreaPayloadThresholdToJson(
  ForecastAreaPayloadThreshold instance,
) => <String, dynamic>{
  'op': ?_$ForecastAreaPayloadThresholdOpEnumEnumMap[instance.op],
  'value': ?instance.value,
  'units': ?instance.units,
};

const _$ForecastAreaPayloadThresholdOpEnumEnumMap = {
  ForecastAreaPayloadThresholdOpEnum.greaterThanEqual: '>=',
  ForecastAreaPayloadThresholdOpEnum.lessThanEqual: '<=',
};
