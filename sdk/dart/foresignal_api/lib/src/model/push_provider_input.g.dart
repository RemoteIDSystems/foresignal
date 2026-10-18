// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_provider_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushProviderInputCWProxy {
  PushProviderInput type(PushProviderInputTypeEnum type);

  PushProviderInput appIds(List<String> appIds);

  PushProviderInput config(PushProviderInputConfig config);

  PushProviderInput secrets(PushProviderInputSecrets? secrets);

  PushProviderInput enabled(bool? enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushProviderInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushProviderInput(...).copyWith(id: 12, name: "My name")
  /// ```
  PushProviderInput call({
    PushProviderInputTypeEnum type,
    List<String> appIds,
    PushProviderInputConfig config,
    PushProviderInputSecrets? secrets,
    bool? enabled,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushProviderInput.copyWith(...)` or call `instanceOfPushProviderInput.copyWith.fieldName(value)` for a single field.
class _$PushProviderInputCWProxyImpl implements _$PushProviderInputCWProxy {
  const _$PushProviderInputCWProxyImpl(this._value);

  final PushProviderInput _value;

  @override
  PushProviderInput type(PushProviderInputTypeEnum type) => call(type: type);

  @override
  PushProviderInput appIds(List<String> appIds) => call(appIds: appIds);

  @override
  PushProviderInput config(PushProviderInputConfig config) =>
      call(config: config);

  @override
  PushProviderInput secrets(PushProviderInputSecrets? secrets) =>
      call(secrets: secrets);

  @override
  PushProviderInput enabled(bool? enabled) => call(enabled: enabled);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushProviderInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushProviderInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushProviderInput call({
    Object? type = const $CopyWithPlaceholder(),
    Object? appIds = const $CopyWithPlaceholder(),
    Object? config = const $CopyWithPlaceholder(),
    Object? secrets = const $CopyWithPlaceholder(),
    Object? enabled = const $CopyWithPlaceholder(),
  }) {
    return PushProviderInput(
      type: type == const $CopyWithPlaceholder() || type == null
          ? _value.type
          // ignore: cast_nullable_to_non_nullable
          : type as PushProviderInputTypeEnum,
      appIds: appIds == const $CopyWithPlaceholder() || appIds == null
          ? _value.appIds
          // ignore: cast_nullable_to_non_nullable
          : appIds as List<String>,
      config: config == const $CopyWithPlaceholder() || config == null
          ? _value.config
          // ignore: cast_nullable_to_non_nullable
          : config as PushProviderInputConfig,
      secrets: secrets == const $CopyWithPlaceholder()
          ? _value.secrets
          // ignore: cast_nullable_to_non_nullable
          : secrets as PushProviderInputSecrets?,
      enabled: enabled == const $CopyWithPlaceholder()
          ? _value.enabled
          // ignore: cast_nullable_to_non_nullable
          : enabled as bool?,
    );
  }
}

extension $PushProviderInputCopyWith on PushProviderInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushProviderInput.copyWith(...)` or `instanceOfPushProviderInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushProviderInputCWProxy get copyWith =>
      _$PushProviderInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushProviderInput _$PushProviderInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PushProviderInput', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['type', 'appIds', 'config']);
      final val = PushProviderInput(
        type: $checkedConvert(
          'type',
          (v) => $enumDecode(_$PushProviderInputTypeEnumEnumMap, v),
        ),
        appIds: $checkedConvert(
          'appIds',
          (v) => (v as List<dynamic>).map((e) => e as String).toList(),
        ),
        config: $checkedConvert(
          'config',
          (v) => PushProviderInputConfig.fromJson(v as Map<String, dynamic>),
        ),
        secrets: $checkedConvert(
          'secrets',
          (v) => v == null
              ? null
              : PushProviderInputSecrets.fromJson(v as Map<String, dynamic>),
        ),
        enabled: $checkedConvert('enabled', (v) => v as bool? ?? true),
      );
      return val;
    });

Map<String, dynamic> _$PushProviderInputToJson(PushProviderInput instance) =>
    <String, dynamic>{
      'type': _$PushProviderInputTypeEnumEnumMap[instance.type]!,
      'appIds': instance.appIds,
      'config': instance.config.toJson(),
      'secrets': ?instance.secrets?.toJson(),
      'enabled': ?instance.enabled,
    };

const _$PushProviderInputTypeEnumEnumMap = {
  PushProviderInputTypeEnum.apns: 'apns',
  PushProviderInputTypeEnum.fcm: 'fcm',
};
