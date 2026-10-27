//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mfa_verify_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MfaVerifyRequest {
  /// Returns a new [MfaVerifyRequest] instance.
  MfaVerifyRequest({

    required  this.code,
  });

      /// Current 6-digit TOTP code; once MFA is active, a recovery code is accepted too (consumed on use)
  @JsonKey(
    
    name: r'code',
    required: true,
    includeIfNull: false,
  )


  final String code;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MfaVerifyRequest &&
      other.code == code;

    @override
    int get hashCode =>
        code.hashCode;

  factory MfaVerifyRequest.fromJson(Map<String, dynamic> json) => _$MfaVerifyRequestFromJson(json);

  Map<String, dynamic> toJson() => _$MfaVerifyRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

