// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'social_platform_credentials.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$SocialPlatformCredentialsCWProxy {
  SocialPlatformCredentials required_(bool required_);

  SocialPlatformCredentials configured(bool configured);

  SocialPlatformCredentials fields(List<String>? fields);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformCredentials(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformCredentials(...).copyWith(id: 12, name: "My name")
  /// ```
  SocialPlatformCredentials call({
    bool required_,
    bool configured,
    List<String>? fields,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfSocialPlatformCredentials.copyWith(...)` or call `instanceOfSocialPlatformCredentials.copyWith.fieldName(value)` for a single field.
class _$SocialPlatformCredentialsCWProxyImpl
    implements _$SocialPlatformCredentialsCWProxy {
  const _$SocialPlatformCredentialsCWProxyImpl(this._value);

  final SocialPlatformCredentials _value;

  @override
  SocialPlatformCredentials required_(bool required_) =>
      call(required_: required_);

  @override
  SocialPlatformCredentials configured(bool configured) =>
      call(configured: configured);

  @override
  SocialPlatformCredentials fields(List<String>? fields) =>
      call(fields: fields);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `SocialPlatformCredentials(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// SocialPlatformCredentials(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  SocialPlatformCredentials call({
    Object? required_ = const $CopyWithPlaceholder(),
    Object? configured = const $CopyWithPlaceholder(),
    Object? fields = const $CopyWithPlaceholder(),
  }) {
    return SocialPlatformCredentials(
      required_: required_ == const $CopyWithPlaceholder() || required_ == null
          ? _value.required_
          // ignore: cast_nullable_to_non_nullable
          : required_ as bool,
      configured:
          configured == const $CopyWithPlaceholder() || configured == null
          ? _value.configured
          // ignore: cast_nullable_to_non_nullable
          : configured as bool,
      fields: fields == const $CopyWithPlaceholder()
          ? _value.fields
          // ignore: cast_nullable_to_non_nullable
          : fields as List<String>?,
    );
  }
}

extension $SocialPlatformCredentialsCopyWith on SocialPlatformCredentials {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfSocialPlatformCredentials.copyWith(...)` or `instanceOfSocialPlatformCredentials.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$SocialPlatformCredentialsCWProxy get copyWith =>
      _$SocialPlatformCredentialsCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

SocialPlatformCredentials _$SocialPlatformCredentialsFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('SocialPlatformCredentials', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['required', 'configured']);
  final val = SocialPlatformCredentials(
    required_: $checkedConvert('required', (v) => v as bool),
    configured: $checkedConvert('configured', (v) => v as bool),
    fields: $checkedConvert(
      'fields',
      (v) => (v as List<dynamic>?)?.map((e) => e as String).toList(),
    ),
  );
  return val;
}, fieldKeyMap: const {'required_': 'required'});

Map<String, dynamic> _$SocialPlatformCredentialsToJson(
  SocialPlatformCredentials instance,
) => <String, dynamic>{
  'required': instance.required_,
  'configured': instance.configured,
  'fields': ?instance.fields,
};
