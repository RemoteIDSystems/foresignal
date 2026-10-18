//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'apns_provider_config.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ApnsProviderConfig {
  /// Returns a new [ApnsProviderConfig] instance.
  ApnsProviderConfig({

    required  this.teamId,

    required  this.keyId,

    required  this.keyEnvironment,

     this.timeSensitive = false,
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


  final ApnsProviderConfigKeyEnvironmentEnum keyEnvironment;



      /// Only when the app has the Time Sensitive capability
  @JsonKey(
    defaultValue: false,
    name: r'timeSensitive',
    required: false,
    includeIfNull: false,
  )


  final bool? timeSensitive;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ApnsProviderConfig &&
      other.teamId == teamId &&
      other.keyId == keyId &&
      other.keyEnvironment == keyEnvironment &&
      other.timeSensitive == timeSensitive;

    @override
    int get hashCode =>
        teamId.hashCode +
        keyId.hashCode +
        keyEnvironment.hashCode +
        timeSensitive.hashCode;

  factory ApnsProviderConfig.fromJson(Map<String, dynamic> json) => _$ApnsProviderConfigFromJson(json);

  Map<String, dynamic> toJson() => _$ApnsProviderConfigToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ApnsProviderConfigKeyEnvironmentEnum {
@JsonValue(r'sandbox')
sandbox(r'sandbox'),
@JsonValue(r'production')
production(r'production'),
@JsonValue(r'both')
both(r'both');

const ApnsProviderConfigKeyEnvironmentEnum(this.value);

final String value;

@override
String toString() => value;
}


