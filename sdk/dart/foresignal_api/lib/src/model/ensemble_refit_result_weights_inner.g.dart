// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ensemble_refit_result_weights_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EnsembleRefitResultWeightsInnerCWProxy {
  EnsembleRefitResultWeightsInner forecasterId(String forecasterId);

  EnsembleRefitResultWeightsInner category(String category);

  EnsembleRefitResultWeightsInner weight(num weight);

  EnsembleRefitResultWeightsInner n(int? n);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EnsembleRefitResultWeightsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EnsembleRefitResultWeightsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  EnsembleRefitResultWeightsInner call({
    String forecasterId,
    String category,
    num weight,
    int? n,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEnsembleRefitResultWeightsInner.copyWith(...)` or call `instanceOfEnsembleRefitResultWeightsInner.copyWith.fieldName(value)` for a single field.
class _$EnsembleRefitResultWeightsInnerCWProxyImpl
    implements _$EnsembleRefitResultWeightsInnerCWProxy {
  const _$EnsembleRefitResultWeightsInnerCWProxyImpl(this._value);

  final EnsembleRefitResultWeightsInner _value;

  @override
  EnsembleRefitResultWeightsInner forecasterId(String forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  EnsembleRefitResultWeightsInner category(String category) =>
      call(category: category);

  @override
  EnsembleRefitResultWeightsInner weight(num weight) => call(weight: weight);

  @override
  EnsembleRefitResultWeightsInner n(int? n) => call(n: n);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EnsembleRefitResultWeightsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EnsembleRefitResultWeightsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EnsembleRefitResultWeightsInner call({
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? category = const $CopyWithPlaceholder(),
    Object? weight = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
  }) {
    return EnsembleRefitResultWeightsInner(
      forecasterId:
          forecasterId == const $CopyWithPlaceholder() || forecasterId == null
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String,
      category: category == const $CopyWithPlaceholder() || category == null
          ? _value.category
          // ignore: cast_nullable_to_non_nullable
          : category as String,
      weight: weight == const $CopyWithPlaceholder() || weight == null
          ? _value.weight
          // ignore: cast_nullable_to_non_nullable
          : weight as num,
      n: n == const $CopyWithPlaceholder()
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int?,
    );
  }
}

extension $EnsembleRefitResultWeightsInnerCopyWith
    on EnsembleRefitResultWeightsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEnsembleRefitResultWeightsInner.copyWith(...)` or `instanceOfEnsembleRefitResultWeightsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EnsembleRefitResultWeightsInnerCWProxy get copyWith =>
      _$EnsembleRefitResultWeightsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EnsembleRefitResultWeightsInner _$EnsembleRefitResultWeightsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EnsembleRefitResultWeightsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['forecasterId', 'category', 'weight']);
  final val = EnsembleRefitResultWeightsInner(
    forecasterId: $checkedConvert('forecasterId', (v) => v as String),
    category: $checkedConvert('category', (v) => v as String),
    weight: $checkedConvert('weight', (v) => v as num),
    n: $checkedConvert('n', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$EnsembleRefitResultWeightsInnerToJson(
  EnsembleRefitResultWeightsInner instance,
) => <String, dynamic>{
  'forecasterId': instance.forecasterId,
  'category': instance.category,
  'weight': instance.weight,
  'n': ?instance.n,
};
