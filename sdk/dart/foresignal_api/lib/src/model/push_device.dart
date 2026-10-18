//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/push_filters.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_device.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushDevice {
  /// Returns a new [PushDevice] instance.
  PushDevice({

    required  this.id,

    required  this.transport,

    required  this.platform,

    required  this.appId,

     this.environment,

    required  this.tokenSuffix,

     this.tokenId,

     this.name,

     this.deviceName,

     this.appVersion,

     this.filters,

     this.contentLevel,

     this.lang,

    required  this.status,

    required  this.createdAt,

     this.lastSuccessAt,

     this.lastFailureAt,

     this.failureCount,

     this.lastStatus,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'transport',
    required: true,
    includeIfNull: false,
  )


  final PushDeviceTransportEnum transport;



  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final PushDevicePlatformEnum platform;



  @JsonKey(
    
    name: r'appId',
    required: true,
    includeIfNull: false,
  )


  final String appId;



  @JsonKey(
    
    name: r'environment',
    required: false,
    includeIfNull: false,
  )


  final PushDeviceEnvironmentEnum? environment;



      /// Last 6 characters of the device token
  @JsonKey(
    
    name: r'tokenSuffix',
    required: true,
    includeIfNull: false,
  )


  final String tokenSuffix;



      /// API token that registered the device
  @JsonKey(
    
    name: r'tokenId',
    required: false,
    includeIfNull: false,
  )


  final String? tokenId;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'deviceName',
    required: false,
    includeIfNull: false,
  )


  final String? deviceName;



  @JsonKey(
    
    name: r'appVersion',
    required: false,
    includeIfNull: false,
  )


  final String? appVersion;



  @JsonKey(
    
    name: r'filters',
    required: false,
    includeIfNull: false,
  )


  final PushFilters? filters;



  @JsonKey(
    
    name: r'contentLevel',
    required: false,
    includeIfNull: false,
  )


  final PushDeviceContentLevelEnum? contentLevel;



  @JsonKey(
    
    name: r'lang',
    required: false,
    includeIfNull: false,
  )


  final String? lang;



      /// 032 never disables a device by itself: dead tokens are deleted. `disabled` is reserved.
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final PushDeviceStatusEnum status;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'lastSuccessAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastSuccessAt;



  @JsonKey(
    
    name: r'lastFailureAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastFailureAt;



  @JsonKey(
    
    name: r'failureCount',
    required: false,
    includeIfNull: false,
  )


  final int? failureCount;



  @JsonKey(
    
    name: r'lastStatus',
    required: false,
    includeIfNull: false,
  )


  final int? lastStatus;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushDevice &&
      other.id == id &&
      other.transport == transport &&
      other.platform == platform &&
      other.appId == appId &&
      other.environment == environment &&
      other.tokenSuffix == tokenSuffix &&
      other.tokenId == tokenId &&
      other.name == name &&
      other.deviceName == deviceName &&
      other.appVersion == appVersion &&
      other.filters == filters &&
      other.contentLevel == contentLevel &&
      other.lang == lang &&
      other.status == status &&
      other.createdAt == createdAt &&
      other.lastSuccessAt == lastSuccessAt &&
      other.lastFailureAt == lastFailureAt &&
      other.failureCount == failureCount &&
      other.lastStatus == lastStatus;

    @override
    int get hashCode =>
        id.hashCode +
        transport.hashCode +
        platform.hashCode +
        appId.hashCode +
        (environment == null ? 0 : environment.hashCode) +
        tokenSuffix.hashCode +
        tokenId.hashCode +
        (name == null ? 0 : name.hashCode) +
        (deviceName == null ? 0 : deviceName.hashCode) +
        (appVersion == null ? 0 : appVersion.hashCode) +
        filters.hashCode +
        contentLevel.hashCode +
        (lang == null ? 0 : lang.hashCode) +
        status.hashCode +
        createdAt.hashCode +
        (lastSuccessAt == null ? 0 : lastSuccessAt.hashCode) +
        (lastFailureAt == null ? 0 : lastFailureAt.hashCode) +
        failureCount.hashCode +
        (lastStatus == null ? 0 : lastStatus.hashCode);

  factory PushDevice.fromJson(Map<String, dynamic> json) => _$PushDeviceFromJson(json);

  Map<String, dynamic> toJson() => _$PushDeviceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PushDeviceTransportEnum {
@JsonValue(r'apns')
apns(r'apns'),
@JsonValue(r'fcm')
fcm(r'fcm');

const PushDeviceTransportEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PushDevicePlatformEnum {
@JsonValue(r'ios')
ios(r'ios'),
@JsonValue(r'ipados')
ipados(r'ipados'),
@JsonValue(r'macos')
macos(r'macos'),
@JsonValue(r'android')
android(r'android');

const PushDevicePlatformEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PushDeviceEnvironmentEnum {
@JsonValue(r'sandbox')
sandbox(r'sandbox'),
@JsonValue(r'production')
production(r'production');

const PushDeviceEnvironmentEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PushDeviceContentLevelEnum {
@JsonValue(r'minimal')
minimal(r'minimal'),
@JsonValue(r'short')
short(r'short');

const PushDeviceContentLevelEnum(this.value);

final String value;

@override
String toString() => value;
}


/// 032 never disables a device by itself: dead tokens are deleted. `disabled` is reserved.
enum PushDeviceStatusEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'disabled')
disabled(r'disabled');

const PushDeviceStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


