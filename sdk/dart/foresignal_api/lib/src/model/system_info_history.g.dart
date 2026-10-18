// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'system_info_history.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SystemInfoHistoryCWProxy {
  SystemInfoHistory tiers(List<SystemInfoHistoryTiersInner>? tiers);

  SystemInfoHistory versionsPerMinute(num? versionsPerMinute);

  SystemInfoHistory projectedBytes(int? projectedBytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoHistory(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoHistory(...).copyWith(id: 12, name: "My name")
  /// ```
  SystemInfoHistory call({
    List<SystemInfoHistoryTiersInner>? tiers,
    num? versionsPerMinute,
    int? projectedBytes,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSystemInfoHistory.copyWith(...)` or call `instanceOfSystemInfoHistory.copyWith.fieldName(value)` for a single field.
class _$SystemInfoHistoryCWProxyImpl implements _$SystemInfoHistoryCWProxy {
  const _$SystemInfoHistoryCWProxyImpl(this._value);

  final SystemInfoHistory _value;

  @override
  SystemInfoHistory tiers(List<SystemInfoHistoryTiersInner>? tiers) =>
      call(tiers: tiers);

  @override
  SystemInfoHistory versionsPerMinute(num? versionsPerMinute) =>
      call(versionsPerMinute: versionsPerMinute);

  @override
  SystemInfoHistory projectedBytes(int? projectedBytes) =>
      call(projectedBytes: projectedBytes);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SystemInfoHistory(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SystemInfoHistory(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SystemInfoHistory call({
    Object? tiers = const $CopyWithPlaceholder(),
    Object? versionsPerMinute = const $CopyWithPlaceholder(),
    Object? projectedBytes = const $CopyWithPlaceholder(),
  }) {
    return SystemInfoHistory(
      tiers: tiers == const $CopyWithPlaceholder()
          ? _value.tiers
          // ignore: cast_nullable_to_non_nullable
          : tiers as List<SystemInfoHistoryTiersInner>?,
      versionsPerMinute: versionsPerMinute == const $CopyWithPlaceholder()
          ? _value.versionsPerMinute
          // ignore: cast_nullable_to_non_nullable
          : versionsPerMinute as num?,
      projectedBytes: projectedBytes == const $CopyWithPlaceholder()
          ? _value.projectedBytes
          // ignore: cast_nullable_to_non_nullable
          : projectedBytes as int?,
    );
  }
}

extension $SystemInfoHistoryCopyWith on SystemInfoHistory {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSystemInfoHistory.copyWith(...)` or `instanceOfSystemInfoHistory.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SystemInfoHistoryCWProxy get copyWith =>
      _$SystemInfoHistoryCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SystemInfoHistory _$SystemInfoHistoryFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SystemInfoHistory', json, ($checkedConvert) {
  final val = SystemInfoHistory(
    tiers: $checkedConvert(
      'tiers',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) =>
                SystemInfoHistoryTiersInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    versionsPerMinute: $checkedConvert('versionsPerMinute', (v) => v as num?),
    projectedBytes: $checkedConvert(
      'projectedBytes',
      (v) => (v as num?)?.toInt(),
    ),
  );
  return val;
});

Map<String, dynamic> _$SystemInfoHistoryToJson(SystemInfoHistory instance) =>
    <String, dynamic>{
      'tiers': ?instance.tiers?.map((e) => e.toJson()).toList(),
      'versionsPerMinute': ?instance.versionsPerMinute,
      'projectedBytes': ?instance.projectedBytes,
    };
