// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_board_calibration_operator.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelBoardCalibrationOperatorCWProxy {
  IntelBoardCalibrationOperator brierSkill(num? brierSkill);

  IntelBoardCalibrationOperator calibration(String? calibration);

  IntelBoardCalibrationOperator n(int n);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardCalibrationOperator(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardCalibrationOperator(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelBoardCalibrationOperator call({
    num? brierSkill,
    String? calibration,
    int n,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelBoardCalibrationOperator.copyWith(...)` or call `instanceOfIntelBoardCalibrationOperator.copyWith.fieldName(value)` for a single field.
class _$IntelBoardCalibrationOperatorCWProxyImpl
    implements _$IntelBoardCalibrationOperatorCWProxy {
  const _$IntelBoardCalibrationOperatorCWProxyImpl(this._value);

  final IntelBoardCalibrationOperator _value;

  @override
  IntelBoardCalibrationOperator brierSkill(num? brierSkill) =>
      call(brierSkill: brierSkill);

  @override
  IntelBoardCalibrationOperator calibration(String? calibration) =>
      call(calibration: calibration);

  @override
  IntelBoardCalibrationOperator n(int n) => call(n: n);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardCalibrationOperator(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardCalibrationOperator(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelBoardCalibrationOperator call({
    Object? brierSkill = const $CopyWithPlaceholder(),
    Object? calibration = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
  }) {
    return IntelBoardCalibrationOperator(
      brierSkill: brierSkill == const $CopyWithPlaceholder()
          ? _value.brierSkill
          // ignore: cast_nullable_to_non_nullable
          : brierSkill as num?,
      calibration: calibration == const $CopyWithPlaceholder()
          ? _value.calibration
          // ignore: cast_nullable_to_non_nullable
          : calibration as String?,
      n: n == const $CopyWithPlaceholder() || n == null
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int,
    );
  }
}

extension $IntelBoardCalibrationOperatorCopyWith
    on IntelBoardCalibrationOperator {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelBoardCalibrationOperator.copyWith(...)` or `instanceOfIntelBoardCalibrationOperator.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelBoardCalibrationOperatorCWProxy get copyWith =>
      _$IntelBoardCalibrationOperatorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelBoardCalibrationOperator _$IntelBoardCalibrationOperatorFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelBoardCalibrationOperator', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['n']);
  final val = IntelBoardCalibrationOperator(
    brierSkill: $checkedConvert('brierSkill', (v) => v as num?),
    calibration: $checkedConvert('calibration', (v) => v as String?),
    n: $checkedConvert('n', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$IntelBoardCalibrationOperatorToJson(
  IntelBoardCalibrationOperator instance,
) => <String, dynamic>{
  'brierSkill': ?instance.brierSkill,
  'calibration': ?instance.calibration,
  'n': instance.n,
};
