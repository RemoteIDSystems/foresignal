// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'quiet_hours.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$QuietHoursCWProxy {
  QuietHours start(String start);

  QuietHours end(String end);

  QuietHours days(List<QuietHoursDaysEnum>? days);

  QuietHours bypassSeverity(QuietHoursBypassSeverityEnum? bypassSeverity);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `QuietHours(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// QuietHours(...).copyWith(id: 12, name: "My name")
  /// ```
  QuietHours call({
    String start,
    String end,
    List<QuietHoursDaysEnum>? days,
    QuietHoursBypassSeverityEnum? bypassSeverity,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfQuietHours.copyWith(...)` or call `instanceOfQuietHours.copyWith.fieldName(value)` for a single field.
class _$QuietHoursCWProxyImpl implements _$QuietHoursCWProxy {
  const _$QuietHoursCWProxyImpl(this._value);

  final QuietHours _value;

  @override
  QuietHours start(String start) => call(start: start);

  @override
  QuietHours end(String end) => call(end: end);

  @override
  QuietHours days(List<QuietHoursDaysEnum>? days) => call(days: days);

  @override
  QuietHours bypassSeverity(QuietHoursBypassSeverityEnum? bypassSeverity) =>
      call(bypassSeverity: bypassSeverity);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `QuietHours(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// QuietHours(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  QuietHours call({
    Object? start = const $CopyWithPlaceholder(),
    Object? end = const $CopyWithPlaceholder(),
    Object? days = const $CopyWithPlaceholder(),
    Object? bypassSeverity = const $CopyWithPlaceholder(),
  }) {
    return QuietHours(
      start: start == const $CopyWithPlaceholder() || start == null
          ? _value.start
          // ignore: cast_nullable_to_non_nullable
          : start as String,
      end: end == const $CopyWithPlaceholder() || end == null
          ? _value.end
          // ignore: cast_nullable_to_non_nullable
          : end as String,
      days: days == const $CopyWithPlaceholder()
          ? _value.days
          // ignore: cast_nullable_to_non_nullable
          : days as List<QuietHoursDaysEnum>?,
      bypassSeverity: bypassSeverity == const $CopyWithPlaceholder()
          ? _value.bypassSeverity
          // ignore: cast_nullable_to_non_nullable
          : bypassSeverity as QuietHoursBypassSeverityEnum?,
    );
  }
}

extension $QuietHoursCopyWith on QuietHours {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfQuietHours.copyWith(...)` or `instanceOfQuietHours.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$QuietHoursCWProxy get copyWith => _$QuietHoursCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

QuietHours _$QuietHoursFromJson(Map<String, dynamic> json) =>
    $checkedCreate('QuietHours', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['start', 'end']);
      final val = QuietHours(
        start: $checkedConvert('start', (v) => v as String),
        end: $checkedConvert('end', (v) => v as String),
        days: $checkedConvert(
          'days',
          (v) => (v as List<dynamic>?)
              ?.map((e) => $enumDecode(_$QuietHoursDaysEnumEnumMap, e))
              .toList(),
        ),
        bypassSeverity: $checkedConvert(
          'bypassSeverity',
          (v) => $enumDecodeNullable(_$QuietHoursBypassSeverityEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$QuietHoursToJson(
  QuietHours instance,
) => <String, dynamic>{
  'start': instance.start,
  'end': instance.end,
  'days': ?instance.days?.map((e) => _$QuietHoursDaysEnumEnumMap[e]!).toList(),
  'bypassSeverity':
      ?_$QuietHoursBypassSeverityEnumEnumMap[instance.bypassSeverity],
};

const _$QuietHoursDaysEnumEnumMap = {
  QuietHoursDaysEnum.MON: 'MON',
  QuietHoursDaysEnum.TUE: 'TUE',
  QuietHoursDaysEnum.WED: 'WED',
  QuietHoursDaysEnum.THU: 'THU',
  QuietHoursDaysEnum.FRI: 'FRI',
  QuietHoursDaysEnum.SAT: 'SAT',
  QuietHoursDaysEnum.SUN: 'SUN',
};

const _$QuietHoursBypassSeverityEnumEnumMap = {
  QuietHoursBypassSeverityEnum.info: 'info',
  QuietHoursBypassSeverityEnum.warning: 'warning',
  QuietHoursBypassSeverityEnum.critical: 'critical',
};
