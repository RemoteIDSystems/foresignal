// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'time_range.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TimeRangeCWProxy {
  TimeRange start(DateTime start);

  TimeRange end(DateTime? end);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TimeRange(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TimeRange(...).copyWith(id: 12, name: "My name")
  /// ```
  TimeRange call({DateTime start, DateTime? end});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTimeRange.copyWith(...)` or call `instanceOfTimeRange.copyWith.fieldName(value)` for a single field.
class _$TimeRangeCWProxyImpl implements _$TimeRangeCWProxy {
  const _$TimeRangeCWProxyImpl(this._value);

  final TimeRange _value;

  @override
  TimeRange start(DateTime start) => call(start: start);

  @override
  TimeRange end(DateTime? end) => call(end: end);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TimeRange(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TimeRange(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TimeRange call({
    Object? start = const $CopyWithPlaceholder(),
    Object? end = const $CopyWithPlaceholder(),
  }) {
    return TimeRange(
      start: start == const $CopyWithPlaceholder() || start == null
          ? _value.start
          // ignore: cast_nullable_to_non_nullable
          : start as DateTime,
      end: end == const $CopyWithPlaceholder()
          ? _value.end
          // ignore: cast_nullable_to_non_nullable
          : end as DateTime?,
    );
  }
}

extension $TimeRangeCopyWith on TimeRange {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTimeRange.copyWith(...)` or `instanceOfTimeRange.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TimeRangeCWProxy get copyWith => _$TimeRangeCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TimeRange _$TimeRangeFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TimeRange', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['start']);
      final val = TimeRange(
        start: $checkedConvert('start', (v) => DateTime.parse(v as String)),
        end: $checkedConvert(
          'end',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TimeRangeToJson(TimeRange instance) => <String, dynamic>{
  'start': instance.start.toIso8601String(),
  'end': ?instance.end?.toIso8601String(),
};
