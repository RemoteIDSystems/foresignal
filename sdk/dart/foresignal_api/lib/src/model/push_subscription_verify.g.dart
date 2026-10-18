// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_subscription_verify.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushSubscriptionVerifyCWProxy {
  PushSubscriptionVerify code(String code);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushSubscriptionVerify(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushSubscriptionVerify(...).copyWith(id: 12, name: "My name")
  /// ```
  PushSubscriptionVerify call({String code});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushSubscriptionVerify.copyWith(...)` or call `instanceOfPushSubscriptionVerify.copyWith.fieldName(value)` for a single field.
class _$PushSubscriptionVerifyCWProxyImpl
    implements _$PushSubscriptionVerifyCWProxy {
  const _$PushSubscriptionVerifyCWProxyImpl(this._value);

  final PushSubscriptionVerify _value;

  @override
  PushSubscriptionVerify code(String code) => call(code: code);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushSubscriptionVerify(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushSubscriptionVerify(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushSubscriptionVerify call({Object? code = const $CopyWithPlaceholder()}) {
    return PushSubscriptionVerify(
      code: code == const $CopyWithPlaceholder() || code == null
          ? _value.code
          // ignore: cast_nullable_to_non_nullable
          : code as String,
    );
  }
}

extension $PushSubscriptionVerifyCopyWith on PushSubscriptionVerify {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushSubscriptionVerify.copyWith(...)` or `instanceOfPushSubscriptionVerify.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushSubscriptionVerifyCWProxy get copyWith =>
      _$PushSubscriptionVerifyCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushSubscriptionVerify _$PushSubscriptionVerifyFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PushSubscriptionVerify', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['code']);
  final val = PushSubscriptionVerify(
    code: $checkedConvert('code', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$PushSubscriptionVerifyToJson(
  PushSubscriptionVerify instance,
) => <String, dynamic>{'code': instance.code};
