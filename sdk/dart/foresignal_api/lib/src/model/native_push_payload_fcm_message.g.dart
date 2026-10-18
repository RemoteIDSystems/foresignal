// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'native_push_payload_fcm_message.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NativePushPayloadFcmMessageCWProxy {
  NativePushPayloadFcmMessage token(String token);

  NativePushPayloadFcmMessage data(NativePushPayloadFcmMessageData data);

  NativePushPayloadFcmMessage android(
    NativePushPayloadFcmMessageAndroid? android,
  );

  NativePushPayloadFcmMessage apns(Map<String, Object>? apns);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadFcmMessage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadFcmMessage(...).copyWith(id: 12, name: "My name")
  /// ```
  NativePushPayloadFcmMessage call({
    String token,
    NativePushPayloadFcmMessageData data,
    NativePushPayloadFcmMessageAndroid? android,
    Map<String, Object>? apns,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNativePushPayloadFcmMessage.copyWith(...)` or call `instanceOfNativePushPayloadFcmMessage.copyWith.fieldName(value)` for a single field.
class _$NativePushPayloadFcmMessageCWProxyImpl
    implements _$NativePushPayloadFcmMessageCWProxy {
  const _$NativePushPayloadFcmMessageCWProxyImpl(this._value);

  final NativePushPayloadFcmMessage _value;

  @override
  NativePushPayloadFcmMessage token(String token) => call(token: token);

  @override
  NativePushPayloadFcmMessage data(NativePushPayloadFcmMessageData data) =>
      call(data: data);

  @override
  NativePushPayloadFcmMessage android(
    NativePushPayloadFcmMessageAndroid? android,
  ) => call(android: android);

  @override
  NativePushPayloadFcmMessage apns(Map<String, Object>? apns) =>
      call(apns: apns);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadFcmMessage(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadFcmMessage(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NativePushPayloadFcmMessage call({
    Object? token = const $CopyWithPlaceholder(),
    Object? data = const $CopyWithPlaceholder(),
    Object? android = const $CopyWithPlaceholder(),
    Object? apns = const $CopyWithPlaceholder(),
  }) {
    return NativePushPayloadFcmMessage(
      token: token == const $CopyWithPlaceholder() || token == null
          ? _value.token
          // ignore: cast_nullable_to_non_nullable
          : token as String,
      data: data == const $CopyWithPlaceholder() || data == null
          ? _value.data
          // ignore: cast_nullable_to_non_nullable
          : data as NativePushPayloadFcmMessageData,
      android: android == const $CopyWithPlaceholder()
          ? _value.android
          // ignore: cast_nullable_to_non_nullable
          : android as NativePushPayloadFcmMessageAndroid?,
      apns: apns == const $CopyWithPlaceholder()
          ? _value.apns
          // ignore: cast_nullable_to_non_nullable
          : apns as Map<String, Object>?,
    );
  }
}

extension $NativePushPayloadFcmMessageCopyWith on NativePushPayloadFcmMessage {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNativePushPayloadFcmMessage.copyWith(...)` or `instanceOfNativePushPayloadFcmMessage.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NativePushPayloadFcmMessageCWProxy get copyWith =>
      _$NativePushPayloadFcmMessageCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NativePushPayloadFcmMessage _$NativePushPayloadFcmMessageFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NativePushPayloadFcmMessage', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['token', 'data']);
  final val = NativePushPayloadFcmMessage(
    token: $checkedConvert('token', (v) => v as String),
    data: $checkedConvert(
      'data',
      (v) =>
          NativePushPayloadFcmMessageData.fromJson(v as Map<String, dynamic>),
    ),
    android: $checkedConvert(
      'android',
      (v) => v == null
          ? null
          : NativePushPayloadFcmMessageAndroid.fromJson(
              v as Map<String, dynamic>,
            ),
    ),
    apns: $checkedConvert(
      'apns',
      (v) =>
          (v as Map<String, dynamic>?)?.map((k, e) => MapEntry(k, e as Object)),
    ),
  );
  return val;
});

Map<String, dynamic> _$NativePushPayloadFcmMessageToJson(
  NativePushPayloadFcmMessage instance,
) => <String, dynamic>{
  'token': instance.token,
  'data': instance.data.toJson(),
  'android': ?instance.android?.toJson(),
  'apns': ?instance.apns,
};
