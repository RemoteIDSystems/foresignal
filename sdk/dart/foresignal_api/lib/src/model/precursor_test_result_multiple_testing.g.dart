// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test_result_multiple_testing.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestResultMultipleTestingCWProxy {
  PrecursorTestResultMultipleTesting testsLast30d(int? testsLast30d);

  PrecursorTestResultMultipleTesting bonferroniPValue(num? bonferroniPValue);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestResultMultipleTesting(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestResultMultipleTesting(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTestResultMultipleTesting call({
    int? testsLast30d,
    num? bonferroniPValue,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTestResultMultipleTesting.copyWith(...)` or call `instanceOfPrecursorTestResultMultipleTesting.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestResultMultipleTestingCWProxyImpl
    implements _$PrecursorTestResultMultipleTestingCWProxy {
  const _$PrecursorTestResultMultipleTestingCWProxyImpl(this._value);

  final PrecursorTestResultMultipleTesting _value;

  @override
  PrecursorTestResultMultipleTesting testsLast30d(int? testsLast30d) =>
      call(testsLast30d: testsLast30d);

  @override
  PrecursorTestResultMultipleTesting bonferroniPValue(num? bonferroniPValue) =>
      call(bonferroniPValue: bonferroniPValue);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestResultMultipleTesting(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestResultMultipleTesting(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTestResultMultipleTesting call({
    Object? testsLast30d = const $CopyWithPlaceholder(),
    Object? bonferroniPValue = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTestResultMultipleTesting(
      testsLast30d: testsLast30d == const $CopyWithPlaceholder()
          ? _value.testsLast30d
          // ignore: cast_nullable_to_non_nullable
          : testsLast30d as int?,
      bonferroniPValue: bonferroniPValue == const $CopyWithPlaceholder()
          ? _value.bonferroniPValue
          // ignore: cast_nullable_to_non_nullable
          : bonferroniPValue as num?,
    );
  }
}

extension $PrecursorTestResultMultipleTestingCopyWith
    on PrecursorTestResultMultipleTesting {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTestResultMultipleTesting.copyWith(...)` or `instanceOfPrecursorTestResultMultipleTesting.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestResultMultipleTestingCWProxy get copyWith =>
      _$PrecursorTestResultMultipleTestingCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTestResultMultipleTesting _$PrecursorTestResultMultipleTestingFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorTestResultMultipleTesting', json, (
  $checkedConvert,
) {
  final val = PrecursorTestResultMultipleTesting(
    testsLast30d: $checkedConvert('testsLast30d', (v) => (v as num?)?.toInt()),
    bonferroniPValue: $checkedConvert('bonferroniPValue', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$PrecursorTestResultMultipleTestingToJson(
  PrecursorTestResultMultipleTesting instance,
) => <String, dynamic>{
  'testsLast30d': ?instance.testsLast30d,
  'bonferroniPValue': ?instance.bonferroniPValue,
};
