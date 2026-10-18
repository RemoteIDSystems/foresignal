//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'fcm_provider_config.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FcmProviderConfig {
  /// Returns a new [FcmProviderConfig] instance.
  FcmProviderConfig({

    required  this.projectId,

    required  this.clientEmail,

     this.tokenUri = 'https://oauth2.googleapis.com/token',
  });

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
    bool operator ==(Object other) => identical(this, other) || other is FcmProviderConfig &&
      other.projectId == projectId &&
      other.clientEmail == clientEmail &&
      other.tokenUri == tokenUri;

    @override
    int get hashCode =>
        projectId.hashCode +
        clientEmail.hashCode +
        tokenUri.hashCode;

  factory FcmProviderConfig.fromJson(Map<String, dynamic> json) => _$FcmProviderConfigFromJson(json);

  Map<String, dynamic> toJson() => _$FcmProviderConfigToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

