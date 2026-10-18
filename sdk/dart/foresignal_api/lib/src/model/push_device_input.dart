//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/push_filters.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_device_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushDeviceInput {
  /// Returns a new [PushDeviceInput] instance.
  PushDeviceInput({

    required  this.transport,

    required  this.platform,

    required  this.appId,

     this.environment,

    required  this.token,

     this.name,

     this.filters,

     this.contentLevel,

     this.lang,
  });

  @JsonKey(
    
    name: r'transport',
    required: true,
    includeIfNull: false,
  )


  final PushDeviceInputTransportEnum transport;



  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final PushDeviceInputPlatformEnum platform;



  @JsonKey(
    
    name: r'appId',
    required: true,
    includeIfNull: false,
  )


  final String appId;



      /// APNs only; must be covered by the provider's keyEnvironment
  @JsonKey(
    
    name: r'environment',
    required: false,
    includeIfNull: false,
  )


  final PushDeviceInputEnvironmentEnum? environment;



      /// APNs: 64–200 hex chars; FCM: 1–4096 chars
  @JsonKey(
    
    name: r'token',
    required: true,
    includeIfNull: false,
  )


  final String token;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'filters',
    required: false,
    includeIfNull: false,
  )


  final PushFilters? filters;



      /// minimal: no alert text through Apple/Google; short: shortTitle (≤ 40 chars)
  @JsonKey(
    
    name: r'contentLevel',
    required: false,
    includeIfNull: false,
  )


  final PushDeviceInputContentLevelEnum? contentLevel;



  @JsonKey(
    
    name: r'lang',
    required: false,
    includeIfNull: false,
  )


  final String? lang;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushDeviceInput &&
      other.transport == transport &&
      other.platform == platform &&
      other.appId == appId &&
      other.environment == environment &&
      other.token == token &&
      other.name == name &&
      other.filters == filters &&
      other.contentLevel == contentLevel &&
      other.lang == lang;

    @override
    int get hashCode =>
        transport.hashCode +
        platform.hashCode +
        appId.hashCode +
        (environment == null ? 0 : environment.hashCode) +
        token.hashCode +
        (name == null ? 0 : name.hashCode) +
        filters.hashCode +
        contentLevel.hashCode +
        (lang == null ? 0 : lang.hashCode);

  factory PushDeviceInput.fromJson(Map<String, dynamic> json) => _$PushDeviceInputFromJson(json);

  Map<String, dynamic> toJson() => _$PushDeviceInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PushDeviceInputTransportEnum {
@JsonValue(r'apns')
apns(r'apns'),
@JsonValue(r'fcm')
fcm(r'fcm');

const PushDeviceInputTransportEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PushDeviceInputPlatformEnum {
@JsonValue(r'ios')
ios(r'ios'),
@JsonValue(r'ipados')
ipados(r'ipados'),
@JsonValue(r'macos')
macos(r'macos'),
@JsonValue(r'android')
android(r'android');

const PushDeviceInputPlatformEnum(this.value);

final String value;

@override
String toString() => value;
}


/// APNs only; must be covered by the provider's keyEnvironment
enum PushDeviceInputEnvironmentEnum {
@JsonValue(r'sandbox')
sandbox(r'sandbox'),
@JsonValue(r'production')
production(r'production');

const PushDeviceInputEnvironmentEnum(this.value);

final String value;

@override
String toString() => value;
}


/// minimal: no alert text through Apple/Google; short: shortTitle (≤ 40 chars)
enum PushDeviceInputContentLevelEnum {
@JsonValue(r'minimal')
minimal(r'minimal'),
@JsonValue(r'short')
short(r'short');

const PushDeviceInputContentLevelEnum(this.value);

final String value;

@override
String toString() => value;
}


