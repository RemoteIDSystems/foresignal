//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/push_provider_input_config.dart';
import 'package:foresignal_api/src/model/push_provider_input_secrets.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_provider_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushProviderInput {
  /// Returns a new [PushProviderInput] instance.
  PushProviderInput({

    required  this.type,

    required  this.appIds,

    required  this.config,

     this.secrets,

     this.enabled = true,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final PushProviderInputTypeEnum type;



      /// Bundle ids / Android package names served by this key
  @JsonKey(
    
    name: r'appIds',
    required: true,
    includeIfNull: false,
  )


  final List<String> appIds;



  @JsonKey(
    
    name: r'config',
    required: true,
    includeIfNull: false,
  )


  final PushProviderInputConfig config;



  @JsonKey(
    
    name: r'secrets',
    required: false,
    includeIfNull: false,
  )


  final PushProviderInputSecrets? secrets;



  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushProviderInput &&
      other.type == type &&
      other.appIds == appIds &&
      other.config == config &&
      other.secrets == secrets &&
      other.enabled == enabled;

    @override
    int get hashCode =>
        type.hashCode +
        appIds.hashCode +
        config.hashCode +
        secrets.hashCode +
        enabled.hashCode;

  factory PushProviderInput.fromJson(Map<String, dynamic> json) => _$PushProviderInputFromJson(json);

  Map<String, dynamic> toJson() => _$PushProviderInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PushProviderInputTypeEnum {
@JsonValue(r'apns')
apns(r'apns'),
@JsonValue(r'fcm')
fcm(r'fcm');

const PushProviderInputTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


