// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'timeline_alerts_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$TimelineAlertsInnerCWProxy {
  TimelineAlertsInner t(DateTime t);

  TimelineAlertsInner info(int? info);

  TimelineAlertsInner warning(int? warning);

  TimelineAlertsInner critical(int? critical);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TimelineAlertsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TimelineAlertsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  TimelineAlertsInner call({
    DateTime t,
    int? info,
    int? warning,
    int? critical,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfTimelineAlertsInner.copyWith(...)` or call `instanceOfTimelineAlertsInner.copyWith.fieldName(value)` for a single field.
class _$TimelineAlertsInnerCWProxyImpl implements _$TimelineAlertsInnerCWProxy {
  const _$TimelineAlertsInnerCWProxyImpl(this._value);

  final TimelineAlertsInner _value;

  @override
  TimelineAlertsInner t(DateTime t) => call(t: t);

  @override
  TimelineAlertsInner info(int? info) => call(info: info);

  @override
  TimelineAlertsInner warning(int? warning) => call(warning: warning);

  @override
  TimelineAlertsInner critical(int? critical) => call(critical: critical);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `TimelineAlertsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// TimelineAlertsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  TimelineAlertsInner call({
    Object? t = const $CopyWithPlaceholder(),
    Object? info = const $CopyWithPlaceholder(),
    Object? warning = const $CopyWithPlaceholder(),
    Object? critical = const $CopyWithPlaceholder(),
  }) {
    return TimelineAlertsInner(
      t: t == const $CopyWithPlaceholder() || t == null
          ? _value.t
          // ignore: cast_nullable_to_non_nullable
          : t as DateTime,
      info: info == const $CopyWithPlaceholder()
          ? _value.info
          // ignore: cast_nullable_to_non_nullable
          : info as int?,
      warning: warning == const $CopyWithPlaceholder()
          ? _value.warning
          // ignore: cast_nullable_to_non_nullable
          : warning as int?,
      critical: critical == const $CopyWithPlaceholder()
          ? _value.critical
          // ignore: cast_nullable_to_non_nullable
          : critical as int?,
    );
  }
}

extension $TimelineAlertsInnerCopyWith on TimelineAlertsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfTimelineAlertsInner.copyWith(...)` or `instanceOfTimelineAlertsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$TimelineAlertsInnerCWProxy get copyWith =>
      _$TimelineAlertsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

TimelineAlertsInner _$TimelineAlertsInnerFromJson(Map<String, dynamic> json) =>
    $checkedCreate('TimelineAlertsInner', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['t']);
      final val = TimelineAlertsInner(
        t: $checkedConvert('t', (v) => DateTime.parse(v as String)),
        info: $checkedConvert('info', (v) => (v as num?)?.toInt()),
        warning: $checkedConvert('warning', (v) => (v as num?)?.toInt()),
        critical: $checkedConvert('critical', (v) => (v as num?)?.toInt()),
      );
      return val;
    });

Map<String, dynamic> _$TimelineAlertsInnerToJson(
  TimelineAlertsInner instance,
) => <String, dynamic>{
  't': instance.t.toIso8601String(),
  'info': ?instance.info,
  'warning': ?instance.warning,
  'critical': ?instance.critical,
};
