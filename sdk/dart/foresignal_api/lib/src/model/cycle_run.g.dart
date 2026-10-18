// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'cycle_run.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$CycleRunCWProxy {
  CycleRun id(String id);

  CycleRun cycleId(String? cycleId);

  CycleRun situationId(String? situationId);

  CycleRun cadence(CycleRunCadenceEnum cadence);

  CycleRun trigger(CycleRunTriggerEnum trigger);

  CycleRun status(CycleRunStatusEnum status);

  CycleRun periodStart(DateTime periodStart);

  CycleRun periodEnd(DateTime periodEnd);

  CycleRun previousRunId(String? previousRunId);

  CycleRun startedAt(DateTime? startedAt);

  CycleRun finishedAt(DateTime? finishedAt);

  CycleRun packet(CyclePacket? packet);

  CycleRun roles(List<CycleRunRolesInner>? roles);

  CycleRun tokensUsed(int? tokensUsed);

  CycleRun assessmentId(String? assessmentId);

  CycleRun maintenance(MaintenanceRun? maintenance);

  CycleRun manifestId(String? manifestId);

  CycleRun warnings(List<String>? warnings);

  CycleRun error(String? error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CycleRun(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CycleRun(...).copyWith(id: 12, name: "My name")
  /// ```
  CycleRun call({
    String id,
    String? cycleId,
    String? situationId,
    CycleRunCadenceEnum cadence,
    CycleRunTriggerEnum trigger,
    CycleRunStatusEnum status,
    DateTime periodStart,
    DateTime periodEnd,
    String? previousRunId,
    DateTime? startedAt,
    DateTime? finishedAt,
    CyclePacket? packet,
    List<CycleRunRolesInner>? roles,
    int? tokensUsed,
    String? assessmentId,
    MaintenanceRun? maintenance,
    String? manifestId,
    List<String>? warnings,
    String? error,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfCycleRun.copyWith(...)` or call `instanceOfCycleRun.copyWith.fieldName(value)` for a single field.
class _$CycleRunCWProxyImpl implements _$CycleRunCWProxy {
  const _$CycleRunCWProxyImpl(this._value);

  final CycleRun _value;

  @override
  CycleRun id(String id) => call(id: id);

  @override
  CycleRun cycleId(String? cycleId) => call(cycleId: cycleId);

  @override
  CycleRun situationId(String? situationId) => call(situationId: situationId);

  @override
  CycleRun cadence(CycleRunCadenceEnum cadence) => call(cadence: cadence);

  @override
  CycleRun trigger(CycleRunTriggerEnum trigger) => call(trigger: trigger);

  @override
  CycleRun status(CycleRunStatusEnum status) => call(status: status);

  @override
  CycleRun periodStart(DateTime periodStart) => call(periodStart: periodStart);

  @override
  CycleRun periodEnd(DateTime periodEnd) => call(periodEnd: periodEnd);

  @override
  CycleRun previousRunId(String? previousRunId) =>
      call(previousRunId: previousRunId);

  @override
  CycleRun startedAt(DateTime? startedAt) => call(startedAt: startedAt);

  @override
  CycleRun finishedAt(DateTime? finishedAt) => call(finishedAt: finishedAt);

  @override
  CycleRun packet(CyclePacket? packet) => call(packet: packet);

  @override
  CycleRun roles(List<CycleRunRolesInner>? roles) => call(roles: roles);

  @override
  CycleRun tokensUsed(int? tokensUsed) => call(tokensUsed: tokensUsed);

  @override
  CycleRun assessmentId(String? assessmentId) =>
      call(assessmentId: assessmentId);

  @override
  CycleRun maintenance(MaintenanceRun? maintenance) =>
      call(maintenance: maintenance);

  @override
  CycleRun manifestId(String? manifestId) => call(manifestId: manifestId);

  @override
  CycleRun warnings(List<String>? warnings) => call(warnings: warnings);

  @override
  CycleRun error(String? error) => call(error: error);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `CycleRun(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// CycleRun(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  CycleRun call({
    Object? id = const $CopyWithPlaceholder(),
    Object? cycleId = const $CopyWithPlaceholder(),
    Object? situationId = const $CopyWithPlaceholder(),
    Object? cadence = const $CopyWithPlaceholder(),
    Object? trigger = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? periodStart = const $CopyWithPlaceholder(),
    Object? periodEnd = const $CopyWithPlaceholder(),
    Object? previousRunId = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? packet = const $CopyWithPlaceholder(),
    Object? roles = const $CopyWithPlaceholder(),
    Object? tokensUsed = const $CopyWithPlaceholder(),
    Object? assessmentId = const $CopyWithPlaceholder(),
    Object? maintenance = const $CopyWithPlaceholder(),
    Object? manifestId = const $CopyWithPlaceholder(),
    Object? warnings = const $CopyWithPlaceholder(),
    Object? error = const $CopyWithPlaceholder(),
  }) {
    return CycleRun(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      cycleId: cycleId == const $CopyWithPlaceholder()
          ? _value.cycleId
          // ignore: cast_nullable_to_non_nullable
          : cycleId as String?,
      situationId: situationId == const $CopyWithPlaceholder()
          ? _value.situationId
          // ignore: cast_nullable_to_non_nullable
          : situationId as String?,
      cadence: cadence == const $CopyWithPlaceholder() || cadence == null
          ? _value.cadence
          // ignore: cast_nullable_to_non_nullable
          : cadence as CycleRunCadenceEnum,
      trigger: trigger == const $CopyWithPlaceholder() || trigger == null
          ? _value.trigger
          // ignore: cast_nullable_to_non_nullable
          : trigger as CycleRunTriggerEnum,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as CycleRunStatusEnum,
      periodStart:
          periodStart == const $CopyWithPlaceholder() || periodStart == null
          ? _value.periodStart
          // ignore: cast_nullable_to_non_nullable
          : periodStart as DateTime,
      periodEnd: periodEnd == const $CopyWithPlaceholder() || periodEnd == null
          ? _value.periodEnd
          // ignore: cast_nullable_to_non_nullable
          : periodEnd as DateTime,
      previousRunId: previousRunId == const $CopyWithPlaceholder()
          ? _value.previousRunId
          // ignore: cast_nullable_to_non_nullable
          : previousRunId as String?,
      startedAt: startedAt == const $CopyWithPlaceholder()
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime?,
      finishedAt: finishedAt == const $CopyWithPlaceholder()
          ? _value.finishedAt
          // ignore: cast_nullable_to_non_nullable
          : finishedAt as DateTime?,
      packet: packet == const $CopyWithPlaceholder()
          ? _value.packet
          // ignore: cast_nullable_to_non_nullable
          : packet as CyclePacket?,
      roles: roles == const $CopyWithPlaceholder()
          ? _value.roles
          // ignore: cast_nullable_to_non_nullable
          : roles as List<CycleRunRolesInner>?,
      tokensUsed: tokensUsed == const $CopyWithPlaceholder()
          ? _value.tokensUsed
          // ignore: cast_nullable_to_non_nullable
          : tokensUsed as int?,
      assessmentId: assessmentId == const $CopyWithPlaceholder()
          ? _value.assessmentId
          // ignore: cast_nullable_to_non_nullable
          : assessmentId as String?,
      maintenance: maintenance == const $CopyWithPlaceholder()
          ? _value.maintenance
          // ignore: cast_nullable_to_non_nullable
          : maintenance as MaintenanceRun?,
      manifestId: manifestId == const $CopyWithPlaceholder()
          ? _value.manifestId
          // ignore: cast_nullable_to_non_nullable
          : manifestId as String?,
      warnings: warnings == const $CopyWithPlaceholder()
          ? _value.warnings
          // ignore: cast_nullable_to_non_nullable
          : warnings as List<String>?,
      error: error == const $CopyWithPlaceholder()
          ? _value.error
          // ignore: cast_nullable_to_non_nullable
          : error as String?,
    );
  }
}

extension $CycleRunCopyWith on CycleRun {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfCycleRun.copyWith(...)` or `instanceOfCycleRun.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$CycleRunCWProxy get copyWith => _$CycleRunCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

CycleRun _$CycleRunFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('CycleRun', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'cadence',
      'trigger',
      'status',
      'periodStart',
      'periodEnd',
    ],
  );
  final val = CycleRun(
    id: $checkedConvert('id', (v) => v as String),
    cycleId: $checkedConvert('cycleId', (v) => v as String?),
    situationId: $checkedConvert('situationId', (v) => v as String?),
    cadence: $checkedConvert(
      'cadence',
      (v) => $enumDecode(_$CycleRunCadenceEnumEnumMap, v),
    ),
    trigger: $checkedConvert(
      'trigger',
      (v) => $enumDecode(_$CycleRunTriggerEnumEnumMap, v),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$CycleRunStatusEnumEnumMap, v),
    ),
    periodStart: $checkedConvert(
      'periodStart',
      (v) => DateTime.parse(v as String),
    ),
    periodEnd: $checkedConvert('periodEnd', (v) => DateTime.parse(v as String)),
    previousRunId: $checkedConvert('previousRunId', (v) => v as String?),
    startedAt: $checkedConvert(
      'startedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    finishedAt: $checkedConvert(
      'finishedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    packet: $checkedConvert(
      'packet',
      (v) => v == null ? null : CyclePacket.fromJson(v as Map<String, dynamic>),
    ),
    roles: $checkedConvert(
      'roles',
      (v) => (v as List<dynamic>?)
          ?.map((e) => CycleRunRolesInner.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    tokensUsed: $checkedConvert('tokensUsed', (v) => (v as num?)?.toInt()),
    assessmentId: $checkedConvert('assessmentId', (v) => v as String?),
    maintenance: $checkedConvert(
      'maintenance',
      (v) =>
          v == null ? null : MaintenanceRun.fromJson(v as Map<String, dynamic>),
    ),
    manifestId: $checkedConvert('manifestId', (v) => v as String?),
    warnings: $checkedConvert(
      'warnings',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    error: $checkedConvert('error', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$CycleRunToJson(CycleRun instance) => <String, dynamic>{
  'id': instance.id,
  'cycleId': ?instance.cycleId,
  'situationId': ?instance.situationId,
  'cadence': _$CycleRunCadenceEnumEnumMap[instance.cadence]!,
  'trigger': _$CycleRunTriggerEnumEnumMap[instance.trigger]!,
  'status': _$CycleRunStatusEnumEnumMap[instance.status]!,
  'periodStart': instance.periodStart.toIso8601String(),
  'periodEnd': instance.periodEnd.toIso8601String(),
  'previousRunId': ?instance.previousRunId,
  'startedAt': ?instance.startedAt?.toIso8601String(),
  'finishedAt': ?instance.finishedAt?.toIso8601String(),
  'packet': ?instance.packet?.toJson(),
  'roles': ?instance.roles?.map((e) => e.toJson()).toList(),
  'tokensUsed': ?instance.tokensUsed,
  'assessmentId': ?instance.assessmentId,
  'maintenance': ?instance.maintenance?.toJson(),
  'manifestId': ?instance.manifestId,
  'warnings': ?instance.warnings,
  'error': ?instance.error,
};

const _$CycleRunCadenceEnumEnumMap = {
  CycleRunCadenceEnum.daily: 'daily',
  CycleRunCadenceEnum.weekly: 'weekly',
  CycleRunCadenceEnum.monthly: 'monthly',
  CycleRunCadenceEnum.onDemand: 'on-demand',
};

const _$CycleRunTriggerEnumEnumMap = {
  CycleRunTriggerEnum.schedule: 'schedule',
  CycleRunTriggerEnum.missed: 'missed',
  CycleRunTriggerEnum.manual: 'manual',
  CycleRunTriggerEnum.situation: 'situation',
};

const _$CycleRunStatusEnumEnumMap = {
  CycleRunStatusEnum.queued: 'queued',
  CycleRunStatusEnum.running: 'running',
  CycleRunStatusEnum.ok: 'ok',
  CycleRunStatusEnum.partial: 'partial',
  CycleRunStatusEnum.digestOnly: 'digest-only',
  CycleRunStatusEnum.failed: 'failed',
};
