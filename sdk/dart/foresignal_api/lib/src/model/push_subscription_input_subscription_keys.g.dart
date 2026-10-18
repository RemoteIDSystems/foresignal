// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_subscription_input_subscription_keys.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushSubscriptionInputSubscriptionKeysCWProxy {
  PushSubscriptionInputSubscriptionKeys p256dh(String p256dh);

  PushSubscriptionInputSubscriptionKeys auth(String auth);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushSubscriptionInputSubscriptionKeys(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushSubscriptionInputSubscriptionKeys(...).copyWith(id: 12, name: "My name")
  /// ```
  PushSubscriptionInputSubscriptionKeys call({String p256dh, String auth});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushSubscriptionInputSubscriptionKeys.copyWith(...)` or call `instanceOfPushSubscriptionInputSubscriptionKeys.copyWith.fieldName(value)` for a single field.
class _$PushSubscriptionInputSubscriptionKeysCWProxyImpl
    implements _$PushSubscriptionInputSubscriptionKeysCWProxy {
  const _$PushSubscriptionInputSubscriptionKeysCWProxyImpl(this._value);

  final PushSubscriptionInputSubscriptionKeys _value;

  @override
  PushSubscriptionInputSubscriptionKeys p256dh(String p256dh) =>
      call(p256dh: p256dh);

  @override
  PushSubscriptionInputSubscriptionKeys auth(String auth) => call(auth: auth);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushSubscriptionInputSubscriptionKeys(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushSubscriptionInputSubscriptionKeys(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushSubscriptionInputSubscriptionKeys call({
    Object? p256dh = const $CopyWithPlaceholder(),
    Object? auth = const $CopyWithPlaceholder(),
  }) {
    return PushSubscriptionInputSubscriptionKeys(
      p256dh: p256dh == const $CopyWithPlaceholder() || p256dh == null
          ? _value.p256dh
          // ignore: cast_nullable_to_non_nullable
          : p256dh as String,
      auth: auth == const $CopyWithPlaceholder() || auth == null
          ? _value.auth
          // ignore: cast_nullable_to_non_nullable
          : auth as String,
    );
  }
}

extension $PushSubscriptionInputSubscriptionKeysCopyWith
    on PushSubscriptionInputSubscriptionKeys {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushSubscriptionInputSubscriptionKeys.copyWith(...)` or `instanceOfPushSubscriptionInputSubscriptionKeys.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushSubscriptionInputSubscriptionKeysCWProxy get copyWith =>
      _$PushSubscriptionInputSubscriptionKeysCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushSubscriptionInputSubscriptionKeys
_$PushSubscriptionInputSubscriptionKeysFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PushSubscriptionInputSubscriptionKeys', json, (
      $checkedConvert,
    ) {
      $checkKeys(json, requiredKeys: const ['p256dh', 'auth']);
      final val = PushSubscriptionInputSubscriptionKeys(
        p256dh: $checkedConvert('p256dh', (v) => v as String),
        auth: $checkedConvert('auth', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$PushSubscriptionInputSubscriptionKeysToJson(
  PushSubscriptionInputSubscriptionKeys instance,
) => <String, dynamic>{'p256dh': instance.p256dh, 'auth': instance.auth};
