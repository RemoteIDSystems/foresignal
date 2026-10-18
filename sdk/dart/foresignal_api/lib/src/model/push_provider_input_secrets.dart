//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'push_provider_input_secrets.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PushProviderInputSecrets {
  /// Returns a new [PushProviderInputSecrets] instance.
  PushProviderInputSecrets({

     this.p8,

     this.serviceAccountJson,
  });

      /// APNs .p8 PEM (EC P-256 PKCS#8)
  @JsonKey(
    
    name: r'p8',
    required: false,
    includeIfNull: false,
  )


  final String? p8;



      /// FCM service-account JSON, as a string or an object; only private_key is stored, encrypted
  @JsonKey(
    
    name: r'serviceAccountJson',
    required: false,
    includeIfNull: false,
  )


  final String? serviceAccountJson;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PushProviderInputSecrets &&
      other.p8 == p8 &&
      other.serviceAccountJson == serviceAccountJson;

    @override
    int get hashCode =>
        p8.hashCode +
        serviceAccountJson.hashCode;

  factory PushProviderInputSecrets.fromJson(Map<String, dynamic> json) => _$PushProviderInputSecretsFromJson(json);

  Map<String, dynamic> toJson() => _$PushProviderInputSecretsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

