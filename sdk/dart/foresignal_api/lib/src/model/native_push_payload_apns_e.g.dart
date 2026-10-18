// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'native_push_payload_apns_e.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NativePushPayloadApnsECWProxy {
  NativePushPayloadApnsE v(NativePushPayloadApnsEVEnum v);

  NativePushPayloadApnsE id(int id);

  NativePushPayloadApnsE a(int? a);

  NativePushPayloadApnsE s(NativePushPayloadApnsESEnum s);

  NativePushPayloadApnsE ev(NativePushPayloadApnsEEvEnum ev);

  NativePushPayloadApnsE l(String? l);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadApnsE(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadApnsE(...).copyWith(id: 12, name: "My name")
  /// ```
  NativePushPayloadApnsE call({
    NativePushPayloadApnsEVEnum v,
    int id,
    int? a,
    NativePushPayloadApnsESEnum s,
    NativePushPayloadApnsEEvEnum ev,
    String? l,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNativePushPayloadApnsE.copyWith(...)` or call `instanceOfNativePushPayloadApnsE.copyWith.fieldName(value)` for a single field.
class _$NativePushPayloadApnsECWProxyImpl
    implements _$NativePushPayloadApnsECWProxy {
  const _$NativePushPayloadApnsECWProxyImpl(this._value);

  final NativePushPayloadApnsE _value;

  @override
  NativePushPayloadApnsE v(NativePushPayloadApnsEVEnum v) => call(v: v);

  @override
  NativePushPayloadApnsE id(int id) => call(id: id);

  @override
  NativePushPayloadApnsE a(int? a) => call(a: a);

  @override
  NativePushPayloadApnsE s(NativePushPayloadApnsESEnum s) => call(s: s);

  @override
  NativePushPayloadApnsE ev(NativePushPayloadApnsEEvEnum ev) => call(ev: ev);

  @override
  NativePushPayloadApnsE l(String? l) => call(l: l);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadApnsE(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadApnsE(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NativePushPayloadApnsE call({
    Object? v = const $CopyWithPlaceholder(),
    Object? id = const $CopyWithPlaceholder(),
    Object? a = const $CopyWithPlaceholder(),
    Object? s = const $CopyWithPlaceholder(),
    Object? ev = const $CopyWithPlaceholder(),
    Object? l = const $CopyWithPlaceholder(),
  }) {
    return NativePushPayloadApnsE(
      v: v == const $CopyWithPlaceholder() || v == null
          ? _value.v
          // ignore: cast_nullable_to_non_nullable
          : v as NativePushPayloadApnsEVEnum,
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as int,
      a: a == const $CopyWithPlaceholder()
          ? _value.a
          // ignore: cast_nullable_to_non_nullable
          : a as int?,
      s: s == const $CopyWithPlaceholder() || s == null
          ? _value.s
          // ignore: cast_nullable_to_non_nullable
          : s as NativePushPayloadApnsESEnum,
      ev: ev == const $CopyWithPlaceholder() || ev == null
          ? _value.ev
          // ignore: cast_nullable_to_non_nullable
          : ev as NativePushPayloadApnsEEvEnum,
      l: l == const $CopyWithPlaceholder()
          ? _value.l
          // ignore: cast_nullable_to_non_nullable
          : l as String?,
    );
  }
}

extension $NativePushPayloadApnsECopyWith on NativePushPayloadApnsE {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNativePushPayloadApnsE.copyWith(...)` or `instanceOfNativePushPayloadApnsE.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NativePushPayloadApnsECWProxy get copyWith =>
      _$NativePushPayloadApnsECWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NativePushPayloadApnsE _$NativePushPayloadApnsEFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NativePushPayloadApnsE', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['v', 'id', 's', 'ev']);
  final val = NativePushPayloadApnsE(
    v: $checkedConvert(
      'v',
      (v) => $enumDecode(_$NativePushPayloadApnsEVEnumEnumMap, v),
    ),
    id: $checkedConvert('id', (v) => (v as num).toInt()),
    a: $checkedConvert('a', (v) => (v as num?)?.toInt()),
    s: $checkedConvert(
      's',
      (v) => $enumDecode(_$NativePushPayloadApnsESEnumEnumMap, v),
    ),
    ev: $checkedConvert(
      'ev',
      (v) => $enumDecode(_$NativePushPayloadApnsEEvEnumEnumMap, v),
    ),
    l: $checkedConvert('l', (v) => v as String?),
  );
  return val;
});

Map<String, dynamic> _$NativePushPayloadApnsEToJson(
  NativePushPayloadApnsE instance,
) => <String, dynamic>{
  'v': _$NativePushPayloadApnsEVEnumEnumMap[instance.v]!,
  'id': instance.id,
  'a': ?instance.a,
  's': _$NativePushPayloadApnsESEnumEnumMap[instance.s]!,
  'ev': _$NativePushPayloadApnsEEvEnumEnumMap[instance.ev]!,
  'l': ?instance.l,
};

const _$NativePushPayloadApnsEVEnumEnumMap = {
  NativePushPayloadApnsEVEnum.number1: 1,
};

const _$NativePushPayloadApnsESEnumEnumMap = {
  NativePushPayloadApnsESEnum.info: 'info',
  NativePushPayloadApnsESEnum.warning: 'warning',
  NativePushPayloadApnsESEnum.critical: 'critical',
};

const _$NativePushPayloadApnsEEvEnumEnumMap = {
  NativePushPayloadApnsEEvEnum.alert: 'alert',
  NativePushPayloadApnsEEvEnum.briefing: 'briefing',
  NativePushPayloadApnsEEvEnum.system: 'system',
};
