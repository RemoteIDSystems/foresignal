// GENERATED CODE - DO NOT MODIFY BY HAND

part of 'device_info.dart';

// **************************************************************************
// CopyWithGenerator
// **************************************************************************

abstract class _$DeviceInfoCWProxy {
  DeviceInfo deviceName(String deviceName);

  DeviceInfo platform(DeviceInfoPlatformEnum platform);

  DeviceInfo appVersion(String? appVersion);

  DeviceInfo reportedAt(DateTime? reportedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeviceInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeviceInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  DeviceInfo call({
    String deviceName,
    DeviceInfoPlatformEnum platform,
    String? appVersion,
    DateTime? reportedAt,
  });
}

/// Callable proxy for `copyWith` functionality.
/// Use as `instanceOfDeviceInfo.copyWith(...)` or call `instanceOfDeviceInfo.copyWith.fieldName(value)` for a single field.
class _$DeviceInfoCWProxyImpl implements _$DeviceInfoCWProxy {
  const _$DeviceInfoCWProxyImpl(this._value);

  final DeviceInfo _value;

  @override
  DeviceInfo deviceName(String deviceName) => call(deviceName: deviceName);

  @override
  DeviceInfo platform(DeviceInfoPlatformEnum platform) =>
      call(platform: platform);

  @override
  DeviceInfo appVersion(String? appVersion) => call(appVersion: appVersion);

  @override
  DeviceInfo reportedAt(DateTime? reportedAt) => call(reportedAt: reportedAt);

  /// Creates a new instance with the provided field values.
  /// Omitted fields keep their values; explicit `null` clears nullable fields.
  /// The public API rejects `null` for non-nullable fields. To update a single field use `DeviceInfo(...).copyWith.fieldName(value)`.
  ///
  /// Example:
  /// ```dart
  /// DeviceInfo(...).copyWith(id: 12, name: "My name")
  /// ```
  @override
  DeviceInfo call({
    Object? deviceName = const $CopyWithPlaceholder(),
    Object? platform = const $CopyWithPlaceholder(),
    Object? appVersion = const $CopyWithPlaceholder(),
    Object? reportedAt = const $CopyWithPlaceholder(),
  }) {
    return DeviceInfo(
      deviceName:
          deviceName == const $CopyWithPlaceholder() || deviceName == null
          ? _value.deviceName
          // ignore: cast_nullable_to_non_nullable
          : deviceName as String,
      platform: platform == const $CopyWithPlaceholder() || platform == null
          ? _value.platform
          // ignore: cast_nullable_to_non_nullable
          : platform as DeviceInfoPlatformEnum,
      appVersion: appVersion == const $CopyWithPlaceholder()
          ? _value.appVersion
          // ignore: cast_nullable_to_non_nullable
          : appVersion as String?,
      reportedAt: reportedAt == const $CopyWithPlaceholder()
          ? _value.reportedAt
          // ignore: cast_nullable_to_non_nullable
          : reportedAt as DateTime?,
    );
  }
}

extension $DeviceInfoCopyWith on DeviceInfo {
  /// Returns a callable class used to build a new instance with modified fields.
  /// Example: `instanceOfDeviceInfo.copyWith(...)` or `instanceOfDeviceInfo.copyWith.fieldName(...)`.
  // ignore: library_private_types_in_public_api
  _$DeviceInfoCWProxy get copyWith => _$DeviceInfoCWProxyImpl(this);
}

// **************************************************************************
// JsonSerializableGenerator
// **************************************************************************

DeviceInfo _$DeviceInfoFromJson(Map<String, dynamic> json) =>
    $checkedCreate('DeviceInfo', json, ($checkedConvert) {
      $checkKeys(json, requiredKeys: const ['deviceName', 'platform']);
      final val = DeviceInfo(
        deviceName: $checkedConvert('deviceName', (v) => v as String),
        platform: $checkedConvert(
          'platform',
          (v) => $enumDecode(_$DeviceInfoPlatformEnumEnumMap, v),
        ),
        appVersion: $checkedConvert('appVersion', (v) => v as String?),
        reportedAt: $checkedConvert(
          'reportedAt',
          (v) => v == null ? null : DateTime.parse(v as String),
        ),
      );
      return val;
    });

Map<String, dynamic> _$DeviceInfoToJson(DeviceInfo instance) =>
    <String, dynamic>{
      'deviceName': instance.deviceName,
      'platform': _$DeviceInfoPlatformEnumEnumMap[instance.platform]!,
      'appVersion': ?instance.appVersion,
      'reportedAt': ?instance.reportedAt?.toIso8601String(),
    };

const _$DeviceInfoPlatformEnumEnumMap = {
  DeviceInfoPlatformEnum.ios: 'ios',
  DeviceInfoPlatformEnum.android: 'android',
  DeviceInfoPlatformEnum.macos: 'macos',
  DeviceInfoPlatformEnum.windows: 'windows',
  DeviceInfoPlatformEnum.linux: 'linux',
  DeviceInfoPlatformEnum.web: 'web',
  DeviceInfoPlatformEnum.other: 'other',
};
