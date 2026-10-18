// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'event_selector.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$EventSelectorCWProxy {
  EventSelector rule(Map<String, Object>? rule);

  EventSelector alertRuleId(String? alertRuleId);

  EventSelector alertRuleTag(String? alertRuleTag);

  EventSelector intelEventType(String? intelEventType);

  EventSelector anomalyRuleId(String? anomalyRuleId);

  EventSelector classification(String? classification);

  EventSelector situationActivated(
    EventSelectorSituationActivated? situationActivated,
  );

  EventSelector changePoint(EventSelectorChangePoint? changePoint);

  EventSelector unexplainedMove(EventSelectorUnexplainedMove? unexplainedMove);

  EventSelector referenceFamily(String? referenceFamily);

  EventSelector timestamps(List<DateTime>? timestamps);

  EventSelector minSeverity(EventSelectorMinSeverityEnum? minSeverity);

  EventSelector regionCodes(List<String>? regionCodes);

  EventSelector label(String? label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventSelector(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventSelector(...).copyWith(id: 12, name: "My name")
  /// ```
  EventSelector call({
    Map<String, Object>? rule,
    String? alertRuleId,
    String? alertRuleTag,
    String? intelEventType,
    String? anomalyRuleId,
    String? classification,
    EventSelectorSituationActivated? situationActivated,
    EventSelectorChangePoint? changePoint,
    EventSelectorUnexplainedMove? unexplainedMove,
    String? referenceFamily,
    List<DateTime>? timestamps,
    EventSelectorMinSeverityEnum? minSeverity,
    List<String>? regionCodes,
    String? label,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfEventSelector.copyWith(...)` or call `instanceOfEventSelector.copyWith.fieldName(value)` for a single field.
class _$EventSelectorCWProxyImpl implements _$EventSelectorCWProxy {
  const _$EventSelectorCWProxyImpl(this._value);

  final EventSelector _value;

  @override
  EventSelector rule(Map<String, Object>? rule) => call(rule: rule);

  @override
  EventSelector alertRuleId(String? alertRuleId) =>
      call(alertRuleId: alertRuleId);

  @override
  EventSelector alertRuleTag(String? alertRuleTag) =>
      call(alertRuleTag: alertRuleTag);

  @override
  EventSelector intelEventType(String? intelEventType) =>
      call(intelEventType: intelEventType);

  @override
  EventSelector anomalyRuleId(String? anomalyRuleId) =>
      call(anomalyRuleId: anomalyRuleId);

  @override
  EventSelector classification(String? classification) =>
      call(classification: classification);

  @override
  EventSelector situationActivated(
    EventSelectorSituationActivated? situationActivated,
  ) => call(situationActivated: situationActivated);

  @override
  EventSelector changePoint(EventSelectorChangePoint? changePoint) =>
      call(changePoint: changePoint);

  @override
  EventSelector unexplainedMove(
    EventSelectorUnexplainedMove? unexplainedMove,
  ) => call(unexplainedMove: unexplainedMove);

  @override
  EventSelector referenceFamily(String? referenceFamily) =>
      call(referenceFamily: referenceFamily);

  @override
  EventSelector timestamps(List<DateTime>? timestamps) =>
      call(timestamps: timestamps);

  @override
  EventSelector minSeverity(EventSelectorMinSeverityEnum? minSeverity) =>
      call(minSeverity: minSeverity);

  @override
  EventSelector regionCodes(List<String>? regionCodes) =>
      call(regionCodes: regionCodes);

  @override
  EventSelector label(String? label) => call(label: label);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `EventSelector(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// EventSelector(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  EventSelector call({
    Object? rule = const $CopyWithPlaceholder(),
    Object? alertRuleId = const $CopyWithPlaceholder(),
    Object? alertRuleTag = const $CopyWithPlaceholder(),
    Object? intelEventType = const $CopyWithPlaceholder(),
    Object? anomalyRuleId = const $CopyWithPlaceholder(),
    Object? classification = const $CopyWithPlaceholder(),
    Object? situationActivated = const $CopyWithPlaceholder(),
    Object? changePoint = const $CopyWithPlaceholder(),
    Object? unexplainedMove = const $CopyWithPlaceholder(),
    Object? referenceFamily = const $CopyWithPlaceholder(),
    Object? timestamps = const $CopyWithPlaceholder(),
    Object? minSeverity = const $CopyWithPlaceholder(),
    Object? regionCodes = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
  }) {
    return EventSelector(
      rule: rule == const $CopyWithPlaceholder()
          ? _value.rule
          // ignore: cast_nullable_to_non_nullable
          : rule as Map<String, Object>?,
      alertRuleId: alertRuleId == const $CopyWithPlaceholder()
          ? _value.alertRuleId
          // ignore: cast_nullable_to_non_nullable
          : alertRuleId as String?,
      alertRuleTag: alertRuleTag == const $CopyWithPlaceholder()
          ? _value.alertRuleTag
          // ignore: cast_nullable_to_non_nullable
          : alertRuleTag as String?,
      intelEventType: intelEventType == const $CopyWithPlaceholder()
          ? _value.intelEventType
          // ignore: cast_nullable_to_non_nullable
          : intelEventType as String?,
      anomalyRuleId: anomalyRuleId == const $CopyWithPlaceholder()
          ? _value.anomalyRuleId
          // ignore: cast_nullable_to_non_nullable
          : anomalyRuleId as String?,
      classification: classification == const $CopyWithPlaceholder()
          ? _value.classification
          // ignore: cast_nullable_to_non_nullable
          : classification as String?,
      situationActivated: situationActivated == const $CopyWithPlaceholder()
          ? _value.situationActivated
          // ignore: cast_nullable_to_non_nullable
          : situationActivated as EventSelectorSituationActivated?,
      changePoint: changePoint == const $CopyWithPlaceholder()
          ? _value.changePoint
          // ignore: cast_nullable_to_non_nullable
          : changePoint as EventSelectorChangePoint?,
      unexplainedMove: unexplainedMove == const $CopyWithPlaceholder()
          ? _value.unexplainedMove
          // ignore: cast_nullable_to_non_nullable
          : unexplainedMove as EventSelectorUnexplainedMove?,
      referenceFamily: referenceFamily == const $CopyWithPlaceholder()
          ? _value.referenceFamily
          // ignore: cast_nullable_to_non_nullable
          : referenceFamily as String?,
      timestamps: timestamps == const $CopyWithPlaceholder()
          ? _value.timestamps
          // ignore: cast_nullable_to_non_nullable
          : timestamps as List<DateTime>?,
      minSeverity: minSeverity == const $CopyWithPlaceholder()
          ? _value.minSeverity
          // ignore: cast_nullable_to_non_nullable
          : minSeverity as EventSelectorMinSeverityEnum?,
      regionCodes: regionCodes == const $CopyWithPlaceholder()
          ? _value.regionCodes
          // ignore: cast_nullable_to_non_nullable
          : regionCodes as List<String>?,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
    );
  }
}

extension $EventSelectorCopyWith on EventSelector {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfEventSelector.copyWith(...)` or `instanceOfEventSelector.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$EventSelectorCWProxy get copyWith => _$EventSelectorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

EventSelector _$EventSelectorFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('EventSelector', json, ($checkedConvert) {
  final val = EventSelector(
    rule: $checkedConvert(
      'rule',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    alertRuleId: $checkedConvert('alertRuleId', (v) => v as String?),
    alertRuleTag: $checkedConvert('alertRuleTag', (v) => v as String?),
    intelEventType: $checkedConvert('intelEventType', (v) => v as String?),
    anomalyRuleId: $checkedConvert('anomalyRuleId', (v) => v as String?),
    classification: $checkedConvert('classification', (v) => v as String?),
    situationActivated: $checkedConvert(
      'situationActivated',
      (v) => v == null
          ? null
          : EventSelectorSituationActivated.fromJson(v as Map<String, dynamic>),
    ),
    changePoint: $checkedConvert(
      'changePoint',
      (v) => v == null
          ? null
          : EventSelectorChangePoint.fromJson(v as Map<String, dynamic>),
    ),
    unexplainedMove: $checkedConvert(
      'unexplainedMove',
      (v) => v == null
          ? null
          : EventSelectorUnexplainedMove.fromJson(v as Map<String, dynamic>),
    ),
    referenceFamily: $checkedConvert('referenceFamily', (v) => v as String?),
    timestamps: $checkedConvert(
      'timestamps',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DateTime.parse(e as String))
          .toList(),
    ),
    minSeverity: $checkedConvert(
      'minSeverity',
      (v) => $enumDecodeNullable(_$EventSelectorMinSeverityEnumEnumMap, v),
    ),
    regionCodes: $checkedConvert(
      'regionCodes',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    label: $checkedConvert('label', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$EventSelectorToJson(
  EventSelector instance,
) => <String, dynamic>{
  'rule': ?instance.rule,
  'alertRuleId': ?instance.alertRuleId,
  'alertRuleTag': ?instance.alertRuleTag,
  'intelEventType': ?instance.intelEventType,
  'anomalyRuleId': ?instance.anomalyRuleId,
  'classification': ?instance.classification,
  'situationActivated': ?instance.situationActivated?.toJson(),
  'changePoint': ?instance.changePoint?.toJson(),
  'unexplainedMove': ?instance.unexplainedMove?.toJson(),
  'referenceFamily': ?instance.referenceFamily,
  'timestamps': ?instance.timestamps?.map((e) => e.toIso8601String()).toList(),
  'minSeverity': ?_$EventSelectorMinSeverityEnumEnumMap[instance.minSeverity],
  'regionCodes': ?instance.regionCodes,
  'label': ?instance.label,
};

const _$EventSelectorMinSeverityEnumEnumMap = {
  EventSelectorMinSeverityEnum.info: 'info',
  EventSelectorMinSeverityEnum.warning: 'warning',
  EventSelectorMinSeverityEnum.critical: 'critical',
  EventSelectorMinSeverityEnum.notable: 'notable',
  EventSelectorMinSeverityEnum.significant: 'significant',
};
