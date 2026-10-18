// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test_result_reverse.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestResultReverseCWProxy {
  PrecursorTestResultReverse hits(int? hits);

  PrecursorTestResultReverse hitRate(num? hitRate);

  PrecursorTestResultReverse lift(num? lift);

  PrecursorTestResultReverse pValue(num? pValue);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestResultReverse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestResultReverse(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTestResultReverse call({
    int? hits,
    num? hitRate,
    num? lift,
    num? pValue,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTestResultReverse.copyWith(...)` or call `instanceOfPrecursorTestResultReverse.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestResultReverseCWProxyImpl
    implements _$PrecursorTestResultReverseCWProxy {
  const _$PrecursorTestResultReverseCWProxyImpl(this._value);

  final PrecursorTestResultReverse _value;

  @override
  PrecursorTestResultReverse hits(int? hits) => call(hits: hits);

  @override
  PrecursorTestResultReverse hitRate(num? hitRate) => call(hitRate: hitRate);

  @override
  PrecursorTestResultReverse lift(num? lift) => call(lift: lift);

  @override
  PrecursorTestResultReverse pValue(num? pValue) => call(pValue: pValue);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestResultReverse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestResultReverse(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTestResultReverse call({
    Object? hits = const $CopyWithPlaceholder(),
    Object? hitRate = const $CopyWithPlaceholder(),
    Object? lift = const $CopyWithPlaceholder(),
    Object? pValue = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTestResultReverse(
      hits: hits == const $CopyWithPlaceholder()
          ? _value.hits
          // ignore: cast_nullable_to_non_nullable
          : hits as int?,
      hitRate: hitRate == const $CopyWithPlaceholder()
          ? _value.hitRate
          // ignore: cast_nullable_to_non_nullable
          : hitRate as num?,
      lift: lift == const $CopyWithPlaceholder()
          ? _value.lift
          // ignore: cast_nullable_to_non_nullable
          : lift as num?,
      pValue: pValue == const $CopyWithPlaceholder()
          ? _value.pValue
          // ignore: cast_nullable_to_non_nullable
          : pValue as num?,
    );
  }
}

extension $PrecursorTestResultReverseCopyWith on PrecursorTestResultReverse {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTestResultReverse.copyWith(...)` or `instanceOfPrecursorTestResultReverse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestResultReverseCWProxy get copyWith =>
      _$PrecursorTestResultReverseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTestResultReverse _$PrecursorTestResultReverseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorTestResultReverse', json, ($checkedConvert) {
  final val = PrecursorTestResultReverse(
    hits: $checkedConvert('hits', (v) => (v as num?)?.toInt()),
    hitRate: $checkedConvert('hitRate', (v) => v as num?),
    lift: $checkedConvert('lift', (v) => v as num?),
    pValue: $checkedConvert('pValue', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$PrecursorTestResultReverseToJson(
  PrecursorTestResultReverse instance,
) => <String, dynamic>{
  'hits': ?instance.hits,
  'hitRate': ?instance.hitRate,
  'lift': ?instance.lift,
  'pValue': ?instance.pValue,
};
