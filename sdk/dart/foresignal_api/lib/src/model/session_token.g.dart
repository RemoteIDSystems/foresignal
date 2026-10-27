// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'session_token.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SessionTokenCWProxy {
  SessionToken token(String token);

  SessionToken tokenPrefix(String? tokenPrefix);

  SessionToken userId(String userId);

  SessionToken issuedAt(DateTime? issuedAt);

  SessionToken expiresAt(DateTime? expiresAt);

  SessionToken idleExpiresAt(DateTime? idleExpiresAt);

  SessionToken mfaRequired(bool? mfaRequired);

  SessionToken scopes(List<String>? scopes);

  SessionToken permissions(List<String>? permissions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SessionToken(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SessionToken(...).copyWith(id: 12, name: "My name")
  /// ```
  SessionToken call({
    String token,
    String? tokenPrefix,
    String userId,
    DateTime? issuedAt,
    DateTime? expiresAt,
    DateTime? idleExpiresAt,
    bool? mfaRequired,
    List<String>? scopes,
    List<String>? permissions,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSessionToken.copyWith(...)` or call `instanceOfSessionToken.copyWith.fieldName(value)` for a single field.
class _$SessionTokenCWProxyImpl implements _$SessionTokenCWProxy {
  const _$SessionTokenCWProxyImpl(this._value);

  final SessionToken _value;

  @override
  SessionToken token(String token) => call(token: token);

  @override
  SessionToken tokenPrefix(String? tokenPrefix) =>
      call(tokenPrefix: tokenPrefix);

  @override
  SessionToken userId(String userId) => call(userId: userId);

  @override
  SessionToken issuedAt(DateTime? issuedAt) => call(issuedAt: issuedAt);

  @override
  SessionToken expiresAt(DateTime? expiresAt) => call(expiresAt: expiresAt);

  @override
  SessionToken idleExpiresAt(DateTime? idleExpiresAt) =>
      call(idleExpiresAt: idleExpiresAt);

  @override
  SessionToken mfaRequired(bool? mfaRequired) => call(mfaRequired: mfaRequired);

  @override
  SessionToken scopes(List<String>? scopes) => call(scopes: scopes);

  @override
  SessionToken permissions(List<String>? permissions) =>
      call(permissions: permissions);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SessionToken(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SessionToken(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SessionToken call({
    Object? token = const $CopyWithPlaceholder(),
    Object? tokenPrefix = const $CopyWithPlaceholder(),
    Object? userId = const $CopyWithPlaceholder(),
    Object? issuedAt = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
    Object? idleExpiresAt = const $CopyWithPlaceholder(),
    Object? mfaRequired = const $CopyWithPlaceholder(),
    Object? scopes = const $CopyWithPlaceholder(),
    Object? permissions = const $CopyWithPlaceholder(),
  }) {
    return SessionToken(
      token: token == const $CopyWithPlaceholder() || token == null
          ? _value.token
          // ignore: cast_nullable_to_non_nullable
          : token as String,
      tokenPrefix: tokenPrefix == const $CopyWithPlaceholder()
          ? _value.tokenPrefix
          // ignore: cast_nullable_to_non_nullable
          : tokenPrefix as String?,
      userId: userId == const $CopyWithPlaceholder() || userId == null
          ? _value.userId
          // ignore: cast_nullable_to_non_nullable
          : userId as String,
      issuedAt: issuedAt == const $CopyWithPlaceholder()
          ? _value.issuedAt
          // ignore: cast_nullable_to_non_nullable
          : issuedAt as DateTime?,
      expiresAt: expiresAt == const $CopyWithPlaceholder()
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime?,
      idleExpiresAt: idleExpiresAt == const $CopyWithPlaceholder()
          ? _value.idleExpiresAt
          // ignore: cast_nullable_to_non_nullable
          : idleExpiresAt as DateTime?,
      mfaRequired: mfaRequired == const $CopyWithPlaceholder()
          ? _value.mfaRequired
          // ignore: cast_nullable_to_non_nullable
          : mfaRequired as bool?,
      scopes: scopes == const $CopyWithPlaceholder()
          ? _value.scopes
          // ignore: cast_nullable_to_non_nullable
          : scopes as List<String>?,
      permissions: permissions == const $CopyWithPlaceholder()
          ? _value.permissions
          // ignore: cast_nullable_to_non_nullable
          : permissions as List<String>?,
    );
  }
}

extension $SessionTokenCopyWith on SessionToken {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSessionToken.copyWith(...)` or `instanceOfSessionToken.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SessionTokenCWProxy get copyWith => _$SessionTokenCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SessionToken _$SessionTokenFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SessionToken', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['token', 'userId', 'expiresAt']);
      final val = SessionToken(
        token: $checkedConvert('token', (v) => v as String),
        tokenPrefix: $checkedConvert('tokenPrefix', (v) => v as String?),
        userId: $checkedConvert('userId', (v) => v as String),
        issuedAt: $checkedConvert(
          'issuedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        idleExpiresAt: $checkedConvert(
          'idleExpiresAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        mfaRequired: $checkedConvert('mfaRequired', (v) => v as bool? ?? false),
        scopes: $checkedConvert(
          'scopes',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
        permissions: $checkedConvert(
          'permissions',
          (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
        ),
      );
      return val;
    });

Map<String, dynamic> _$SessionTokenToJson(SessionToken instance) =>
    <String, dynamic>{
      'token': instance.token,
      'tokenPrefix': ?instance.tokenPrefix,
      'userId': instance.userId,
      'issuedAt': ?instance.issuedAt?.toIso8601String(),
      'expiresAt': instance.expiresAt?.toIso8601String(),
      'idleExpiresAt': ?instance.idleExpiresAt?.toIso8601String(),
      'mfaRequired': ?instance.mfaRequired,
      'scopes': ?instance.scopes,
      'permissions': ?instance.permissions,
    };
