// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'rule_quality_precision_ci.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$RuleQualityPrecisionCiCWProxy {
  RuleQualityPrecisionCi lower(num? lower);

  RuleQualityPrecisionCi upper(num? upper);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleQualityPrecisionCi(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleQualityPrecisionCi(...).copyWith(id: 12, name: "My name")
  /// ```
  RuleQualityPrecisionCi call({num? lower, num? upper});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfRuleQualityPrecisionCi.copyWith(...)` or call `instanceOfRuleQualityPrecisionCi.copyWith.fieldName(value)` for a single field.
class _$RuleQualityPrecisionCiCWProxyImpl
    implements _$RuleQualityPrecisionCiCWProxy {
  const _$RuleQualityPrecisionCiCWProxyImpl(this._value);

  final RuleQualityPrecisionCi _value;

  @override
  RuleQualityPrecisionCi lower(num? lower) => call(lower: lower);

  @override
  RuleQualityPrecisionCi upper(num? upper) => call(upper: upper);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `RuleQualityPrecisionCi(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// RuleQualityPrecisionCi(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  RuleQualityPrecisionCi call({
    Object? lower = const $CopyWithPlaceholder(),
    Object? upper = const $CopyWithPlaceholder(),
  }) {
    return RuleQualityPrecisionCi(
      lower: lower == const $CopyWithPlaceholder()
          ? _value.lower
          // ignore: cast_nullable_to_non_nullable
          : lower as num?,
      upper: upper == const $CopyWithPlaceholder()
          ? _value.upper
          // ignore: cast_nullable_to_non_nullable
          : upper as num?,
    );
  }
}

extension $RuleQualityPrecisionCiCopyWith on RuleQualityPrecisionCi {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfRuleQualityPrecisionCi.copyWith(...)` or `instanceOfRuleQualityPrecisionCi.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$RuleQualityPrecisionCiCWProxy get copyWith =>
      _$RuleQualityPrecisionCiCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

RuleQualityPrecisionCi _$RuleQualityPrecisionCiFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('RuleQualityPrecisionCi', json, ($checkedConvert) {
  final val = RuleQualityPrecisionCi(
    lower: $checkedConvert('lower', (v) => v as num?),
    upper: $checkedConvert('upper', (v) => v as num?),
  );
  return val;
});

Map<String, dynamic> _$RuleQualityPrecisionCiToJson(
  RuleQualityPrecisionCi instance,
) => <String, dynamic>{'lower': ?instance.lower, 'upper': ?instance.upper};
