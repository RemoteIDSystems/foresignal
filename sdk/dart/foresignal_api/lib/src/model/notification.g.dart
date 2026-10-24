// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationCWProxy {
  Notification id(int id);

  Notification event(String event);

  Notification subtype(String? subtype);

  Notification severity(NotificationSeverityEnum severity);

  Notification title(String title);

  Notification body(String? body);

  Notification alertId(int? alertId);

  Notification briefingRunId(String? briefingRunId);

  Notification ruleId(String? ruleId);

  Notification createdAt(DateTime createdAt);

  Notification read(bool read);

  Notification alertState(NotificationAlertStateEnum? alertState);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Notification(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Notification(...).copyWith(id: 12, name: "My name")
  /// ```
  Notification call({
    int id,
    String event,
    String? subtype,
    NotificationSeverityEnum severity,
    String title,
    String? body,
    int? alertId,
    String? briefingRunId,
    String? ruleId,
    DateTime createdAt,
    bool read,
    NotificationAlertStateEnum? alertState,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotification.copyWith(...)` or call `instanceOfNotification.copyWith.fieldName(value)` for a single field.
class _$NotificationCWProxyImpl implements _$NotificationCWProxy {
  const _$NotificationCWProxyImpl(this._value);

  final Notification _value;

  @override
  Notification id(int id) => call(id: id);

  @override
  Notification event(String event) => call(event: event);

  @override
  Notification subtype(String? subtype) => call(subtype: subtype);

  @override
  Notification severity(NotificationSeverityEnum severity) =>
      call(severity: severity);

  @override
  Notification title(String title) => call(title: title);

  @override
  Notification body(String? body) => call(body: body);

  @override
  Notification alertId(int? alertId) => call(alertId: alertId);

  @override
  Notification briefingRunId(String? briefingRunId) =>
      call(briefingRunId: briefingRunId);

  @override
  Notification ruleId(String? ruleId) => call(ruleId: ruleId);

  @override
  Notification createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  Notification read(bool read) => call(read: read);

  @override
  Notification alertState(NotificationAlertStateEnum? alertState) =>
      call(alertState: alertState);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `Notification(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// Notification(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  Notification call({
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
  }) {
    return Notification(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      event: event == const $CopyWithPlaceholder() || event == null
          ? _value.event
          // ignore: cast_nullable_to_non_nullable
          : event as String,
      subtype: subtype == const $CopyWithPlaceholder()
          ? _value.subtype
          // ignore: cast_nullable_to_non_nullable
          : subtype as String?,
      severity: severity == const $CopyWithPlaceholder() || severity == null
          ? _value.severity
          // ignore: cast_nullable_to_non_nullable
          : severity as NotificationSeverityEnum,
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
          : alertState as NotificationAlertStateEnum?,
    );
  }
}

extension $NotificationCopyWith on Notification {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotification.copyWith(...)` or `instanceOfNotification.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationCWProxy get copyWith => _$NotificationCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

Notification _$NotificationFromJson(Map<String, dynamic> json) =>
    $checkedCreate('Notification', json, ($checkedConvert) {
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
      final val = Notification(
        id: $checkedConvert('id', (v) => (v as num).toInt()),
        event: $checkedConvert('event', (v) => v as String),
        subtype: $checkedConvert('subtype', (v) => v as String?),
        severity: $checkedConvert(
          'severity',
          (v) => $enumDecode(_$NotificationSeverityEnumEnumMap, v),
        ),
        title: $checkedConvert('title', (v) => v as String),
        body: $checkedConvert('body', (v) => v as String?),
        alertId: $checkedConvert('alertId', (v) => (v as num?)?.toInt()),
        briefingRunId: $checkedConvert('briefingRunId', (v) => v as String?),
        ruleId: $checkedConvert('ruleId', (v) => v as String?),
        createdAt: $checkedConvert(
          'createdAt',
          (v) => DateTime.parse(v as String),
        ),
        read: $checkedConvert('read', (v) => v as bool),
        alertState: $checkedConvert(
          'alertState',
          (v) => $enumDecodeNullable(_$NotificationAlertStateEnumEnumMap, v),
        ),
      );
      return val;
    });

Map<String, dynamic> _$NotificationToJson(Notification instance) =>
    <String, dynamic>{
      'id': instance.id,
      'event': instance.event,
      'subtype': ?instance.subtype,
      'severity': _$NotificationSeverityEnumEnumMap[instance.severity]!,
      'title': instance.title,
      'body': ?instance.body,
      'alertId': ?instance.alertId,
      'briefingRunId': ?instance.briefingRunId,
      'ruleId': ?instance.ruleId,
      'createdAt': instance.createdAt.toIso8601String(),
      'read': instance.read,
      'alertState': ?_$NotificationAlertStateEnumEnumMap[instance.alertState],
    };

const _$NotificationSeverityEnumEnumMap = {
  NotificationSeverityEnum.info: 'info',
  NotificationSeverityEnum.warning: 'warning',
  NotificationSeverityEnum.critical: 'critical',
};

const _$NotificationAlertStateEnumEnumMap = {
  NotificationAlertStateEnum.firing: 'firing',
  NotificationAlertStateEnum.acknowledged: 'acknowledged',
  NotificationAlertStateEnum.resolved: 'resolved',
};
