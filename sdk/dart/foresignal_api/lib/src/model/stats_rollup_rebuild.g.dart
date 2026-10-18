// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stats_rollup_rebuild.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StatsRollupRebuildCWProxy {
  StatsRollupRebuild from(DateTime from);

  StatsRollupRebuild to(DateTime to);

  StatsRollupRebuild hours(int? hours);

  StatsRollupRebuild days(int? days);

  StatsRollupRebuild status(StatsRollupRebuildStatusEnum status);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsRollupRebuild(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsRollupRebuild(...).copyWith(id: 12, name: "My name")
  /// ```
  StatsRollupRebuild call({
    DateTime from,
    DateTime to,
    int? hours,
    int? days,
    StatsRollupRebuildStatusEnum status,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStatsRollupRebuild.copyWith(...)` or call `instanceOfStatsRollupRebuild.copyWith.fieldName(value)` for a single field.
class _$StatsRollupRebuildCWProxyImpl implements _$StatsRollupRebuildCWProxy {
  const _$StatsRollupRebuildCWProxyImpl(this._value);

  final StatsRollupRebuild _value;

  @override
  StatsRollupRebuild from(DateTime from) => call(from: from);

  @override
  StatsRollupRebuild to(DateTime to) => call(to: to);

  @override
  StatsRollupRebuild hours(int? hours) => call(hours: hours);

  @override
  StatsRollupRebuild days(int? days) => call(days: days);

  @override
  StatsRollupRebuild status(StatsRollupRebuildStatusEnum status) =>
      call(status: status);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsRollupRebuild(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsRollupRebuild(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StatsRollupRebuild call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
    Object? hours = const $CopyWithPlaceholder(),
    Object? days = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
  }) {
    return StatsRollupRebuild(
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime,
      hours: hours == const $CopyWithPlaceholder()
          ? _value.hours
          // ignore: cast_nullable_to_non_nullable
          : hours as int?,
      days: days == const $CopyWithPlaceholder()
          ? _value.days
          // ignore: cast_nullable_to_non_nullable
          : days as int?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as StatsRollupRebuildStatusEnum,
    );
  }
}

extension $StatsRollupRebuildCopyWith on StatsRollupRebuild {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStatsRollupRebuild.copyWith(...)` or `instanceOfStatsRollupRebuild.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StatsRollupRebuildCWProxy get copyWith =>
      _$StatsRollupRebuildCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StatsRollupRebuild _$StatsRollupRebuildFromJson(Map<String, dynamic> json) =>
    $checkedCreate('StatsRollupRebuild', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['from', 'to', 'status']);
      final val = StatsRollupRebuild(
        from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
        to: $checkedConvert('to', (v) => DateTime.parse(v as String)),
        hours: $checkedConvert('hours', (v) => (v as num?)?.toInt()),
        days: $checkedConvert('days', (v) => (v as num?)?.toInt()),
        status: $checkedConvert(
          'status',
          (v) => $enumDecode(_$StatsRollupRebuildStatusEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$StatsRollupRebuildToJson(StatsRollupRebuild instance) =>
    <String, dynamic>{
      'from': instance.from.toIso8601String(),
      'to': instance.to.toIso8601String(),
      'hours': ?instance.hours,
      'days': ?instance.days,
      'status': _$StatsRollupRebuildStatusEnumEnumMap[instance.status]!,
    };

const _$StatsRollupRebuildStatusEnumEnumMap = {
  StatsRollupRebuildStatusEnum.queued: 'queued',
  StatsRollupRebuildStatusEnum.running: 'running',
};
