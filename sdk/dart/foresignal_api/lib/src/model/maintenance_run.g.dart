// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'maintenance_run.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$MaintenanceRunCWProxy {
  MaintenanceRun id(String id);

  MaintenanceRun trigger(MaintenanceRunTriggerEnum trigger);

  MaintenanceRun status(MaintenanceRunStatusEnum status);

  MaintenanceRun startedAt(DateTime startedAt);

  MaintenanceRun finishedAt(DateTime? finishedAt);

  MaintenanceRun steps(List<MaintenanceRunStepsInner> steps);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MaintenanceRun(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MaintenanceRun(...).copyWith(id: 12, name: "My name")
  /// ```
  MaintenanceRun call({
    String id,
    MaintenanceRunTriggerEnum trigger,
    MaintenanceRunStatusEnum status,
    DateTime startedAt,
    DateTime? finishedAt,
    List<MaintenanceRunStepsInner> steps,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfMaintenanceRun.copyWith(...)` or call `instanceOfMaintenanceRun.copyWith.fieldName(value)` for a single field.
class _$MaintenanceRunCWProxyImpl implements _$MaintenanceRunCWProxy {
  const _$MaintenanceRunCWProxyImpl(this._value);

  final MaintenanceRun _value;

  @override
  MaintenanceRun id(String id) => call(id: id);

  @override
  MaintenanceRun trigger(MaintenanceRunTriggerEnum trigger) =>
      call(trigger: trigger);

  @override
  MaintenanceRun status(MaintenanceRunStatusEnum status) =>
      call(status: status);

  @override
  MaintenanceRun startedAt(DateTime startedAt) => call(startedAt: startedAt);

  @override
  MaintenanceRun finishedAt(DateTime? finishedAt) =>
      call(finishedAt: finishedAt);

  @override
  MaintenanceRun steps(List<MaintenanceRunStepsInner> steps) =>
      call(steps: steps);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `MaintenanceRun(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// MaintenanceRun(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  MaintenanceRun call({
    Object? id = const $CopyWithPlaceholder(),
    Object? trigger = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? startedAt = const $CopyWithPlaceholder(),
    Object? finishedAt = const $CopyWithPlaceholder(),
    Object? steps = const $CopyWithPlaceholder(),
  }) {
    return MaintenanceRun(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      trigger: trigger == const $CopyWithPlaceholder() || trigger == null
          ? _value.trigger
          // ignore: cast_nullable_to_non_nullable
          : trigger as MaintenanceRunTriggerEnum,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as MaintenanceRunStatusEnum,
      startedAt: startedAt == const $CopyWithPlaceholder() || startedAt == null
          ? _value.startedAt
          // ignore: cast_nullable_to_non_nullable
          : startedAt as DateTime,
      finishedAt: finishedAt == const $CopyWithPlaceholder()
          ? _value.finishedAt
          // ignore: cast_nullable_to_non_nullable
          : finishedAt as DateTime?,
      steps: steps == const $CopyWithPlaceholder() || steps == null
          ? _value.steps
          // ignore: cast_nullable_to_non_nullable
          : steps as List<MaintenanceRunStepsInner>,
    );
  }
}

extension $MaintenanceRunCopyWith on MaintenanceRun {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfMaintenanceRun.copyWith(...)` or `instanceOfMaintenanceRun.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$MaintenanceRunCWProxy get copyWith => _$MaintenanceRunCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

MaintenanceRun _$MaintenanceRunFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('MaintenanceRun', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'trigger', 'status', 'startedAt', 'steps'],
  );
  final val = MaintenanceRun(
    id: $checkedConvert('id', (v) => v as String),
    trigger: $checkedConvert(
      'trigger',
      (v) => $enumDecode(_$MaintenanceRunTriggerEnumEnumMap, v),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$MaintenanceRunStatusEnumEnumMap, v),
    ),
    startedAt: $checkedConvert('startedAt', (v) => DateTime.parse(v as String)),
    finishedAt: $checkedConvert(
      'finishedAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    steps: $checkedConvert(
      'steps',
      (v) => (v as List<dynamic>)
          .map(
            (e) => MaintenanceRunStepsInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$MaintenanceRunToJson(MaintenanceRun instance) =>
    <String, dynamic>{
      'id': instance.id,
      'trigger': _$MaintenanceRunTriggerEnumEnumMap[instance.trigger]!,
      'status': _$MaintenanceRunStatusEnumEnumMap[instance.status]!,
      'startedAt': instance.startedAt.toIso8601String(),
      'finishedAt': ?instance.finishedAt?.toIso8601String(),
      'steps': instance.steps.map((e) => e.toJson()).toList(),
    };

const _$MaintenanceRunTriggerEnumEnumMap = {
  MaintenanceRunTriggerEnum.monthlyCycle: 'monthly-cycle',
  MaintenanceRunTriggerEnum.manual: 'manual',
  MaintenanceRunTriggerEnum.schedule: 'schedule',
};

const _$MaintenanceRunStatusEnumEnumMap = {
  MaintenanceRunStatusEnum.running: 'running',
  MaintenanceRunStatusEnum.done: 'done',
  MaintenanceRunStatusEnum.partial: 'partial',
  MaintenanceRunStatusEnum.failed: 'failed',
};
