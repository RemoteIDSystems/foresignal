// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'stream_notifications200_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$StreamNotifications200ResponseCWProxy {
  StreamNotifications200Response id(int id);

  StreamNotifications200Response event(String event);

  StreamNotifications200Response subtype(String? subtype);

  StreamNotifications200Response severity(
    StreamNotifications200ResponseSeverityEnum severity,
  );

  StreamNotifications200Response title(String title);

  StreamNotifications200Response body(String? body);

  StreamNotifications200Response alertId(int? alertId);

  StreamNotifications200Response briefingRunId(String? briefingRunId);

  StreamNotifications200Response ruleId(String? ruleId);

  StreamNotifications200Response createdAt(DateTime createdAt);

  StreamNotifications200Response read(bool read);

  StreamNotifications200Response alertState(
    StreamNotifications200ResponseAlertStateEnum? alertState,
  );

  StreamNotifications200Response unread(int unread);

  StreamNotifications200Response unreadCritical(int unreadCritical);

  StreamNotifications200Response latestId(int? latestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StreamNotifications200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StreamNotifications200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  StreamNotifications200Response call({
    int id,
    String event,
    String? subtype,
    StreamNotifications200ResponseSeverityEnum severity,
    String title,
    String? body,
    int? alertId,
    String? briefingRunId,
    String? ruleId,
    DateTime createdAt,
    bool read,
    StreamNotifications200ResponseAlertStateEnum? alertState,
    int unread,
    int unreadCritical,
    int? latestId,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfStreamNotifications200Response.copyWith(...)` or call `instanceOfStreamNotifications200Response.copyWith.fieldName(value)` for a single field.
class _$StreamNotifications200ResponseCWProxyImpl
    implements _$StreamNotifications200ResponseCWProxy {
  const _$StreamNotifications200ResponseCWProxyImpl(this._value);

  final StreamNotifications200Response _value;

  @override
  StreamNotifications200Response id(int id) => call(id: id);

  @override
  StreamNotifications200Response event(String event) => call(event: event);

  @override
  StreamNotifications200Response subtype(String? subtype) =>
      call(subtype: subtype);

  @override
  StreamNotifications200Response severity(
    StreamNotifications200ResponseSeverityEnum severity,
  ) => call(severity: severity);

  @override
  StreamNotifications200Response title(String title) => call(title: title);

  @override
  StreamNotifications200Response body(String? body) => call(body: body);

  @override
  StreamNotifications200Response alertId(int? alertId) =>
      call(alertId: alertId);

  @override
  StreamNotifications200Response briefingRunId(String? briefingRunId) =>
      call(briefingRunId: briefingRunId);

  @override
  StreamNotifications200Response ruleId(String? ruleId) => call(ruleId: ruleId);

  @override
  StreamNotifications200Response createdAt(DateTime createdAt) =>
      call(createdAt: createdAt);

  @override
  StreamNotifications200Response read(bool read) => call(read: read);

  @override
  StreamNotifications200Response alertState(
    StreamNotifications200ResponseAlertStateEnum? alertState,
  ) => call(alertState: alertState);

  @override
  StreamNotifications200Response unread(int unread) => call(unread: unread);

  @override
  StreamNotifications200Response unreadCritical(int unreadCritical) =>
      call(unreadCritical: unreadCritical);

  @override
  StreamNotifications200Response latestId(int? latestId) =>
      call(latestId: latestId);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `StreamNotifications200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// StreamNotifications200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  StreamNotifications200Response call({
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
    Object? unread = const $CopyWithPlaceholder(),
    Object? unreadCritical = const $CopyWithPlaceholder(),
    Object? latestId = const $CopyWithPlaceholder(),
  }) {
    return StreamNotifications200Response(
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
          : severity as StreamNotifications200ResponseSeverityEnum,
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
          : alertState as StreamNotifications200ResponseAlertStateEnum?,
      unread: unread == const $CopyWithPlaceholder() || unread == null
          ? _value.unread
          // ignore: cast_nullable_to_non_nullable
          : unread as int,
      unreadCritical:
          unreadCritical == const $CopyWithPlaceholder() ||
              unreadCritical == null
          ? _value.unreadCritical
          // ignore: cast_nullable_to_non_nullable
          : unreadCritical as int,
      latestId: latestId == const $CopyWithPlaceholder()
          ? _value.latestId
          // ignore: cast_nullable_to_non_nullable
          : latestId as int?,
    );
  }
}

extension $StreamNotifications200ResponseCopyWith
    on StreamNotifications200Response {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfStreamNotifications200Response.copyWith(...)` or `instanceOfStreamNotifications200Response.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$StreamNotifications200ResponseCWProxy get copyWith =>
      _$StreamNotifications200ResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

StreamNotifications200Response _$StreamNotifications200ResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('StreamNotifications200Response', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'event',
      'severity',
      'title',
      'createdAt',
      'read',
      'unread',
      'unreadCritical',
    ],
  );
  final val = StreamNotifications200Response(
    id: $checkedConvert('id', (v) => (v as num).toInt()),
    event: $checkedConvert('event', (v) => v as String),
    subtype: $checkedConvert('subtype', (v) => v as String?),
    severity: $checkedConvert(
      'severity',
      (v) =>
          $enumDecode(_$StreamNotifications200ResponseSeverityEnumEnumMap, v),
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
      (v) => $enumDecodeNullable(
        _$StreamNotifications200ResponseAlertStateEnumEnumMap,
        v,
      ),
    ),
    unread: $checkedConvert('unread', (v) => (v as num).toInt()),
    unreadCritical: $checkedConvert(
      'unreadCritical',
      (v) => (v as num).toInt(),
    ),
    latestId: $checkedConvert('latestId', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$StreamNotifications200ResponseToJson(
  StreamNotifications200Response instance,
) => <String, dynamic>{
  'id': instance.id,
  'event': instance.event,
  'subtype': ?instance.subtype,
  'severity':
      _$StreamNotifications200ResponseSeverityEnumEnumMap[instance.severity]!,
  'title': instance.title,
  'body': ?instance.body,
  'alertId': ?instance.alertId,
  'briefingRunId': ?instance.briefingRunId,
  'ruleId': ?instance.ruleId,
  'createdAt': instance.createdAt.toIso8601String(),
  'read': instance.read,
  'alertState':
      ?_$StreamNotifications200ResponseAlertStateEnumEnumMap[instance
          .alertState],
  'unread': instance.unread,
  'unreadCritical': instance.unreadCritical,
  'latestId': ?instance.latestId,
};

const _$StreamNotifications200ResponseSeverityEnumEnumMap = {
  StreamNotifications200ResponseSeverityEnum.info: 'info',
  StreamNotifications200ResponseSeverityEnum.warning: 'warning',
  StreamNotifications200ResponseSeverityEnum.critical: 'critical',
};

const _$StreamNotifications200ResponseAlertStateEnumEnumMap = {
  StreamNotifications200ResponseAlertStateEnum.firing: 'firing',
  StreamNotifications200ResponseAlertStateEnum.acknowledged: 'acknowledged',
  StreamNotifications200ResponseAlertStateEnum.resolved: 'resolved',
};
