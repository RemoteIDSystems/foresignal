// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'anomaly_record_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnomalyRecordPayloadCWProxy {
  AnomalyRecordPayload ruleId(String ruleId);

  AnomalyRecordPayload name(String? name);

  AnomalyRecordPayload groupKey(String groupKey);

  AnomalyRecordPayload state(AnomalyRecordPayloadStateEnum? state);

  AnomalyRecordPayload observed(int? observed);

  AnomalyRecordPayload expected(num? expected);

  AnomalyRecordPayload pValue(num? pValue);

  AnomalyRecordPayload classification(
    AnomalyRecordPayloadClassificationEnum classification,
  );

  AnomalyRecordPayload detector(DetectorRef? detector);

  AnomalyRecordPayload automated(bool? automated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyRecordPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyRecordPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  AnomalyRecordPayload call({
    String ruleId,
    String? name,
    String groupKey,
    AnomalyRecordPayloadStateEnum? state,
    int? observed,
    num? expected,
    num? pValue,
    AnomalyRecordPayloadClassificationEnum classification,
    DetectorRef? detector,
    bool? automated,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnomalyRecordPayload.copyWith(...)` or call `instanceOfAnomalyRecordPayload.copyWith.fieldName(value)` for a single field.
class _$AnomalyRecordPayloadCWProxyImpl
    implements _$AnomalyRecordPayloadCWProxy {
  const _$AnomalyRecordPayloadCWProxyImpl(this._value);

  final AnomalyRecordPayload _value;

  @override
  AnomalyRecordPayload ruleId(String ruleId) => call(ruleId: ruleId);

  @override
  AnomalyRecordPayload name(String? name) => call(name: name);

  @override
  AnomalyRecordPayload groupKey(String groupKey) => call(groupKey: groupKey);

  @override
  AnomalyRecordPayload state(AnomalyRecordPayloadStateEnum? state) =>
      call(state: state);

  @override
  AnomalyRecordPayload observed(int? observed) => call(observed: observed);

  @override
  AnomalyRecordPayload expected(num? expected) => call(expected: expected);

  @override
  AnomalyRecordPayload pValue(num? pValue) => call(pValue: pValue);

  @override
  AnomalyRecordPayload classification(
    AnomalyRecordPayloadClassificationEnum classification,
  ) => call(classification: classification);

  @override
  AnomalyRecordPayload detector(DetectorRef? detector) =>
      call(detector: detector);

  @override
  AnomalyRecordPayload automated(bool? automated) => call(automated: automated);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnomalyRecordPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnomalyRecordPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnomalyRecordPayload call({
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? groupKey = const $CopyWithPlaceholder(),
    Object? state = const $CopyWithPlaceholder(),
    Object? observed = const $CopyWithPlaceholder(),
    Object? expected = const $CopyWithPlaceholder(),
    Object? pValue = const $CopyWithPlaceholder(),
    Object? classification = const $CopyWithPlaceholder(),
    Object? detector = const $CopyWithPlaceholder(),
    Object? automated = const $CopyWithPlaceholder(),
  }) {
    return AnomalyRecordPayload(
      ruleId: ruleId == const $CopyWithPlaceholder() || ruleId == null
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      groupKey: groupKey == const $CopyWithPlaceholder() || groupKey == null
          ? _value.groupKey
          // ignore: cast_nullable_to_non_nullable
          : groupKey as String,
      state: state == const $CopyWithPlaceholder()
          ? _value.state
          // ignore: cast_nullable_to_non_nullable
          : state as AnomalyRecordPayloadStateEnum?,
      observed: observed == const $CopyWithPlaceholder()
          ? _value.observed
          // ignore: cast_nullable_to_non_nullable
          : observed as int?,
      expected: expected == const $CopyWithPlaceholder()
          ? _value.expected
          // ignore: cast_nullable_to_non_nullable
          : expected as num?,
      pValue: pValue == const $CopyWithPlaceholder()
          ? _value.pValue
          // ignore: cast_nullable_to_non_nullable
          : pValue as num?,
      classification:
          classification == const $CopyWithPlaceholder() ||
              classification == null
          ? _value.classification
          // ignore: cast_nullable_to_non_nullable
          : classification as AnomalyRecordPayloadClassificationEnum,
      detector: detector == const $CopyWithPlaceholder()
          ? _value.detector
          // ignore: cast_nullable_to_non_nullable
          : detector as DetectorRef?,
      automated: automated == const $CopyWithPlaceholder()
          ? _value.automated
          // ignore: cast_nullable_to_non_nullable
          : automated as bool?,
    );
  }
}

extension $AnomalyRecordPayloadCopyWith on AnomalyRecordPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnomalyRecordPayload.copyWith(...)` or `instanceOfAnomalyRecordPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnomalyRecordPayloadCWProxy get copyWith =>
      _$AnomalyRecordPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnomalyRecordPayload _$AnomalyRecordPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnomalyRecordPayload', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['ruleId', 'groupKey', 'classification'],
  );
  final val = AnomalyRecordPayload(
    ruleId: $checkedConvert('ruleId', (v) => v as String),
    name: $checkedConvert('name', (v) => v as String?),
    groupKey: $checkedConvert('groupKey', (v) => v as String),
    state: $checkedConvert(
      'state',
      (v) => $enumDecodeNullable(_$AnomalyRecordPayloadStateEnumEnumMap, v),
    ),
    observed: $checkedConvert('observed', (v) => (v as num?)?.toInt()),
    expected: $checkedConvert('expected', (v) => v as num?),
    pValue: $checkedConvert('pValue', (v) => v as num?),
    classification: $checkedConvert(
      'classification',
      (v) => $enumDecode(_$AnomalyRecordPayloadClassificationEnumEnumMap, v),
    ),
    detector: $checkedConvert(
      'detector',
      (v) => v == null ? null : DetectorRef.fromJson(v as Map<String, dynamic>),
    ),
    automated: $checkedConvert('automated', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$AnomalyRecordPayloadToJson(
  AnomalyRecordPayload instance,
) => <String, dynamic>{
  'ruleId': instance.ruleId,
  'name': ?instance.name,
  'groupKey': instance.groupKey,
  'state': ?_$AnomalyRecordPayloadStateEnumEnumMap[instance.state],
  'observed': ?instance.observed,
  'expected': ?instance.expected,
  'pValue': ?instance.pValue,
  'classification':
      _$AnomalyRecordPayloadClassificationEnumEnumMap[instance.classification]!,
  'detector': ?instance.detector?.toJson(),
  'automated': ?instance.automated,
};

const _$AnomalyRecordPayloadStateEnumEnumMap = {
  AnomalyRecordPayloadStateEnum.active: 'active',
  AnomalyRecordPayloadStateEnum.clear: 'clear',
  AnomalyRecordPayloadStateEnum.unknown: 'unknown',
  AnomalyRecordPayloadStateEnum.learning: 'learning',
};

const _$AnomalyRecordPayloadClassificationEnumEnumMap = {
  AnomalyRecordPayloadClassificationEnum.activityUp: 'activity-up',
  AnomalyRecordPayloadClassificationEnum.activityDown: 'activity-down',
  AnomalyRecordPayloadClassificationEnum.coverageDrop: 'coverage-drop',
  AnomalyRecordPayloadClassificationEnum.insufficientCoverage:
      'insufficient-coverage',
  AnomalyRecordPayloadClassificationEnum.learning: 'learning',
  AnomalyRecordPayloadClassificationEnum.normal: 'normal',
};
