// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'alertmanager_webhook_alerts_inner.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AlertmanagerWebhookAlertsInnerCWProxy {
  AlertmanagerWebhookAlertsInner status(
    AlertmanagerWebhookAlertsInnerStatusEnum status,
  );

  AlertmanagerWebhookAlertsInner labels(Map<String, String> labels);

  AlertmanagerWebhookAlertsInner annotations(Map<String, String>? annotations);

  AlertmanagerWebhookAlertsInner startsAt(DateTime startsAt);

  AlertmanagerWebhookAlertsInner endsAt(DateTime? endsAt);

  AlertmanagerWebhookAlertsInner generatorURL(String? generatorURL);

  AlertmanagerWebhookAlertsInner fingerprint(String fingerprint);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertmanagerWebhookAlertsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertmanagerWebhookAlertsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  AlertmanagerWebhookAlertsInner call({
    AlertmanagerWebhookAlertsInnerStatusEnum status,
    Map<String, String> labels,
    Map<String, String>? annotations,
    DateTime startsAt,
    DateTime? endsAt,
    String? generatorURL,
    String fingerprint,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAlertmanagerWebhookAlertsInner.copyWith(...)` or call `instanceOfAlertmanagerWebhookAlertsInner.copyWith.fieldName(value)` for a single field.
class _$AlertmanagerWebhookAlertsInnerCWProxyImpl
    implements _$AlertmanagerWebhookAlertsInnerCWProxy {
  const _$AlertmanagerWebhookAlertsInnerCWProxyImpl(this._value);

  final AlertmanagerWebhookAlertsInner _value;

  @override
  AlertmanagerWebhookAlertsInner status(
    AlertmanagerWebhookAlertsInnerStatusEnum status,
  ) => call(status: status);

  @override
  AlertmanagerWebhookAlertsInner labels(Map<String, String> labels) =>
      call(labels: labels);

  @override
  AlertmanagerWebhookAlertsInner annotations(
    Map<String, String>? annotations,
  ) => call(annotations: annotations);

  @override
  AlertmanagerWebhookAlertsInner startsAt(DateTime startsAt) =>
      call(startsAt: startsAt);

  @override
  AlertmanagerWebhookAlertsInner endsAt(DateTime? endsAt) =>
      call(endsAt: endsAt);

  @override
  AlertmanagerWebhookAlertsInner generatorURL(String? generatorURL) =>
      call(generatorURL: generatorURL);

  @override
  AlertmanagerWebhookAlertsInner fingerprint(String fingerprint) =>
      call(fingerprint: fingerprint);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AlertmanagerWebhookAlertsInner(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AlertmanagerWebhookAlertsInner(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AlertmanagerWebhookAlertsInner call({
    Object? status = const $CopyWithPlaceholder(),
    Object? labels = const $CopyWithPlaceholder(),
    Object? annotations = const $CopyWithPlaceholder(),
    Object? startsAt = const $CopyWithPlaceholder(),
    Object? endsAt = const $CopyWithPlaceholder(),
    Object? generatorURL = const $CopyWithPlaceholder(),
    Object? fingerprint = const $CopyWithPlaceholder(),
  }) {
    return AlertmanagerWebhookAlertsInner(
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as AlertmanagerWebhookAlertsInnerStatusEnum,
      labels: labels == const $CopyWithPlaceholder() || labels == null
          ? _value.labels
          // ignore: cast_nullable_to_non_nullable
          : labels as Map<String, String>,
      annotations: annotations == const $CopyWithPlaceholder()
          ? _value.annotations
          // ignore: cast_nullable_to_non_nullable
          : annotations as Map<String, String>?,
      startsAt: startsAt == const $CopyWithPlaceholder() || startsAt == null
          ? _value.startsAt
          // ignore: cast_nullable_to_non_nullable
          : startsAt as DateTime,
      endsAt: endsAt == const $CopyWithPlaceholder()
          ? _value.endsAt
          // ignore: cast_nullable_to_non_nullable
          : endsAt as DateTime?,
      generatorURL: generatorURL == const $CopyWithPlaceholder()
          ? _value.generatorURL
          // ignore: cast_nullable_to_non_nullable
          : generatorURL as String?,
      fingerprint:
          fingerprint == const $CopyWithPlaceholder() || fingerprint == null
          ? _value.fingerprint
          // ignore: cast_nullable_to_non_nullable
          : fingerprint as String,
    );
  }
}

extension $AlertmanagerWebhookAlertsInnerCopyWith
    on AlertmanagerWebhookAlertsInner {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAlertmanagerWebhookAlertsInner.copyWith(...)` or `instanceOfAlertmanagerWebhookAlertsInner.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AlertmanagerWebhookAlertsInnerCWProxy get copyWith =>
      _$AlertmanagerWebhookAlertsInnerCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AlertmanagerWebhookAlertsInner _$AlertmanagerWebhookAlertsInnerFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('AlertmanagerWebhookAlertsInner', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const ['status', 'labels', 'startsAt', 'fingerprint'],
  );
  final val = AlertmanagerWebhookAlertsInner(
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$AlertmanagerWebhookAlertsInnerStatusEnumEnumMap, v),
    ),
    labels: $checkedConvert(
      'labels',
      (v) => Map<String, String>.from(v as Map),
    ),
    annotations: $checkedConvert(
      'annotations',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as String)),
    ),
    startsAt: $checkedConvert('startsAt', (v) => DateTime.parse(v as String)),
    endsAt: $checkedConvert(
      'endsAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    generatorURL: $checkedConvert('generatorURL', (v) => v as String?),
    fingerprint: $checkedConvert('fingerprint', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$AlertmanagerWebhookAlertsInnerToJson(
  AlertmanagerWebhookAlertsInner instance,
) => <String, dynamic>{
  'status': _$AlertmanagerWebhookAlertsInnerStatusEnumEnumMap[instance.status]!,
  'labels': instance.labels,
  'annotations': ?instance.annotations,
  'startsAt': instance.startsAt.toIso8601String(),
  'endsAt': ?instance.endsAt?.toIso8601String(),
  'generatorURL': ?instance.generatorURL,
  'fingerprint': instance.fingerprint,
};

const _$AlertmanagerWebhookAlertsInnerStatusEnumEnumMap = {
  AlertmanagerWebhookAlertsInnerStatusEnum.firing: 'firing',
  AlertmanagerWebhookAlertsInnerStatusEnum.resolved: 'resolved',
};
