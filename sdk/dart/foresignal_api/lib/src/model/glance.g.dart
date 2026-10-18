// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'glance.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GlanceCWProxy {
  Glance serverTime(DateTime serverTime);

  Glance status(GlanceStatusEnum status);

  Glance alerts(GlanceAlerts? alerts);

  Glance sources(GlanceSources? sources);

  Glance briefing(GlanceBriefing? briefing);

  Glance activity(GlanceActivity? activity);

  Glance lastSeq(int lastSeq);

  Glance links(Map<String, String>? links);

  Glance sections(Map<String, Object>? sections);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Glance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Glance(...).copyWith(id: 12, name: "My name")
  /// ```
  Glance call({
    DateTime serverTime,
    GlanceStatusEnum status,
    GlanceAlerts? alerts,
    GlanceSources? sources,
    GlanceBriefing? briefing,
    GlanceActivity? activity,
    int lastSeq,
    Map<String, String>? links,
    Map<String, Object>? sections,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGlance.copyWith(...)` or call `instanceOfGlance.copyWith.fieldName(value)` for a single field.
class _$GlanceCWProxyImpl implements _$GlanceCWProxy {
  const _$GlanceCWProxyImpl(this._value);

  final Glance _value;

  @override
  Glance serverTime(DateTime serverTime) => call(serverTime: serverTime);

  @override
  Glance status(GlanceStatusEnum status) => call(status: status);

  @override
  Glance alerts(GlanceAlerts? alerts) => call(alerts: alerts);

  @override
  Glance sources(GlanceSources? sources) => call(sources: sources);

  @override
  Glance briefing(GlanceBriefing? briefing) => call(briefing: briefing);

  @override
  Glance activity(GlanceActivity? activity) => call(activity: activity);

  @override
  Glance lastSeq(int lastSeq) => call(lastSeq: lastSeq);

  @override
  Glance links(Map<String, String>? links) => call(links: links);

  @override
  Glance sections(Map<String, Object>? sections) => call(sections: sections);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Glance(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Glance(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Glance call({
    Object? serverTime = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? alerts = const $CopyWithPlaceholder(),
    Object? sources = const $CopyWithPlaceholder(),
    Object? briefing = const $CopyWithPlaceholder(),
    Object? activity = const $CopyWithPlaceholder(),
    Object? lastSeq = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? sections = const $CopyWithPlaceholder(),
  }) {
    return Glance(
      serverTime:
          serverTime == const $CopyWithPlaceholder() || serverTime == null
          ? _value.serverTime
          // ignore: cast_nullable_to_non_nullable
          : serverTime as DateTime,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as GlanceStatusEnum,
      alerts: alerts == const $CopyWithPlaceholder()
          ? _value.alerts
          // ignore: cast_nullable_to_non_nullable
          : alerts as GlanceAlerts?,
      sources: sources == const $CopyWithPlaceholder()
          ? _value.sources
          // ignore: cast_nullable_to_non_nullable
          : sources as GlanceSources?,
      briefing: briefing == const $CopyWithPlaceholder()
          ? _value.briefing
          // ignore: cast_nullable_to_non_nullable
          : briefing as GlanceBriefing?,
      activity: activity == const $CopyWithPlaceholder()
          ? _value.activity
          // ignore: cast_nullable_to_non_nullable
          : activity as GlanceActivity?,
      lastSeq: lastSeq == const $CopyWithPlaceholder() || lastSeq == null
          ? _value.lastSeq
          // ignore: cast_nullable_to_non_nullable
          : lastSeq as int,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as Map<String, String>?,
      sections: sections == const $CopyWithPlaceholder()
          ? _value.sections
          // ignore: cast_nullable_to_non_nullable
          : sections as Map<String, Object>?,
    );
  }
}

extension $GlanceCopyWith on Glance {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGlance.copyWith(...)` or `instanceOfGlance.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GlanceCWProxy get copyWith => _$GlanceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Glance _$GlanceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('Glance', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['serverTime', 'status', 'lastSeq']);
  final val = Glance(
    serverTime: $checkedConvert(
      'serverTime',
      (v) => DateTime.parse(v as String),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$GlanceStatusEnumEnumMap, v),
    ),
    alerts: $checkedConvert(
      'alerts',
      (v) =>
          v == null ? null : GlanceAlerts.fromJson(v as Map<String, dynamic>),
    ),
    sources: $checkedConvert(
      'sources',
      (v) =>
          v == null ? null : GlanceSources.fromJson(v as Map<String, dynamic>),
    ),
    briefing: $checkedConvert(
      'briefing',
      (v) =>
          v == null ? null : GlanceBriefing.fromJson(v as Map<String, dynamic>),
    ),
    activity: $checkedConvert(
      'activity',
      (v) =>
          v == null ? null : GlanceActivity.fromJson(v as Map<String, dynamic>),
    ),
    lastSeq: $checkedConvert('lastSeq', (v) => (v as num).toInt()),
    links: $checkedConvert(
      'links',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
    sections: $checkedConvert(
      'sections',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$GlanceToJson(Glance instance) => <String, dynamic>{
  'serverTime': instance.serverTime.toIso8601String(),
  'status': _$GlanceStatusEnumEnumMap[instance.status]!,
  'alerts': ?instance.alerts?.toJson(),
  'sources': ?instance.sources?.toJson(),
  'briefing': ?instance.briefing?.toJson(),
  'activity': ?instance.activity?.toJson(),
  'lastSeq': instance.lastSeq,
  'links': ?instance.links,
  'sections': ?instance.sections,
};

const _$GlanceStatusEnumEnumMap = {
  GlanceStatusEnum.ok: 'ok',
  GlanceStatusEnum.degraded: 'degraded',
  GlanceStatusEnum.failing: 'failing',
};
