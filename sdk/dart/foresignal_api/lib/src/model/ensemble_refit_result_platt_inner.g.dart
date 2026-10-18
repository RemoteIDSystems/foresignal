// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ensemble_refit_result_platt_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EnsembleRefitResultPlattInnerCWProxy {
  EnsembleRefitResultPlattInner forecasterId(String forecasterId);

  EnsembleRefitResultPlattInner category(String category);

  EnsembleRefitResultPlattInner a(num a);

  EnsembleRefitResultPlattInner b(num b);

  EnsembleRefitResultPlattInner n(int n);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EnsembleRefitResultPlattInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EnsembleRefitResultPlattInner(...).copyWith(id: 12, name: "My name")
  /// ```
  EnsembleRefitResultPlattInner call({
    String forecasterId,
    String category,
    num a,
    num b,
    int n,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEnsembleRefitResultPlattInner.copyWith(...)` or call `instanceOfEnsembleRefitResultPlattInner.copyWith.fieldName(value)` for a single field.
class _$EnsembleRefitResultPlattInnerCWProxyImpl
    implements _$EnsembleRefitResultPlattInnerCWProxy {
  const _$EnsembleRefitResultPlattInnerCWProxyImpl(this._value);

  final EnsembleRefitResultPlattInner _value;

  @override
  EnsembleRefitResultPlattInner forecasterId(String forecasterId) =>
      call(forecasterId: forecasterId);

  @override
  EnsembleRefitResultPlattInner category(String category) =>
      call(category: category);

  @override
  EnsembleRefitResultPlattInner a(num a) => call(a: a);

  @override
  EnsembleRefitResultPlattInner b(num b) => call(b: b);

  @override
  EnsembleRefitResultPlattInner n(int n) => call(n: n);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EnsembleRefitResultPlattInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EnsembleRefitResultPlattInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EnsembleRefitResultPlattInner call({
    Object? forecasterId = const $CopyWithPlaceholder(),
    Object? category = const $CopyWithPlaceholder(),
    Object? a = const $CopyWithPlaceholder(),
    Object? b = const $CopyWithPlaceholder(),
    Object? n = const $CopyWithPlaceholder(),
  }) {
    return EnsembleRefitResultPlattInner(
      forecasterId:
          forecasterId == const $CopyWithPlaceholder() || forecasterId == null
          ? _value.forecasterId
          // ignore: cast_nullable_to_non_nullable
          : forecasterId as String,
      category: category == const $CopyWithPlaceholder() || category == null
          ? _value.category
          // ignore: cast_nullable_to_non_nullable
          : category as String,
      a: a == const $CopyWithPlaceholder() || a == null
          ? _value.a
          // ignore: cast_nullable_to_non_nullable
          : a as num,
      b: b == const $CopyWithPlaceholder() || b == null
          ? _value.b
          // ignore: cast_nullable_to_non_nullable
          : b as num,
      n: n == const $CopyWithPlaceholder() || n == null
          ? _value.n
          // ignore: cast_nullable_to_non_nullable
          : n as int,
    );
  }
}

extension $EnsembleRefitResultPlattInnerCopyWith
    on EnsembleRefitResultPlattInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEnsembleRefitResultPlattInner.copyWith(...)` or `instanceOfEnsembleRefitResultPlattInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EnsembleRefitResultPlattInnerCWProxy get copyWith =>
      _$EnsembleRefitResultPlattInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EnsembleRefitResultPlattInner _$EnsembleRefitResultPlattInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EnsembleRefitResultPlattInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['forecasterId', 'category', 'a', 'b', 'n'],
  );
  final val = EnsembleRefitResultPlattInner(
    forecasterId: $checkedConvert('forecasterId', (v) => v as String),
    category: $checkedConvert('category', (v) => v as String),
    a: $checkedConvert('a', (v) => v as num),
    b: $checkedConvert('b', (v) => v as num),
    n: $checkedConvert('n', (v) => (v as num).toInt()),
  );
  return val;
});

Map<String, dynamic> _$EnsembleRefitResultPlattInnerToJson(
  EnsembleRefitResultPlattInner instance,
) => <String, dynamic>{
  'forecasterId': instance.forecasterId,
  'category': instance.category,
  'a': instance.a,
  'b': instance.b,
  'n': instance.n,
};
