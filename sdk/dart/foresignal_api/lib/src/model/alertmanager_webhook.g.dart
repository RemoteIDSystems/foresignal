// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'alertmanager_webhook.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AlertmanagerWebhookCWProxy {
  AlertmanagerWebhook version(AlertmanagerWebhookVersionEnum version);

  AlertmanagerWebhook groupKey(String? groupKey);

  AlertmanagerWebhook truncatedAlerts(int? truncatedAlerts);

  AlertmanagerWebhook status(AlertmanagerWebhookStatusEnum? status);

  AlertmanagerWebhook receiver(String? receiver);

  AlertmanagerWebhook groupLabels(Map<String, String>? groupLabels);

  AlertmanagerWebhook commonLabels(Map<String, String>? commonLabels);

  AlertmanagerWebhook commonAnnotations(Map<String, String>? commonAnnotations);

  AlertmanagerWebhook externalURL(String? externalURL);

  AlertmanagerWebhook alerts(List<AlertmanagerWebhookAlertsInner> alerts);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertmanagerWebhook(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertmanagerWebhook(...).copyWith(id: 12, name: "My name")
  /// ```
  AlertmanagerWebhook call({
    AlertmanagerWebhookVersionEnum version,
    String? groupKey,
    int? truncatedAlerts,
    AlertmanagerWebhookStatusEnum? status,
    String? receiver,
    Map<String, String>? groupLabels,
    Map<String, String>? commonLabels,
    Map<String, String>? commonAnnotations,
    String? externalURL,
    List<AlertmanagerWebhookAlertsInner> alerts,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAlertmanagerWebhook.copyWith(...)` or call `instanceOfAlertmanagerWebhook.copyWith.fieldName(value)` for a single field.
class _$AlertmanagerWebhookCWProxyImpl implements _$AlertmanagerWebhookCWProxy {
  const _$AlertmanagerWebhookCWProxyImpl(this._value);

  final AlertmanagerWebhook _value;

  @override
  AlertmanagerWebhook version(AlertmanagerWebhookVersionEnum version) =>
      call(version: version);

  @override
  AlertmanagerWebhook groupKey(String? groupKey) => call(groupKey: groupKey);

  @override
  AlertmanagerWebhook truncatedAlerts(int? truncatedAlerts) =>
      call(truncatedAlerts: truncatedAlerts);

  @override
  AlertmanagerWebhook status(AlertmanagerWebhookStatusEnum? status) =>
      call(status: status);

  @override
  AlertmanagerWebhook receiver(String? receiver) => call(receiver: receiver);

  @override
  AlertmanagerWebhook groupLabels(Map<String, String>? groupLabels) =>
      call(groupLabels: groupLabels);

  @override
  AlertmanagerWebhook commonLabels(Map<String, String>? commonLabels) =>
      call(commonLabels: commonLabels);

  @override
  AlertmanagerWebhook commonAnnotations(
    Map<String, String>? commonAnnotations,
  ) => call(commonAnnotations: commonAnnotations);

  @override
  AlertmanagerWebhook externalURL(String? externalURL) =>
      call(externalURL: externalURL);

  @override
  AlertmanagerWebhook alerts(List<AlertmanagerWebhookAlertsInner> alerts) =>
      call(alerts: alerts);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertmanagerWebhook(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertmanagerWebhook(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AlertmanagerWebhook call({
    Object? version = const $CopyWithPlaceholder(),
    Object? groupKey = const $CopyWithPlaceholder(),
    Object? truncatedAlerts = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? receiver = const $CopyWithPlaceholder(),
    Object? groupLabels = const $CopyWithPlaceholder(),
    Object? commonLabels = const $CopyWithPlaceholder(),
    Object? commonAnnotations = const $CopyWithPlaceholder(),
    Object? externalURL = const $CopyWithPlaceholder(),
    Object? alerts = const $CopyWithPlaceholder(),
  }) {
    return AlertmanagerWebhook(
      version: version == const $CopyWithPlaceholder() || version == null
          ? _value.version
          // ignore: cast_nullable_to_non_nullable
          : version as AlertmanagerWebhookVersionEnum,
      groupKey: groupKey == const $CopyWithPlaceholder()
          ? _value.groupKey
          // ignore: cast_nullable_to_non_nullable
          : groupKey as String?,
      truncatedAlerts: truncatedAlerts == const $CopyWithPlaceholder()
          ? _value.truncatedAlerts
          // ignore: cast_nullable_to_non_nullable
          : truncatedAlerts as int?,
      status: status == const $CopyWithPlaceholder()
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as AlertmanagerWebhookStatusEnum?,
      receiver: receiver == const $CopyWithPlaceholder()
          ? _value.receiver
          // ignore: cast_nullable_to_non_nullable
          : receiver as String?,
      groupLabels: groupLabels == const $CopyWithPlaceholder()
          ? _value.groupLabels
          // ignore: cast_nullable_to_non_nullable
          : groupLabels as Map<String, String>?,
      commonLabels: commonLabels == const $CopyWithPlaceholder()
          ? _value.commonLabels
          // ignore: cast_nullable_to_non_nullable
          : commonLabels as Map<String, String>?,
      commonAnnotations: commonAnnotations == const $CopyWithPlaceholder()
          ? _value.commonAnnotations
          // ignore: cast_nullable_to_non_nullable
          : commonAnnotations as Map<String, String>?,
      externalURL: externalURL == const $CopyWithPlaceholder()
          ? _value.externalURL
          // ignore: cast_nullable_to_non_nullable
          : externalURL as String?,
      alerts: alerts == const $CopyWithPlaceholder() || alerts == null
          ? _value.alerts
          // ignore: cast_nullable_to_non_nullable
          : alerts as List<AlertmanagerWebhookAlertsInner>,
    );
  }
}

extension $AlertmanagerWebhookCopyWith on AlertmanagerWebhook {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAlertmanagerWebhook.copyWith(...)` or `instanceOfAlertmanagerWebhook.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AlertmanagerWebhookCWProxy get copyWith =>
      _$AlertmanagerWebhookCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AlertmanagerWebhook _$AlertmanagerWebhookFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AlertmanagerWebhook', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['version', 'alerts']);
  final val = AlertmanagerWebhook(
    version: $checkedConvert(
      'version',
      (v) => $enumDecode(_$AlertmanagerWebhookVersionEnumEnumMap, v),
    ),
    groupKey: $checkedConvert('groupKey', (v) => v as String?),
    truncatedAlerts: $checkedConvert(
      'truncatedAlerts',
      (v) => (v as num?)?.toInt(),
    ),
    status: $checkedConvert(
      'status',
      (v) => $enumDecodeNullable(_$AlertmanagerWebhookStatusEnumEnumMap, v),
    ),
    receiver: $checkedConvert('receiver', (v) => v as String?),
    groupLabels: $checkedConvert(
      'groupLabels',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
    commonLabels: $checkedConvert(
      'commonLabels',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
    commonAnnotations: $checkedConvert(
      'commonAnnotations',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
    externalURL: $checkedConvert('externalURL', (v) => v as String?),
    alerts: $checkedConvert(
      'alerts',
      (v) => (v as List<dynamic>)
          .map(
            (e) => AlertmanagerWebhookAlertsInner.fromJson(
              e as Map<String, dynamic>,
            ),
          )
          .toList(),
    ),
  );
  return val;
});

Map<String, dynamic> _$AlertmanagerWebhookToJson(
  AlertmanagerWebhook instance,
) => <String, dynamic>{
  'version': _$AlertmanagerWebhookVersionEnumEnumMap[instance.version]!,
  'groupKey': ?instance.groupKey,
  'truncatedAlerts': ?instance.truncatedAlerts,
  'status': ?_$AlertmanagerWebhookStatusEnumEnumMap[instance.status],
  'receiver': ?instance.receiver,
  'groupLabels': ?instance.groupLabels,
  'commonLabels': ?instance.commonLabels,
  'commonAnnotations': ?instance.commonAnnotations,
  'externalURL': ?instance.externalURL,
  'alerts': instance.alerts.map((e) => e.toJson()).toList(),
};

const _$AlertmanagerWebhookVersionEnumEnumMap = {
  AlertmanagerWebhookVersionEnum.n4: '4',
};

const _$AlertmanagerWebhookStatusEnumEnumMap = {
  AlertmanagerWebhookStatusEnum.firing: 'firing',
  AlertmanagerWebhookStatusEnum.resolved: 'resolved',
};
