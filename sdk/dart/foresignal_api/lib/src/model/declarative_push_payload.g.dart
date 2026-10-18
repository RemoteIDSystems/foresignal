// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'declarative_push_payload.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DeclarativePushPayloadCWProxy {
  DeclarativePushPayload webPush(DeclarativePushPayloadWebPushEnum webPush);

  DeclarativePushPayload notification(
    DeclarativePushPayloadNotification notification,
  );

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeclarativePushPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeclarativePushPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  DeclarativePushPayload call({
    DeclarativePushPayloadWebPushEnum webPush,
    DeclarativePushPayloadNotification notification,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDeclarativePushPayload.copyWith(...)` or call `instanceOfDeclarativePushPayload.copyWith.fieldName(value)` for a single field.
class _$DeclarativePushPayloadCWProxyImpl
    implements _$DeclarativePushPayloadCWProxy {
  const _$DeclarativePushPayloadCWProxyImpl(this._value);

  final DeclarativePushPayload _value;

  @override
  DeclarativePushPayload webPush(DeclarativePushPayloadWebPushEnum webPush) =>
      call(webPush: webPush);

  @override
  DeclarativePushPayload notification(
    DeclarativePushPayloadNotification notification,
  ) => call(notification: notification);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeclarativePushPayload(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeclarativePushPayload(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DeclarativePushPayload call({
    Object? webPush = const $CopyWithPlaceholder(),
    Object? notification = const $CopyWithPlaceholder(),
  }) {
    return DeclarativePushPayload(
      webPush: webPush == const $CopyWithPlaceholder() || webPush == null
          ? _value.webPush
          // ignore: cast_nullable_to_non_nullable
          : webPush as DeclarativePushPayloadWebPushEnum,
      notification:
          notification == const $CopyWithPlaceholder() || notification == null
          ? _value.notification
          // ignore: cast_nullable_to_non_nullable
          : notification as DeclarativePushPayloadNotification,
    );
  }
}

extension $DeclarativePushPayloadCopyWith on DeclarativePushPayload {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDeclarativePushPayload.copyWith(...)` or `instanceOfDeclarativePushPayload.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DeclarativePushPayloadCWProxy get copyWith =>
      _$DeclarativePushPayloadCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DeclarativePushPayload _$DeclarativePushPayloadFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('DeclarativePushPayload', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['web_push', 'notification']);
  final val = DeclarativePushPayload(
    webPush: $checkedConvert(
      'web_push',
      (v) => $enumDecode(_$DeclarativePushPayloadWebPushEnumEnumMap, v),
    ),
    notification: $checkedConvert(
      'notification',
      (v) => DeclarativePushPayloadNotification.fromJson(
        v as Map<String, dynamic>,
      ),
    ),
  );
  return val;
}, fieldKeyMap: const {'webPush': 'web_push'});

Map<String, dynamic> _$DeclarativePushPayloadToJson(
  DeclarativePushPayload instance,
) => <String, dynamic>{
  'web_push': _$DeclarativePushPayloadWebPushEnumEnumMap[instance.webPush]!,
  'notification': instance.notification.toJson(),
};

const _$DeclarativePushPayloadWebPushEnumEnumMap = {
  DeclarativePushPayloadWebPushEnum.number8030: 8030,
};
