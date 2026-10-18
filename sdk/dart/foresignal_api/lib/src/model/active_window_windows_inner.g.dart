// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'active_window_windows_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ActiveWindowWindowsInnerCWProxy {
  ActiveWindowWindowsInner days(List<ActiveWindowWindowsInnerDaysEnum> days);

  ActiveWindowWindowsInner start(String start);

  ActiveWindowWindowsInner end(String end);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ActiveWindowWindowsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ActiveWindowWindowsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  ActiveWindowWindowsInner call({
    List<ActiveWindowWindowsInnerDaysEnum> days,
    String start,
    String end,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfActiveWindowWindowsInner.copyWith(...)` or call `instanceOfActiveWindowWindowsInner.copyWith.fieldName(value)` for a single field.
class _$ActiveWindowWindowsInnerCWProxyImpl
    implements _$ActiveWindowWindowsInnerCWProxy {
  const _$ActiveWindowWindowsInnerCWProxyImpl(this._value);

  final ActiveWindowWindowsInner _value;

  @override
  ActiveWindowWindowsInner days(List<ActiveWindowWindowsInnerDaysEnum> days) =>
      call(days: days);

  @override
  ActiveWindowWindowsInner start(String start) => call(start: start);

  @override
  ActiveWindowWindowsInner end(String end) => call(end: end);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ActiveWindowWindowsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ActiveWindowWindowsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ActiveWindowWindowsInner call({
    Object? days = const $CopyWithPlaceholder(),
    Object? start = const $CopyWithPlaceholder(),
    Object? end = const $CopyWithPlaceholder(),
  }) {
    return ActiveWindowWindowsInner(
      days: days == const $CopyWithPlaceholder() || days == null
          ? _value.days
          // ignore: cast_nullable_to_non_nullable
          : days as List<ActiveWindowWindowsInnerDaysEnum>,
      start: start == const $CopyWithPlaceholder() || start == null
          ? _value.start
          // ignore: cast_nullable_to_non_nullable
          : start as String,
      end: end == const $CopyWithPlaceholder() || end == null
          ? _value.end
          // ignore: cast_nullable_to_non_nullable
          : end as String,
    );
  }
}

extension $ActiveWindowWindowsInnerCopyWith on ActiveWindowWindowsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfActiveWindowWindowsInner.copyWith(...)` or `instanceOfActiveWindowWindowsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ActiveWindowWindowsInnerCWProxy get copyWith =>
      _$ActiveWindowWindowsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ActiveWindowWindowsInner _$ActiveWindowWindowsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ActiveWindowWindowsInner', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['days', 'start', 'end']);
  final val = ActiveWindowWindowsInner(
    days: $checkedConvert(
      'days',
      (v) => (v as List<dynamic>)
          .map((e) => $enumDecode(_$ActiveWindowWindowsInnerDaysEnumEnumMap, e))
          .toList(),
    ),
    start: $checkedConvert('start', (v) => v as String),
    end: $checkedConvert('end', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$ActiveWindowWindowsInnerToJson(
  ActiveWindowWindowsInner instance,
) => <String, dynamic>{
  'days': instance.days
      .map((e) => _$ActiveWindowWindowsInnerDaysEnumEnumMap[e]!)
      .toList(),
  'start': instance.start,
  'end': instance.end,
};

const _$ActiveWindowWindowsInnerDaysEnumEnumMap = {
  ActiveWindowWindowsInnerDaysEnum.MON: 'MON',
  ActiveWindowWindowsInnerDaysEnum.TUE: 'TUE',
  ActiveWindowWindowsInnerDaysEnum.WED: 'WED',
  ActiveWindowWindowsInnerDaysEnum.THU: 'THU',
  ActiveWindowWindowsInnerDaysEnum.FRI: 'FRI',
  ActiveWindowWindowsInnerDaysEnum.SAT: 'SAT',
  ActiveWindowWindowsInnerDaysEnum.SUN: 'SUN',
};
