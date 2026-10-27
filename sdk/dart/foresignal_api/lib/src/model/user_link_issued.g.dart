// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'user_link_issued.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$UserLinkIssuedCWProxy {
  UserLinkIssued kind(String kind);

  UserLinkIssued emailed(bool emailed);

  UserLinkIssued token(String? token);

  UserLinkIssued tokenPrefix(String tokenPrefix);

  UserLinkIssued expiresAt(DateTime expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserLinkIssued(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserLinkIssued(...).copyWith(id: 12, name: "My name")
  /// ```
  UserLinkIssued call({
    String kind,
    bool emailed,
    String? token,
    String tokenPrefix,
    DateTime expiresAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfUserLinkIssued.copyWith(...)` or call `instanceOfUserLinkIssued.copyWith.fieldName(value)` for a single field.
class _$UserLinkIssuedCWProxyImpl implements _$UserLinkIssuedCWProxy {
  const _$UserLinkIssuedCWProxyImpl(this._value);

  final UserLinkIssued _value;

  @override
  UserLinkIssued kind(String kind) => call(kind: kind);

  @override
  UserLinkIssued emailed(bool emailed) => call(emailed: emailed);

  @override
  UserLinkIssued token(String? token) => call(token: token);

  @override
  UserLinkIssued tokenPrefix(String tokenPrefix) =>
      call(tokenPrefix: tokenPrefix);

  @override
  UserLinkIssued expiresAt(DateTime expiresAt) => call(expiresAt: expiresAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `UserLinkIssued(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// UserLinkIssued(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  UserLinkIssued call({
    Object? kind = const $CopyWithPlaceholder(),
    Object? emailed = const $CopyWithPlaceholder(),
    Object? token = const $CopyWithPlaceholder(),
    Object? tokenPrefix = const $CopyWithPlaceholder(),
    Object? expiresAt = const $CopyWithPlaceholder(),
  }) {
    return UserLinkIssued(
      kind: kind == const $CopyWithPlaceholder() || kind == null
          ? _value.kind
          // ignore: cast_nullable_to_non_nullable
          : kind as String,
      emailed: emailed == const $CopyWithPlaceholder() || emailed == null
          ? _value.emailed
          // ignore: cast_nullable_to_non_nullable
          : emailed as bool,
      token: token == const $CopyWithPlaceholder()
          ? _value.token
          // ignore: cast_nullable_to_non_nullable
          : token as String?,
      tokenPrefix:
          tokenPrefix == const $CopyWithPlaceholder() || tokenPrefix == null
          ? _value.tokenPrefix
          // ignore: cast_nullable_to_non_nullable
          : tokenPrefix as String,
      expiresAt: expiresAt == const $CopyWithPlaceholder() || expiresAt == null
          ? _value.expiresAt
          // ignore: cast_nullable_to_non_nullable
          : expiresAt as DateTime,
    );
  }
}

extension $UserLinkIssuedCopyWith on UserLinkIssued {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfUserLinkIssued.copyWith(...)` or `instanceOfUserLinkIssued.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$UserLinkIssuedCWProxy get copyWith => _$UserLinkIssuedCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

UserLinkIssued _$UserLinkIssuedFromJson(Map<String, dynamic> json) =>
    $checkedCreate('UserLinkIssued', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['kind', 'emailed', 'tokenPrefix', 'expiresAt'],
      );
      final val = UserLinkIssued(
        kind: $checkedConvert('kind', (v) => v as String),
        emailed: $checkedConvert('emailed', (v) => v as bool),
        token: $checkedConvert('token', (v) => v as String?),
        tokenPrefix: $checkedConvert('tokenPrefix', (v) => v as String),
        expiresAt: $checkedConvert(
          'expiresAt',
          (v) => DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$UserLinkIssuedToJson(UserLinkIssued instance) =>
    <String, dynamic>{
      'kind': instance.kind,
      'emailed': instance.emailed,
      'token': ?instance.token,
      'tokenPrefix': instance.tokenPrefix,
      'expiresAt': instance.expiresAt.toIso8601String(),
    };
