// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'forecast_evaluation_skipped.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ForecastEvaluationSkippedCWProxy {
  ForecastEvaluationSkipped leakageRisk(int leakageRisk);

  ForecastEvaluationSkipped other(int? other);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastEvaluationSkipped(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastEvaluationSkipped(...).copyWith(id: 12, name: "My name")
  /// ```
  ForecastEvaluationSkipped call({int leakageRisk, int? other});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfForecastEvaluationSkipped.copyWith(...)` or call `instanceOfForecastEvaluationSkipped.copyWith.fieldName(value)` for a single field.
class _$ForecastEvaluationSkippedCWProxyImpl
    implements _$ForecastEvaluationSkippedCWProxy {
  const _$ForecastEvaluationSkippedCWProxyImpl(this._value);

  final ForecastEvaluationSkipped _value;

  @override
  ForecastEvaluationSkipped leakageRisk(int leakageRisk) =>
      call(leakageRisk: leakageRisk);

  @override
  ForecastEvaluationSkipped other(int? other) => call(other: other);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ForecastEvaluationSkipped(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ForecastEvaluationSkipped(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ForecastEvaluationSkipped call({
    Object? leakageRisk = const $CopyWithPlaceholder(),
    Object? other = const $CopyWithPlaceholder(),
  }) {
    return ForecastEvaluationSkipped(
      leakageRisk:
          leakageRisk == const $CopyWithPlaceholder() || leakageRisk == null
          ? _value.leakageRisk
          // ignore: cast_nullable_to_non_nullable
          : leakageRisk as int,
      other: other == const $CopyWithPlaceholder()
          ? _value.other
          // ignore: cast_nullable_to_non_nullable
          : other as int?,
    );
  }
}

extension $ForecastEvaluationSkippedCopyWith on ForecastEvaluationSkipped {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfForecastEvaluationSkipped.copyWith(...)` or `instanceOfForecastEvaluationSkipped.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ForecastEvaluationSkippedCWProxy get copyWith =>
      _$ForecastEvaluationSkippedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ForecastEvaluationSkipped _$ForecastEvaluationSkippedFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ForecastEvaluationSkipped', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['leakageRisk']);
  final val = ForecastEvaluationSkipped(
    leakageRisk: $checkedConvert('leakageRisk', (v) => (v as num).toInt()),
    other: $checkedConvert('other', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$ForecastEvaluationSkippedToJson(
  ForecastEvaluationSkipped instance,
) => <String, dynamic>{
  'leakageRisk': instance.leakageRisk,
  'other': ?instance.other,
};
