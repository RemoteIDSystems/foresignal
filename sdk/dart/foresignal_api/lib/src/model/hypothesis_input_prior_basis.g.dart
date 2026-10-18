// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_input_prior_basis.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisInputPriorBasisCWProxy {
  HypothesisInputPriorBasis type(HypothesisInputPriorBasisTypeEnum type);

  HypothesisInputPriorBasis ref(String? ref);

  HypothesisInputPriorBasis rationale(String? rationale);

  HypothesisInputPriorBasis value(num? value);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisInputPriorBasis(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisInputPriorBasis(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisInputPriorBasis call({
    HypothesisInputPriorBasisTypeEnum type,
    String? ref,
    String? rationale,
    num? value,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisInputPriorBasis.copyWith(...)` or call `instanceOfHypothesisInputPriorBasis.copyWith.fieldName(value)` for a single field.
class _$HypothesisInputPriorBasisCWProxyImpl
    implements _$HypothesisInputPriorBasisCWProxy {
  const _$HypothesisInputPriorBasisCWProxyImpl(this._value);

  final HypothesisInputPriorBasis _value;

  @override
  HypothesisInputPriorBasis type(HypothesisInputPriorBasisTypeEnum type) =>
      call(type: type);

  @override
  HypothesisInputPriorBasis ref(String? ref) => call(ref: ref);

  @override
  HypothesisInputPriorBasis rationale(String? rationale) =>
      call(rationale: rationale);

  @override
  HypothesisInputPriorBasis value(num? value) => call(value: value);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisInputPriorBasis(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisInputPriorBasis(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisInputPriorBasis call({
    Object? type = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
  }) {
    return HypothesisInputPriorBasis(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as HypothesisInputPriorBasisTypeEnum,
      ref: ref == const $CopyWithPlaceholder()
          ? _value.ref
          // ignore: cast_nullable_to_non_nullable
          : ref as String?,
      rationale: rationale == const $CopyWithPlaceholder()
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as String?,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num?,
    );
  }
}

extension $HypothesisInputPriorBasisCopyWith on HypothesisInputPriorBasis {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisInputPriorBasis.copyWith(...)` or `instanceOfHypothesisInputPriorBasis.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisInputPriorBasisCWProxy get copyWith =>
      _$HypothesisInputPriorBasisCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisInputPriorBasis _$HypothesisInputPriorBasisFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisInputPriorBasis', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type']);
  final val = HypothesisInputPriorBasis(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$HypothesisInputPriorBasisTypeEnumEnumMap, v),
    ),
    ref: $checkedConvert('ref', (v) => v as String?),
    rationale: $checkedConvert('rationale', (v) => v as String?),
    value: $checkedConvert('value', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$HypothesisInputPriorBasisToJson(
  HypothesisInputPriorBasis instance,
) => <String, dynamic>{
  'type': _$HypothesisInputPriorBasisTypeEnumEnumMap[instance.type]!,
  'ref': ?instance.ref,
  'rationale': ?instance.rationale,
  'value': ?instance.value,
};

const _$HypothesisInputPriorBasisTypeEnumEnumMap = {
  HypothesisInputPriorBasisTypeEnum.baseRate: 'base-rate',
  HypothesisInputPriorBasisTypeEnum.analogs: 'analogs',
  HypothesisInputPriorBasisTypeEnum.operator_: 'operator',
};
