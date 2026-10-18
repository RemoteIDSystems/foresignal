// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'coordination_indicator.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CoordinationIndicatorCWProxy {
  CoordinationIndicator value(num? value);

  CoordinationIndicator nullExpectation(num? nullExpectation);

  CoordinationIndicator pValue(num? pValue);

  CoordinationIndicator qValue(num? qValue);

  CoordinationIndicator n(int? n);

  CoordinationIndicator available(bool available);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CoordinationIndicator(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CoordinationIndicator(...).copyWith(id: 12, name: "My name")
  /// ```
  CoordinationIndicator call({
    num? value,
    num? nullExpectation,
    num? pValue,
    num? qValue,
    int? n,
    bool available,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCoordinationIndicator.copyWith(...)` or call `instanceOfCoordinationIndicator.copyWith.fieldName(value)` for a single field.
class _$CoordinationIndicatorCWProxyImpl
    implements _$CoordinationIndicatorCWProxy {
  const _$CoordinationIndicatorCWProxyImpl(this._value);

  final CoordinationIndicator _value;

  @override
  CoordinationIndicator value(num? value) => call(value: value);

  @override
  CoordinationIndicator nullExpectation(num? nullExpectation) =>
      call(nullExpectation: nullExpectation);

  @override
  CoordinationIndicator pValue(num? pValue) => call(pValue: pValue);

  @override
  CoordinationIndicator qValue(num? qValue) => call(qValue: qValue);

  @override
  CoordinationIndicator n(int? n) => call(n: n);

  @override
  CoordinationIndicator available(bool available) => call(available: available);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CoordinationIndicator(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CoordinationIndicator(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CoordinationIndicator call({
    Object? value = const $CopyWithPlaceholder(),
    Object? nullExpectation = const $CopyWithPlaceholder(),
    Object? pValue = const $CopyWithPlaceholder(),
    Object? qValue = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
    Object? available = const $CopyWithPlaceholder(),
  }) {
    return CoordinationIndicator(
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num?,
      nullExpectation: nullExpectation == const $CopyWithPlaceholder()
          ? _value.nullExpectation
          // ignore: cast_nullable_to_non_nullable
          : nullExpectation as num?,
      pValue: pValue == const $CopyWithPlaceholder()
          ? _value.pValue
          // ignore: cast_nullable_to_non_nullable
          : pValue as num?,
      qValue: qValue == const $CopyWithPlaceholder()
          ? _value.qValue
          // ignore: cast_nullable_to_non_nullable
          : qValue as num?,
      n: n == const $CopyWithPlaceholder()
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int?,
      available: available == const $CopyWithPlaceholder() || available == null
          ? _value.available
          // ignore: cast_nullable_to_non_nullable
          : available as bool,
    );
  }
}

extension $CoordinationIndicatorCopyWith on CoordinationIndicator {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCoordinationIndicator.copyWith(...)` or `instanceOfCoordinationIndicator.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CoordinationIndicatorCWProxy get copyWith =>
      _$CoordinationIndicatorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CoordinationIndicator _$CoordinationIndicatorFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CoordinationIndicator', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['value', 'nullExpectation', 'pValue', 'available'],
  );
  final val = CoordinationIndicator(
    value: $checkedConvert('value', (v) => v as num?),
    nullExpectation: $checkedConvert('nullExpectation', (v) => v as num?),
    pValue: $checkedConvert('pValue', (v) => v as num?),
    qValue: $checkedConvert('qValue', (v) => v as num?),
    n: $checkedConvert('n', (v) => (v as num?)?.toInt()),
    available: $checkedConvert('available', (v) => v as bool),
  );
  return val;
});

Map<String, dynamic> _$CoordinationIndicatorToJson(
  CoordinationIndicator instance,
) => <String, dynamic>{
  'value': instance.value,
  'nullExpectation': instance.nullExpectation,
  'pValue': instance.pValue,
  'qValue': ?instance.qValue,
  'n': ?instance.n,
  'available': instance.available,
};
