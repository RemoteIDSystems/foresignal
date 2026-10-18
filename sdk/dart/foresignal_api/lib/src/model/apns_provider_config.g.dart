// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'apns_provider_config.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$ApnsProviderConfigCWProxy {
  ApnsProviderConfig teamId(String teamId);

  ApnsProviderConfig keyId(String keyId);

  ApnsProviderConfig keyEnvironment(
    ApnsProviderConfigKeyEnvironmentEnum keyEnvironment,
  );

  ApnsProviderConfig timeSensitive(bool? timeSensitive);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ApnsProviderConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ApnsProviderConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  ApnsProviderConfig call({
    String teamId,
    String keyId,
    ApnsProviderConfigKeyEnvironmentEnum keyEnvironment,
    bool? timeSensitive,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfApnsProviderConfig.copyWith(...)` or call `instanceOfApnsProviderConfig.copyWith.fieldName(value)` for a single field.
class _$ApnsProviderConfigCWProxyImpl implements _$ApnsProviderConfigCWProxy {
  const _$ApnsProviderConfigCWProxyImpl(this._value);

  final ApnsProviderConfig _value;

  @override
  ApnsProviderConfig teamId(String teamId) => call(teamId: teamId);

  @override
  ApnsProviderConfig keyId(String keyId) => call(keyId: keyId);

  @override
  ApnsProviderConfig keyEnvironment(
    ApnsProviderConfigKeyEnvironmentEnum keyEnvironment,
  ) => call(keyEnvironment: keyEnvironment);

  @override
  ApnsProviderConfig timeSensitive(bool? timeSensitive) =>
      call(timeSensitive: timeSensitive);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `ApnsProviderConfig(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// ApnsProviderConfig(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  ApnsProviderConfig call({
    Object? teamId = const $CopyWithPlaceholder(),
    Object? keyId = const $CopyWithPlaceholder(),
    Object? keyEnvironment = const $CopyWithPlaceholder(),
    Object? timeSensitive = const $CopyWithPlaceholder(),
  }) {
    return ApnsProviderConfig(
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
          : keyEnvironment as ApnsProviderConfigKeyEnvironmentEnum,
      timeSensitive: timeSensitive == const $CopyWithPlaceholder()
          ? _value.timeSensitive
          // ignore: cast_nullable_to_non_nullable
          : timeSensitive as bool?,
    );
  }
}

extension $ApnsProviderConfigCopyWith on ApnsProviderConfig {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfApnsProviderConfig.copyWith(...)` or `instanceOfApnsProviderConfig.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$ApnsProviderConfigCWProxy get copyWith =>
      _$ApnsProviderConfigCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

ApnsProviderConfig _$ApnsProviderConfigFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('ApnsProviderConfig', json, ($checkedConvert) {
  $checkKeys(json, requiredKeys: const ['teamId', 'keyId', 'keyEnvironment']);
  final val = ApnsProviderConfig(
    teamId: $checkedConvert('teamId', (v) => v as String),
    keyId: $checkedConvert('keyId', (v) => v as String),
    keyEnvironment: $checkedConvert(
      'keyEnvironment',
      (v) => $enumDecode(_$ApnsProviderConfigKeyEnvironmentEnumEnumMap, v),
    ),
    timeSensitive: $checkedConvert('timeSensitive', (v) => v as bool? ?? false),
  );
  return val;
});

Map<String, dynamic> _$ApnsProviderConfigToJson(
  ApnsProviderConfig instance,
) => <String, dynamic>{
  'teamId': instance.teamId,
  'keyId': instance.keyId,
  'keyEnvironment':
      _$ApnsProviderConfigKeyEnvironmentEnumEnumMap[instance.keyEnvironment]!,
  'timeSensitive': ?instance.timeSensitive,
};

const _$ApnsProviderConfigKeyEnvironmentEnumEnumMap = {
  ApnsProviderConfigKeyEnvironmentEnum.sandbox: 'sandbox',
  ApnsProviderConfigKeyEnvironmentEnum.production: 'production',
  ApnsProviderConfigKeyEnvironmentEnum.both: 'both',
};
