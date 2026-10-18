// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'timeline_bucket.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TimelineBucketCWProxy {
  TimelineBucket t(DateTime t);

  TimelineBucket total(int total);

  TimelineBucket counts(Map<String, int>? counts);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TimelineBucket(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TimelineBucket(...).copyWith(id: 12, name: "My name")
  /// ```
  TimelineBucket call({DateTime t, int total, Map<String, int>? counts});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTimelineBucket.copyWith(...)` or call `instanceOfTimelineBucket.copyWith.fieldName(value)` for a single field.
class _$TimelineBucketCWProxyImpl implements _$TimelineBucketCWProxy {
  const _$TimelineBucketCWProxyImpl(this._value);

  final TimelineBucket _value;

  @override
  TimelineBucket t(DateTime t) => call(t: t);

  @override
  TimelineBucket total(int total) => call(total: total);

  @override
  TimelineBucket counts(Map<String, int>? counts) => call(counts: counts);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TimelineBucket(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TimelineBucket(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TimelineBucket call({
    Object? t = const $CopyWithPlaceholder(),
    Object? total = const $CopyWithPlaceholder(),
    Object? counts = const $CopyWithPlaceholder(),
  }) {
    return TimelineBucket(
      t: t == const $CopyWithPlaceholder() || t == null
          ? _value.t
          // ignore: cast_nullable_to_non_nullable
          : t as DateTime,
      total: total == const $CopyWithPlaceholder() || total == null
          ? _value.total
          // ignore: cast_nullable_to_non_nullable
          : total as int,
      counts: counts == const $CopyWithPlaceholder()
          ? _value.counts
          // ignore: cast_nullable_to_non_nullable
          : counts as Map<String, int>?,
    );
  }
}

extension $TimelineBucketCopyWith on TimelineBucket {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTimelineBucket.copyWith(...)` or `instanceOfTimelineBucket.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TimelineBucketCWProxy get copyWith => _$TimelineBucketCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TimelineBucket _$TimelineBucketFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TimelineBucket', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['t', 'total']);
      final val = TimelineBucket(
        t: $checkedConvert('t', (v) => DateTime.parse(v as String)),
        total: $checkedConvert('total', (v) => (v as num).toInt()),
        counts: $checkedConvert(
          'counts',
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, (e as num).toInt()),
          ),
        ),
      );
      return val;
    });

Map<String, dynamic> _$TimelineBucketToJson(TimelineBucket instance) =>
    <String, dynamic>{
      't': instance.t.toIso8601String(),
      'total': instance.total,
      'counts': ?instance.counts,
    };
