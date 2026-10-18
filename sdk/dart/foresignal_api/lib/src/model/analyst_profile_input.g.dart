// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_profile_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystProfileInputCWProxy {
  AnalystProfileInput name(String name);

  AnalystProfileInput enabled(bool? enabled);

  AnalystProfileInput schedule(AnalystProfileSchedule? schedule);

  AnalystProfileInput scope(AnalystProfileScope? scope);

  AnalystProfileInput lookback(String? lookback);

  AnalystProfileInput instructions(String? instructions);

  AnalystProfileInput providerId(String? providerId);

  AnalystProfileInput effort(AnalystProfileInputEffortEnum? effort);

  AnalystProfileInput maxRunsPerDay(int? maxRunsPerDay);

  AnalystProfileInput maxTokensPerRun(int? maxTokensPerRun);

  AnalystProfileInput maxToolCalls(int? maxToolCalls);

  AnalystProfileInput maxWallSeconds(int? maxWallSeconds);

  AnalystProfileInput allowActions(bool? allowActions);

  AnalystProfileInput allowedActions(
    List<AnalystProfileInputAllowedActionsEnum>? allowedActions,
  );

  AnalystProfileInput maxAutoActionsPerDay(int? maxAutoActionsPerDay);

  AnalystProfileInput channelIds(List<String>? channelIds);

  AnalystProfileInput notifyMinPriority(
    AnalystProfileInputNotifyMinPriorityEnum? notifyMinPriority,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystProfileInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystProfileInput(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystProfileInput call({
    String name,
    bool? enabled,
    AnalystProfileSchedule? schedule,
    AnalystProfileScope? scope,
    String? lookback,
    String? instructions,
    String? providerId,
    AnalystProfileInputEffortEnum? effort,
    int? maxRunsPerDay,
    int? maxTokensPerRun,
    int? maxToolCalls,
    int? maxWallSeconds,
    bool? allowActions,
    List<AnalystProfileInputAllowedActionsEnum>? allowedActions,
    int? maxAutoActionsPerDay,
    List<String>? channelIds,
    AnalystProfileInputNotifyMinPriorityEnum? notifyMinPriority,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystProfileInput.copyWith(...)` or call `instanceOfAnalystProfileInput.copyWith.fieldName(value)` for a single field.
class _$AnalystProfileInputCWProxyImpl implements _$AnalystProfileInputCWProxy {
  const _$AnalystProfileInputCWProxyImpl(this._value);

  final AnalystProfileInput _value;

  @override
  AnalystProfileInput name(String name) => call(name: name);

  @override
  AnalystProfileInput enabled(bool? enabled) => call(enabled: enabled);

  @override
  AnalystProfileInput schedule(AnalystProfileSchedule? schedule) =>
      call(schedule: schedule);

  @override
  AnalystProfileInput scope(AnalystProfileScope? scope) => call(scope: scope);

  @override
  AnalystProfileInput lookback(String? lookback) => call(lookback: lookback);

  @override
  AnalystProfileInput instructions(String? instructions) =>
      call(instructions: instructions);

  @override
  AnalystProfileInput providerId(String? providerId) =>
      call(providerId: providerId);

  @override
  AnalystProfileInput effort(AnalystProfileInputEffortEnum? effort) =>
      call(effort: effort);

  @override
  AnalystProfileInput maxRunsPerDay(int? maxRunsPerDay) =>
      call(maxRunsPerDay: maxRunsPerDay);

  @override
  AnalystProfileInput maxTokensPerRun(int? maxTokensPerRun) =>
      call(maxTokensPerRun: maxTokensPerRun);

  @override
  AnalystProfileInput maxToolCalls(int? maxToolCalls) =>
      call(maxToolCalls: maxToolCalls);

  @override
  AnalystProfileInput maxWallSeconds(int? maxWallSeconds) =>
      call(maxWallSeconds: maxWallSeconds);

  @override
  AnalystProfileInput allowActions(bool? allowActions) =>
      call(allowActions: allowActions);

  @override
  AnalystProfileInput allowedActions(
    List<AnalystProfileInputAllowedActionsEnum>? allowedActions,
  ) => call(allowedActions: allowedActions);

  @override
  AnalystProfileInput maxAutoActionsPerDay(int? maxAutoActionsPerDay) =>
      call(maxAutoActionsPerDay: maxAutoActionsPerDay);

  @override
  AnalystProfileInput channelIds(List<String>? channelIds) =>
      call(channelIds: channelIds);

  @override
  AnalystProfileInput notifyMinPriority(
    AnalystProfileInputNotifyMinPriorityEnum? notifyMinPriority,
  ) => call(notifyMinPriority: notifyMinPriority);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystProfileInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystProfileInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystProfileInput call({
    Object? name = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? schedule = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
    Object? lookback = const $CopyWithPlaceholder(),
    Object? instructions = const $CopyWithPlaceholder(),
    Object? providerId = const $CopyWithPlaceholder(),
    Object? effort = const $CopyWithPlaceholder(),
    Object? maxRunsPerDay = const $CopyWithPlaceholder(),
    Object? maxTokensPerRun = const $CopyWithPlaceholder(),
    Object? maxToolCalls = const $CopyWithPlaceholder(),
    Object? maxWallSeconds = const $CopyWithPlaceholder(),
    Object? allowActions = const $CopyWithPlaceholder(),
    Object? allowedActions = const $CopyWithPlaceholder(),
    Object? maxAutoActionsPerDay = const $CopyWithPlaceholder(),
    Object? channelIds = const $CopyWithPlaceholder(),
    Object? notifyMinPriority = const $CopyWithPlaceholder(),
  }) {
    return AnalystProfileInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
      schedule: schedule == const $CopyWithPlaceholder()
          ? _value.schedule
          // ignore: cast_nullable_to_non_nullable
          : schedule as AnalystProfileSchedule?,
      scope: scope == const $CopyWithPlaceholder()
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as AnalystProfileScope?,
      lookback: lookback == const $CopyWithPlaceholder()
          ? _value.lookback
          // ignore: cast_nullable_to_non_nullable
          : lookback as String?,
      instructions: instructions == const $CopyWithPlaceholder()
          ? _value.instructions
          // ignore: cast_nullable_to_non_nullable
          : instructions as String?,
      providerId: providerId == const $CopyWithPlaceholder()
          ? _value.providerId
          // ignore: cast_nullable_to_non_nullable
          : providerId as String?,
      effort: effort == const $CopyWithPlaceholder()
          ? _value.effort
          // ignore: cast_nullable_to_non_nullable
          : effort as AnalystProfileInputEffortEnum?,
      maxRunsPerDay: maxRunsPerDay == const $CopyWithPlaceholder()
          ? _value.maxRunsPerDay
          // ignore: cast_nullable_to_non_nullable
          : maxRunsPerDay as int?,
      maxTokensPerRun: maxTokensPerRun == const $CopyWithPlaceholder()
          ? _value.maxTokensPerRun
          // ignore: cast_nullable_to_non_nullable
          : maxTokensPerRun as int?,
      maxToolCalls: maxToolCalls == const $CopyWithPlaceholder()
          ? _value.maxToolCalls
          // ignore: cast_nullable_to_non_nullable
          : maxToolCalls as int?,
      maxWallSeconds: maxWallSeconds == const $CopyWithPlaceholder()
          ? _value.maxWallSeconds
          // ignore: cast_nullable_to_non_nullable
          : maxWallSeconds as int?,
      allowActions: allowActions == const $CopyWithPlaceholder()
          ? _value.allowActions
          // ignore: cast_nullable_to_non_nullable
          : allowActions as bool?,
      allowedActions: allowedActions == const $CopyWithPlaceholder()
          ? _value.allowedActions
          // ignore: cast_nullable_to_non_nullable
          : allowedActions as List<AnalystProfileInputAllowedActionsEnum>?,
      maxAutoActionsPerDay: maxAutoActionsPerDay == const $CopyWithPlaceholder()
          ? _value.maxAutoActionsPerDay
          // ignore: cast_nullable_to_non_nullable
          : maxAutoActionsPerDay as int?,
      channelIds: channelIds == const $CopyWithPlaceholder()
          ? _value.channelIds
          // ignore: cast_nullable_to_non_nullable
          : channelIds as List<String>?,
      notifyMinPriority: notifyMinPriority == const $CopyWithPlaceholder()
          ? _value.notifyMinPriority
          // ignore: cast_nullable_to_non_nullable
          : notifyMinPriority as AnalystProfileInputNotifyMinPriorityEnum?,
    );
  }
}

extension $AnalystProfileInputCopyWith on AnalystProfileInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystProfileInput.copyWith(...)` or `instanceOfAnalystProfileInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystProfileInputCWProxy get copyWith =>
      _$AnalystProfileInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystProfileInput _$AnalystProfileInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalystProfileInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name']);
  final val = AnalystProfileInput(
    name: $checkedConvert('name', (v) => v as String),
    enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
    schedule: $checkedConvert(
      'schedule',
      (v) => v == null
          ? null
          : AnalystProfileSchedule.fromJson(v as Map<String, dynamic>),
    ),
    scope: $checkedConvert(
      'scope',
      (v) => v == null
          ? null
          : AnalystProfileScope.fromJson(v as Map<String, dynamic>),
    ),
    lookback: $checkedConvert('lookback', (v) => v as String?),
    instructions: $checkedConvert('instructions', (v) => v as String?),
    providerId: $checkedConvert('providerId', (v) => v as String?),
    effort: $checkedConvert(
      'effort',
      (v) => $enumDecodeNullable(_$AnalystProfileInputEffortEnumEnumMap, v),
    ),
    maxRunsPerDay: $checkedConvert(
      'maxRunsPerDay',
      (v) => (v as num?)?.toInt() ?? 48,
    ),
    maxTokensPerRun: $checkedConvert(
      'maxTokensPerRun',
      (v) => (v as num?)?.toInt() ?? 60000,
    ),
    maxToolCalls: $checkedConvert(
      'maxToolCalls',
      (v) => (v as num?)?.toInt() ?? 16,
    ),
    maxWallSeconds: $checkedConvert(
      'maxWallSeconds',
      (v) => (v as num?)?.toInt() ?? 180,
    ),
    allowActions: $checkedConvert('allowActions', (v) => v as bool? ?? false),
    allowedActions: $checkedConvert(
      'allowedActions',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) =>
                $enumDecode(_$AnalystProfileInputAllowedActionsEnumEnumMap, e),
          )
          .toList(),
    ),
    maxAutoActionsPerDay: $checkedConvert(
      'maxAutoActionsPerDay',
      (v) => (v as num?)?.toInt() ?? 5,
    ),
    channelIds: $checkedConvert(
      'channelIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    notifyMinPriority: $checkedConvert(
      'notifyMinPriority',
      (v) => $enumDecodeNullable(
        _$AnalystProfileInputNotifyMinPriorityEnumEnumMap,
        v,
      ),
    ),
  );
  return val;
});

Map<String, dynamic> _$AnalystProfileInputToJson(
  AnalystProfileInput instance,
) => <String, dynamic>{
  'name': instance.name,
  'enabled': ?instance.enabled,
  'schedule': ?instance.schedule?.toJson(),
  'scope': ?instance.scope?.toJson(),
  'lookback': ?instance.lookback,
  'instructions': ?instance.instructions,
  'providerId': ?instance.providerId,
  'effort': ?_$AnalystProfileInputEffortEnumEnumMap[instance.effort],
  'maxRunsPerDay': ?instance.maxRunsPerDay,
  'maxTokensPerRun': ?instance.maxTokensPerRun,
  'maxToolCalls': ?instance.maxToolCalls,
  'maxWallSeconds': ?instance.maxWallSeconds,
  'allowActions': ?instance.allowActions,
  'allowedActions': ?instance.allowedActions
      ?.map((e) => _$AnalystProfileInputAllowedActionsEnumEnumMap[e]!)
      .toList(),
  'maxAutoActionsPerDay': ?instance.maxAutoActionsPerDay,
  'channelIds': ?instance.channelIds,
  'notifyMinPriority':
      ?_$AnalystProfileInputNotifyMinPriorityEnumEnumMap[instance
          .notifyMinPriority],
};

const _$AnalystProfileInputEffortEnumEnumMap = {
  AnalystProfileInputEffortEnum.low: 'low',
  AnalystProfileInputEffortEnum.medium: 'medium',
};

const _$AnalystProfileInputAllowedActionsEnumEnumMap = {
  AnalystProfileInputAllowedActionsEnum.proposeWatch: 'propose_watch',
  AnalystProfileInputAllowedActionsEnum.proposeIntelEvent:
      'propose_intel_event',
  AnalystProfileInputAllowedActionsEnum.proposeInvestigationItem:
      'propose_investigation_item',
  AnalystProfileInputAllowedActionsEnum.annotateEvent: 'annotate_event',
  AnalystProfileInputAllowedActionsEnum.proposeHypothesisSet:
      'propose_hypothesis_set',
  AnalystProfileInputAllowedActionsEnum.proposeIndicator: 'propose_indicator',
  AnalystProfileInputAllowedActionsEnum.proposeQuestion: 'propose_question',
  AnalystProfileInputAllowedActionsEnum.proposeSituationMerge:
      'propose_situation_merge',
  AnalystProfileInputAllowedActionsEnum.proposeChronicle: 'propose_chronicle',
};

const _$AnalystProfileInputNotifyMinPriorityEnumEnumMap = {
  AnalystProfileInputNotifyMinPriorityEnum.low: 'low',
  AnalystProfileInputNotifyMinPriorityEnum.normal: 'normal',
  AnalystProfileInputNotifyMinPriorityEnum.high: 'high',
};
