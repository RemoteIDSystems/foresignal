// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'trip_schedule.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TripScheduleCWProxy {
  TripSchedule type(TripScheduleTypeEnum type);

  TripSchedule departAt(DateTime? departAt);

  TripSchedule days(List<TripScheduleDaysEnum>? days);

  TripSchedule departTime(String? departTime);

  TripSchedule timezone(String? timezone);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TripSchedule(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TripSchedule(...).copyWith(id: 12, name: "My name")
  /// ```
  TripSchedule call({
    TripScheduleTypeEnum type,
    DateTime? departAt,
    List<TripScheduleDaysEnum>? days,
    String? departTime,
    String? timezone,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTripSchedule.copyWith(...)` or call `instanceOfTripSchedule.copyWith.fieldName(value)` for a single field.
class _$TripScheduleCWProxyImpl implements _$TripScheduleCWProxy {
  const _$TripScheduleCWProxyImpl(this._value);

  final TripSchedule _value;

  @override
  TripSchedule type(TripScheduleTypeEnum type) => call(type: type);

  @override
  TripSchedule departAt(DateTime? departAt) => call(departAt: departAt);

  @override
  TripSchedule days(List<TripScheduleDaysEnum>? days) => call(days: days);

  @override
  TripSchedule departTime(String? departTime) => call(departTime: departTime);

  @override
  TripSchedule timezone(String? timezone) => call(timezone: timezone);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TripSchedule(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TripSchedule(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TripSchedule call({
    Object? type = const $CopyWithPlaceholder(),
    Object? departAt = const $CopyWithPlaceholder(),
    Object? days = const $CopyWithPlaceholder(),
    Object? departTime = const $CopyWithPlaceholder(),
    Object? timezone = const $CopyWithPlaceholder(),
  }) {
    return TripSchedule(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as TripScheduleTypeEnum,
      departAt: departAt == const $CopyWithPlaceholder()
          ? _value.departAt
          // ignore: cast_nullable_to_non_nullable
          : departAt as DateTime?,
      days: days == const $CopyWithPlaceholder()
          ? _value.days
          // ignore: cast_nullable_to_non_nullable
          : days as List<TripScheduleDaysEnum>?,
      departTime: departTime == const $CopyWithPlaceholder()
          ? _value.departTime
          // ignore: cast_nullable_to_non_nullable
          : departTime as String?,
      timezone: timezone == const $CopyWithPlaceholder()
          ? _value.timezone
          // ignore: cast_nullable_to_non_nullable
          : timezone as String?,
    );
  }
}

extension $TripScheduleCopyWith on TripSchedule {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTripSchedule.copyWith(...)` or `instanceOfTripSchedule.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TripScheduleCWProxy get copyWith => _$TripScheduleCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TripSchedule _$TripScheduleFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TripSchedule', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type']);
      final val = TripSchedule(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$TripScheduleTypeEnumEnumMap, v),
        ),
        departAt: $checkedConvert(
          'departAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        days: $checkedConvert(
          'days',
          (v) => (v as List<dynamic>?)
              ?.map((e) => $enumDecode(_$TripScheduleDaysEnumEnumMap, e))
              .toList(),
        ),
        departTime: $checkedConvert('departTime', (v) => v as String?),
        timezone: $checkedConvert('timezone', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$TripScheduleToJson(TripSchedule instance) =>
    <String, dynamic>{
      'type': _$TripScheduleTypeEnumEnumMap[instance.type]!,
      'departAt': ?instance.departAt?.toIso8601String(),
      'days': ?instance.days
          ?.map((e) => _$TripScheduleDaysEnumEnumMap[e]!)
          .toList(),
      'departTime': ?instance.departTime,
      'timezone': ?instance.timezone,
    };

const _$TripScheduleTypeEnumEnumMap = {
  TripScheduleTypeEnum.once: 'once',
  TripScheduleTypeEnum.recurring: 'recurring',
};

const _$TripScheduleDaysEnumEnumMap = {
  TripScheduleDaysEnum.MO: 'MO',
  TripScheduleDaysEnum.TU: 'TU',
  TripScheduleDaysEnum.WE: 'WE',
  TripScheduleDaysEnum.TH: 'TH',
  TripScheduleDaysEnum.FR: 'FR',
  TripScheduleDaysEnum.SA: 'SA',
  TripScheduleDaysEnum.SU: 'SU',
};
