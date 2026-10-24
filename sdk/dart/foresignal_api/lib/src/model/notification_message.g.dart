// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_message.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationMessageCWProxy {
  NotificationMessage id(int id);

  NotificationMessage event(String event);

  NotificationMessage subtype(String subtype);

  NotificationMessage title(String title);

  NotificationMessage shortTitle(String? shortTitle);

  NotificationMessage bodyMarkdown(String? bodyMarkdown);

  NotificationMessage severity(NotificationMessageSeverityEnum severity);

  NotificationMessage url(String? url);

  NotificationMessage links(List<NotificationLink>? links);

  NotificationMessage data(Map<String, Object>? data);

  NotificationMessage tags(List<String>? tags);

  NotificationMessage attachments(List<Attachment>? attachments);

  NotificationMessage updateOf(int? updateOf);

  NotificationMessage origin(String? origin);

  NotificationMessage createdAt(DateTime? createdAt);

  NotificationMessage exercise(bool? exercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationMessage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationMessage(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationMessage call({
    int id,
    String event,
    String subtype,
    String title,
    String? shortTitle,
    String? bodyMarkdown,
    NotificationMessageSeverityEnum severity,
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
/// Use as `instanceOfNotificationMessage.copyWith(...)` or call `instanceOfNotificationMessage.copyWith.fieldName(value)` for a single field.
class _$NotificationMessageCWProxyImpl implements _$NotificationMessageCWProxy {
  const _$NotificationMessageCWProxyImpl(this._value);

  final NotificationMessage _value;

  @override
  NotificationMessage id(int id) => call(id: id);

  @override
  NotificationMessage event(String event) => call(event: event);

  @override
  NotificationMessage subtype(String subtype) => call(subtype: subtype);

  @override
  NotificationMessage title(String title) => call(title: title);

  @override
  NotificationMessage shortTitle(String? shortTitle) =>
      call(shortTitle: shortTitle);

  @override
  NotificationMessage bodyMarkdown(String? bodyMarkdown) =>
      call(bodyMarkdown: bodyMarkdown);

  @override
  NotificationMessage severity(NotificationMessageSeverityEnum severity) =>
      call(severity: severity);

  @override
  NotificationMessage url(String? url) => call(url: url);

  @override
  NotificationMessage links(List<NotificationLink>? links) =>
      call(links: links);

  @override
  NotificationMessage data(Map<String, Object>? data) => call(data: data);

  @override
  NotificationMessage tags(List<String>? tags) => call(tags: tags);

  @override
  NotificationMessage attachments(List<Attachment>? attachments) =>
      call(attachments: attachments);

  @override
  NotificationMessage updateOf(int? updateOf) => call(updateOf: updateOf);

  @override
  NotificationMessage origin(String? origin) => call(origin: origin);

  @override
  NotificationMessage createdAt(DateTime? createdAt) =>
      call(createdAt: createdAt);

  @override
  NotificationMessage exercise(bool? exercise) => call(exercise: exercise);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationMessage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationMessage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationMessage call({
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
    return NotificationMessage(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as String,
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
          : severity as NotificationMessageSeverityEnum,
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

extension $NotificationMessageCopyWith on NotificationMessage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationMessage.copyWith(...)` or `instanceOfNotificationMessage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationMessageCWProxy get copyWith =>
      _$NotificationMessageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationMessage _$NotificationMessageFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NotificationMessage', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['id', 'event', 'subtype', 'title', 'severity'],
      );
      final val = NotificationMessage(
        id: $checkedConvert('id', (v) => (v as num).toInt()),
        event: $checkedConvert('event', (v) => v as String),
        subtype: $checkedConvert('subtype', (v) => v as String),
        title: $checkedConvert('title', (v) => v as String),
        shortTitle: $checkedConvert('shortTitle', (v) => v as String?),
        bodyMarkdown: $checkedConvert('bodyMarkdown', (v) => v as String?),
        severity: $checkedConvert(
          'severity',
          (v) => $enumDecode(_$NotificationMessageSeverityEnumEnumMap, v),
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
          (v) => (v as Map<String, dynamic>?)?.map(
            (k, e) => MapEntry(k, e as Object),
          ),
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

Map<String, dynamic> _$NotificationMessageToJson(
  NotificationMessage instance,
) => <String, dynamic>{
  'id': instance.id,
  'event': instance.event,
  'subtype': instance.subtype,
  'title': instance.title,
  'shortTitle': ?instance.shortTitle,
  'bodyMarkdown': ?instance.bodyMarkdown,
  'severity': _$NotificationMessageSeverityEnumEnumMap[instance.severity]!,
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

const _$NotificationMessageSeverityEnumEnumMap = {
  NotificationMessageSeverityEnum.info: 'info',
  NotificationMessageSeverityEnum.warning: 'warning',
  NotificationMessageSeverityEnum.critical: 'critical',
};
