// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'situation_event.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SituationEventCWProxy {
  SituationEvent event(SituationEventEventEnum event);

  SituationEvent situation(SituationSummary situation);

  SituationEvent version(int version);

  SituationEvent previousStatus(SituationStatus? previousStatus);

  SituationEvent previousSeverity(Severity? previousSeverity);

  SituationEvent mergedInto(String? mergedInto);

  SituationEvent at(DateTime at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  SituationEvent call({
    SituationEventEventEnum event,
    SituationSummary situation,
    int version,
    SituationStatus? previousStatus,
    Severity? previousSeverity,
    String? mergedInto,
    DateTime at,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSituationEvent.copyWith(...)` or call `instanceOfSituationEvent.copyWith.fieldName(value)` for a single field.
class _$SituationEventCWProxyImpl implements _$SituationEventCWProxy {
  const _$SituationEventCWProxyImpl(this._value);

  final SituationEvent _value;

  @override
  SituationEvent event(SituationEventEventEnum event) => call(event: event);

  @override
  SituationEvent situation(SituationSummary situation) =>
      call(situation: situation);

  @override
  SituationEvent version(int version) => call(version: version);

  @override
  SituationEvent previousStatus(SituationStatus? previousStatus) =>
      call(previousStatus: previousStatus);

  @override
  SituationEvent previousSeverity(Severity? previousSeverity) =>
      call(previousSeverity: previousSeverity);

  @override
  SituationEvent mergedInto(String? mergedInto) => call(mergedInto: mergedInto);

  @override
  SituationEvent at(DateTime at) => call(at: at);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SituationEvent(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SituationEvent(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SituationEvent call({
    Object? event = const $CopyWithPlaceholder(),
    Object? situation = const $CopyWithPlaceholder(),
    Object? version = const $CopyWithPlaceholder(),
    Object? previousStatus = const $CopyWithPlaceholder(),
    Object? previousSeverity = const $CopyWithPlaceholder(),
    Object? mergedInto = const $CopyWithPlaceholder(),
    Object? at = const $CopyWithPlaceholder(),
  }) {
    return SituationEvent(
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as SituationEventEventEnum,
      situation: situation == const $CopyWithPlaceholder() || situation == null
          ? _value.situation
          // ignore: cast_nullable_to_non_nullable
          : situation as SituationSummary,
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as int,
      previousStatus: previousStatus == const $CopyWithPlaceholder()
          ? _value.previousStatus
          // ignore: cast_nullable_to_non_nullable
          : previousStatus as SituationStatus?,
      previousSeverity: previousSeverity == const $CopyWithPlaceholder()
          ? _value.previousSeverity
          // ignore: cast_nullable_to_non_nullable
          : previousSeverity as Severity?,
      mergedInto: mergedInto == const $CopyWithPlaceholder()
          ? _value.mergedInto
          // ignore: cast_nullable_to_non_nullable
          : mergedInto as String?,
      at: at == const $CopyWithPlaceholder() || at == null
          ? _value.at
          // ignore: cast_nullable_to_non_nullable
          : at as DateTime,
    );
  }
}

extension $SituationEventCopyWith on SituationEvent {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSituationEvent.copyWith(...)` or `instanceOfSituationEvent.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SituationEventCWProxy get copyWith => _$SituationEventCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SituationEvent _$SituationEventFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SituationEvent', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['event', 'situation', 'version', 'at'],
      );
      final val = SituationEvent(
        event: $checkedConvert(
          'event',
          (v) => $enumDecode(_$SituationEventEventEnumEnumMap, v),
        ),
        situation: $checkedConvert(
          'situation',
          (v) => SituationSummary.fromJson(v as Map<String, dynamic>),
        ),
        version: $checkedConvert('version', (v) => (v as num).toInt()),
        previousStatus: $checkedConvert(
          'previousStatus',
          (v) => $enumDecodeNullable(_$SituationStatusEnumMap, v),
        ),
        previousSeverity: $checkedConvert(
          'previousSeverity',
          (v) => $enumDecodeNullable(_$SeverityEnumMap, v),
        ),
        mergedInto: $checkedConvert('mergedInto', (v) => v as String?),
        at: $checkedConvert('at', (v) => DateTime.parse(v as String)),
      );
      return val;
    });

Map<String, dynamic> _$SituationEventToJson(SituationEvent instance) =>
    <String, dynamic>{
      'event': _$SituationEventEventEnumEnumMap[instance.event]!,
      'situation': instance.situation.toJson(),
      'version': instance.version,
      'previousStatus': ?_$SituationStatusEnumMap[instance.previousStatus],
      'previousSeverity': ?_$SeverityEnumMap[instance.previousSeverity],
      'mergedInto': ?instance.mergedInto,
      'at': instance.at.toIso8601String(),
    };

const _$SituationEventEventEnumEnumMap = {
  SituationEventEventEnum.situationPeriodCreated: 'situation.created',
  SituationEventEventEnum.situationPeriodUpdated: 'situation.updated',
  SituationEventEventEnum.situationPeriodActivated: 'situation.activated',
  SituationEventEventEnum.situationPeriodEscalated: 'situation.escalated',
  SituationEventEventEnum.situationPeriodDormant: 'situation.dormant',
  SituationEventEventEnum.situationPeriodClosed: 'situation.closed',
  SituationEventEventEnum.situationPeriodMerged: 'situation.merged',
};

const _$SituationStatusEnumMap = {
  SituationStatus.emerging: 'emerging',
  SituationStatus.active: 'active',
  SituationStatus.dormant: 'dormant',
  SituationStatus.closed: 'closed',
};

const _$SeverityEnumMap = {
  Severity.info: 'info',
  Severity.warning: 'warning',
  Severity.critical: 'critical',
};
