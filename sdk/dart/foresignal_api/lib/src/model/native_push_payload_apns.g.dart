// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'native_push_payload_apns.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$NativePushPayloadApnsCWProxy {
  NativePushPayloadApns aps(NativePushPayloadApnsAps? aps);

  NativePushPayloadApns e(NativePushPayloadApnsE? e);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadApns(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadApns(...).copyWith(id: 12, name: "My name")
  /// ```
  NativePushPayloadApns call({
    NativePushPayloadApnsAps? aps,
    NativePushPayloadApnsE? e,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfNativePushPayloadApns.copyWith(...)` or call `instanceOfNativePushPayloadApns.copyWith.fieldName(value)` for a single field.
class _$NativePushPayloadApnsCWProxyImpl
    implements _$NativePushPayloadApnsCWProxy {
  const _$NativePushPayloadApnsCWProxyImpl(this._value);

  final NativePushPayloadApns _value;

  @override
  NativePushPayloadApns aps(NativePushPayloadApnsAps? aps) => call(aps: aps);

  @override
  NativePushPayloadApns e(NativePushPayloadApnsE? e) => call(e: e);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `NativePushPayloadApns(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// NativePushPayloadApns(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  NativePushPayloadApns call({
    Object? aps = const $CopyWithPlaceholder(),
    Object? e = const $CopyWithPlaceholder(),
  }) {
    return NativePushPayloadApns(
      aps: aps == const $CopyWithPlaceholder()
          ? _value.aps
          // ignore: cast_nullable_to_non_nullable
          : aps as NativePushPayloadApnsAps?,
      e: e == const $CopyWithPlaceholder()
          ? _value.e
          // ignore: cast_nullable_to_non_nullable
          : e as NativePushPayloadApnsE?,
    );
  }
}

extension $NativePushPayloadApnsCopyWith on NativePushPayloadApns {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfNativePushPayloadApns.copyWith(...)` or `instanceOfNativePushPayloadApns.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$NativePushPayloadApnsCWProxy get copyWith =>
      _$NativePushPayloadApnsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

NativePushPayloadApns _$NativePushPayloadApnsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('NativePushPayloadApns', json, ($checkedConvert) {
  final val = NativePushPayloadApns(
    aps: $checkedConvert(
      'aps',
      (v) => v == null
          ? null
          : NativePushPayloadApnsAps.fromJson(v as Map<String, dynamic>),
    ),
    e: $checkedConvert(
      'e',
      (v) => v == null
          ? null
          : NativePushPayloadApnsE.fromJson(v as Map<String, dynamic>),
    ),
  );
  return val;
});

Map<String, dynamic> _$NativePushPayloadApnsToJson(
  NativePushPayloadApns instance,
) => <String, dynamic>{
  'aps': ?instance.aps?.toJson(),
  'e': ?instance.e?.toJson(),
};
