// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ensemble_refit_result.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EnsembleRefitResultCWProxy {
  EnsembleRefitResult fittedAt(DateTime fittedAt);

  EnsembleRefitResult extremizing(EnsembleRefitResultExtremizing extremizing);

  EnsembleRefitResult platt(List<EnsembleRefitResultPlattInner> platt);

  EnsembleRefitResult weights(List<EnsembleRefitResultWeightsInner> weights);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EnsembleRefitResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EnsembleRefitResult(...).copyWith(id: 12, name: "My name")
  /// ```
  EnsembleRefitResult call({
    DateTime fittedAt,
    EnsembleRefitResultExtremizing extremizing,
    List<EnsembleRefitResultPlattInner> platt,
    List<EnsembleRefitResultWeightsInner> weights,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEnsembleRefitResult.copyWith(...)` or call `instanceOfEnsembleRefitResult.copyWith.fieldName(value)` for a single field.
class _$EnsembleRefitResultCWProxyImpl implements _$EnsembleRefitResultCWProxy {
  const _$EnsembleRefitResultCWProxyImpl(this._value);

  final EnsembleRefitResult _value;

  @override
  EnsembleRefitResult fittedAt(DateTime fittedAt) => call(fittedAt: fittedAt);

  @override
  EnsembleRefitResult extremizing(EnsembleRefitResultExtremizing extremizing) =>
      call(extremizing: extremizing);

  @override
  EnsembleRefitResult platt(List<EnsembleRefitResultPlattInner> platt) =>
      call(platt: platt);

  @override
  EnsembleRefitResult weights(List<EnsembleRefitResultWeightsInner> weights) =>
      call(weights: weights);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EnsembleRefitResult(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EnsembleRefitResult(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EnsembleRefitResult call({
    Object? fittedAt = const $CopyWithPlaceholder(),
    Object? extremizing = const $CopyWithPlaceholder(),
    Object? platt = const $CopyWithPlaceholder(),
    Object? weights = const $CopyWithPlaceholder(),
  }) {
    return EnsembleRefitResult(
      fittedAt: fittedAt == const $CopyWithPlaceholder() || fittedAt == null
          ? _value.fittedAt
          // ignore: cast_nullable_to_non_nullable
          : fittedAt as DateTime,
      extremizing:
          extremizing == const $CopyWithPlaceholder() || extremizing == null
          ? _value.extremizing
          // ignore: cast_nullable_to_non_nullable
          : extremizing as EnsembleRefitResultExtremizing,
      platt: platt == const $CopyWithPlaceholder() || platt == null
          ? _value.platt
          // ignore: cast_nullable_to_non_nullable
          : platt as List<EnsembleRefitResultPlattInner>,
      weights: weights == const $CopyWithPlaceholder() || weights == null
          ? _value.weights
          // ignore: cast_nullable_to_non_nullable
          : weights as List<EnsembleRefitResultWeightsInner>,
    );
  }
}

extension $EnsembleRefitResultCopyWith on EnsembleRefitResult {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEnsembleRefitResult.copyWith(...)` or `instanceOfEnsembleRefitResult.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EnsembleRefitResultCWProxy get copyWith =>
      _$EnsembleRefitResultCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EnsembleRefitResult _$EnsembleRefitResultFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EnsembleRefitResult', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['fittedAt', 'extremizing', 'platt', 'weights'],
  );
  final val = EnsembleRefitResult(
    fittedAt: $checkedConvert('fittedAt', (v) => DateTime.parse(v as String)),
    extremizing: $checkedConvert(
      'extremizing',
      (v) => EnsembleRefitResultExtremizing.fromJson(v as Map<String, dynamic>),
    ),
    platt: $checkedConvert(
      'platt',
      (v) => (v as List<dynamic>)
          .map(
            (e) => EnsembleRefitResultPlattInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
    weights: $checkedConvert(
      'weights',
      (v) => (v as List<dynamic>)
          .map(
            (e) => EnsembleRefitResultWeightsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$EnsembleRefitResultToJson(
  EnsembleRefitResult instance,
) => <String, dynamic>{
  'fittedAt': instance.fittedAt.toIso8601String(),
  'extremizing': instance.extremizing.toJson(),
  'platt': instance.platt.map((e) => e.toJson()).toList(),
  'weights': instance.weights.map((e) => e.toJson()).toList(),
};
