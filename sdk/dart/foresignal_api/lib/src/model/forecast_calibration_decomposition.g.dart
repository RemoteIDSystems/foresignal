// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_calibration_decomposition.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastCalibrationDecompositionCWProxy {
  ForecastCalibrationDecomposition score(num score);

  ForecastCalibrationDecomposition miscalibration(num miscalibration);

  ForecastCalibrationDecomposition discrimination(num discrimination);

  ForecastCalibrationDecomposition uncertainty(num uncertainty);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastCalibrationDecomposition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastCalibrationDecomposition(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastCalibrationDecomposition call({
    num score,
    num miscalibration,
    num discrimination,
    num uncertainty,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastCalibrationDecomposition.copyWith(...)` or call `instanceOfForecastCalibrationDecomposition.copyWith.fieldName(value)` for a single field.
class _$ForecastCalibrationDecompositionCWProxyImpl
    implements _$ForecastCalibrationDecompositionCWProxy {
  const _$ForecastCalibrationDecompositionCWProxyImpl(this._value);

  final ForecastCalibrationDecomposition _value;

  @override
  ForecastCalibrationDecomposition score(num score) => call(score: score);

  @override
  ForecastCalibrationDecomposition miscalibration(num miscalibration) =>
      call(miscalibration: miscalibration);

  @override
  ForecastCalibrationDecomposition discrimination(num discrimination) =>
      call(discrimination: discrimination);

  @override
  ForecastCalibrationDecomposition uncertainty(num uncertainty) =>
      call(uncertainty: uncertainty);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastCalibrationDecomposition(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastCalibrationDecomposition(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastCalibrationDecomposition call({
    Object? score = const $CopyWithPlaceholder(),
    Object? miscalibration = const $CopyWithPlaceholder(),
    Object? discrimination = const $CopyWithPlaceholder(),
    Object? uncertainty = const $CopyWithPlaceholder(),
  }) {
    return ForecastCalibrationDecomposition(
      score: score == const $CopyWithPlaceholder() || score == null
          ? _value.score
          // ignore: cast_nullable_to_non_nullable
          : score as num,
      miscalibration:
          miscalibration == const $CopyWithPlaceholder() ||
              miscalibration == null
          ? _value.miscalibration
          // ignore: cast_nullable_to_non_nullable
          : miscalibration as num,
      discrimination:
          discrimination == const $CopyWithPlaceholder() ||
              discrimination == null
          ? _value.discrimination
          // ignore: cast_nullable_to_non_nullable
          : discrimination as num,
      uncertainty:
          uncertainty == const $CopyWithPlaceholder() || uncertainty == null
          ? _value.uncertainty
          // ignore: cast_nullable_to_non_nullable
          : uncertainty as num,
    );
  }
}

extension $ForecastCalibrationDecompositionCopyWith
    on ForecastCalibrationDecomposition {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastCalibrationDecomposition.copyWith(...)` or `instanceOfForecastCalibrationDecomposition.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastCalibrationDecompositionCWProxy get copyWith =>
      _$ForecastCalibrationDecompositionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastCalibrationDecomposition _$ForecastCalibrationDecompositionFromJson(
  Map<String, dynamic> json,
) =>
    $checkedCreate('ForecastCalibrationDecomposition', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const [
          'score',
          'miscalibration',
          'discrimination',
          'uncertainty',
        ],
      );
      final val = ForecastCalibrationDecomposition(
        score: $checkedConvert('score', (v) => v as num),
        miscalibration: $checkedConvert('miscalibration', (v) => v as num),
        discrimination: $checkedConvert('discrimination', (v) => v as num),
        uncertainty: $checkedConvert('uncertainty', (v) => v as num),
      );
      return val;
    });

Map<String, dynamic> _$ForecastCalibrationDecompositionToJson(
  ForecastCalibrationDecomposition instance,
) => <String, dynamic>{
  'score': instance.score,
  'miscalibration': instance.miscalibration,
  'discrimination': instance.discrimination,
  'uncertainty': instance.uncertainty,
};
