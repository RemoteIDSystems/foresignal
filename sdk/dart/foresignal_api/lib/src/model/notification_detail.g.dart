// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_detail.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationDetailCWProxy {
  NotificationDetail id(int id);

  NotificationDetail event(NotificationDetailEventEnum event);

  NotificationDetail subtype(String? subtype);

  NotificationDetail severity(NotificationDetailSeverityEnum severity);

  NotificationDetail title(String title);

  NotificationDetail body(String? body);

  NotificationDetail alertId(int? alertId);

  NotificationDetail briefingRunId(String? briefingRunId);

  NotificationDetail ruleId(String? ruleId);

  NotificationDetail createdAt(DateTime createdAt);

  NotificationDetail read(bool read);

  NotificationDetail alertState(NotificationDetailAlertStateEnum? alertState);

  NotificationDetail links(List<NotificationLink>? links);

  NotificationDetail actions(List<NotificationAction>? actions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationDetail(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationDetail(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationDetail call({
    int id,
    NotificationDetailEventEnum event,
    String? subtype,
    NotificationDetailSeverityEnum severity,
    String title,
    String? body,
    int? alertId,
    String? briefingRunId,
    String? ruleId,
    DateTime createdAt,
    bool read,
    NotificationDetailAlertStateEnum? alertState,
    List<NotificationLink>? links,
    List<NotificationAction>? actions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationDetail.copyWith(...)` or call `instanceOfNotificationDetail.copyWith.fieldName(value)` for a single field.
class _$NotificationDetailCWProxyImpl implements _$NotificationDetailCWProxy {
  const _$NotificationDetailCWProxyImpl(this._value);

  final NotificationDetail _value;

  @override
  NotificationDetail id(int id) => call(id: id);

  @override
  NotificationDetail event(NotificationDetailEventEnum event) =>
      call(event: event);

  @override
  NotificationDetail subtype(String? subtype) => call(subtype: subtype);

  @override
  NotificationDetail severity(NotificationDetailSeverityEnum severity) =>
      call(severity: severity);

  @override
  NotificationDetail title(String title) => call(title: title);

  @override
  NotificationDetail body(String? body) => call(body: body);

  @override
  NotificationDetail alertId(int? alertId) => call(alertId: alertId);

  @override
  NotificationDetail briefingRunId(String? briefingRunId) =>
      call(briefingRunId: briefingRunId);

  @override
  NotificationDetail ruleId(String? ruleId) => call(ruleId: ruleId);

  @override
  NotificationDetail createdAt(DateTime createdAt) =>
      call(createdAt: createdAt);

  @override
  NotificationDetail read(bool read) => call(read: read);

  @override
  NotificationDetail alertState(NotificationDetailAlertStateEnum? alertState) =>
      call(alertState: alertState);

  @override
  NotificationDetail links(List<NotificationLink>? links) => call(links: links);

  @override
  NotificationDetail actions(List<NotificationAction>? actions) =>
      call(actions: actions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationDetail(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationDetail(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationDetail call({
    Object? id = const $CopyWithPlaceholder(),
    Object? event = const $CopyWithPlaceholder(),
    Object? subtype = const $CopyWithPlaceholder(),
    Object? severity = const $CopyWithPlaceholder(),
    Object? title = const $CopyWithPlaceholder(),
    Object? body = const $CopyWithPlaceholder(),
    Object? alertId = const $CopyWithPlaceholder(),
    Object? briefingRunId = const $CopyWithPlaceholder(),
    Object? ruleId = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? read = const $CopyWithPlaceholder(),
    Object? alertState = const $CopyWithPlaceholder(),
    Object? links = const $CopyWithPlaceholder(),
    Object? actions = const $CopyWithPlaceholder(),
  }) {
    return NotificationDetail(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as NotificationDetailEventEnum,
      subtype: subtype == const $CopyWithPlaceholder()
          ? _value.subtype
          // ignore: cast_nullable_to_non_nullable
          : subtype as String?,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as NotificationDetailSeverityEnum,
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      body: body == const $CopyWithPlaceholder()
          ? _value.body
          // ignore: cast_nullable_to_non_nullable
          : body as String?,
      alertId: alertId == const $CopyWithPlaceholder()
          ? _value.alertId
          // ignore: cast_nullable_to_non_nullable
          : alertId as int?,
      briefingRunId: briefingRunId == const $CopyWithPlaceholder()
          ? _value.briefingRunId
          // ignore: cast_nullable_to_non_nullable
          : briefingRunId as String?,
      ruleId: ruleId == const $CopyWithPlaceholder()
          ? _value.ruleId
          // ignore: cast_nullable_to_non_nullable
          : ruleId as String?,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      read: read == const $CopyWithPlaceholder() || read == null
          ? _value.read
          // ignore: cast_nullable_to_non_nullable
          : read as bool,
      alertState: alertState == const $CopyWithPlaceholder()
          ? _value.alertState
          // ignore: cast_nullable_to_non_nullable
          : alertState as NotificationDetailAlertStateEnum?,
      links: links == const $CopyWithPlaceholder()
          ? _value.links
          // ignore: cast_nullable_to_non_nullable
          : links as List<NotificationLink>?,
      actions: actions == const $CopyWithPlaceholder()
          ? _value.actions
          // ignore: cast_nullable_to_non_nullable
          : actions as List<NotificationAction>?,
    );
  }
}

extension $NotificationDetailCopyWith on NotificationDetail {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationDetail.copyWith(...)` or `instanceOfNotificationDetail.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationDetailCWProxy get copyWith =>
      _$NotificationDetailCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationDetail _$NotificationDetailFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NotificationDetail', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'event',
      'severity',
      'title',
      'createdAt',
      'read',
    ],
  );
  final val = NotificationDetail(
    id: $checkedConvert('id', (v) => (v as num).toInt()),
    event: $checkedConvert(
      'event',
      (v) => $enumDecode(_$NotificationDetailEventEnumEnumMap, v),
    ),
    subtype: $checkedConvert('subtype', (v) => v as String?),
    severity: $checkedConvert(
      'severity',
      (v) => $enumDecode(_$NotificationDetailSeverityEnumEnumMap, v),
    ),
    title: $checkedConvert('title', (v) => v as String),
    body: $checkedConvert('body', (v) => v as String?),
    alertId: $checkedConvert('alertId', (v) => (v as num?)?.toInt()),
    briefingRunId: $checkedConvert('briefingRunId', (v) => v as String?),
    ruleId: $checkedConvert('ruleId', (v) => v as String?),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    read: $checkedConvert('read', (v) => v as bool),
    alertState: $checkedConvert(
      'alertState',
      (v) => $enumDecodeNullable(_$NotificationDetailAlertStateEnumEnumMap, v),
    ),
    links: $checkedConvert(
      'links',
      (v) => (v as List<dynamic>?)
          ?.map((e) => NotificationLink.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
    actions: $checkedConvert(
      'actions',
      (v) => (v as List<dynamic>?)
          ?.map((e) => NotificationAction.fromJson(e as Map<String, dynamic>))
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$NotificationDetailToJson(
  NotificationDetail instance,
) => <String, dynamic>{
  'id': instance.id,
  'event': _$NotificationDetailEventEnumEnumMap[instance.event]!,
  'subtype': ?instance.subtype,
  'severity': _$NotificationDetailSeverityEnumEnumMap[instance.severity]!,
  'title': instance.title,
  'body': ?instance.body,
  'alertId': ?instance.alertId,
  'briefingRunId': ?instance.briefingRunId,
  'ruleId': ?instance.ruleId,
  'createdAt': instance.createdAt.toIso8601String(),
  'read': instance.read,
  'alertState': ?_$NotificationDetailAlertStateEnumEnumMap[instance.alertState],
  'links': ?instance.links?.map((e) => e.toJson()).toList(),
  'actions': ?instance.actions?.map((e) => e.toJson()).toList(),
};

const _$NotificationDetailEventEnumEnumMap = {
  NotificationDetailEventEnum.alert: 'alert',
  NotificationDetailEventEnum.briefing: 'briefing',
  NotificationDetailEventEnum.system: 'system',
};

const _$NotificationDetailSeverityEnumEnumMap = {
  NotificationDetailSeverityEnum.info: 'info',
  NotificationDetailSeverityEnum.warning: 'warning',
  NotificationDetailSeverityEnum.critical: 'critical',
};

const _$NotificationDetailAlertStateEnumEnumMap = {
  NotificationDetailAlertStateEnum.firing: 'firing',
  NotificationDetailAlertStateEnum.acknowledged: 'acknowledged',
  NotificationDetailAlertStateEnum.resolved: 'resolved',
};
