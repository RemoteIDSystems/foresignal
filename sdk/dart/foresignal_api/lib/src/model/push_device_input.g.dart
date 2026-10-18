// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_device_input.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushDeviceInputCWProxy {
  PushDeviceInput transport(PushDeviceInputTransportEnum transport);

  PushDeviceInput platform(PushDeviceInputPlatformEnum platform);

  PushDeviceInput appId(String appId);

  PushDeviceInput environment(PushDeviceInputEnvironmentEnum? environment);

  PushDeviceInput token(String token);

  PushDeviceInput name(String? name);

  PushDeviceInput filters(PushFilters? filters);

  PushDeviceInput contentLevel(PushDeviceInputContentLevelEnum? contentLevel);

  PushDeviceInput lang(String? lang);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushDeviceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushDeviceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  PushDeviceInput call({
    PushDeviceInputTransportEnum transport,
    PushDeviceInputPlatformEnum platform,
    String appId,
    PushDeviceInputEnvironmentEnum? environment,
    String token,
    String? name,
    PushFilters? filters,
    PushDeviceInputContentLevelEnum? contentLevel,
    String? lang,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushDeviceInput.copyWith(...)` or call `instanceOfPushDeviceInput.copyWith.fieldName(value)` for a single field.
class _$PushDeviceInputCWProxyImpl implements _$PushDeviceInputCWProxy {
  const _$PushDeviceInputCWProxyImpl(this._value);

  final PushDeviceInput _value;

  @override
  PushDeviceInput transport(PushDeviceInputTransportEnum transport) =>
      call(transport: transport);

  @override
  PushDeviceInput platform(PushDeviceInputPlatformEnum platform) =>
      call(platform: platform);

  @override
  PushDeviceInput appId(String appId) => call(appId: appId);

  @override
  PushDeviceInput environment(PushDeviceInputEnvironmentEnum? environment) =>
      call(environment: environment);

  @override
  PushDeviceInput token(String token) => call(token: token);

  @override
  PushDeviceInput name(String? name) => call(name: name);

  @override
  PushDeviceInput filters(PushFilters? filters) => call(filters: filters);

  @override
  PushDeviceInput contentLevel(PushDeviceInputContentLevelEnum? contentLevel) =>
      call(contentLevel: contentLevel);

  @override
  PushDeviceInput lang(String? lang) => call(lang: lang);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushDeviceInput(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushDeviceInput(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushDeviceInput call({
    Object? transport = const $CopyWithPlaceholder(),
    Object? platform = const $CopyWithPlaceholder(),
    Object? appId = const $CopyWithPlaceholder(),
    Object? environment = const $CopyWithPlaceholder(),
    Object? token = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? filters = const $CopyWithPlaceholder(),
    Object? contentLevel = const $CopyWithPlaceholder(),
    Object? lang = const $CopyWithPlaceholder(),
  }) {
    return PushDeviceInput(
      transport: transport == const $CopyWithPlaceholder() || transport == null
          ? _value.transport
          // ignore: cast_nullable_to_non_nullable
          : transport as PushDeviceInputTransportEnum,
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as PushDeviceInputPlatformEnum,
      appId: appId == const $CopyWithPlaceholder() || appId == null
          ? _value.appId
          // ignore: cast_nullable_to_non_nullable
          : appId as String,
      environment: environment == const $CopyWithPlaceholder()
          ? _value.environment
          // ignore: cast_nullable_to_non_nullable
          : environment as PushDeviceInputEnvironmentEnum?,
      token: token == const $CopyWithPlaceholder() || token == null
          ? _value.token
          // ignore: cast_nullable_to_non_nullable
          : token as String,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      filters: filters == const $CopyWithPlaceholder()
          ? _value.filters
          // ignore: cast_nullable_to_non_nullable
          : filters as PushFilters?,
      contentLevel: contentLevel == const $CopyWithPlaceholder()
          ? _value.contentLevel
          // ignore: cast_nullable_to_non_nullable
          : contentLevel as PushDeviceInputContentLevelEnum?,
      lang: lang == const $CopyWithPlaceholder()
          ? _value.lang
          // ignore: cast_nullable_to_non_nullable
          : lang as String?,
    );
  }
}

extension $PushDeviceInputCopyWith on PushDeviceInput {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushDeviceInput.copyWith(...)` or `instanceOfPushDeviceInput.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushDeviceInputCWProxy get copyWith => _$PushDeviceInputCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushDeviceInput _$PushDeviceInputFromJson(Map<String, dynamic> json) =>
    $checkedCreate('PushDeviceInput', json, ($checkedConvert) {
      $checkKeys(
        json,
        requiredKeys: const ['transport', 'platform', 'appId', 'token'],
      );
      final val = PushDeviceInput(
        transport: $checkedConvert(
          'transport',
          (v) => $enumDecode(_$PushDeviceInputTransportEnumEnumMap, v),
        ),
        platform: $checkedConvert(
          'platform',
          (v) => $enumDecode(_$PushDeviceInputPlatformEnumEnumMap, v),
        ),
        appId: $checkedConvert('appId', (v) => v as String),
        environment: $checkedConvert(
          'environment',
          (v) =>
              $enumDecodeNullable(_$PushDeviceInputEnvironmentEnumEnumMap, v),
        ),
        token: $checkedConvert('token', (v) => v as String),
        name: $checkedConvert('name', (v) => v as String?),
        filters: $checkedConvert(
          'filters',
          (v) => v == null
              ? null
              : PushFilters.fromJson(v as Map<String, dynamic>),
        ),
        contentLevel: $checkedConvert(
          'contentLevel',
          (v) =>
              $enumDecodeNullable(_$PushDeviceInputContentLevelEnumEnumMap, v),
        ),
        lang: $checkedConvert('lang', (v) => v as String?),
      );
      return val;
    });

Map<String, dynamic> _$PushDeviceInputToJson(
  PushDeviceInput instance,
) => <String, dynamic>{
  'transport': _$PushDeviceInputTransportEnumEnumMap[instance.transport]!,
  'platform': _$PushDeviceInputPlatformEnumEnumMap[instance.platform]!,
  'appId': instance.appId,
  'environment': ?_$PushDeviceInputEnvironmentEnumEnumMap[instance.environment],
  'token': instance.token,
  'name': ?instance.name,
  'filters': ?instance.filters?.toJson(),
  'contentLevel':
      ?_$PushDeviceInputContentLevelEnumEnumMap[instance.contentLevel],
  'lang': ?instance.lang,
};

const _$PushDeviceInputTransportEnumEnumMap = {
  PushDeviceInputTransportEnum.apns: 'apns',
  PushDeviceInputTransportEnum.fcm: 'fcm',
};

const _$PushDeviceInputPlatformEnumEnumMap = {
  PushDeviceInputPlatformEnum.ios: 'ios',
  PushDeviceInputPlatformEnum.ipados: 'ipados',
  PushDeviceInputPlatformEnum.macos: 'macos',
  PushDeviceInputPlatformEnum.android: 'android',
};

const _$PushDeviceInputEnvironmentEnumEnumMap = {
  PushDeviceInputEnvironmentEnum.sandbox: 'sandbox',
  PushDeviceInputEnvironmentEnum.production: 'production',
};

const _$PushDeviceInputContentLevelEnumEnumMap = {
  PushDeviceInputContentLevelEnum.minimal: 'minimal',
  PushDeviceInputContentLevelEnum.short: 'short',
};
