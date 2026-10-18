// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'get_vapid_public_key200_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$GetVapidPublicKey200ResponseCWProxy {
  GetVapidPublicKey200Response publicKey(String publicKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GetVapidPublicKey200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GetVapidPublicKey200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  GetVapidPublicKey200Response call({String publicKey});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfGetVapidPublicKey200Response.copyWith(...)` or call `instanceOfGetVapidPublicKey200Response.copyWith.fieldName(value)` for a single field.
class _$GetVapidPublicKey200ResponseCWProxyImpl
    implements _$GetVapidPublicKey200ResponseCWProxy {
  const _$GetVapidPublicKey200ResponseCWProxyImpl(this._value);

  final GetVapidPublicKey200Response _value;

  @override
  GetVapidPublicKey200Response publicKey(String publicKey) =>
      call(publicKey: publicKey);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `GetVapidPublicKey200Response(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// GetVapidPublicKey200Response(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  GetVapidPublicKey200Response call({
    Object? publicKey = const $CopyWithPlaceholder(),
  }) {
    return GetVapidPublicKey200Response(
      publicKey: publicKey == const $CopyWithPlaceholder() || publicKey == null
          ? _value.publicKey
          // ignore: cast_nullable_to_non_nullable
          : publicKey as String,
    );
  }
}

extension $GetVapidPublicKey200ResponseCopyWith
    on GetVapidPublicKey200Response {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfGetVapidPublicKey200Response.copyWith(...)` or `instanceOfGetVapidPublicKey200Response.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$GetVapidPublicKey200ResponseCWProxy get copyWith =>
      _$GetVapidPublicKey200ResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

GetVapidPublicKey200Response _$GetVapidPublicKey200ResponseFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('GetVapidPublicKey200Response', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['publicKey']);
  final val = GetVapidPublicKey200Response(
    publicKey: $checkedConvert('publicKey', (v) => v as String),
  );
  return val;
});

Map<String, dynamic> _$GetVapidPublicKey200ResponseToJson(
  GetVapidPublicKey200Response instance,
) => <String, dynamic>{'publicKey': instance.publicKey};
