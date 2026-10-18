// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'native_push_payload_fcm.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NativePushPayloadFcmCWProxy {
  NativePushPayloadFcm message(NativePushPayloadFcmMessage? message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadFcm(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadFcm(...).copyWith(id: 12, name: "My name")
  /// ```
  NativePushPayloadFcm call({NativePushPayloadFcmMessage? message});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNativePushPayloadFcm.copyWith(...)` or call `instanceOfNativePushPayloadFcm.copyWith.fieldName(value)` for a single field.
class _$NativePushPayloadFcmCWProxyImpl
    implements _$NativePushPayloadFcmCWProxy {
  const _$NativePushPayloadFcmCWProxyImpl(this._value);

  final NativePushPayloadFcm _value;

  @override
  NativePushPayloadFcm message(NativePushPayloadFcmMessage? message) =>
      call(message: message);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadFcm(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadFcm(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NativePushPayloadFcm call({Object? message = const $CopyWithPlaceholder()}) {
    return NativePushPayloadFcm(
      message: message == const $CopyWithPlaceholder()
          ? _value.message
          // ignore: cast_nullable_to_non_nullable
          : message as NativePushPayloadFcmMessage?,
    );
  }
}

extension $NativePushPayloadFcmCopyWith on NativePushPayloadFcm {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNativePushPayloadFcm.copyWith(...)` or `instanceOfNativePushPayloadFcm.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NativePushPayloadFcmCWProxy get copyWith =>
      _$NativePushPayloadFcmCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NativePushPayloadFcm _$NativePushPayloadFcmFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NativePushPayloadFcm', json, ($checkedConvert) {
  final val = NativePushPayloadFcm(
    message: $checkedConvert(
      'message',
      (v) => v == null
          ? null
          : NativePushPayloadFcmMessage.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$NativePushPayloadFcmToJson(
  NativePushPayloadFcm instance,
) => <String, dynamic>{'message': ?instance.message?.toJson()};
