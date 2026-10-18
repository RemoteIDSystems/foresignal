// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'native_push_payload_apns_aps_alert.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NativePushPayloadApnsApsAlertCWProxy {
  NativePushPayloadApnsApsAlert title(String title);

  NativePushPayloadApnsApsAlert body(String body);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadApnsApsAlert(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadApnsApsAlert(...).copyWith(id: 12, name: "My name")
  /// ```
  NativePushPayloadApnsApsAlert call({String title, String body});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNativePushPayloadApnsApsAlert.copyWith(...)` or call `instanceOfNativePushPayloadApnsApsAlert.copyWith.fieldName(value)` for a single field.
class _$NativePushPayloadApnsApsAlertCWProxyImpl
    implements _$NativePushPayloadApnsApsAlertCWProxy {
  const _$NativePushPayloadApnsApsAlertCWProxyImpl(this._value);

  final NativePushPayloadApnsApsAlert _value;

  @override
  NativePushPayloadApnsApsAlert title(String title) => call(title: title);

  @override
  NativePushPayloadApnsApsAlert body(String body) => call(body: body);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadApnsApsAlert(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadApnsApsAlert(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NativePushPayloadApnsApsAlert call({
    Object? title = const $CopyWithPlaceholder(),
    Object? body = const $CopyWithPlaceholder(),
  }) {
    return NativePushPayloadApnsApsAlert(
      title: title == const $CopyWithPlaceholder() || title == null
          ? _value.title
          // ignore: cast_nullable_to_non_nullable
          : title as String,
      body: body == const $CopyWithPlaceholder() || body == null
          ? _value.body
          // ignore: cast_nullable_to_non_nullable
          : body as String,
    );
  }
}

extension $NativePushPayloadApnsApsAlertCopyWith
    on NativePushPayloadApnsApsAlert {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNativePushPayloadApnsApsAlert.copyWith(...)` or `instanceOfNativePushPayloadApnsApsAlert.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NativePushPayloadApnsApsAlertCWProxy get copyWith =>
      _$NativePushPayloadApnsApsAlertCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NativePushPayloadApnsApsAlert _$NativePushPayloadApnsApsAlertFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NativePushPayloadApnsApsAlert', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['title', 'body']);
  final val = NativePushPayloadApnsApsAlert(
    title: $checkedConvert('title', (v) => v as String),
    body: $checkedConvert('body', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$NativePushPayloadApnsApsAlertToJson(
  NativePushPayloadApnsApsAlert instance,
) => <String, dynamic>{'title': instance.title, 'body': instance.body};
