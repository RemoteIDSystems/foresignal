//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'signature_verifier_config.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SignatureVerifierConfig {
  /// Returns a new [SignatureVerifierConfig] instance.
  SignatureVerifierConfig({

    required  this.scheme,

     this.header,

     this.secret,

     this.secretSet,

     this.keyOptional = false,
  });

      /// `hmac-sha256`: header (default X-Signature-256) `sha256=<hex>` over the raw body; `standard-webhooks`: webhook-id/-timestamp/-signature `v1,<base64>` over `id.timestamp.body`, ±5 min, replayed ids rejected for 10 min; `foresignal`: X-Foresignal-Timestamp + X-Foresignal-Signature `sha256=HMAC(\"<ts>.<body>\")`
  @JsonKey(
    
    name: r'scheme',
    required: true,
    includeIfNull: false,
  )


  final SignatureVerifierConfigSchemeEnum scheme;



      /// hmac-sha256 only; default X-Signature-256
  @JsonKey(
    
    name: r'header',
    required: false,
    includeIfNull: false,
  )


  final String? header;



      /// Write-only; omit on update to keep the stored secret
  @JsonKey(
    
    name: r'secret',
    required: false,
    includeIfNull: false,
  )


  final String? secret;



  @JsonKey(
    
    name: r'secretSet',
    required: false,
    includeIfNull: false,
  )


  final bool? secretSet;



      /// When true a valid signature alone authenticates; otherwise the ingest key is required too
  @JsonKey(
    defaultValue: false,
    name: r'keyOptional',
    required: false,
    includeIfNull: false,
  )


  final bool? keyOptional;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SignatureVerifierConfig &&
      other.scheme == scheme &&
      other.header == header &&
      other.secret == secret &&
      other.secretSet == secretSet &&
      other.keyOptional == keyOptional;

    @override
    int get hashCode =>
        scheme.hashCode +
        (header == null ? 0 : header.hashCode) +
        secret.hashCode +
        secretSet.hashCode +
        keyOptional.hashCode;

  factory SignatureVerifierConfig.fromJson(Map<String, dynamic> json) => _$SignatureVerifierConfigFromJson(json);

  Map<String, dynamic> toJson() => _$SignatureVerifierConfigToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `hmac-sha256`: header (default X-Signature-256) `sha256=<hex>` over the raw body; `standard-webhooks`: webhook-id/-timestamp/-signature `v1,<base64>` over `id.timestamp.body`, ±5 min, replayed ids rejected for 10 min; `foresignal`: X-Foresignal-Timestamp + X-Foresignal-Signature `sha256=HMAC(\"<ts>.<body>\")`
enum SignatureVerifierConfigSchemeEnum {
@JsonValue(r'none')
none(r'none'),
@JsonValue(r'hmac-sha256')
hmacSha256(r'hmac-sha256'),
@JsonValue(r'standard-webhooks')
standardWebhooks(r'standard-webhooks'),
@JsonValue(r'foresignal')
foresignal(r'foresignal');

const SignatureVerifierConfigSchemeEnum(this.value);

final String value;

@override
String toString() => value;
}


