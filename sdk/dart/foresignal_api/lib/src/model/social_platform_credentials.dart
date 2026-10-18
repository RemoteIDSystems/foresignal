//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_platform_credentials.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialPlatformCredentials {
  /// Returns a new [SocialPlatformCredentials] instance.
  SocialPlatformCredentials({

    required  this.required_,

    required  this.configured,

     this.fields,
  });

  @JsonKey(
    
    name: r'required',
    required: true,
    includeIfNull: false,
  )


  final bool required_;



  @JsonKey(
    
    name: r'configured',
    required: true,
    includeIfNull: false,
  )


  final bool configured;



      /// Credential field names, e.g. `bearerToken`, `apiKey`, `apiId`
  @JsonKey(
    
    name: r'fields',
    required: false,
    includeIfNull: false,
  )


  final List<String>? fields;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialPlatformCredentials &&
      other.required_ == required_ &&
      other.configured == configured &&
      other.fields == fields;

    @override
    int get hashCode =>
        required_.hashCode +
        configured.hashCode +
        fields.hashCode;

  factory SocialPlatformCredentials.fromJson(Map<String, dynamic> json) => _$SocialPlatformCredentialsFromJson(json);

  Map<String, dynamic> toJson() => _$SocialPlatformCredentialsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

