// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_provider_input_config.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushProviderInputConfigCWProxy {
  PushProviderInputConfig teamId(String teamId);

  PushProviderInputConfig keyId(String keyId);

  PushProviderInputConfig keyEnvironment(
    PushProviderInputConfigKeyEnvironmentEnum keyEnvironment,
  );

  PushProviderInputConfig timeSensitive(bool? timeSensitive);

  PushProviderInputConfig projectId(String projectId);

  PushProviderInputConfig clientEmail(String clientEmail);

  PushProviderInputConfig tokenUri(String? tokenUri);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushProviderInputConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushProviderInputConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  PushProviderInputConfig call({
    String teamId,
    String keyId,
    PushProviderInputConfigKeyEnvironmentEnum keyEnvironment,
    bool? timeSensitive,
    String projectId,
    String clientEmail,
    String? tokenUri,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushProviderInputConfig.copyWith(...)` or call `instanceOfPushProviderInputConfig.copyWith.fieldName(value)` for a single field.
class _$PushProviderInputConfigCWProxyImpl
    implements _$PushProviderInputConfigCWProxy {
  const _$PushProviderInputConfigCWProxyImpl(this._value);

  final PushProviderInputConfig _value;

  @override
  PushProviderInputConfig teamId(String teamId) => call(teamId: teamId);

  @override
  PushProviderInputConfig keyId(String keyId) => call(keyId: keyId);

  @override
  PushProviderInputConfig keyEnvironment(
    PushProviderInputConfigKeyEnvironmentEnum keyEnvironment,
  ) => call(keyEnvironment: keyEnvironment);

  @override
  PushProviderInputConfig timeSensitive(bool? timeSensitive) =>
      call(timeSensitive: timeSensitive);

  @override
  PushProviderInputConfig projectId(String projectId) =>
      call(projectId: projectId);

  @override
  PushProviderInputConfig clientEmail(String clientEmail) =>
      call(clientEmail: clientEmail);

  @override
  PushProviderInputConfig tokenUri(String? tokenUri) =>
      call(tokenUri: tokenUri);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushProviderInputConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushProviderInputConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushProviderInputConfig call({
    Object? teamId = const $CopyWithPlaceholder(),
    Object? keyId = const $CopyWithPlaceholder(),
    Object? keyEnvironment = const $CopyWithPlaceholder(),
    Object? timeSensitive = const $CopyWithPlaceholder(),
    Object? projectId = const $CopyWithPlaceholder(),
    Object? clientEmail = const $CopyWithPlaceholder(),
    Object? tokenUri = const $CopyWithPlaceholder(),
  }) {
    return PushProviderInputConfig(
      teamId: teamId == const $CopyWithPlaceholder() || teamId == null
          ? _value.teamId
          // ignore: cast_nullable_to_non_nullable
          : teamId as String,
      keyId: keyId == const $CopyWithPlaceholder() || keyId == null
          ? _value.keyId
          // ignore: cast_nullable_to_non_nullable
          : keyId as String,
      keyEnvironment:
          keyEnvironment == const $CopyWithPlaceholder() ||
              keyEnvironment == null
          ? _value.keyEnvironment
          // ignore: cast_nullable_to_non_nullable
          : keyEnvironment as PushProviderInputConfigKeyEnvironmentEnum,
      timeSensitive: timeSensitive == const $CopyWithPlaceholder()
          ? _value.timeSensitive
          // ignore: cast_nullable_to_non_nullable
          : timeSensitive as bool?,
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

extension $PushProviderInputConfigCopyWith on PushProviderInputConfig {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushProviderInputConfig.copyWith(...)` or `instanceOfPushProviderInputConfig.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushProviderInputConfigCWProxy get copyWith =>
      _$PushProviderInputConfigCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushProviderInputConfig _$PushProviderInputConfigFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PushProviderInputConfig', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'teamId',
      'keyId',
      'keyEnvironment',
      'projectId',
      'clientEmail',
    ],
  );
  final val = PushProviderInputConfig(
    teamId: $checkedConvert('teamId', (v) => v as String),
    keyId: $checkedConvert('keyId', (v) => v as String),
    keyEnvironment: $checkedConvert(
      'keyEnvironment',
      (v) => $enumDecode(_$PushProviderInputConfigKeyEnvironmentEnumEnumMap, v),
    ),
    timeSensitive: $checkedConvert('timeSensitive', (v) => v as bool? ?? false),
    projectId: $checkedConvert('projectId', (v) => v as String),
    clientEmail: $checkedConvert('clientEmail', (v) => v as String),
    tokenUri: $checkedConvert(
      'tokenUri',
      (v) => v as String? ?? 'https://oauth2.googleapis.com/token',
    ),
  );
  return val;
});

Map<String, dynamic> _$PushProviderInputConfigToJson(
  PushProviderInputConfig instance,
) => <String, dynamic>{
  'teamId': instance.teamId,
  'keyId': instance.keyId,
  'keyEnvironment':
      _$PushProviderInputConfigKeyEnvironmentEnumEnumMap[instance
          .keyEnvironment]!,
  'timeSensitive': ?instance.timeSensitive,
  'projectId': instance.projectId,
  'clientEmail': instance.clientEmail,
  'tokenUri': ?instance.tokenUri,
};

const _$PushProviderInputConfigKeyEnvironmentEnumEnumMap = {
  PushProviderInputConfigKeyEnvironmentEnum.sandbox: 'sandbox',
  PushProviderInputConfigKeyEnvironmentEnum.production: 'production',
  PushProviderInputConfigKeyEnvironmentEnum.both: 'both',
};
