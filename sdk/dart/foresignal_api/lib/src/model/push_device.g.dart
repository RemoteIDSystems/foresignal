// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'push_device.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$PushDeviceCWProxy {
  PushDevice id(String id);

  PushDevice transport(PushDeviceTransportEnum transport);

  PushDevice platform(PushDevicePlatformEnum platform);

  PushDevice appId(String appId);

  PushDevice environment(PushDeviceEnvironmentEnum? environment);

  PushDevice tokenSuffix(String tokenSuffix);

  PushDevice tokenId(String? tokenId);

  PushDevice name(String? name);

  PushDevice deviceName(String? deviceName);

  PushDevice appVersion(String? appVersion);

  PushDevice filters(PushFilters? filters);

  PushDevice contentLevel(PushDeviceContentLevelEnum? contentLevel);

  PushDevice lang(String? lang);

  PushDevice status(PushDeviceStatusEnum status);

  PushDevice createdAt(DateTime createdAt);

  PushDevice lastSuccessAt(DateTime? lastSuccessAt);

  PushDevice lastFailureAt(DateTime? lastFailureAt);

  PushDevice failureCount(int? failureCount);

  PushDevice lastStatus(int? lastStatus);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushDevice(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushDevice(...).copyWith(id: 12, name: "My name")
  /// ```
  PushDevice call({
    String id,
    PushDeviceTransportEnum transport,
    PushDevicePlatformEnum platform,
    String appId,
    PushDeviceEnvironmentEnum? environment,
    String tokenSuffix,
    String? tokenId,
    String? name,
    String? deviceName,
    String? appVersion,
    PushFilters? filters,
    PushDeviceContentLevelEnum? contentLevel,
    String? lang,
    PushDeviceStatusEnum status,
    DateTime createdAt,
    DateTime? lastSuccessAt,
    DateTime? lastFailureAt,
    int? failureCount,
    int? lastStatus,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfPushDevice.copyWith(...)` or call `instanceOfPushDevice.copyWith.fieldName(value)` for a single field.
class _$PushDeviceCWProxyImpl implements _$PushDeviceCWProxy {
  const _$PushDeviceCWProxyImpl(this._value);

  final PushDevice _value;

  @override
  PushDevice id(String id) => call(id: id);

  @override
  PushDevice transport(PushDeviceTransportEnum transport) =>
      call(transport: transport);

  @override
  PushDevice platform(PushDevicePlatformEnum platform) =>
      call(platform: platform);

  @override
  PushDevice appId(String appId) => call(appId: appId);

  @override
  PushDevice environment(PushDeviceEnvironmentEnum? environment) =>
      call(environment: environment);

  @override
  PushDevice tokenSuffix(String tokenSuffix) => call(tokenSuffix: tokenSuffix);

  @override
  PushDevice tokenId(String? tokenId) => call(tokenId: tokenId);

  @override
  PushDevice name(String? name) => call(name: name);

  @override
  PushDevice deviceName(String? deviceName) => call(deviceName: deviceName);

  @override
  PushDevice appVersion(String? appVersion) => call(appVersion: appVersion);

  @override
  PushDevice filters(PushFilters? filters) => call(filters: filters);

  @override
  PushDevice contentLevel(PushDeviceContentLevelEnum? contentLevel) =>
      call(contentLevel: contentLevel);

  @override
  PushDevice lang(String? lang) => call(lang: lang);

  @override
  PushDevice status(PushDeviceStatusEnum status) => call(status: status);

  @override
  PushDevice createdAt(DateTime createdAt) => call(createdAt: createdAt);

  @override
  PushDevice lastSuccessAt(DateTime? lastSuccessAt) =>
      call(lastSuccessAt: lastSuccessAt);

  @override
  PushDevice lastFailureAt(DateTime? lastFailureAt) =>
      call(lastFailureAt: lastFailureAt);

  @override
  PushDevice failureCount(int? failureCount) =>
      call(failureCount: failureCount);

  @override
  PushDevice lastStatus(int? lastStatus) => call(lastStatus: lastStatus);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `PushDevice(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// PushDevice(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  PushDevice call({
    Object? id = const $CopyWithPlaceholder(),
    Object? transport = const $CopyWithPlaceholder(),
    Object? platform = const $CopyWithPlaceholder(),
    Object? appId = const $CopyWithPlaceholder(),
    Object? environment = const $CopyWithPlaceholder(),
    Object? tokenSuffix = const $CopyWithPlaceholder(),
    Object? tokenId = const $CopyWithPlaceholder(),
    Object? name = const $CopyWithPlaceholder(),
    Object? deviceName = const $CopyWithPlaceholder(),
    Object? appVersion = const $CopyWithPlaceholder(),
    Object? filters = const $CopyWithPlaceholder(),
    Object? contentLevel = const $CopyWithPlaceholder(),
    Object? lang = const $CopyWithPlaceholder(),
    Object? status = const $CopyWithPlaceholder(),
    Object? createdAt = const $CopyWithPlaceholder(),
    Object? lastSuccessAt = const $CopyWithPlaceholder(),
    Object? lastFailureAt = const $CopyWithPlaceholder(),
    Object? failureCount = const $CopyWithPlaceholder(),
    Object? lastStatus = const $CopyWithPlaceholder(),
  }) {
    return PushDevice(
      id: id == const $CopyWithPlaceholder() || id == null
          ? _value.id
          // ignore: cast_nullable_to_non_nullable
          : id as String,
      transport: transport == const $CopyWithPlaceholder() || transport == null
          ? _value.transport
          // ignore: cast_nullable_to_non_nullable
          : transport as PushDeviceTransportEnum,
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as PushDevicePlatformEnum,
      appId: appId == const $CopyWithPlaceholder() || appId == null
          ? _value.appId
          // ignore: cast_nullable_to_non_nullable
          : appId as String,
      environment: environment == const $CopyWithPlaceholder()
          ? _value.environment
          // ignore: cast_nullable_to_non_nullable
          : environment as PushDeviceEnvironmentEnum?,
      tokenSuffix:
          tokenSuffix == const $CopyWithPlaceholder() || tokenSuffix == null
          ? _value.tokenSuffix
          // ignore: cast_nullable_to_non_nullable
          : tokenSuffix as String,
      tokenId: tokenId == const $CopyWithPlaceholder()
          ? _value.tokenId
          // ignore: cast_nullable_to_non_nullable
          : tokenId as String?,
      name: name == const $CopyWithPlaceholder()
          ? _value.name
          // ignore: cast_nullable_to_non_nullable
          : name as String?,
      deviceName: deviceName == const $CopyWithPlaceholder()
          ? _value.deviceName
          // ignore: cast_nullable_to_non_nullable
          : deviceName as String?,
      appVersion: appVersion == const $CopyWithPlaceholder()
          ? _value.appVersion
          // ignore: cast_nullable_to_non_nullable
          : appVersion as String?,
      filters: filters == const $CopyWithPlaceholder()
          ? _value.filters
          // ignore: cast_nullable_to_non_nullable
          : filters as PushFilters?,
      contentLevel: contentLevel == const $CopyWithPlaceholder()
          ? _value.contentLevel
          // ignore: cast_nullable_to_non_nullable
          : contentLevel as PushDeviceContentLevelEnum?,
      lang: lang == const $CopyWithPlaceholder()
          ? _value.lang
          // ignore: cast_nullable_to_non_nullable
          : lang as String?,
      status: status == const $CopyWithPlaceholder() || status == null
          ? _value.status
          // ignore: cast_nullable_to_non_nullable
          : status as PushDeviceStatusEnum,
      createdAt: createdAt == const $CopyWithPlaceholder() || createdAt == null
          ? _value.createdAt
          // ignore: cast_nullable_to_non_nullable
          : createdAt as DateTime,
      lastSuccessAt: lastSuccessAt == const $CopyWithPlaceholder()
          ? _value.lastSuccessAt
          // ignore: cast_nullable_to_non_nullable
          : lastSuccessAt as DateTime?,
      lastFailureAt: lastFailureAt == const $CopyWithPlaceholder()
          ? _value.lastFailureAt
          // ignore: cast_nullable_to_non_nullable
          : lastFailureAt as DateTime?,
      failureCount: failureCount == const $CopyWithPlaceholder()
          ? _value.failureCount
          // ignore: cast_nullable_to_non_nullable
          : failureCount as int?,
      lastStatus: lastStatus == const $CopyWithPlaceholder()
          ? _value.lastStatus
          // ignore: cast_nullable_to_non_nullable
          : lastStatus as int?,
    );
  }
}

extension $PushDeviceCopyWith on PushDevice {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfPushDevice.copyWith(...)` or `instanceOfPushDevice.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$PushDeviceCWProxy get copyWith => _$PushDeviceCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

PushDevice _$PushDeviceFromJson(
  Map<String, dynamic> json,
) => $checkedCreate('PushDevice', json, ($checkedConvert) {
  $checkKeys(
    json,
    requiredKeys: const [
      'id',
      'transport',
      'platform',
      'appId',
      'tokenSuffix',
      'status',
      'createdAt',
    ],
  );
  final val = PushDevice(
    id: $checkedConvert('id', (v) => v as String),
    transport: $checkedConvert(
      'transport',
      (v) => $enumDecode(_$PushDeviceTransportEnumEnumMap, v),
    ),
    platform: $checkedConvert(
      'platform',
      (v) => $enumDecode(_$PushDevicePlatformEnumEnumMap, v),
    ),
    appId: $checkedConvert('appId', (v) => v as String),
    environment: $checkedConvert(
      'environment',
      (v) => $enumDecodeNullable(_$PushDeviceEnvironmentEnumEnumMap, v),
    ),
    tokenSuffix: $checkedConvert('tokenSuffix', (v) => v as String),
    tokenId: $checkedConvert('tokenId', (v) => v as String?),
    name: $checkedConvert('name', (v) => v as String?),
    deviceName: $checkedConvert('deviceName', (v) => v as String?),
    appVersion: $checkedConvert('appVersion', (v) => v as String?),
    filters: $checkedConvert(
      'filters',
      (v) => v == null ? null : PushFilters.fromJson(v as Map<String, dynamic>),
    ),
    contentLevel: $checkedConvert(
      'contentLevel',
      (v) => $enumDecodeNullable(_$PushDeviceContentLevelEnumEnumMap, v),
    ),
    lang: $checkedConvert('lang', (v) => v as String?),
    status: $checkedConvert(
      'status',
      (v) => $enumDecode(_$PushDeviceStatusEnumEnumMap, v),
    ),
    createdAt: $checkedConvert('createdAt', (v) => DateTime.parse(v as String)),
    lastSuccessAt: $checkedConvert(
      'lastSuccessAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    lastFailureAt: $checkedConvert(
      'lastFailureAt',
      (v) => v == null ? null : DateTime.parse(v as String),
    ),
    failureCount: $checkedConvert('failureCount', (v) => (v as num?)?.toInt()),
    lastStatus: $checkedConvert('lastStatus', (v) => (v as num?)?.toInt()),
  );
  return val;
});

Map<String, dynamic> _$PushDeviceToJson(
  PushDevice instance,
) => <String, dynamic>{
  'id': instance.id,
  'transport': _$PushDeviceTransportEnumEnumMap[instance.transport]!,
  'platform': _$PushDevicePlatformEnumEnumMap[instance.platform]!,
  'appId': instance.appId,
  'environment': ?_$PushDeviceEnvironmentEnumEnumMap[instance.environment],
  'tokenSuffix': instance.tokenSuffix,
  'tokenId': ?instance.tokenId,
  'name': ?instance.name,
  'deviceName': ?instance.deviceName,
  'appVersion': ?instance.appVersion,
  'filters': ?instance.filters?.toJson(),
  'contentLevel': ?_$PushDeviceContentLevelEnumEnumMap[instance.contentLevel],
  'lang': ?instance.lang,
  'status': _$PushDeviceStatusEnumEnumMap[instance.status]!,
  'createdAt': instance.createdAt.toIso8601String(),
  'lastSuccessAt': ?instance.lastSuccessAt?.toIso8601String(),
  'lastFailureAt': ?instance.lastFailureAt?.toIso8601String(),
  'failureCount': ?instance.failureCount,
  'lastStatus': ?instance.lastStatus,
};

const _$PushDeviceTransportEnumEnumMap = {
  PushDeviceTransportEnum.apns: 'apns',
  PushDeviceTransportEnum.fcm: 'fcm',
};

const _$PushDevicePlatformEnumEnumMap = {
  PushDevicePlatformEnum.ios: 'ios',
  PushDevicePlatformEnum.ipados: 'ipados',
  PushDevicePlatformEnum.macos: 'macos',
  PushDevicePlatformEnum.android: 'android',
};

const _$PushDeviceEnvironmentEnumEnumMap = {
  PushDeviceEnvironmentEnum.sandbox: 'sandbox',
  PushDeviceEnvironmentEnum.production: 'production',
};

const _$PushDeviceContentLevelEnumEnumMap = {
  PushDeviceContentLevelEnum.minimal: 'minimal',
  PushDeviceContentLevelEnum.short: 'short',
};

const _$PushDeviceStatusEnumEnumMap = {
  PushDeviceStatusEnum.active: 'active',
  PushDeviceStatusEnum.disabled: 'disabled',
};
