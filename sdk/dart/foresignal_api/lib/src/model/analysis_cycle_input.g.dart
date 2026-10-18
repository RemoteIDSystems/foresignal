// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analysis_cycle_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalysisCycleInputCWProxy {
  AnalysisCycleInput name(String name);

  AnalysisCycleInput cadence(AnalysisCycleInputCadenceEnum cadence);

  AnalysisCycleInput schedule(AnalysisCycleInputSchedule schedule);

  AnalysisCycleInput scope(CycleScope? scope);

  AnalysisCycleInput analystProfileId(String? analystProfileId);

  AnalysisCycleInput roles(Set<AnalysisCycleInputRolesEnum>? roles);

  AnalysisCycleInput forecasterProviders(List<String>? forecasterProviders);

  AnalysisCycleInput samples(int? samples);

  AnalysisCycleInput maxQuestions(int? maxQuestions);

  AnalysisCycleInput maxTokensPerCycle(int? maxTokensPerCycle);

  AnalysisCycleInput channelIds(List<String>? channelIds);

  AnalysisCycleInput notifyMinPriority(
    AnalysisCycleInputNotifyMinPriorityEnum? notifyMinPriority,
  );

  AnalysisCycleInput enabled(bool? enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisCycleInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisCycleInput(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalysisCycleInput call({
    String name,
    AnalysisCycleInputCadenceEnum cadence,
    AnalysisCycleInputSchedule schedule,
    CycleScope? scope,
    String? analystProfileId,
    Set<AnalysisCycleInputRolesEnum>? roles,
    List<String>? forecasterProviders,
    int? samples,
    int? maxQuestions,
    int? maxTokensPerCycle,
    List<String>? channelIds,
    AnalysisCycleInputNotifyMinPriorityEnum? notifyMinPriority,
    bool? enabled,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalysisCycleInput.copyWith(...)` or call `instanceOfAnalysisCycleInput.copyWith.fieldName(value)` for a single field.
class _$AnalysisCycleInputCWProxyImpl implements _$AnalysisCycleInputCWProxy {
  const _$AnalysisCycleInputCWProxyImpl(this._value);

  final AnalysisCycleInput _value;

  @override
  AnalysisCycleInput name(String name) => call(name: name);

  @override
  AnalysisCycleInput cadence(AnalysisCycleInputCadenceEnum cadence) =>
      call(cadence: cadence);

  @override
  AnalysisCycleInput schedule(AnalysisCycleInputSchedule schedule) =>
      call(schedule: schedule);

  @override
  AnalysisCycleInput scope(CycleScope? scope) => call(scope: scope);

  @override
  AnalysisCycleInput analystProfileId(String? analystProfileId) =>
      call(analystProfileId: analystProfileId);

  @override
  AnalysisCycleInput roles(Set<AnalysisCycleInputRolesEnum>? roles) =>
      call(roles: roles);

  @override
  AnalysisCycleInput forecasterProviders(List<String>? forecasterProviders) =>
      call(forecasterProviders: forecasterProviders);

  @override
  AnalysisCycleInput samples(int? samples) => call(samples: samples);

  @override
  AnalysisCycleInput maxQuestions(int? maxQuestions) =>
      call(maxQuestions: maxQuestions);

  @override
  AnalysisCycleInput maxTokensPerCycle(int? maxTokensPerCycle) =>
      call(maxTokensPerCycle: maxTokensPerCycle);

  @override
  AnalysisCycleInput channelIds(List<String>? channelIds) =>
      call(channelIds: channelIds);

  @override
  AnalysisCycleInput notifyMinPriority(
    AnalysisCycleInputNotifyMinPriorityEnum? notifyMinPriority,
  ) => call(notifyMinPriority: notifyMinPriority);

  @override
  AnalysisCycleInput enabled(bool? enabled) => call(enabled: enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisCycleInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisCycleInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalysisCycleInput call({
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
  }) {
    return AnalysisCycleInput(
      name: name == const $CopyWithPlaceholder() || name == null
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String,
      cadence: cadence == const $CopyWithPlaceholder() || cadence == null
          ? _value.cadence
          // ignore: cast_nullable_to_non_nullable
          : cadence as AnalysisCycleInputCadenceEnum,
      schedule: schedule == const $CopyWithPlaceholder() || schedule == null
          ? _value.schedule
          // ignore: cast_nullable_to_non_nullable
          : schedule as AnalysisCycleInputSchedule,
      scope: scope == const $CopyWithPlaceholder()
          ? _value.scope
          // ignore: cast_nullable_to_non_nullable
          : scope as CycleScope?,
      analystProfileId: analystProfileId == const $CopyWithPlaceholder()
          ? _value.analystProfileId
          // ignore: cast_nullable_to_non_nullable
          : analystProfileId as String?,
      roles: roles == const $CopyWithPlaceholder()
          ? _value.roles
          // ignore: cast_nullable_to_non_nullable
          : roles as Set<AnalysisCycleInputRolesEnum>?,
      forecasterProviders: forecasterProviders == const $CopyWithPlaceholder()
          ? _value.forecasterProviders
          // ignore: cast_nullable_to_non_nullable
          : forecasterProviders as List<String>?,
      samples: samples == const $CopyWithPlaceholder()
          ? _value.samples
          // ignore: cast_nullable_to_non_nullable
          : samples as int?,
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
          : notifyMinPriority as AnalysisCycleInputNotifyMinPriorityEnum?,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
    );
  }
}

extension $AnalysisCycleInputCopyWith on AnalysisCycleInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalysisCycleInput.copyWith(...)` or `instanceOfAnalysisCycleInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalysisCycleInputCWProxy get copyWith =>
      _$AnalysisCycleInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalysisCycleInput _$AnalysisCycleInputFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalysisCycleInput', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['name', 'cadence', 'schedule']);
  final val = AnalysisCycleInput(
    name: $checkedConvert('name', (v) => v as String),
    cadence: $checkedConvert(
      'cadence',
      (v) => $enumDecode(_$AnalysisCycleInputCadenceEnumEnumMap, v),
    ),
    schedule: $checkedConvert(
      'schedule',
      (v) => AnalysisCycleInputSchedule.fromJson(v as Map<String, dynamic>),
    ),
    scope: $checkedConvert(
      'scope',
      (v) => v == null ? null : CycleScope.fromJson(v as Map<String, dynamic>),
    ),
    analystProfileId: $checkedConvert('analystProfileId', (v) => v as String?),
    roles: $checkedConvert(
      'roles',
      (v) => (v as List<dynamic>?)
          ?.map((e) => $enumDecode(_$AnalysisCycleInputRolesEnumEnumMap, e))
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
      (v) => $enumDecodeNullable(
        _$AnalysisCycleInputNotifyMinPriorityEnumEnumMap,
        v,
      ),
    ),
    enabled: $checkedConvert('enabled', (v) => v as bool?),
  );
  return val;
});

Map<String, dynamic> _$AnalysisCycleInputToJson(AnalysisCycleInput instance) =>
    <String, dynamic>{
      'name': instance.name,
      'cadence': _$AnalysisCycleInputCadenceEnumEnumMap[instance.cadence]!,
      'schedule': instance.schedule.toJson(),
      'scope': ?instance.scope?.toJson(),
      'analystProfileId': ?instance.analystProfileId,
      'roles': ?instance.roles
          ?.map((e) => _$AnalysisCycleInputRolesEnumEnumMap[e]!)
          .toList(),
      'forecasterProviders': ?instance.forecasterProviders,
      'samples': ?instance.samples,
      'maxQuestions': ?instance.maxQuestions,
      'maxTokensPerCycle': ?instance.maxTokensPerCycle,
      'channelIds': ?instance.channelIds,
      'notifyMinPriority':
          ?_$AnalysisCycleInputNotifyMinPriorityEnumEnumMap[instance
              .notifyMinPriority],
      'enabled': ?instance.enabled,
    };

const _$AnalysisCycleInputCadenceEnumEnumMap = {
  AnalysisCycleInputCadenceEnum.daily: 'daily',
  AnalysisCycleInputCadenceEnum.weekly: 'weekly',
  AnalysisCycleInputCadenceEnum.monthly: 'monthly',
};

const _$AnalysisCycleInputRolesEnumEnumMap = {
  AnalysisCycleInputRolesEnum.analyst: 'analyst',
  AnalysisCycleInputRolesEnum.redTeam: 'red-team',
  AnalysisCycleInputRolesEnum.forecaster: 'forecaster',
  AnalysisCycleInputRolesEnum.proposer: 'proposer',
};

const _$AnalysisCycleInputNotifyMinPriorityEnumEnumMap = {
  AnalysisCycleInputNotifyMinPriorityEnum.low: 'low',
  AnalysisCycleInputNotifyMinPriorityEnum.normal: 'normal',
  AnalysisCycleInputNotifyMinPriorityEnum.high: 'high',
};
