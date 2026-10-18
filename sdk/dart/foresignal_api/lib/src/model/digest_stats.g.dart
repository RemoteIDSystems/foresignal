// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'digest_stats.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DigestStatsCWProxy {
  DigestStats fromSeq(int fromSeq);

  DigestStats toSeq(int toSeq);

  DigestStats gap(bool gap);

  DigestStats gapReason(String? gapReason);

  DigestStats counts(List<DigestCount> counts);

  DigestStats active(List<DigestActive>? active);

  DigestStats alerts(DigestAlerts? alerts);

  DigestStats notable(List<DigestStatsNotableInner>? notable);

  DigestStats health(List<DigestHealthItem>? health);

  DigestStats healthSnapshot(bool? healthSnapshot);

  DigestStats sections(Map<String, Object>? sections);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestStats(...).copyWith(id: 12, name: "My name")
  /// ```
  DigestStats call({
    int fromSeq,
    int toSeq,
    bool gap,
    String? gapReason,
    List<DigestCount> counts,
    List<DigestActive>? active,
    DigestAlerts? alerts,
    List<DigestStatsNotableInner>? notable,
    List<DigestHealthItem>? health,
    bool? healthSnapshot,
    Map<String, Object>? sections,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDigestStats.copyWith(...)` or call `instanceOfDigestStats.copyWith.fieldName(value)` for a single field.
class _$DigestStatsCWProxyImpl implements _$DigestStatsCWProxy {
  const _$DigestStatsCWProxyImpl(this._value);

  final DigestStats _value;

  @override
  DigestStats fromSeq(int fromSeq) => call(fromSeq: fromSeq);

  @override
  DigestStats toSeq(int toSeq) => call(toSeq: toSeq);

  @override
  DigestStats gap(bool gap) => call(gap: gap);

  @override
  DigestStats gapReason(String? gapReason) => call(gapReason: gapReason);

  @override
  DigestStats counts(List<DigestCount> counts) => call(counts: counts);

  @override
  DigestStats active(List<DigestActive>? active) => call(active: active);

  @override
  DigestStats alerts(DigestAlerts? alerts) => call(alerts: alerts);

  @override
  DigestStats notable(List<DigestStatsNotableInner>? notable) =>
      call(notable: notable);

  @override
  DigestStats health(List<DigestHealthItem>? health) => call(health: health);

  @override
  DigestStats healthSnapshot(bool? healthSnapshot) =>
      call(healthSnapshot: healthSnapshot);

  @override
  DigestStats sections(Map<String, Object>? sections) =>
      call(sections: sections);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DigestStats(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DigestStats(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DigestStats call({
    Object? fromSeq = const $CopyWithPlaceholder(),
    Object? toSeq = const $CopyWithPlaceholder(),
    Object? gap = const $CopyWithPlaceholder(),
    Object? gapReason = const $CopyWithPlaceholder(),
    Object? counts = const $CopyWithPlaceholder(),
    Object? active = const $CopyWithPlaceholder(),
    Object? alerts = const $CopyWithPlaceholder(),
    Object? notable = const $CopyWithPlaceholder(),
    Object? health = const $CopyWithPlaceholder(),
    Object? healthSnapshot = const $CopyWithPlaceholder(),
    Object? sections = const $CopyWithPlaceholder(),
  }) {
    return DigestStats(
      fromSeq: fromSeq == const $CopyWithPlaceholder() || fromSeq == null
          ? _value.fromSeq
          // ignore: cast_nullable_to_non_nullable
          : fromSeq as int,
      toSeq: toSeq == const $CopyWithPlaceholder() || toSeq == null
          ? _value.toSeq
          // ignore: cast_nullable_to_non_nullable
          : toSeq as int,
      gap: gap == const $CopyWithPlaceholder() || gap == null
          ? _value.gap
          // ignore: cast_nullable_to_non_nullable
          : gap as bool,
      gapReason: gapReason == const $CopyWithPlaceholder()
          ? _value.gapReason
          // ignore: cast_nullable_to_non_nullable
          : gapReason as String?,
      counts: counts == const $CopyWithPlaceholder() || counts == null
          ? _value.counts
          // ignore: cast_nullable_to_non_nullable
          : counts as List<DigestCount>,
      active: active == const $CopyWithPlaceholder()
          ? _value.active
          // ignore: cast_nullable_to_non_nullable
          : active as List<DigestActive>?,
      alerts: alerts == const $CopyWithPlaceholder()
          ? _value.alerts
          // ignore: cast_nullable_to_non_nullable
          : alerts as DigestAlerts?,
      notable: notable == const $CopyWithPlaceholder()
          ? _value.notable
          // ignore: cast_nullable_to_non_nullable
          : notable as List<DigestStatsNotableInner>?,
      health: health == const $CopyWithPlaceholder()
          ? _value.health
          // ignore: cast_nullable_to_non_nullable
          : health as List<DigestHealthItem>?,
      healthSnapshot: healthSnapshot == const $CopyWithPlaceholder()
          ? _value.healthSnapshot
          // ignore: cast_nullable_to_non_nullable
          : healthSnapshot as bool?,
      sections: sections == const $CopyWithPlaceholder()
          ? _value.sections
          // ignore: cast_nullable_to_non_nullable
          : sections as Map<String, Object>?,
    );
  }
}

extension $DigestStatsCopyWith on DigestStats {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDigestStats.copyWith(...)` or `instanceOfDigestStats.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DigestStatsCWProxy get copyWith => _$DigestStatsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DigestStats _$DigestStatsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DigestStats', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['fromSeq', 'toSeq', 'gap', 'counts']);
  final val = DigestStats(
    fromSeq: $checkedConvert('fromSeq', (v) => (v as num).toInt()),
    toSeq: $checkedConvert('toSeq', (v) => (v as num).toInt()),
    gap: $checkedConvert('gap', (v) => v as bool),
    gapReason: $checkedConvert('gapReason', (v) => v as String?),
    counts: $checkedConvert(
      'counts',
      (v) => (v as List<dynamic>)
          .map((e) => DigestCount.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    active: $checkedConvert(
      'active',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DigestActive.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    alerts: $checkedConvert(
      'alerts',
      (v) =>
          v == null ? null : DigestAlerts.fromJson(v as Map<String, dynamic>),
    ),
    notable: $checkedConvert(
      'notable',
      (v) => (v as List<dynamic>?)
          ?.map(
            (e) => DigestStatsNotableInner.fromJson(e as Map<String, dynamic>),
          )
          .toList(),
    ),
    health: $checkedConvert(
      'health',
      (v) => (v as List<dynamic>?)
          ?.map((e) => DigestHealthItem.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    healthSnapshot: $checkedConvert('healthSnapshot', (v) => v as bool?),
    sections: $checkedConvert(
      'sections',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$DigestStatsToJson(DigestStats instance) =>
    <String, dynamic>{
      'fromSeq': instance.fromSeq,
      'toSeq': instance.toSeq,
      'gap': instance.gap,
      'gapReason': ?instance.gapReason,
      'counts': instance.counts.map((e) => e.toJson()).toList(),
      'active': ?instance.active?.map((e) => e.toJson()).toList(),
      'alerts': ?instance.alerts?.toJson(),
      'notable': ?instance.notable?.map((e) => e.toJson()).toList(),
      'health': ?instance.health?.map((e) => e.toJson()).toList(),
      'healthSnapshot': ?instance.healthSnapshot,
      'sections': ?instance.sections,
    };
