// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'digest.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DigestCWProxy {
  Digest fromSeq(int fromSeq);

  Digest toSeq(int toSeq);

  Digest gap(bool gap);

  Digest gapReason(String? gapReason);

  Digest counts(List<DigestCount> counts);

  Digest active(List<DigestActive>? active);

  Digest alerts(DigestAlerts? alerts);

  Digest notable(List<DigestStatsNotableInner>? notable);

  Digest health(List<DigestHealthItem>? health);

  Digest healthSnapshot(bool? healthSnapshot);

  Digest sections(Map<String, Object>? sections);

  Digest fromTime(DateTime fromTime);

  Digest toTime(DateTime toTime);

  Digest next(DigestAllOfNext next);

  Digest weatherAtPlaces(List<PlaceWeatherDigest>? weatherAtPlaces);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Digest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Digest(...).copyWith(id: 12, name: "My name")
  /// ```
  Digest call({
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
    DateTime fromTime,
    DateTime toTime,
    DigestAllOfNext next,
    List<PlaceWeatherDigest>? weatherAtPlaces,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDigest.copyWith(...)` or call `instanceOfDigest.copyWith.fieldName(value)` for a single field.
class _$DigestCWProxyImpl implements _$DigestCWProxy {
  const _$DigestCWProxyImpl(this._value);

  final Digest _value;

  @override
  Digest fromSeq(int fromSeq) => call(fromSeq: fromSeq);

  @override
  Digest toSeq(int toSeq) => call(toSeq: toSeq);

  @override
  Digest gap(bool gap) => call(gap: gap);

  @override
  Digest gapReason(String? gapReason) => call(gapReason: gapReason);

  @override
  Digest counts(List<DigestCount> counts) => call(counts: counts);

  @override
  Digest active(List<DigestActive>? active) => call(active: active);

  @override
  Digest alerts(DigestAlerts? alerts) => call(alerts: alerts);

  @override
  Digest notable(List<DigestStatsNotableInner>? notable) =>
      call(notable: notable);

  @override
  Digest health(List<DigestHealthItem>? health) => call(health: health);

  @override
  Digest healthSnapshot(bool? healthSnapshot) =>
      call(healthSnapshot: healthSnapshot);

  @override
  Digest sections(Map<String, Object>? sections) => call(sections: sections);

  @override
  Digest fromTime(DateTime fromTime) => call(fromTime: fromTime);

  @override
  Digest toTime(DateTime toTime) => call(toTime: toTime);

  @override
  Digest next(DigestAllOfNext next) => call(next: next);

  @override
  Digest weatherAtPlaces(List<PlaceWeatherDigest>? weatherAtPlaces) =>
      call(weatherAtPlaces: weatherAtPlaces);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Digest(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Digest(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Digest call({
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
    Object? fromTime = const $CopyWithPlaceholder(),
    Object? toTime = const $CopyWithPlaceholder(),
    Object? next = const $CopyWithPlaceholder(),
    Object? weatherAtPlaces = const $CopyWithPlaceholder(),
  }) {
    return Digest(
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
      fromTime: fromTime == const $CopyWithPlaceholder() || fromTime == null
          ? _value.fromTime
          // ignore: cast_nullable_to_non_nullable
          : fromTime as DateTime,
      toTime: toTime == const $CopyWithPlaceholder() || toTime == null
          ? _value.toTime
          // ignore: cast_nullable_to_non_nullable
          : toTime as DateTime,
      next: next == const $CopyWithPlaceholder() || next == null
          ? _value.next
          // ignore: cast_nullable_to_non_nullable
          : next as DigestAllOfNext,
      weatherAtPlaces: weatherAtPlaces == const $CopyWithPlaceholder()
          ? _value.weatherAtPlaces
          // ignore: cast_nullable_to_non_nullable
          : weatherAtPlaces as List<PlaceWeatherDigest>?,
    );
  }
}

extension $DigestCopyWith on Digest {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDigest.copyWith(...)` or `instanceOfDigest.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DigestCWProxy get copyWith => _$DigestCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Digest _$DigestFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Digest', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'fromSeq',
      'toSeq',
      'gap',
      'counts',
      'fromTime',
      'toTime',
      'next',
    ],
  );
  final val = Digest(
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
    fromTime: $checkedConvert('fromTime', (v) => DateTime.parse(v as String)),
    toTime: $checkedConvert('toTime', (v) => DateTime.parse(v as String)),
    next: $checkedConvert(
      'next',
      (v) => DigestAllOfNext.fromJson(v as Map<String, dynamic>),
    ),
    weatherAtPlaces: $checkedConvert(
      'weatherAtPlaces',
      (v) => (v as List<dynamic>?)
          ?.map((e) => PlaceWeatherDigest.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$DigestToJson(Digest instance) => <String, dynamic>{
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
  'fromTime': instance.fromTime.toIso8601String(),
  'toTime': instance.toTime.toIso8601String(),
  'next': instance.next.toJson(),
  'weatherAtPlaces': ?instance.weatherAtPlaces?.map((e) => e.toJson()).toList(),
};
