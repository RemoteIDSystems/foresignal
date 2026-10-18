// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'fcm_provider_config.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$FcmProviderConfigCWProxy {
  FcmProviderConfig projectId(String projectId);

  FcmProviderConfig clientEmail(String clientEmail);

  FcmProviderConfig tokenUri(String? tokenUri);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FcmProviderConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FcmProviderConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  FcmProviderConfig call({
    String projectId,
    String clientEmail,
    String? tokenUri,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfFcmProviderConfig.copyWith(...)` or call `instanceOfFcmProviderConfig.copyWith.fieldName(value)` for a single field.
class _$FcmProviderConfigCWProxyImpl implements _$FcmProviderConfigCWProxy {
  const _$FcmProviderConfigCWProxyImpl(this._value);

  final FcmProviderConfig _value;

  @override
  FcmProviderConfig projectId(String projectId) => call(projectId: projectId);

  @override
  FcmProviderConfig clientEmail(String clientEmail) =>
      call(clientEmail: clientEmail);

  @override
  FcmProviderConfig tokenUri(String? tokenUri) => call(tokenUri: tokenUri);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `FcmProviderConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// FcmProviderConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  FcmProviderConfig call({
    Object? projectId = const $CopyWithPlaceholder(),
    Object? clientEmail = const $CopyWithPlaceholder(),
    Object? tokenUri = const $CopyWithPlaceholder(),
  }) {
    return FcmProviderConfig(
      projectId: projectId == const $CopyWithPlaceholder() || projectId == null
          ? _value.projectId
          // ignore: cast_nullable_to_non_nullable
          : projectId as String,
      clientEmail:
          clientEmail == const $CopyWithPlaceholder() || clientEmail == null
          ? _value.clientEmail
          // ignore: cast_nullable_to_non_nullable
          : clientEmail as String,
      tokenUri: tokenUri == const $CopyWithPlaceholder()
          ? _value.tokenUri
          // ignore: cast_nullable_to_non_nullable
          : tokenUri as String?,
    );
  }
}

extension $FcmProviderConfigCopyWith on FcmProviderConfig {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfFcmProviderConfig.copyWith(...)` or `instanceOfFcmProviderConfig.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$FcmProviderConfigCWProxy get copyWith =>
      _$FcmProviderConfigCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

FcmProviderConfig _$FcmProviderConfigFromJson(Map<String, dynamic> json) =>
    $checkedCreate('FcmProviderConfig', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['projectId', 'clientEmail']);
      final val = FcmProviderConfig(
        projectId: $checkedConvert('projectId', (v) => v as String),
        clientEmail: $checkedConvert('clientEmail', (v) => v as String),
        tokenUri: $checkedConvert(
          'tokenUri',
          (v) => v as String? ?? 'https://oauth2.googleapis.com/token',
        ),
      );
      return val;
    });

Map<String, dynamic> _$FcmProviderConfigToJson(FcmProviderConfig instance) =>
    <String, dynamic>{
      'projectId': instance.projectId,
      'clientEmail': instance.clientEmail,
      'tokenUri': ?instance.tokenUri,
    };
