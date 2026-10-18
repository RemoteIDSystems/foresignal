//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'get_vapid_public_key200_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GetVapidPublicKey200Response {
  /// Returns a new [GetVapidPublicKey200Response] instance.
  GetVapidPublicKey200Response({

    required  this.publicKey,
  });

      /// Base64url uncompressed P-256 point
  @JsonKey(
    
    name: r'publicKey',
    required: true,
    includeIfNull: false,
  )


  final String publicKey;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GetVapidPublicKey200Response &&
      other.publicKey == publicKey;

    @override
    int get hashCode =>
        publicKey.hashCode;

  factory GetVapidPublicKey200Response.fromJson(Map<String, dynamic> json) => _$GetVapidPublicKey200ResponseFromJson(json);

  Map<String, dynamic> toJson() => _$GetVapidPublicKey200ResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

