//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'device_info.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DeviceInfo {
  /// Returns a new [DeviceInfo] instance.
  DeviceInfo({

    required  this.deviceName,

    required  this.platform,

     this.appVersion,

     this.reportedAt,
  });

  @JsonKey(
    
    name: r'deviceName',
    required: true,
    includeIfNull: false,
  )


  final String deviceName;



  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final DeviceInfoPlatformEnum platform;



  @JsonKey(
    
    name: r'appVersion',
    required: false,
    includeIfNull: false,
  )


  final String? appVersion;



  @JsonKey(
    
    name: r'reportedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? reportedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DeviceInfo &&
      other.deviceName == deviceName &&
      other.platform == platform &&
      other.appVersion == appVersion &&
      other.reportedAt == reportedAt;

    @override
    int get hashCode =>
        deviceName.hashCode +
        platform.hashCode +
        (appVersion == null ? 0 : appVersion.hashCode) +
        (reportedAt == null ? 0 : reportedAt.hashCode);

  factory DeviceInfo.fromJson(Map<String, dynamic> json) => _$DeviceInfoFromJson(json);

  Map<String, dynamic> toJson() => _$DeviceInfoToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DeviceInfoPlatformEnum {
@JsonValue(r'ios')
ios(r'ios'),
@JsonValue(r'android')
android(r'android'),
@JsonValue(r'macos')
macos(r'macos'),
@JsonValue(r'windows')
windows(r'windows'),
@JsonValue(r'linux')
linux(r'linux'),
@JsonValue(r'web')
web(r'web'),
@JsonValue(r'other')
other(r'other');

const DeviceInfoPlatformEnum(this.value);

final String value;

@override
String toString() => value;
}


