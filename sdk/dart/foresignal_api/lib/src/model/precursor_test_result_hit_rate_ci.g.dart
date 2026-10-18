// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test_result_hit_rate_ci.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestResultHitRateCiCWProxy {
  PrecursorTestResultHitRateCi lower(num? lower);

  PrecursorTestResultHitRateCi upper(num? upper);

  PrecursorTestResultHitRateCi method(
    PrecursorTestResultHitRateCiMethodEnum? method,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestResultHitRateCi(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestResultHitRateCi(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTestResultHitRateCi call({
    num? lower,
    num? upper,
    PrecursorTestResultHitRateCiMethodEnum? method,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTestResultHitRateCi.copyWith(...)` or call `instanceOfPrecursorTestResultHitRateCi.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestResultHitRateCiCWProxyImpl
    implements _$PrecursorTestResultHitRateCiCWProxy {
  const _$PrecursorTestResultHitRateCiCWProxyImpl(this._value);

  final PrecursorTestResultHitRateCi _value;

  @override
  PrecursorTestResultHitRateCi lower(num? lower) => call(lower: lower);

  @override
  PrecursorTestResultHitRateCi upper(num? upper) => call(upper: upper);

  @override
  PrecursorTestResultHitRateCi method(
    PrecursorTestResultHitRateCiMethodEnum? method,
  ) => call(method: method);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestResultHitRateCi(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestResultHitRateCi(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTestResultHitRateCi call({
    Object? lower = const $CopyWithPlaceholder(),
    Object? upper = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTestResultHitRateCi(
      lower: lower == const $CopyWithPlaceholder()
          ? _value.lower
          // ignore: cast_nullable_to_non_nullable
          : lower as num?,
      upper: upper == const $CopyWithPlaceholder()
          ? _value.upper
          // ignore: cast_nullable_to_non_nullable
          : upper as num?,
      method: method == const $CopyWithPlaceholder()
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as PrecursorTestResultHitRateCiMethodEnum?,
    );
  }
}

extension $PrecursorTestResultHitRateCiCopyWith
    on PrecursorTestResultHitRateCi {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTestResultHitRateCi.copyWith(...)` or `instanceOfPrecursorTestResultHitRateCi.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestResultHitRateCiCWProxy get copyWith =>
      _$PrecursorTestResultHitRateCiCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTestResultHitRateCi _$PrecursorTestResultHitRateCiFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorTestResultHitRateCi', json, ($checkedConvert) {
  final val = PrecursorTestResultHitRateCi(
    lower: $checkedConvert('lower', (v) => v as num?),
    upper: $checkedConvert('upper', (v) => v as num?),
    method: $checkedConvert(
      'method',
      (v) => $enumDecodeNullable(
        _$PrecursorTestResultHitRateCiMethodEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$PrecursorTestResultHitRateCiToJson(
  PrecursorTestResultHitRateCi instance,
) => <String, dynamic>{
  'lower': ?instance.lower,
  'upper': ?instance.upper,
  'method': ?_$PrecursorTestResultHitRateCiMethodEnumEnumMap[instance.method],
};

const _$PrecursorTestResultHitRateCiMethodEnumEnumMap = {
  PrecursorTestResultHitRateCiMethodEnum.wilson95: 'wilson-95',
};
