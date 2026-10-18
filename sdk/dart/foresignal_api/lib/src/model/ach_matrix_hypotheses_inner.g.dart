// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'ach_matrix_hypotheses_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AchMatrixHypothesesInnerCWProxy {
  AchMatrixHypothesesInner id(String id);

  AchMatrixHypothesesInner label(String label);

  AchMatrixHypothesesInner probability(num probability);

  AchMatrixHypothesesInner estimativeTerm(IcdTerm? estimativeTerm);

  AchMatrixHypothesesInner inconsistencyScore(num inconsistencyScore);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AchMatrixHypothesesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AchMatrixHypothesesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  AchMatrixHypothesesInner call({
    String id,
    String label,
    num probability,
    IcdTerm? estimativeTerm,
    num inconsistencyScore,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAchMatrixHypothesesInner.copyWith(...)` or call `instanceOfAchMatrixHypothesesInner.copyWith.fieldName(value)` for a single field.
class _$AchMatrixHypothesesInnerCWProxyImpl
    implements _$AchMatrixHypothesesInnerCWProxy {
  const _$AchMatrixHypothesesInnerCWProxyImpl(this._value);

  final AchMatrixHypothesesInner _value;

  @override
  AchMatrixHypothesesInner id(String id) => call(id: id);

  @override
  AchMatrixHypothesesInner label(String label) => call(label: label);

  @override
  AchMatrixHypothesesInner probability(num probability) =>
      call(probability: probability);

  @override
  AchMatrixHypothesesInner estimativeTerm(IcdTerm? estimativeTerm) =>
      call(estimativeTerm: estimativeTerm);

  @override
  AchMatrixHypothesesInner inconsistencyScore(num inconsistencyScore) =>
      call(inconsistencyScore: inconsistencyScore);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AchMatrixHypothesesInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AchMatrixHypothesesInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AchMatrixHypothesesInner call({
    Object? id = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? probability = const $CopyWithPlaceholder(),
    Object? estimativeTerm = const $CopyWithPlaceholder(),
    Object? inconsistencyScore = const $CopyWithPlaceholder(),
  }) {
    return AchMatrixHypothesesInner(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      label: label == const $CopyWithPlaceholder() || label == null
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String,
      probability:
          probability == const $CopyWithPlaceholder() || probability == null
          ? _value.probability
          // ignore: cast_nullable_to_non_nullable
          : probability as num,
      estimativeTerm: estimativeTerm == const $CopyWithPlaceholder()
          ? _value.estimativeTerm
          // ignore: cast_nullable_to_non_nullable
          : estimativeTerm as IcdTerm?,
      inconsistencyScore:
          inconsistencyScore == const $CopyWithPlaceholder() ||
              inconsistencyScore == null
          ? _value.inconsistencyScore
          // ignore: cast_nullable_to_non_nullable
          : inconsistencyScore as num,
    );
  }
}

extension $AchMatrixHypothesesInnerCopyWith on AchMatrixHypothesesInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAchMatrixHypothesesInner.copyWith(...)` or `instanceOfAchMatrixHypothesesInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AchMatrixHypothesesInnerCWProxy get copyWith =>
      _$AchMatrixHypothesesInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AchMatrixHypothesesInner _$AchMatrixHypothesesInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AchMatrixHypothesesInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'label', 'probability', 'inconsistencyScore'],
  );
  final val = AchMatrixHypothesesInner(
    id: $checkedConvert('id', (v) => v as String),
    label: $checkedConvert('label', (v) => v as String),
    probability: $checkedConvert('probability', (v) => v as num),
    estimativeTerm: $checkedConvert(
      'estimativeTerm',
      (v) => $enumDecodeNullable(_$IcdTermEnumMap, v),
    ),
    inconsistencyScore: $checkedConvert('inconsistencyScore', (v) => v as num),
  );
  return val;
});

Map<String, dynamic> _$AchMatrixHypothesesInnerToJson(
  AchMatrixHypothesesInner instance,
) => <String, dynamic>{
  'id': instance.id,
  'label': instance.label,
  'probability': instance.probability,
  'estimativeTerm': ?_$IcdTermEnumMap[instance.estimativeTerm],
  'inconsistencyScore': instance.inconsistencyScore,
};

const _$IcdTermEnumMap = {
  IcdTerm.almostNoChance: 'almost no chance',
  IcdTerm.veryUnlikely: 'very unlikely',
  IcdTerm.unlikely: 'unlikely',
  IcdTerm.roughlyEvenChance: 'roughly even chance',
  IcdTerm.likely: 'likely',
  IcdTerm.veryLikely: 'very likely',
  IcdTerm.almostCertain: 'almost certain',
};
