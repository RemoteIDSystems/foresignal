// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'analyst_run_input_counts.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AnalystRunInputCountsCWProxy {
  AnalystRunInputCounts events(int? events);

  AnalystRunInputCounts anomalies(int? anomalies);

  AnalystRunInputCounts alerts(int? alerts);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystRunInputCounts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystRunInputCounts(...).copyWith(id: 12, name: "My name")
  /// ```
  AnalystRunInputCounts call({int? events, int? anomalies, int? alerts});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAnalystRunInputCounts.copyWith(...)` or call `instanceOfAnalystRunInputCounts.copyWith.fieldName(value)` for a single field.
class _$AnalystRunInputCountsCWProxyImpl
    implements _$AnalystRunInputCountsCWProxy {
  const _$AnalystRunInputCountsCWProxyImpl(this._value);

  final AnalystRunInputCounts _value;

  @override
  AnalystRunInputCounts events(int? events) => call(events: events);

  @override
  AnalystRunInputCounts anomalies(int? anomalies) => call(anomalies: anomalies);

  @override
  AnalystRunInputCounts alerts(int? alerts) => call(alerts: alerts);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AnalystRunInputCounts(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AnalystRunInputCounts(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AnalystRunInputCounts call({
    Object? events = const $CopyWithPlaceholder(),
    Object? anomalies = const $CopyWithPlaceholder(),
    Object? alerts = const $CopyWithPlaceholder(),
  }) {
    return AnalystRunInputCounts(
      events: events == const $CopyWithPlaceholder()
          ? _value.events
          // ignore: cast_nullable_to_non_nullable
          : events as int?,
      anomalies: anomalies == const $CopyWithPlaceholder()
          ? _value.anomalies
          // ignore: cast_nullable_to_non_nullable
          : anomalies as int?,
      alerts: alerts == const $CopyWithPlaceholder()
          ? _value.alerts
          // ignore: cast_nullable_to_non_nullable
          : alerts as int?,
    );
  }
}

extension $AnalystRunInputCountsCopyWith on AnalystRunInputCounts {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAnalystRunInputCounts.copyWith(...)` or `instanceOfAnalystRunInputCounts.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AnalystRunInputCountsCWProxy get copyWith =>
      _$AnalystRunInputCountsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AnalystRunInputCounts _$AnalystRunInputCountsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AnalystRunInputCounts', json, ($checkedConvert) {
  final val = AnalystRunInputCounts(
    events: $checkedConvert('events', (v) => (v as num?)?.toInt()),
    anomalies: $checkedConvert('anomalies', (v) => (v as num?)?.toInt()),
    alerts: $checkedConvert('alerts', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$AnalystRunInputCountsToJson(
  AnalystRunInputCounts instance,
) => <String, dynamic>{
  'events': ?instance.events,
  'anomalies': ?instance.anomalies,
  'alerts': ?instance.alerts,
};
