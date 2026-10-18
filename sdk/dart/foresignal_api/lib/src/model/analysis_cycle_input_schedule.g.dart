// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analysis_cycle_input_schedule.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalysisCycleInputScheduleCWProxy {
  AnalysisCycleInputSchedule cron(String cron);

  AnalysisCycleInputSchedule timezone(String timezone);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisCycleInputSchedule(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisCycleInputSchedule(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalysisCycleInputSchedule call({String cron, String timezone});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalysisCycleInputSchedule.copyWith(...)` or call `instanceOfAnalysisCycleInputSchedule.copyWith.fieldName(value)` for a single field.
class _$AnalysisCycleInputScheduleCWProxyImpl
    implements _$AnalysisCycleInputScheduleCWProxy {
  const _$AnalysisCycleInputScheduleCWProxyImpl(this._value);

  final AnalysisCycleInputSchedule _value;

  @override
  AnalysisCycleInputSchedule cron(String cron) => call(cron: cron);

  @override
  AnalysisCycleInputSchedule timezone(String timezone) =>
      call(timezone: timezone);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalysisCycleInputSchedule(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalysisCycleInputSchedule(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalysisCycleInputSchedule call({
    Object? cron = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
  }) {
    return AnalysisCycleInputSchedule(
      cron: cron == const $CopyWithPlaceholder() || cron == null
          ? _value.cron
          // ignore: cast_nullable_to_non_nullable
          : cron as String,
      timezone: timezone == const $CopyWithPlaceholder() || timezone == null
          ? _value.timezone
          // ignore: cast_nullable_to_non_nullable
          : timezone as String,
    );
  }
}

extension $AnalysisCycleInputScheduleCopyWith on AnalysisCycleInputSchedule {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalysisCycleInputSchedule.copyWith(...)` or `instanceOfAnalysisCycleInputSchedule.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalysisCycleInputScheduleCWProxy get copyWith =>
      _$AnalysisCycleInputScheduleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalysisCycleInputSchedule _$AnalysisCycleInputScheduleFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalysisCycleInputSchedule', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['cron', 'timezone']);
  final val = AnalysisCycleInputSchedule(
    cron: $checkedConvert('cron', (v) => v as String),
    timezone: $checkedConvert('timezone', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$AnalysisCycleInputScheduleToJson(
  AnalysisCycleInputSchedule instance,
) => <String, dynamic>{'cron': instance.cron, 'timezone': instance.timezone};
