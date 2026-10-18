//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'update_token_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class UpdateTokenRequest {
  /// Returns a new [UpdateTokenRequest] instance.
  UpdateTokenRequest({

    required  this.licencePolicy,
  });

      /// Licence policy flags (109); null or empty clears the floor
  @JsonKey(
    
    name: r'licencePolicy',
    required: true,
    includeIfNull: true,
  )


  final List<UpdateTokenRequestLicencePolicyEnum>? licencePolicy;





    @override
    bool operator ==(Object other) => identical(this, other) || other is UpdateTokenRequest &&
      other.licencePolicy == licencePolicy;

    @override
    int get hashCode =>
        (licencePolicy == null ? 0 : licencePolicy.hashCode);

  factory UpdateTokenRequest.fromJson(Map<String, dynamic> json) => _$UpdateTokenRequestFromJson(json);

  Map<String, dynamic> toJson() => _$UpdateTokenRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum UpdateTokenRequestLicencePolicyEnum {
@JsonValue(r'no-nc')
noNc(r'no-nc'),
@JsonValue(r'no-sa')
noSa(r'no-sa'),
@JsonValue(r'redistributable')
redistributable(r'redistributable');

const UpdateTokenRequestLicencePolicyEnum(this.value);

final String value;

@override
String toString() => value;
}


