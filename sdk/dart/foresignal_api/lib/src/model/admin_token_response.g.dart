// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'admin_token_response.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$AdminTokenResponseCWProxy {
  AdminTokenResponse token(String token);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AdminTokenResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AdminTokenResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  AdminTokenResponse call({String token});
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfAdminTokenResponse.copyWith(...)` or call `instanceOfAdminTokenResponse.copyWith.fieldName(value)` for a single field.
class _$AdminTokenResponseCWProxyImpl implements _$AdminTokenResponseCWProxy {
  const _$AdminTokenResponseCWProxyImpl(this._value);

  final AdminTokenResponse _value;

  @override
  AdminTokenResponse token(String token) => call(token: token);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `AdminTokenResponse(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// AdminTokenResponse(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  AdminTokenResponse call({Object? token = const $CopyWithPlaceholder()}) {
    return AdminTokenResponse(
      token: token == const $CopyWithPlaceholder() || token == null
          ? _value.token
          // ignore: cast_nullable_to_non_nullable
          : token as String,
    );
  }
}

extension $AdminTokenResponseCopyWith on AdminTokenResponse {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfAdminTokenResponse.copyWith(...)` or `instanceOfAdminTokenResponse.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$AdminTokenResponseCWProxy get copyWith =>
      _$AdminTokenResponseCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

AdminTokenResponse _$AdminTokenResponseFromJson(Map<String, dynamic> json) =>
    $checkedCreate('AdminTokenResponse', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['token']);
      final val = AdminTokenResponse(
        token: $checkedConvert('token', (v) => v as String),
      );
      return val;
    });

Map<String, dynamic> _$AdminTokenResponseToJson(AdminTokenResponse instance) =>
    <String, dynamic>{'token': instance.token};
