//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mfa_enrollment.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MfaEnrollment {
  /// Returns a new [MfaEnrollment] instance.
  MfaEnrollment({

    required  this.secret,

    required  this.otpauthUri,

     this.qrSvg,
  });

      /// Base32 TOTP secret (RFC 6238: HMAC-SHA1, 6 digits, 30 s); stored sealed with the instance key, shown only here
  @JsonKey(
    
    name: r'secret',
    required: true,
    includeIfNull: false,
  )


  final String secret;



      /// otpauth:// provisioning URI for an authenticator app: otpauth://totp/ForeSignal:<email>?secret=…&issuer=ForeSignal
  @JsonKey(
    
    name: r'otpauthUri',
    required: true,
    includeIfNull: false,
  )


  final String otpauthUri;



      /// Inline SVG QR of the provisioning URI
  @JsonKey(
    
    name: r'qrSvg',
    required: false,
    includeIfNull: false,
  )


  final String? qrSvg;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MfaEnrollment &&
      other.secret == secret &&
      other.otpauthUri == otpauthUri &&
      other.qrSvg == qrSvg;

    @override
    int get hashCode =>
        secret.hashCode +
        otpauthUri.hashCode +
        (qrSvg == null ? 0 : qrSvg.hashCode);

  factory MfaEnrollment.fromJson(Map<String, dynamic> json) => _$MfaEnrollmentFromJson(json);

  Map<String, dynamic> toJson() => _$MfaEnrollmentToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

