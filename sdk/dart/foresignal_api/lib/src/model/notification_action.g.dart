// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'notification_action.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NotificationActionCWProxy {
  NotificationAction action(NotificationActionActionEnum action);

  NotificationAction label(String? label);

  NotificationAction method(NotificationActionMethodEnum? method);

  NotificationAction url(String url);

  NotificationAction confirmUrl(String? confirmUrl);

  NotificationAction params(NotificationActionParams? params);

  NotificationAction expiresAt(DateTime expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationAction(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationAction(...).copyWith(id: 12, name: "My name")
  /// ```
  NotificationAction call({
    NotificationActionActionEnum action,
    String? label,
    NotificationActionMethodEnum? method,
    String url,
    String? confirmUrl,
    NotificationActionParams? params,
    DateTime expiresAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNotificationAction.copyWith(...)` or call `instanceOfNotificationAction.copyWith.fieldName(value)` for a single field.
class _$NotificationActionCWProxyImpl implements _$NotificationActionCWProxy {
  const _$NotificationActionCWProxyImpl(this._value);

  final NotificationAction _value;

  @override
  NotificationAction action(NotificationActionActionEnum action) =>
      call(action: action);

  @override
  NotificationAction label(String? label) => call(label: label);

  @override
  NotificationAction method(NotificationActionMethodEnum? method) =>
      call(method: method);

  @override
  NotificationAction url(String url) => call(url: url);

  @override
  NotificationAction confirmUrl(String? confirmUrl) =>
      call(confirmUrl: confirmUrl);

  @override
  NotificationAction params(NotificationActionParams? params) =>
      call(params: params);

  @override
  NotificationAction expiresAt(DateTime expiresAt) =>
      call(expiresAt: expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NotificationAction(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NotificationAction(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NotificationAction call({
    Object? action = const $CopyWithPlaceholder(),
    Object? label = const $CopyWithPlaceholder(),
    Object? method = const $CopyWithPlaceholder(),
    Object? url = const $CopyWithPlaceholder(),
    Object? confirmUrl = const $CopyWithPlaceholder(),
    Object? params = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
  }) {
    return NotificationAction(
      action: action == const $CopyWithPlaceholder() || action == null
          ? _value.action
          // ignore: cast_nullable_to_non_nullable
          : action as NotificationActionActionEnum,
      label: label == const $CopyWithPlaceholder()
          ? _value.label
          // ignore: cast_nullable_to_non_nullable
          : label as String?,
      method: method == const $CopyWithPlaceholder()
          ? _value.method
          // ignore: cast_nullable_to_non_nullable
          : method as NotificationActionMethodEnum?,
      url: url == const $CopyWithPlaceholder() || url == null
          ? _value.url
          // ignore: cast_nullable_to_non_nullable
          : url as String,
      confirmUrl: confirmUrl == const $CopyWithPlaceholder()
          ? _value.confirmUrl
          // ignore: cast_nullable_to_non_nullable
          : confirmUrl as String?,
      params: params == const $CopyWithPlaceholder()
          ? _value.params
          // ignore: cast_nullable_to_non_nullable
          : params as NotificationActionParams?,
      expiresAt: expiresAt == const $CopyWithPlaceholder() || expiresAt == null
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime,
    );
  }
}

extension $NotificationActionCopyWith on NotificationAction {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNotificationAction.copyWith(...)` or `instanceOfNotificationAction.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NotificationActionCWProxy get copyWith =>
      _$NotificationActionCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NotificationAction _$NotificationActionFromJson(Map<String, dynamic> json) =>
    $checkedCreate('NotificationAction', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['action', 'url', 'expiresAt']);
      final val = NotificationAction(
        action: $checkedConvert(
          'action',
          (v) => $enumDecode(_$NotificationActionActionEnumEnumMap, v),
        ),
        label: $checkedConvert('label', (v) => v as String?),
        method: $checkedConvert(
          'method',
          (v) => $enumDecodeNullable(_$NotificationActionMethodEnumEnumMap, v),
        ),
        url: $checkedConvert('url', (v) => v as String),
        confirmUrl: $checkedConvert('confirmUrl', (v) => v as String?),
        params: $checkedConvert(
          'params',
          (v) => v == null
              ? null
              : NotificationActionParams.fromJson(v as Map<String, dynamic>),
        ),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$NotificationActionToJson(NotificationAction instance) =>
    <String, dynamic>{
      'action': _$NotificationActionActionEnumEnumMap[instance.action]!,
      'label': ?instance.label,
      'method': ?_$NotificationActionMethodEnumEnumMap[instance.method],
      'url': instance.url,
      'confirmUrl': ?instance.confirmUrl,
      'params': ?instance.params?.toJson(),
      'expiresAt': instance.expiresAt.toIso8601String(),
    };

const _$NotificationActionActionEnumEnumMap = {
  NotificationActionActionEnum.ack: 'ack',
  NotificationActionActionEnum.snooze: 'snooze',
  NotificationActionActionEnum.mute: 'mute',
  NotificationActionActionEnum.feedbackColonUseful: 'feedback:useful',
  NotificationActionActionEnum.feedbackColonNoise: 'feedback:noise',
};

const _$NotificationActionMethodEnumEnumMap = {
  NotificationActionMethodEnum.POST: 'POST',
};
