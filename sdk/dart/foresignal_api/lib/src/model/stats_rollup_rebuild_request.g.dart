// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stats_rollup_rebuild_request.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StatsRollupRebuildRequestCWProxy {
  StatsRollupRebuildRequest from(DateTime from);

  StatsRollupRebuildRequest to(DateTime to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsRollupRebuildRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsRollupRebuildRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  StatsRollupRebuildRequest call({DateTime from, DateTime to});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStatsRollupRebuildRequest.copyWith(...)` or call `instanceOfStatsRollupRebuildRequest.copyWith.fieldName(value)` for a single field.
class _$StatsRollupRebuildRequestCWProxyImpl
    implements _$StatsRollupRebuildRequestCWProxy {
  const _$StatsRollupRebuildRequestCWProxyImpl(this._value);

  final StatsRollupRebuildRequest _value;

  @override
  StatsRollupRebuildRequest from(DateTime from) => call(from: from);

  @override
  StatsRollupRebuildRequest to(DateTime to) => call(to: to);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StatsRollupRebuildRequest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StatsRollupRebuildRequest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StatsRollupRebuildRequest call({
    Object? from = const $CopyWithPlaceholder(),
    Object? to = const $CopyWithPlaceholder(),
  }) {
    return StatsRollupRebuildRequest(
      from: from == const $CopyWithPlaceholder() || from == null
          ? _value.from
          // ignore: cast_nullable_to_non_nullable
          : from as DateTime,
      to: to == const $CopyWithPlaceholder() || to == null
          ? _value.to
          // ignore: cast_nullable_to_non_nullable
          : to as DateTime,
    );
  }
}

extension $StatsRollupRebuildRequestCopyWith on StatsRollupRebuildRequest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStatsRollupRebuildRequest.copyWith(...)` or `instanceOfStatsRollupRebuildRequest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StatsRollupRebuildRequestCWProxy get copyWith =>
      _$StatsRollupRebuildRequestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StatsRollupRebuildRequest _$StatsRollupRebuildRequestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StatsRollupRebuildRequest', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['from', 'to']);
  final val = StatsRollupRebuildRequest(
    from: $checkedConvert('from', (v) => DateTime.parse(v as String)),
    to: $checkedConvert('to', (v) => DateTime.parse(v as String)),
  );
  return val;
});

Map<String, dynamic> _$StatsRollupRebuildRequestToJson(
  StatsRollupRebuildRequest instance,
) => <String, dynamic>{
  'from': instance.from.toIso8601String(),
  'to': instance.to.toIso8601String(),
};
