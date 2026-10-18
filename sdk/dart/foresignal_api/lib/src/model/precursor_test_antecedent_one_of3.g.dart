// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test_antecedent_one_of3.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestAntecedentOneOf3CWProxy {
  PrecursorTestAntecedentOneOf3 anomalyRuleId(String anomalyRuleId);

  PrecursorTestAntecedentOneOf3 classification(
    PrecursorTestAntecedentOneOf3ClassificationEnum? classification,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestAntecedentOneOf3(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestAntecedentOneOf3(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTestAntecedentOneOf3 call({
    String anomalyRuleId,
    PrecursorTestAntecedentOneOf3ClassificationEnum? classification,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTestAntecedentOneOf3.copyWith(...)` or call `instanceOfPrecursorTestAntecedentOneOf3.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestAntecedentOneOf3CWProxyImpl
    implements _$PrecursorTestAntecedentOneOf3CWProxy {
  const _$PrecursorTestAntecedentOneOf3CWProxyImpl(this._value);

  final PrecursorTestAntecedentOneOf3 _value;

  @override
  PrecursorTestAntecedentOneOf3 anomalyRuleId(String anomalyRuleId) =>
      call(anomalyRuleId: anomalyRuleId);

  @override
  PrecursorTestAntecedentOneOf3 classification(
    PrecursorTestAntecedentOneOf3ClassificationEnum? classification,
  ) => call(classification: classification);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestAntecedentOneOf3(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestAntecedentOneOf3(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTestAntecedentOneOf3 call({
    Object? anomalyRuleId = const $CopyWithPlaceholder(),
    Object? classification = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTestAntecedentOneOf3(
      anomalyRuleId:
          anomalyRuleId == const $CopyWithPlaceholder() || anomalyRuleId == null
          ? _value.anomalyRuleId
          // ignore: cast_nullable_to_non_nullable
          : anomalyRuleId as String,
      classification: classification == const $CopyWithPlaceholder()
          ? _value.classification
          // ignore: cast_nullable_to_non_nullable
          : classification as PrecursorTestAntecedentOneOf3ClassificationEnum?,
    );
  }
}

extension $PrecursorTestAntecedentOneOf3CopyWith
    on PrecursorTestAntecedentOneOf3 {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTestAntecedentOneOf3.copyWith(...)` or `instanceOfPrecursorTestAntecedentOneOf3.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestAntecedentOneOf3CWProxy get copyWith =>
      _$PrecursorTestAntecedentOneOf3CWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTestAntecedentOneOf3 _$PrecursorTestAntecedentOneOf3FromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorTestAntecedentOneOf3', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['anomalyRuleId']);
  final val = PrecursorTestAntecedentOneOf3(
    anomalyRuleId: $checkedConvert('anomalyRuleId', (v) => v as String),
    classification: $checkedConvert(
      'classification',
      (v) => $enumDecodeNullable(
        _$PrecursorTestAntecedentOneOf3ClassificationEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$PrecursorTestAntecedentOneOf3ToJson(
  PrecursorTestAntecedentOneOf3 instance,
) => <String, dynamic>{
  'anomalyRuleId': instance.anomalyRuleId,
  'classification':
      ?_$PrecursorTestAntecedentOneOf3ClassificationEnumEnumMap[instance
          .classification],
};

const _$PrecursorTestAntecedentOneOf3ClassificationEnumEnumMap = {
  PrecursorTestAntecedentOneOf3ClassificationEnum.activityUp: 'activity-up',
  PrecursorTestAntecedentOneOf3ClassificationEnum.activityDown: 'activity-down',
  PrecursorTestAntecedentOneOf3ClassificationEnum.coverageDrop: 'coverage-drop',
  PrecursorTestAntecedentOneOf3ClassificationEnum.insufficientCoverage:
      'insufficient-coverage',
  PrecursorTestAntecedentOneOf3ClassificationEnum.learning: 'learning',
  PrecursorTestAntecedentOneOf3ClassificationEnum.normal: 'normal',
};
