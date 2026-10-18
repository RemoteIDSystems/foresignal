// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_author.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialAuthorCWProxy {
  SocialAuthor handle(String handle);

  SocialAuthor displayName(String? displayName);

  SocialAuthor accountId(String accountId);

  SocialAuthor accountCreatedAt(DateTime? accountCreatedAt);

  SocialAuthor followers(int? followers);

  SocialAuthor verified(bool? verified);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialAuthor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialAuthor(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialAuthor call({
    String handle,
    String? displayName,
    String accountId,
    DateTime? accountCreatedAt,
    int? followers,
    bool? verified,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialAuthor.copyWith(...)` or call `instanceOfSocialAuthor.copyWith.fieldName(value)` for a single field.
class _$SocialAuthorCWProxyImpl implements _$SocialAuthorCWProxy {
  const _$SocialAuthorCWProxyImpl(this._value);

  final SocialAuthor _value;

  @override
  SocialAuthor handle(String handle) => call(handle: handle);

  @override
  SocialAuthor displayName(String? displayName) =>
      call(displayName: displayName);

  @override
  SocialAuthor accountId(String accountId) => call(accountId: accountId);

  @override
  SocialAuthor accountCreatedAt(DateTime? accountCreatedAt) =>
      call(accountCreatedAt: accountCreatedAt);

  @override
  SocialAuthor followers(int? followers) => call(followers: followers);

  @override
  SocialAuthor verified(bool? verified) => call(verified: verified);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialAuthor(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialAuthor(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialAuthor call({
    Object? handle = const $CopyWithPlaceholder(),
    Object? displayName = const $CopyWithPlaceholder(),
    Object? accountId = const $CopyWithPlaceholder(),
    Object? accountCreatedAt = const $CopyWithPlaceholder(),
    Object? followers = const $CopyWithPlaceholder(),
    Object? verified = const $CopyWithPlaceholder(),
  }) {
    return SocialAuthor(
      handle: handle == const $CopyWithPlaceholder() || handle == null
          ? _value.handle
          // ignore: cast_nullable_to_non_nullable
          : handle as String,
      displayName: displayName == const $CopyWithPlaceholder()
          ? _value.displayName
          // ignore: cast_nullable_to_non_nullable
          : displayName as String?,
      accountId: accountId == const $CopyWithPlaceholder() || accountId == null
          ? _value.accountId
          // ignore: cast_nullable_to_non_nullable
          : accountId as String,
      accountCreatedAt: accountCreatedAt == const $CopyWithPlaceholder()
          ? _value.accountCreatedAt
          // ignore: cast_nullable_to_non_nullable
          : accountCreatedAt as DateTime?,
      followers: followers == const $CopyWithPlaceholder()
          ? _value.followers
          // ignore: cast_nullable_to_non_nullable
          : followers as int?,
      verified: verified == const $CopyWithPlaceholder()
          ? _value.verified
          // ignore: cast_nullable_to_non_nullable
          : verified as bool?,
    );
  }
}

extension $SocialAuthorCopyWith on SocialAuthor {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialAuthor.copyWith(...)` or `instanceOfSocialAuthor.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialAuthorCWProxy get copyWith => _$SocialAuthorCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialAuthor _$SocialAuthorFromJson(Map<String, dynamic> json) =>
    $checkedCreate('SocialAuthor', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['handle', 'accountId']);
      final val = SocialAuthor(
        handle: $checkedConvert('handle', (v) => v as String),
        displayName: $checkedConvert('displayName', (v) => v as String?),
        accountId: $checkedConvert('accountId', (v) => v as String),
        accountCreatedAt: $checkedConvert(
          'accountCreatedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
        followers: $checkedConvert('followers', (v) => (v as num?)?.toInt()),
        verified: $checkedConvert('verified', (v) => v as bool?),
      );
      return val;
    });

Map<String, dynamic> _$SocialAuthorToJson(SocialAuthor instance) =>
    <String, dynamic>{
      'handle': instance.handle,
      'displayName': ?instance.displayName,
      'accountId': instance.accountId,
      'accountCreatedAt': ?instance.accountCreatedAt?.toIso8601String(),
      'followers': ?instance.followers,
      'verified': ?instance.verified,
    };
