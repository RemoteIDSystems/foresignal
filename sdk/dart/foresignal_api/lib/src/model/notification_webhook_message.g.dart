// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_webhook_message.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationWebhookMessageCWProxy {
  NotificationWebhookMessage id(int id);

  NotificationWebhookMessage event(NotificationWebhookMessageEventEnum event);

  NotificationWebhookMessage subtype(String subtype);

  NotificationWebhookMessage title(String title);

  NotificationWebhookMessage shortTitle(String? shortTitle);

  NotificationWebhookMessage bodyMarkdown(String? bodyMarkdown);

  NotificationWebhookMessage severity(
    NotificationWebhookMessageSeverityEnum severity,
  );

  NotificationWebhookMessage url(String? url);

  NotificationWebhookMessage links(List<NotificationLink>? links);

  NotificationWebhookMessage data(Map<String, Object>? data);

  NotificationWebhookMessage tags(List<String>? tags);

  NotificationWebhookMessage attachments(List<Attachment>? attachments);

  NotificationWebhookMessage updateOf(int? updateOf);

  NotificationWebhookMessage origin(String? origin);

  NotificationWebhookMessage createdAt(DateTime? createdAt);

  NotificationWebhookMessage exercise(bool? exercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationWebhookMessage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationWebhookMessage(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationWebhookMessage call({
    int id,
    NotificationWebhookMessageEventEnum event,
    String subtype,
    String title,
    String? shortTitle,
    String? bodyMarkdown,
    NotificationWebhookMessageSeverityEnum severity,
    String? url,
    List<NotificationLink>? links,
    Map<String, Object>? data,
    List<String>? tags,
    List<Attachment>? attachments,
    int? updateOf,
    String? origin,
    DateTime? createdAt,
    bool? exercise,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationWebhookMessage.copyWith(...)` or call `instanceOfNotificationWebhookMessage.copyWith.fieldName(value)` for a single field.
class _$NotificationWebhookMessageCWProxyImpl
    implements _$NotificationWebhookMessageCWProxy {
  const _$NotificationWebhookMessageCWProxyImpl(this._value);

  final NotificationWebhookMessage _value;

  @override
  NotificationWebhookMessage id(int id) => call(id: id);

  @override
  NotificationWebhookMessage event(NotificationWebhookMessageEventEnum event) =>
      call(event: event);

  @override
  NotificationWebhookMessage subtype(String subtype) => call(subtype: subtype);

  @override
  NotificationWebhookMessage title(String title) => call(title: title);

  @override
  NotificationWebhookMessage shortTitle(String? shortTitle) =>
      call(shortTitle: shortTitle);

  @override
  NotificationWebhookMessage bodyMarkdown(String? bodyMarkdown) =>
      call(bodyMarkdown: bodyMarkdown);

  @override
  NotificationWebhookMessage severity(
    NotificationWebhookMessageSeverityEnum severity,
  ) => call(severity: severity);

  @override
  NotificationWebhookMessage url(String? url) => call(url: url);

  @override
  NotificationWebhookMessage links(List<NotificationLink>? links) =>
      call(links: links);

  @override
  NotificationWebhookMessage data(Map<String, Object>? data) =>
      call(data: data);

  @override
  NotificationWebhookMessage tags(List<String>? tags) => call(tags: tags);

  @override
  NotificationWebhookMessage attachments(List<Attachment>? attachments) =>
      call(attachments: attachments);

  @override
  NotificationWebhookMessage updateOf(int? updateOf) =>
      call(updateOf: updateOf);

  @override
  NotificationWebhookMessage origin(String? origin) => call(origin: origin);

  @override
  NotificationWebhookMessage createdAt(DateTime? createdAt) =>
      call(createdAt: createdAt);

  @override
  NotificationWebhookMessage exercise(bool? exercise) =>
      call(exercise: exercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationWebhookMessage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationWebhookMessage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationWebhookMessage call({
    Object? id = const $CopyWithPlaceholder(),
    Object? event = const $CopyWithPlaceholder(),
    Object? subtype = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? shortTitle = const $CopyWithPlaceholder(),
    Object? bodyMarkdown = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? data = const $CopyWithPlaceholder(),
    Object? tags = const $CopyWithPlaceholder(),
    Object? attachments = const $CopyWithPlaceholder(),
    Object? updateOf = const $CopyWithPlaceholder(),
    Object? origin = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? exercise = const $CopyWithPlaceholder(),
  }) {
    return NotificationWebhookMessage(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as NotificationWebhookMessageEventEnum,
      subtype: subtype == const $CopyWithPlaceholder() || subtype == null
          ? _value.subtype
          // ignore: cast_nullable_to_non_nullable
          : subtype as String,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      shortTitle: shortTitle == const $CopyWithPlaceholder()
          ? _value.shortTitle
          // ignore: cast_nullable_to_non_nullable
          : shortTitle as String?,
      bodyMarkdown: bodyMarkdown == const $CopyWithPlaceholder()
          ? _value.bodyMarkdown
          // ignore: cast_nullable_to_non_nullable
          : bodyMarkdown as String?,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as NotificationWebhookMessageSeverityEnum,
      url: url == const $CopyWithPlaceholder()
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String?,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<NotificationLink>?,
      data: data == const $CopyWithPlaceholder()
          ? _value.data
          // ignore: cast_nullable_to_non_nullable
          : data as Map<String, Object>?,
      tags: tags == const $CopyWithPlaceholder()
          ? _value.tags
          // ignore: cast_nullable_to_non_nullable
          : tags as List<String>?,
      attachments: attachments == const $CopyWithPlaceholder()
          ? _value.attachments
          // ignore: cast_nullable_to_non_nullable
          : attachments as List<Attachment>?,
      updateOf: updateOf == const $CopyWithPlaceholder()
          ? _value.updateOf
          // ignore: cast_nullable_to_non_nullable
          : updateOf as int?,
      origin: origin == const $CopyWithPlaceholder()
          ? _value.origin
          // ignore: cast_nullable_to_non_nullable
          : origin as String?,
      createdAt: createdAt == const $CopyWithPlaceholder()
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime?,
      exercise: exercise == const $CopyWithPlaceholder()
          ? _value.exercise
          // ignore: cast_nullable_to_non_nullable
          : exercise as bool?,
    );
  }
}

extension $NotificationWebhookMessageCopyWith on NotificationWebhookMessage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationWebhookMessage.copyWith(...)` or `instanceOfNotificationWebhookMessage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationWebhookMessageCWProxy get copyWith =>
      _$NotificationWebhookMessageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationWebhookMessage _$NotificationWebhookMessageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NotificationWebhookMessage', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['id', 'event', 'subtype', 'title', 'severity'],
  );
  final val = NotificationWebhookMessage(
    id: $checkedConvert('id', (v) => (v as num).toInt()),
    event: $checkedConvert(
      'event',
      (v) => $enumDecode(_$NotificationWebhookMessageEventEnumEnumMap, v),
    ),
    subtype: $checkedConvert('subtype', (v) => v as String),
    title: $checkedConvert('title', (v) => v as String),
    shortTitle: $checkedConvert('shortTitle', (v) => v as String?),
    bodyMarkdown: $checkedConvert('bodyMarkdown', (v) => v as String?),
    severity: $checkedConvert(
      'severity',
      (v) => $enumDecode(_$NotificationWebhookMessageSeverityEnumEnumMap, v),
    ),
    url: $checkedConvert('url', (v) => v as String?),
    links: $checkedConvert(
      'links',
      (v) => (v as List<dynamic>?)
          ?.map((e) => NotificationLink.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    data: $checkedConvert(
      'data',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
    tags: $checkedConvert(
      'tags',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
    attachments: $checkedConvert(
      'attachments',
      (v) => (v as List<dynamic>?)
          ?.map((e) => Attachment.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    updateOf: $checkedConvert('updateOf', (v) => (v as num?)?.toInt()),
    origin: $checkedConvert('origin', (v) => v as String?),
    createdAt: $checkedConvert(
      'createdAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    exercise: $checkedConvert('exercise', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$NotificationWebhookMessageToJson(
  NotificationWebhookMessage instance,
) => <String, dynamic>{
  'id': instance.id,
  'event': _$NotificationWebhookMessageEventEnumEnumMap[instance.event]!,
  'subtype': instance.subtype,
  'title': instance.title,
  'shortTitle': ?instance.shortTitle,
  'bodyMarkdown': ?instance.bodyMarkdown,
  'severity':
      _$NotificationWebhookMessageSeverityEnumEnumMap[instance.severity]!,
  'url': ?instance.url,
  'links': ?instance.links?.map((e) => e.toJson()).toList(),
  'data': ?instance.data,
  'tags': ?instance.tags,
  'attachments': ?instance.attachments?.map((e) => e.toJson()).toList(),
  'updateOf': ?instance.updateOf,
  'origin': ?instance.origin,
  'createdAt': ?instance.createdAt?.toIso8601String(),
  'exercise': ?instance.exercise,
};

const _$NotificationWebhookMessageEventEnumEnumMap = {
  NotificationWebhookMessageEventEnum.alert: 'alert',
  NotificationWebhookMessageEventEnum.briefing: 'briefing',
  NotificationWebhookMessageEventEnum.system: 'system',
  NotificationWebhookMessageEventEnum.chroniclePeriodProposed:
      'chronicle.proposed',
  NotificationWebhookMessageEventEnum.chroniclePeriodCapWarning:
      'chronicle.cap_warning',
  NotificationWebhookMessageEventEnum.chroniclePeriodGap: 'chronicle.gap',
  NotificationWebhookMessageEventEnum.socialPeriodDivergence:
      'social.divergence',
  NotificationWebhookMessageEventEnum.socialPeriodAttentionGap:
      'social.attention_gap',
  NotificationWebhookMessageEventEnum.analystPeriodNote: 'analyst.note',
  NotificationWebhookMessageEventEnum.situationPeriodActivated:
      'situation.activated',
  NotificationWebhookMessageEventEnum.situationPeriodEscalated:
      'situation.escalated',
  NotificationWebhookMessageEventEnum.findingPeriodSignificant:
      'finding.significant',
  NotificationWebhookMessageEventEnum.forecastPeriodResolved:
      'forecast.resolved',
  NotificationWebhookMessageEventEnum.forecastPeriodResolutionNeeded:
      'forecast.resolution_needed',
  NotificationWebhookMessageEventEnum.hypothesisPeriodBandChanged:
      'hypothesis.band_changed',
  NotificationWebhookMessageEventEnum.indicatorPeriodObserved:
      'indicator.observed',
  NotificationWebhookMessageEventEnum.assumptionPeriodBroken:
      'assumption.broken',
  NotificationWebhookMessageEventEnum.assessmentPeriodPublished:
      'assessment.published',
  NotificationWebhookMessageEventEnum.mention: 'mention',
  NotificationWebhookMessageEventEnum.share: 'share',
  NotificationWebhookMessageEventEnum.reportPeriodReceived: 'report.received',
  NotificationWebhookMessageEventEnum.packagePeriodReceived: 'package.received',
  NotificationWebhookMessageEventEnum.chroniclePeriodAccessPeriodRequested:
      'chronicle.access.requested',
  NotificationWebhookMessageEventEnum.savedSearchPeriodMatches:
      'saved-search.matches',
  NotificationWebhookMessageEventEnum.test: 'test',
  NotificationWebhookMessageEventEnum.personal: 'personal',
};

const _$NotificationWebhookMessageSeverityEnumEnumMap = {
  NotificationWebhookMessageSeverityEnum.info: 'info',
  NotificationWebhookMessageSeverityEnum.warning: 'warning',
  NotificationWebhookMessageSeverityEnum.critical: 'critical',
};
