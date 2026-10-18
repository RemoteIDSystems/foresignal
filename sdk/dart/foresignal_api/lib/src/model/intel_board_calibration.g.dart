// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'intel_board_calibration.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IntelBoardCalibrationCWProxy {
  IntelBoardCalibration window(String? window);

  IntelBoardCalibration ensemble(IntelBoardCalibrationEnsemble? ensemble);

  IntelBoardCalibration operator_(IntelBoardCalibrationOperator? operator_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardCalibration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardCalibration(...).copyWith(id: 12, name: "My name")
  /// ```
  IntelBoardCalibration call({
    String? window,
    IntelBoardCalibrationEnsemble? ensemble,
    IntelBoardCalibrationOperator? operator_,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIntelBoardCalibration.copyWith(...)` or call `instanceOfIntelBoardCalibration.copyWith.fieldName(value)` for a single field.
class _$IntelBoardCalibrationCWProxyImpl
    implements _$IntelBoardCalibrationCWProxy {
  const _$IntelBoardCalibrationCWProxyImpl(this._value);

  final IntelBoardCalibration _value;

  @override
  IntelBoardCalibration window(String? window) => call(window: window);

  @override
  IntelBoardCalibration ensemble(IntelBoardCalibrationEnsemble? ensemble) =>
      call(ensemble: ensemble);

  @override
  IntelBoardCalibration operator_(IntelBoardCalibrationOperator? operator_) =>
      call(operator_: operator_);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IntelBoardCalibration(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IntelBoardCalibration(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IntelBoardCalibration call({
    Object? window = const $CopyWithPlaceholder(),
    Object? ensemble = const $CopyWithPlaceholder(),
    Object? operator_ = const $CopyWithPlaceholder(),
  }) {
    return IntelBoardCalibration(
      window: window == const $CopyWithPlaceholder()
          ? _value.window
          // ignore: cast_nullable_to_non_nullable
          : window as String?,
      ensemble: ensemble == const $CopyWithPlaceholder()
          ? _value.ensemble
          // ignore: cast_nullable_to_non_nullable
          : ensemble as IntelBoardCalibrationEnsemble?,
      operator_: operator_ == const $CopyWithPlaceholder()
          ? _value.operator_
          // ignore: cast_nullable_to_non_nullable
          : operator_ as IntelBoardCalibrationOperator?,
    );
  }
}

extension $IntelBoardCalibrationCopyWith on IntelBoardCalibration {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIntelBoardCalibration.copyWith(...)` or `instanceOfIntelBoardCalibration.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IntelBoardCalibrationCWProxy get copyWith =>
      _$IntelBoardCalibrationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IntelBoardCalibration _$IntelBoardCalibrationFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IntelBoardCalibration', json, ($checkedConvert) {
  final val = IntelBoardCalibration(
    window: $checkedConvert('window', (v) => v as String? ?? 'P90D'),
    ensemble: $checkedConvert(
      'ensemble',
      (v) => v == null
          ? null
          : IntelBoardCalibrationEnsemble.fromJson(v as Map<String, dynamic>),
    ),
    operator_: $checkedConvert(
      'operator',
      (v) => v == null
          ? null
          : IntelBoardCalibrationOperator.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
}, fieldKeyMap: const {'operator_': 'operator'});

Map<String, dynamic> _$IntelBoardCalibrationToJson(
  IntelBoardCalibration instance,
) => <String, dynamic>{
  'window': ?instance.window,
  'ensemble': ?instance.ensemble?.toJson(),
  'operator': ?instance.operator_?.toJson(),
};
