// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'hypothesis_prior_basis.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$HypothesisPriorBasisCWProxy {
  HypothesisPriorBasis type(HypothesisPriorBasisTypeEnum type);

  HypothesisPriorBasis ref(String? ref);

  HypothesisPriorBasis rationale(String? rationale);

  HypothesisPriorBasis value(num? value);

  HypothesisPriorBasis refreshedAt(DateTime? refreshedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisPriorBasis(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisPriorBasis(...).copyWith(id: 12, name: "My name")
  /// ```
  HypothesisPriorBasis call({
    HypothesisPriorBasisTypeEnum type,
    String? ref,
    String? rationale,
    num? value,
    DateTime? refreshedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfHypothesisPriorBasis.copyWith(...)` or call `instanceOfHypothesisPriorBasis.copyWith.fieldName(value)` for a single field.
class _$HypothesisPriorBasisCWProxyImpl
    implements _$HypothesisPriorBasisCWProxy {
  const _$HypothesisPriorBasisCWProxyImpl(this._value);

  final HypothesisPriorBasis _value;

  @override
  HypothesisPriorBasis type(HypothesisPriorBasisTypeEnum type) =>
      call(type: type);

  @override
  HypothesisPriorBasis ref(String? ref) => call(ref: ref);

  @override
  HypothesisPriorBasis rationale(String? rationale) =>
      call(rationale: rationale);

  @override
  HypothesisPriorBasis value(num? value) => call(value: value);

  @override
  HypothesisPriorBasis refreshedAt(DateTime? refreshedAt) =>
      call(refreshedAt: refreshedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `HypothesisPriorBasis(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// HypothesisPriorBasis(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  HypothesisPriorBasis call({
    Object? type = const $CopyWithPlaceholder(),
    Object? ref = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? refreshedAt = const $CopyWithPlaceholder(),
  }) {
    return HypothesisPriorBasis(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as HypothesisPriorBasisTypeEnum,
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
      refreshedAt: refreshedAt == const $CopyWithPlaceholder()
          ? _value.refreshedAt
          // ignore: cast_nullable_to_non_nullable
          : refreshedAt as DateTime?,
    );
  }
}

extension $HypothesisPriorBasisCopyWith on HypothesisPriorBasis {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfHypothesisPriorBasis.copyWith(...)` or `instanceOfHypothesisPriorBasis.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$HypothesisPriorBasisCWProxy get copyWith =>
      _$HypothesisPriorBasisCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

HypothesisPriorBasis _$HypothesisPriorBasisFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('HypothesisPriorBasis', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['type']);
  final val = HypothesisPriorBasis(
    type: $checkedConvert(
      'type',
      (v) => $enumDecode(_$HypothesisPriorBasisTypeEnumEnumMap, v),
    ),
    ref: $checkedConvert('ref', (v) => v as String?),
    rationale: $checkedConvert('rationale', (v) => v as String?),
    value: $checkedConvert('value', (v) => v as num?),
    refreshedAt: $checkedConvert(
      'refreshedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$HypothesisPriorBasisToJson(
  HypothesisPriorBasis instance,
) => <String, dynamic>{
  'type': _$HypothesisPriorBasisTypeEnumEnumMap[instance.type]!,
  'ref': ?instance.ref,
  'rationale': ?instance.rationale,
  'value': ?instance.value,
  'refreshedAt': ?instance.refreshedAt?.toIso8601String(),
};

const _$HypothesisPriorBasisTypeEnumEnumMap = {
  HypothesisPriorBasisTypeEnum.baseRate: 'base-rate',
  HypothesisPriorBasisTypeEnum.analogs: 'analogs',
  HypothesisPriorBasisTypeEnum.operator_: 'operator',
};
