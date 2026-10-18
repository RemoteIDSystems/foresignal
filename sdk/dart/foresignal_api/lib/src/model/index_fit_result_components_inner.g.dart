// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'index_fit_result_components_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndexFitResultComponentsInnerCWProxy {
  IndexFitResultComponentsInner key(String key);

  IndexFitResultComponentsInner coefficient(num coefficient);

  IndexFitResultComponentsInner standardError(num? standardError);

  IndexFitResultComponentsInner pValue(num? pValue);

  IndexFitResultComponentsInner significant(bool? significant);

  IndexFitResultComponentsInner currentWeight(num? currentWeight);

  IndexFitResultComponentsInner proposedWeight(num proposedWeight);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndexFitResultComponentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndexFitResultComponentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  IndexFitResultComponentsInner call({
    String key,
    num coefficient,
    num? standardError,
    num? pValue,
    bool? significant,
    num? currentWeight,
    num proposedWeight,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndexFitResultComponentsInner.copyWith(...)` or call `instanceOfIndexFitResultComponentsInner.copyWith.fieldName(value)` for a single field.
class _$IndexFitResultComponentsInnerCWProxyImpl
    implements _$IndexFitResultComponentsInnerCWProxy {
  const _$IndexFitResultComponentsInnerCWProxyImpl(this._value);

  final IndexFitResultComponentsInner _value;

  @override
  IndexFitResultComponentsInner key(String key) => call(key: key);

  @override
  IndexFitResultComponentsInner coefficient(num coefficient) =>
      call(coefficient: coefficient);

  @override
  IndexFitResultComponentsInner standardError(num? standardError) =>
      call(standardError: standardError);

  @override
  IndexFitResultComponentsInner pValue(num? pValue) => call(pValue: pValue);

  @override
  IndexFitResultComponentsInner significant(bool? significant) =>
      call(significant: significant);

  @override
  IndexFitResultComponentsInner currentWeight(num? currentWeight) =>
      call(currentWeight: currentWeight);

  @override
  IndexFitResultComponentsInner proposedWeight(num proposedWeight) =>
      call(proposedWeight: proposedWeight);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndexFitResultComponentsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndexFitResultComponentsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndexFitResultComponentsInner call({
    Object? key = const $CopyWithPlaceholder(),
    Object? coefficient = const $CopyWithPlaceholder(),
    Object? standardError = const $CopyWithPlaceholder(),
    Object? pValue = const $CopyWithPlaceholder(),
    Object? significant = const $CopyWithPlaceholder(),
    Object? currentWeight = const $CopyWithPlaceholder(),
    Object? proposedWeight = const $CopyWithPlaceholder(),
  }) {
    return IndexFitResultComponentsInner(
      key: key == const $CopyWithPlaceholder() || key == null
          ? _value.key
          // ignore: cast_nullable_to_non_nullable
          : key as String,
      coefficient:
          coefficient == const $CopyWithPlaceholder() || coefficient == null
          ? _value.coefficient
          // ignore: cast_nullable_to_non_nullable
          : coefficient as num,
      standardError: standardError == const $CopyWithPlaceholder()
          ? _value.standardError
          // ignore: cast_nullable_to_non_nullable
          : standardError as num?,
      pValue: pValue == const $CopyWithPlaceholder()
          ? _value.pValue
          // ignore: cast_nullable_to_non_nullable
          : pValue as num?,
      significant: significant == const $CopyWithPlaceholder()
          ? _value.significant
          // ignore: cast_nullable_to_non_nullable
          : significant as bool?,
      currentWeight: currentWeight == const $CopyWithPlaceholder()
          ? _value.currentWeight
          // ignore: cast_nullable_to_non_nullable
          : currentWeight as num?,
      proposedWeight:
          proposedWeight == const $CopyWithPlaceholder() ||
              proposedWeight == null
          ? _value.proposedWeight
          // ignore: cast_nullable_to_non_nullable
          : proposedWeight as num,
    );
  }
}

extension $IndexFitResultComponentsInnerCopyWith
    on IndexFitResultComponentsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndexFitResultComponentsInner.copyWith(...)` or `instanceOfIndexFitResultComponentsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndexFitResultComponentsInnerCWProxy get copyWith =>
      _$IndexFitResultComponentsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndexFitResultComponentsInner _$IndexFitResultComponentsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('IndexFitResultComponentsInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['key', 'coefficient', 'proposedWeight'],
  );
  final val = IndexFitResultComponentsInner(
    key: $checkedConvert('key', (v) => v as String),
    coefficient: $checkedConvert('coefficient', (v) => v as num),
    standardError: $checkedConvert('standardError', (v) => v as num?),
    pValue: $checkedConvert('pValue', (v) => v as num?),
    significant: $checkedConvert('significant', (v) => v as bool?),
    currentWeight: $checkedConvert('currentWeight', (v) => v as num?),
    proposedWeight: $checkedConvert('proposedWeight', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$IndexFitResultComponentsInnerToJson(
  IndexFitResultComponentsInner instance,
) => <String, dynamic>{
  'key': instance.key,
  'coefficient': instance.coefficient,
  'standardError': ?instance.standardError,
  'pValue': ?instance.pValue,
  'significant': ?instance.significant,
  'currentWeight': ?instance.currentWeight,
  'proposedWeight': instance.proposedWeight,
};
