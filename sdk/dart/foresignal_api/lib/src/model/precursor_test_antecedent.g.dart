// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'precursor_test_antecedent.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PrecursorTestAntecedentCWProxy {
  PrecursorTestAntecedent rule(Map<String, Object> rule);

  PrecursorTestAntecedent alertRuleId(String alertRuleId);

  PrecursorTestAntecedent intelEventType(
    PrecursorTestAntecedentIntelEventTypeEnum intelEventType,
  );

  PrecursorTestAntecedent anomalyRuleId(String anomalyRuleId);

  PrecursorTestAntecedent classification(
    PrecursorTestAntecedentClassificationEnum? classification,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestAntecedent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestAntecedent(...).copyWith(id: 12, name: "My name")
  /// ```
  PrecursorTestAntecedent call({
    Map<String, Object> rule,
    String alertRuleId,
    PrecursorTestAntecedentIntelEventTypeEnum intelEventType,
    String anomalyRuleId,
    PrecursorTestAntecedentClassificationEnum? classification,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPrecursorTestAntecedent.copyWith(...)` or call `instanceOfPrecursorTestAntecedent.copyWith.fieldName(value)` for a single field.
class _$PrecursorTestAntecedentCWProxyImpl
    implements _$PrecursorTestAntecedentCWProxy {
  const _$PrecursorTestAntecedentCWProxyImpl(this._value);

  final PrecursorTestAntecedent _value;

  @override
  PrecursorTestAntecedent rule(Map<String, Object> rule) => call(rule: rule);

  @override
  PrecursorTestAntecedent alertRuleId(String alertRuleId) =>
      call(alertRuleId: alertRuleId);

  @override
  PrecursorTestAntecedent intelEventType(
    PrecursorTestAntecedentIntelEventTypeEnum intelEventType,
  ) => call(intelEventType: intelEventType);

  @override
  PrecursorTestAntecedent anomalyRuleId(String anomalyRuleId) =>
      call(anomalyRuleId: anomalyRuleId);

  @override
  PrecursorTestAntecedent classification(
    PrecursorTestAntecedentClassificationEnum? classification,
  ) => call(classification: classification);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PrecursorTestAntecedent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PrecursorTestAntecedent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PrecursorTestAntecedent call({
    Object? rule = const $CopyWithPlaceholder(),
    Object? alertRuleId = const $CopyWithPlaceholder(),
    Object? intelEventType = const $CopyWithPlaceholder(),
    Object? anomalyRuleId = const $CopyWithPlaceholder(),
    Object? classification = const $CopyWithPlaceholder(),
  }) {
    return PrecursorTestAntecedent(
      rule: rule == const $CopyWithPlaceholder() || rule == null
          ? _value.rule
          // ignore: cast_nullable_to_non_nullable
          : rule as Map<String, Object>,
      alertRuleId:
          alertRuleId == const $CopyWithPlaceholder() || alertRuleId == null
          ? _value.alertRuleId
          // ignore: cast_nullable_to_non_nullable
          : alertRuleId as String,
      intelEventType:
          intelEventType == const $CopyWithPlaceholder() ||
              intelEventType == null
          ? _value.intelEventType
          // ignore: cast_nullable_to_non_nullable
          : intelEventType as PrecursorTestAntecedentIntelEventTypeEnum,
      anomalyRuleId:
          anomalyRuleId == const $CopyWithPlaceholder() || anomalyRuleId == null
          ? _value.anomalyRuleId
          // ignore: cast_nullable_to_non_nullable
          : anomalyRuleId as String,
      classification: classification == const $CopyWithPlaceholder()
          ? _value.classification
          // ignore: cast_nullable_to_non_nullable
          : classification as PrecursorTestAntecedentClassificationEnum?,
    );
  }
}

extension $PrecursorTestAntecedentCopyWith on PrecursorTestAntecedent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPrecursorTestAntecedent.copyWith(...)` or `instanceOfPrecursorTestAntecedent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PrecursorTestAntecedentCWProxy get copyWith =>
      _$PrecursorTestAntecedentCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PrecursorTestAntecedent _$PrecursorTestAntecedentFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PrecursorTestAntecedent', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'rule',
      'alertRuleId',
      'intelEventType',
      'anomalyRuleId',
    ],
  );
  final val = PrecursorTestAntecedent(
    rule: $checkedConvert(
      'rule',
      (v) =>
          (v as Map<String, dynamic>).map((k, e) => MapEntry(k, e as Object)),
    ),
    alertRuleId: $checkedConvert('alertRuleId', (v) => v as String),
    intelEventType: $checkedConvert(
      'intelEventType',
      (v) => $enumDecode(_$PrecursorTestAntecedentIntelEventTypeEnumEnumMap, v),
    ),
    anomalyRuleId: $checkedConvert('anomalyRuleId', (v) => v as String),
    classification: $checkedConvert(
      'classification',
      (v) => $enumDecodeNullable(
        _$PrecursorTestAntecedentClassificationEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$PrecursorTestAntecedentToJson(
  PrecursorTestAntecedent instance,
) => <String, dynamic>{
  'rule': instance.rule,
  'alertRuleId': instance.alertRuleId,
  'intelEventType':
      _$PrecursorTestAntecedentIntelEventTypeEnumEnumMap[instance
          .intelEventType]!,
  'anomalyRuleId': instance.anomalyRuleId,
  'classification':
      ?_$PrecursorTestAntecedentClassificationEnumEnumMap[instance
          .classification],
};

const _$PrecursorTestAntecedentIntelEventTypeEnumEnumMap = {
  PrecursorTestAntecedentIntelEventTypeEnum.maritimeDisruption:
      'maritime-disruption',
  PrecursorTestAntecedentIntelEventTypeEnum.aviationAnomaly: 'aviation-anomaly',
  PrecursorTestAntecedentIntelEventTypeEnum.infrastructureDisruption:
      'infrastructure-disruption',
  PrecursorTestAntecedentIntelEventTypeEnum.weatherImpact: 'weather-impact',
  PrecursorTestAntecedentIntelEventTypeEnum.seismic: 'seismic',
  PrecursorTestAntecedentIntelEventTypeEnum.custom: 'custom',
};

const _$PrecursorTestAntecedentClassificationEnumEnumMap = {
  PrecursorTestAntecedentClassificationEnum.activityUp: 'activity-up',
  PrecursorTestAntecedentClassificationEnum.activityDown: 'activity-down',
  PrecursorTestAntecedentClassificationEnum.coverageDrop: 'coverage-drop',
  PrecursorTestAntecedentClassificationEnum.insufficientCoverage:
      'insufficient-coverage',
  PrecursorTestAntecedentClassificationEnum.learning: 'learning',
  PrecursorTestAntecedentClassificationEnum.normal: 'normal',
};
