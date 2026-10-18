// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'native_push_payload_fcm_message_data.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NativePushPayloadFcmMessageDataCWProxy {
  NativePushPayloadFcmMessageData v(String? v);

  NativePushPayloadFcmMessageData id(String? id);

  NativePushPayloadFcmMessageData a(String? a);

  NativePushPayloadFcmMessageData s(String? s);

  NativePushPayloadFcmMessageData ev(String? ev);

  NativePushPayloadFcmMessageData l(String? l);

  NativePushPayloadFcmMessageData t(String? t);

  NativePushPayloadFcmMessageData c(String? c);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadFcmMessageData(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadFcmMessageData(...).copyWith(id: 12, name: "My name")
  /// ```
  NativePushPayloadFcmMessageData call({
    String? v,
    String? id,
    String? a,
    String? s,
    String? ev,
    String? l,
    String? t,
    String? c,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNativePushPayloadFcmMessageData.copyWith(...)` or call `instanceOfNativePushPayloadFcmMessageData.copyWith.fieldName(value)` for a single field.
class _$NativePushPayloadFcmMessageDataCWProxyImpl
    implements _$NativePushPayloadFcmMessageDataCWProxy {
  const _$NativePushPayloadFcmMessageDataCWProxyImpl(this._value);

  final NativePushPayloadFcmMessageData _value;

  @override
  NativePushPayloadFcmMessageData v(String? v) => call(v: v);

  @override
  NativePushPayloadFcmMessageData id(String? id) => call(id: id);

  @override
  NativePushPayloadFcmMessageData a(String? a) => call(a: a);

  @override
  NativePushPayloadFcmMessageData s(String? s) => call(s: s);

  @override
  NativePushPayloadFcmMessageData ev(String? ev) => call(ev: ev);

  @override
  NativePushPayloadFcmMessageData l(String? l) => call(l: l);

  @override
  NativePushPayloadFcmMessageData t(String? t) => call(t: t);

  @override
  NativePushPayloadFcmMessageData c(String? c) => call(c: c);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadFcmMessageData(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadFcmMessageData(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NativePushPayloadFcmMessageData call({
    Object? v = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? a = const $CopyWithPlaceholder(),
    Object? s = const $CopyWithPlaceholder(),
    Object? ev = const $CopyWithPlaceholder(),
    Object? l = const $CopyWithPlaceholder(),
    Object? t = const $CopyWithPlaceholder(),
    Object? c = const $CopyWithPlaceholder(),
  }) {
    return NativePushPayloadFcmMessageData(
      v: v == const $CopyWithPlaceholder()
          ? _value.v
          // ignore: cast_nullable_to_non_nullable
          : v as String?,
      id: id == const $CopyWithPlaceholder()
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String?,
      a: a == const $CopyWithPlaceholder()
          ? _value.a
          // ignore: cast_nullable_to_non_nullable
          : a as String?,
      s: s == const $CopyWithPlaceholder()
          ? _value.s
          // ignore: cast_nullable_to_non_nullable
          : s as String?,
      ev: ev == const $CopyWithPlaceholder()
          ? _value.ev
          // ignore: cast_nullable_to_non_nullable
          : ev as String?,
      l: l == const $CopyWithPlaceholder()
          ? _value.l
          // ignore: cast_nullable_to_non_nullable
          : l as String?,
      t: t == const $CopyWithPlaceholder()
          ? _value.t
          // ignore: cast_nullable_to_non_nullable
          : t as String?,
      c: c == const $CopyWithPlaceholder()
          ? _value.c
          // ignore: cast_nullable_to_non_nullable
          : c as String?,
    );
  }
}

extension $NativePushPayloadFcmMessageDataCopyWith
    on NativePushPayloadFcmMessageData {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNativePushPayloadFcmMessageData.copyWith(...)` or `instanceOfNativePushPayloadFcmMessageData.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NativePushPayloadFcmMessageDataCWProxy get copyWith =>
      _$NativePushPayloadFcmMessageDataCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NativePushPayloadFcmMessageData _$NativePushPayloadFcmMessageDataFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NativePushPayloadFcmMessageData', json, ($checkedConvert) {
  final val = NativePushPayloadFcmMessageData(
    v: $checkedConvert('v', (v) => v as String?),
    id: $checkedConvert('id', (v) => v as String?),
    a: $checkedConvert('a', (v) => v as String?),
    s: $checkedConvert('s', (v) => v as String?),
    ev: $checkedConvert('ev', (v) => v as String?),
    l: $checkedConvert('l', (v) => v as String?),
    t: $checkedConvert('t', (v) => v as String?),
    c: $checkedConvert('c', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$NativePushPayloadFcmMessageDataToJson(
  NativePushPayloadFcmMessageData instance,
) => <String, dynamic>{
  'v': ?instance.v,
  'id': ?instance.id,
  'a': ?instance.a,
  's': ?instance.s,
  'ev': ?instance.ev,
  'l': ?instance.l,
  't': ?instance.t,
  'c': ?instance.c,
};
