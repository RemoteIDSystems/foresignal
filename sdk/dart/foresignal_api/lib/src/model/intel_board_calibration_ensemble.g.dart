// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_board_calibration_ensemble.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelBoardCalibrationEnsembleCWProxy {
  IntelBoardCalibrationEnsemble brierSkill(num? brierSkill);

  IntelBoardCalibrationEnsemble calibration(String? calibration);

  IntelBoardCalibrationEnsemble n(int n);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardCalibrationEnsemble(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardCalibrationEnsemble(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelBoardCalibrationEnsemble call({
    num? brierSkill,
    String? calibration,
    int n,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelBoardCalibrationEnsemble.copyWith(...)` or call `instanceOfIntelBoardCalibrationEnsemble.copyWith.fieldName(value)` for a single field.
class _$IntelBoardCalibrationEnsembleCWProxyImpl
    implements _$IntelBoardCalibrationEnsembleCWProxy {
  const _$IntelBoardCalibrationEnsembleCWProxyImpl(this._value);

  final IntelBoardCalibrationEnsemble _value;

  @override
  IntelBoardCalibrationEnsemble brierSkill(num? brierSkill) =>
      call(brierSkill: brierSkill);

  @override
  IntelBoardCalibrationEnsemble calibration(String? calibration) =>
      call(calibration: calibration);

  @override
  IntelBoardCalibrationEnsemble n(int n) => call(n: n);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardCalibrationEnsemble(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardCalibrationEnsemble(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelBoardCalibrationEnsemble call({
    Object? brierSkill = const $CopyWithPlaceholder(),
    Object? calibration = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
  }) {
    return IntelBoardCalibrationEnsemble(
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

extension $IntelBoardCalibrationEnsembleCopyWith
    on IntelBoardCalibrationEnsemble {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelBoardCalibrationEnsemble.copyWith(...)` or `instanceOfIntelBoardCalibrationEnsemble.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelBoardCalibrationEnsembleCWProxy get copyWith =>
      _$IntelBoardCalibrationEnsembleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelBoardCalibrationEnsemble _$IntelBoardCalibrationEnsembleFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelBoardCalibrationEnsemble', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['n']);
  final val = IntelBoardCalibrationEnsemble(
    brierSkill: $checkedConvert('brierSkill', (v) => v as num?),
    calibration: $checkedConvert('calibration', (v) => v as String?),
    n: $checkedConvert('n', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$IntelBoardCalibrationEnsembleToJson(
  IntelBoardCalibrationEnsemble instance,
) => <String, dynamic>{
  'brierSkill': ?instance.brierSkill,
  'calibration': ?instance.calibration,
  'n': instance.n,
};
