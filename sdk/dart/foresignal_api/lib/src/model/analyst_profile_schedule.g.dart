// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_profile_schedule.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystProfileScheduleCWProxy {
  AnalystProfileSchedule cron(String? cron);

  AnalystProfileSchedule timezone(String? timezone);

  AnalystProfileSchedule every(String? every);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystProfileSchedule(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystProfileSchedule(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystProfileSchedule call({String? cron, String? timezone, String? every});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystProfileSchedule.copyWith(...)` or call `instanceOfAnalystProfileSchedule.copyWith.fieldName(value)` for a single field.
class _$AnalystProfileScheduleCWProxyImpl
    implements _$AnalystProfileScheduleCWProxy {
  const _$AnalystProfileScheduleCWProxyImpl(this._value);

  final AnalystProfileSchedule _value;

  @override
  AnalystProfileSchedule cron(String? cron) => call(cron: cron);

  @override
  AnalystProfileSchedule timezone(String? timezone) => call(timezone: timezone);

  @override
  AnalystProfileSchedule every(String? every) => call(every: every);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystProfileSchedule(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystProfileSchedule(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystProfileSchedule call({
    Object? cron = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
    Object? every = const $CopyWithPlaceholder(),
  }) {
    return AnalystProfileSchedule(
      cron: cron == const $CopyWithPlaceholder()
          ? _value.cron
          // ignore: cast_nullable_to_non_nullable
          : cron as String?,
      timezone: timezone == const $CopyWithPlaceholder()
          ? _value.timezone
          // ignore: cast_nullable_to_non_nullable
          : timezone as String?,
      every: every == const $CopyWithPlaceholder()
          ? _value.every
          // ignore: cast_nullable_to_non_nullable
          : every as String?,
    );
  }
}

extension $AnalystProfileScheduleCopyWith on AnalystProfileSchedule {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystProfileSchedule.copyWith(...)` or `instanceOfAnalystProfileSchedule.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystProfileScheduleCWProxy get copyWith =>
      _$AnalystProfileScheduleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystProfileSchedule _$AnalystProfileScheduleFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalystProfileSchedule', json, ($checkedConvert) {
  final val = AnalystProfileSchedule(
    cron: $checkedConvert('cron', (v) => v as String?),
    timezone: $checkedConvert('timezone', (v) => v as String?),
    every: $checkedConvert('every', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$AnalystProfileScheduleToJson(
  AnalystProfileSchedule instance,
) => <String, dynamic>{
  'cron': ?instance.cron,
  'timezone': ?instance.timezone,
  'every': ?instance.every,
};
