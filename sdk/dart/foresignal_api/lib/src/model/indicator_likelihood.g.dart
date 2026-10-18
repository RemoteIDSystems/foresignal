// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'indicator_likelihood.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$IndicatorLikelihoodCWProxy {
  IndicatorLikelihood class_(IndicatorLikelihoodClass_Enum class_);

  IndicatorLikelihood direction(IndicatorLikelihoodDirectionEnum direction);

  IndicatorLikelihood value(num? value);

  IndicatorLikelihood rationale(String? rationale);

  IndicatorLikelihood effective(num? effective);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorLikelihood(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorLikelihood(...).copyWith(id: 12, name: "My name")
  /// ```
  IndicatorLikelihood call({
    IndicatorLikelihoodClass_Enum class_,
    IndicatorLikelihoodDirectionEnum direction,
    num? value,
    String? rationale,
    num? effective,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfIndicatorLikelihood.copyWith(...)` or call `instanceOfIndicatorLikelihood.copyWith.fieldName(value)` for a single field.
class _$IndicatorLikelihoodCWProxyImpl implements _$IndicatorLikelihoodCWProxy {
  const _$IndicatorLikelihoodCWProxyImpl(this._value);

  final IndicatorLikelihood _value;

  @override
  IndicatorLikelihood class_(IndicatorLikelihoodClass_Enum class_) =>
      call(class_: class_);

  @override
  IndicatorLikelihood direction(IndicatorLikelihoodDirectionEnum direction) =>
      call(direction: direction);

  @override
  IndicatorLikelihood value(num? value) => call(value: value);

  @override
  IndicatorLikelihood rationale(String? rationale) =>
      call(rationale: rationale);

  @override
  IndicatorLikelihood effective(num? effective) => call(effective: effective);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `IndicatorLikelihood(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// IndicatorLikelihood(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  IndicatorLikelihood call({
    Object? class_ = const $CopyWithPlaceholder(),
    Object? direction = const $CopyWithPlaceholder(),
    Object? value = const $CopyWithPlaceholder(),
    Object? rationale = const $CopyWithPlaceholder(),
    Object? effective = const $CopyWithPlaceholder(),
  }) {
    return IndicatorLikelihood(
      class_: class_ == const $CopyWithPlaceholder() || class_ == null
          ? _value.class_
          // ignore: cast_nullable_to_non_nullable
          : class_ as IndicatorLikelihoodClass_Enum,
      direction: direction == const $CopyWithPlaceholder() || direction == null
          ? _value.direction
          // ignore: cast_nullable_to_non_nullable
          : direction as IndicatorLikelihoodDirectionEnum,
      value: value == const $CopyWithPlaceholder()
          ? _value.value
          // ignore: cast_nullable_to_non_nullable
          : value as num?,
      rationale: rationale == const $CopyWithPlaceholder()
          ? _value.rationale
          // ignore: cast_nullable_to_non_nullable
          : rationale as String?,
      effective: effective == const $CopyWithPlaceholder()
          ? _value.effective
          // ignore: cast_nullable_to_non_nullable
          : effective as num?,
    );
  }
}

extension $IndicatorLikelihoodCopyWith on IndicatorLikelihood {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfIndicatorLikelihood.copyWith(...)` or `instanceOfIndicatorLikelihood.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$IndicatorLikelihoodCWProxy get copyWith =>
      _$IndicatorLikelihoodCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

IndicatorLikelihood _$IndicatorLikelihoodFromJson(Map<String, dynamic> json) =>
    $checkedCreate('IndicatorLikelihood', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['class', 'direction']);
      final val = IndicatorLikelihood(
        class_: $checkedConvert(
          'class',
          (v) => $enumDecode(_$IndicatorLikelihoodClass_EnumEnumMap, v),
        ),
        direction: $checkedConvert(
          'direction',
          (v) => $enumDecode(_$IndicatorLikelihoodDirectionEnumEnumMap, v),
        ),
        value: $checkedConvert('value', (v) => v as num?),
        rationale: $checkedConvert('rationale', (v) => v as String?),
        effective: $checkedConvert('effective', (v) => v as num?),
      );
      return val;
    }, fieldKeyMap: const {'class_': 'class'});

Map<String, dynamic> _$IndicatorLikelihoodToJson(
  IndicatorLikelihood instance,
) => <String, dynamic>{
  'class': _$IndicatorLikelihoodClass_EnumEnumMap[instance.class_]!,
  'direction': _$IndicatorLikelihoodDirectionEnumEnumMap[instance.direction]!,
  'value': ?instance.value,
  'rationale': ?instance.rationale,
  'effective': ?instance.effective,
};

const _$IndicatorLikelihoodClass_EnumEnumMap = {
  IndicatorLikelihoodClass_Enum.high: 'high',
  IndicatorLikelihoodClass_Enum.medium: 'medium',
  IndicatorLikelihoodClass_Enum.low: 'low',
  IndicatorLikelihoodClass_Enum.neutral: 'neutral',
};

const _$IndicatorLikelihoodDirectionEnumEnumMap = {
  IndicatorLikelihoodDirectionEnum.supports: 'supports',
  IndicatorLikelihoodDirectionEnum.weakens: 'weakens',
};
