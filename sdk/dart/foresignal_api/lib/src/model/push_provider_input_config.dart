//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/apns_provider_config.dart';
import 'package:foresignal_api/src/model/fcm_provider_config.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_provider_input_config.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushProviderInputConfig {
  /// Returns a new [PushProviderInputConfig] instance.
  PushProviderInputConfig({

    required  this.teamId,

    required  this.keyId,

    required  this.keyEnvironment,

     this.timeSensitive = false,

    required  this.projectId,

    required  this.clientEmail,

     this.tokenUri = 'https://oauth2.googleapis.com/token',
  });

  @JsonKey(
    
    name: r'teamId',
    required: true,
    includeIfNull: false,
  )


  final String teamId;



  @JsonKey(
    
    name: r'keyId',
    required: true,
    includeIfNull: false,
  )


  final String keyId;



  @JsonKey(
    
    name: r'keyEnvironment',
    required: true,
    includeIfNull: false,
  )


  final PushProviderInputConfigKeyEnvironmentEnum keyEnvironment;



      /// Only when the app has the Time Sensitive capability
  @JsonKey(
    defaultValue: false,
    name: r'timeSensitive',
    required: false,
    includeIfNull: false,
  )


  final bool? timeSensitive;



  @JsonKey(
    
    name: r'projectId',
    required: true,
    includeIfNull: false,
  )


  final String projectId;



  @JsonKey(
    
    name: r'clientEmail',
    required: true,
    includeIfNull: false,
  )


  final String clientEmail;



      /// OAuth 2.0 token endpoint (https; http is accepted only for a local stand-in)
  @JsonKey(
    defaultValue: 'https://oauth2.googleapis.com/token',
    name: r'tokenUri',
    required: false,
    includeIfNull: false,
  )


  final String? tokenUri;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushProviderInputConfig &&
      other.teamId == teamId &&
      other.keyId == keyId &&
      other.keyEnvironment == keyEnvironment &&
      other.timeSensitive == timeSensitive &&
      other.projectId == projectId &&
      other.clientEmail == clientEmail &&
      other.tokenUri == tokenUri;

    @override
    int get hashCode =>
        teamId.hashCode +
        keyId.hashCode +
        keyEnvironment.hashCode +
        timeSensitive.hashCode +
        projectId.hashCode +
        clientEmail.hashCode +
        tokenUri.hashCode;

  factory PushProviderInputConfig.fromJson(Map<String, dynamic> json) => _$PushProviderInputConfigFromJson(json);

  Map<String, dynamic> toJson() => _$PushProviderInputConfigToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PushProviderInputConfigKeyEnvironmentEnum {
@JsonValue(r'sandbox')
sandbox(r'sandbox'),
@JsonValue(r'production')
production(r'production'),
@JsonValue(r'both')
both(r'both');

const PushProviderInputConfigKeyEnvironmentEnum(this.value);

final String value;

@override
String toString() => value;
}


