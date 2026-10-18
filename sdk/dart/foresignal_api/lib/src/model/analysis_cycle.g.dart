// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analysis_cycle.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalysisCycleCWProxy {
  AnalysisCycle name(String name);

  AnalysisCycle cadence(AnalysisCycleCadenceEnum cadence);

  AnalysisCycle schedule(AnalysisCycleInputSchedule schedule);

  AnalysisCycle scope(CycleScope scope);

  AnalysisCycle analystProfileId(String? analystProfileId);

  AnalysisCycle roles(Set<AnalysisCycleRolesEnum> roles);

  AnalysisCycle forecasterProviders(List<String>? forecasterProviders);

  AnalysisCycle samples(int samples);

  AnalysisCycle maxQuestions(int? maxQuestions);

  AnalysisCycle maxTokensPerCycle(int? maxTokensPerCycle);

  AnalysisCycle channelIds(List<String>? channelIds);

  AnalysisCycle notifyMinPriority(
    AnalysisCycleNotifyMinPriorityEnum? notifyMinPriority,
  );

  AnalysisCycle enabled(bool enabled);

  AnalysisCycle id(String id);

  AnalysisCycle builtIn(bool? builtIn);

  AnalysisCycle nextRunAt(DateTime? nextRunAt);

  AnalysisCycle lastRunId(String? lastRunId);

  AnalysisCycle createdAt(DateTime createdAt);

  AnalysisCycle updatedAt(DateTime? updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisCycle(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisCycle(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalysisCycle call({
    String name,
    AnalysisCycleCadenceEnum cadence,
    AnalysisCycleInputSchedule schedule,
    CycleScope scope,
    String? analystProfileId,
    Set<AnalysisCycleRolesEnum> roles,
    List<String>? forecasterProviders,
    int samples,
    int? maxQuestions,
    int? maxTokensPerCycle,
    List<String>? channelIds,
    AnalysisCycleNotifyMinPriorityEnum? notifyMinPriority,
    bool enabled,
    String id,
    bool? builtIn,
    DateTime? nextRunAt,
    String? lastRunId,
    DateTime createdAt,
    DateTime? updatedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalysisCycle.copyWith(...)` or call `instanceOfAnalysisCycle.copyWith.fieldName(value)` for a single field.
class _$AnalysisCycleCWProxyImpl implements _$AnalysisCycleCWProxy {
  const _$AnalysisCycleCWProxyImpl(this._value);

  final AnalysisCycle _value;

  @override
  AnalysisCycle name(String name) => call(name: name);

  @override
  AnalysisCycle cadence(AnalysisCycleCadenceEnum cadence) =>
      call(cadence: cadence);

  @override
  AnalysisCycle schedule(AnalysisCycleInputSchedule schedule) =>
      call(schedule: schedule);

  @override
  AnalysisCycle scope(CycleScope scope) => call(scope: scope);

  @override
  AnalysisCycle analystProfileId(String? analystProfileId) =>
      call(analystProfileId: analystProfileId);

  @override
  AnalysisCycle roles(Set<AnalysisCycleRolesEnum> roles) => call(roles: roles);

  @override
  AnalysisCycle forecasterProviders(List<String>? forecasterProviders) =>
      call(forecasterProviders: forecasterProviders);

  @override
  AnalysisCycle samples(int samples) => call(samples: samples);

  @override
  AnalysisCycle maxQuestions(int? maxQuestions) =>
      call(maxQuestions: maxQuestions);

  @override
  AnalysisCycle maxTokensPerCycle(int? maxTokensPerCycle) =>
      call(maxTokensPerCycle: maxTokensPerCycle);

  @override
  AnalysisCycle channelIds(List<String>? channelIds) =>
      call(channelIds: channelIds);

  @override
  AnalysisCycle notifyMinPriority(
    AnalysisCycleNotifyMinPriorityEnum? notifyMinPriority,
  ) => call(notifyMinPriority: notifyMinPriority);

  @override
  AnalysisCycle enabled(bool enabled) => call(enabled: enabled);

  @override
  AnalysisCycle id(String id) => call(id: id);

  @override
  AnalysisCycle builtIn(bool? builtIn) => call(builtIn: builtIn);

  @override
  AnalysisCycle nextRunAt(DateTime? nextRunAt) => call(nextRunAt: nextRunAt);

  @override
  AnalysisCycle lastRunId(String? lastRunId) => call(lastRunId: lastRunId);

  @override
  AnalysisCycle createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  AnalysisCycle updatedAt(DateTime? updatedAt) => call(updatedAt: updatedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisCycle(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisCycle(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalysisCycle call({
    Object? name = const $CopyWithPlaceholder(),
    Object? cadence = const $CopyWithPlaceholder(),
    Object? schedule = const $CopyWithPlaceholder(),
    Object? scope = const $CopyWithPlaceholder(),
    Object? analystProfileId = const $CopyWithPlaceholder(),
    Object? roles = const $CopyWithPlaceholder(),
    Object? forecasterProviders = const $CopyWithPlaceholder(),
    Object? samples = const $CopyWithPlaceholder(),
    Object? maxQuestions = const $CopyWithPlaceholder(),
    Object? maxTokensPerCycle = const $CopyWithPlaceholder(),
    Object? channelIds = const $CopyWithPlaceholder(),
    Object? notifyMinPriority = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? builtIn = const $CopyWithPlaceholder(),
    Object? nextRunAt = const $CopyWithPlaceholder(),
    Object? lastRunId = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? updatedAt = const $CopyWithPlaceholder(),
  }) {
    return AnalysisCycle(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      cadence: cadence == const $CopyWithPlaceholder() || cadence == null
          ? _value.cadence
          // ignore: cast_nullable_to_non_nullable
          : cadence as AnalysisCycleCadenceEnum,
      schedule: schedule == const $CopyWithPlaceholder() || schedule == null
          ? _value.schedule
          // ignore: cast_nullable_to_non_nullable
          : schedule as AnalysisCycleInputSchedule,
      scope: scope == const $CopyWithPlaceholder() || scope == null
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as CycleScope,
      analystProfileId: analystProfileId == const $CopyWithPlaceholder()
          ? _value.analystProfileId
          // ignore: cast_nullable_to_non_nullable
          : analystProfileId as String?,
      roles: roles == const $CopyWithPlaceholder() || roles == null
          ? _value.roles
          // ignore: cast_nullable_to_non_nullable
          : roles as Set<AnalysisCycleRolesEnum>,
      forecasterProviders: forecasterProviders == const $CopyWithPlaceholder()
          ? _value.forecasterProviders
          // ignore: cast_nullable_to_non_nullable
          : forecasterProviders as List<String>?,
      samples: samples == const $CopyWithPlaceholder() || samples == null
          ? _value.samples
          // ignore: cast_nullable_to_non_nullable
          : samples as int,
      maxQuestions: maxQuestions == const $CopyWithPlaceholder()
          ? _value.maxQuestions
          // ignore: cast_nullable_to_non_nullable
          : maxQuestions as int?,
      maxTokensPerCycle: maxTokensPerCycle == const $CopyWithPlaceholder()
          ? _value.maxTokensPerCycle
          // ignore: cast_nullable_to_non_nullable
          : maxTokensPerCycle as int?,
      channelIds: channelIds == const $CopyWithPlaceholder()
          ? _value.channelIds
          // ignore: cast_nullable_to_non_nullable
          : channelIds as List<String>?,
      notifyMinPriority: notifyMinPriority == const $CopyWithPlaceholder()
          ? _value.notifyMinPriority
          // ignore: cast_nullable_to_non_nullable
          : notifyMinPriority as AnalysisCycleNotifyMinPriorityEnum?,
      enabled: enabled == const $CopyWithPlaceholder() || enabled == null
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      builtIn: builtIn == const $CopyWithPlaceholder()
          ? _value.builtIn
          // ignore: cast_nullable_to_non_nullable
          : builtIn as bool?,
      nextRunAt: nextRunAt == const $CopyWithPlaceholder()
          ? _value.nextRunAt
          // ignore: cast_nullable_to_non_nullable
          : nextRunAt as DateTime?,
      lastRunId: lastRunId == const $CopyWithPlaceholder()
          ? _value.lastRunId
          // ignore: cast_nullable_to_non_nullable
          : lastRunId as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      updatedAt: updatedAt == const $CopyWithPlaceholder()
          ? _value.updatedAt
          // ignore: cast_nullable_to_non_nullable
          : updatedAt as DateTime?,
    );
  }
}

extension $AnalysisCycleCopyWith on AnalysisCycle {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalysisCycle.copyWith(...)` or `instanceOfAnalysisCycle.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalysisCycleCWProxy get copyWith => _$AnalysisCycleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalysisCycle _$AnalysisCycleFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalysisCycle', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'name',
      'cadence',
      'schedule',
      'scope',
      'roles',
      'samples',
      'enabled',
      'id',
      'createdAt',
    ],
  );
  final val = AnalysisCycle(
    name: $checkedConvert('name', (v) => v as String),
    cadence: $checkedConvert(
      'cadence',
      (v) => $enumDecode(_$AnalysisCycleCadenceEnumEnumMap, v),
    ),
    schedule: $checkedConvert(
      'schedule',
      (v) => AnalysisCycleInputSchedule.fromJson(v as Map<String, dynamic>),
    ),
    scope: $checkedConvert(
      'scope',
      (v) => CycleScope.fromJson(v as Map<String, dynamic>),
    ),
    analystProfileId: $checkedConvert('analystProfileId', (v) => v as String?),
    roles: $checkedConvert(
      'roles',
      (v) => (v as List<dynamic>)
          .map((e) => $enumDecode(_$AnalysisCycleRolesEnumEnumMap, e))
          .toSet(),
    ),
    forecasterProviders: $checkedConvert(
      'forecasterProviders',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    samples: $checkedConvert('samples', (v) => (v as num?)?.toInt() ?? 3),
    maxQuestions: $checkedConvert('maxQuestions', (v) => (v as num?)?.toInt()),
    maxTokensPerCycle: $checkedConvert(
      'maxTokensPerCycle',
      (v) => (v as num?)?.toInt(),
    ),
    channelIds: $checkedConvert(
      'channelIds',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    notifyMinPriority: $checkedConvert(
      'notifyMinPriority',
      (v) =>
          $enumDecodeNullable(_$AnalysisCycleNotifyMinPriorityEnumEnumMap, v),
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool),
    id: $checkedConvert('id', (v) => v as String),
    builtIn: $checkedConvert('builtIn', (v) => v as bool?),
    nextRunAt: $checkedConvert(
      'nextRunAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastRunId: $checkedConvert('lastRunId', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    updatedAt: $checkedConvert(
      'updatedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
  );
  return val;
});

Map<String, dynamic> _$AnalysisCycleToJson(
  AnalysisCycle instance,
) => <String, dynamic>{
  'name': instance.name,
  'cadence': _$AnalysisCycleCadenceEnumEnumMap[instance.cadence]!,
  'schedule': instance.schedule.toJson(),
  'scope': instance.scope.toJson(),
  'analystProfileId': ?instance.analystProfileId,
  'roles': instance.roles
      .map((e) => _$AnalysisCycleRolesEnumEnumMap[e]!)
      .toList(),
  'forecasterProviders': ?instance.forecasterProviders,
  'samples': instance.samples,
  'maxQuestions': ?instance.maxQuestions,
  'maxTokensPerCycle': ?instance.maxTokensPerCycle,
  'channelIds': ?instance.channelIds,
  'notifyMinPriority':
      ?_$AnalysisCycleNotifyMinPriorityEnumEnumMap[instance.notifyMinPriority],
  'enabled': instance.enabled,
  'id': instance.id,
  'builtIn': ?instance.builtIn,
  'nextRunAt': ?instance.nextRunAt?.toIso8601String(),
  'lastRunId': ?instance.lastRunId,
  'createdAt': instance.createdAt.toIso8601String(),
  'updatedAt': ?instance.updatedAt?.toIso8601String(),
};

const _$AnalysisCycleCadenceEnumEnumMap = {
  AnalysisCycleCadenceEnum.daily: 'daily',
  AnalysisCycleCadenceEnum.weekly: 'weekly',
  AnalysisCycleCadenceEnum.monthly: 'monthly',
};

const _$AnalysisCycleRolesEnumEnumMap = {
  AnalysisCycleRolesEnum.analyst: 'analyst',
  AnalysisCycleRolesEnum.redTeam: 'red-team',
  AnalysisCycleRolesEnum.forecaster: 'forecaster',
  AnalysisCycleRolesEnum.proposer: 'proposer',
};

const _$AnalysisCycleNotifyMinPriorityEnumEnumMap = {
  AnalysisCycleNotifyMinPriorityEnum.low: 'low',
  AnalysisCycleNotifyMinPriorityEnum.normal: 'normal',
  AnalysisCycleNotifyMinPriorityEnum.high: 'high',
};
