//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'admin_token_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AdminTokenResponse {
  /// Returns a new [AdminTokenResponse] instance.
  AdminTokenResponse({

    required  this.token,
  });

      /// Shown once; store it securely
  @JsonKey(
    
    name: r'token',
    required: true,
    includeIfNull: false,
  )


  final String token;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AdminTokenResponse &&
      other.token == token;

    @override
    int get hashCode =>
        token.hashCode;

  factory AdminTokenResponse.fromJson(Map<String, dynamic> json) => _$AdminTokenResponseFromJson(json);

  Map<String, dynamic> toJson() => _$AdminTokenResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

