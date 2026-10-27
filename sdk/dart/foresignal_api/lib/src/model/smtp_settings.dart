//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'smtp_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SmtpSettings {
  /// Returns a new [SmtpSettings] instance.
  SmtpSettings({

     this.enabled = false,

     this.host,

     this.port,

     this.security = 'starttls',

     this.username,

     this.fromAddress,

     this.fromName,

     this.secretsSet,
  });

  @JsonKey(
    defaultValue: false,
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'host',
    required: false,
    includeIfNull: false,
  )


  final String? host;



      /// Typically 587 (STARTTLS) or 465 (TLS)
  @JsonKey(
    
    name: r'port',
    required: false,
    includeIfNull: false,
  )


  final int? port;



  @JsonKey(
    defaultValue: 'starttls',
    name: r'security',
    required: false,
    includeIfNull: false,
  )


  final String? security;



  @JsonKey(
    
    name: r'username',
    required: false,
    includeIfNull: false,
  )


  final String? username;



      /// Default info@remoteidsystems.com
  @JsonKey(
    
    name: r'fromAddress',
    required: false,
    includeIfNull: false,
  )


  final String? fromAddress;



      /// Display name on outbound mail
  @JsonKey(
    
    name: r'fromName',
    required: false,
    includeIfNull: false,
  )


  final String? fromName;



      /// Names of secrets that have a stored value
  @JsonKey(
    
    name: r'secretsSet',
    required: false,
    includeIfNull: false,
  )


  final List<String>? secretsSet;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SmtpSettings &&
      other.enabled == enabled &&
      other.host == host &&
      other.port == port &&
      other.security == security &&
      other.username == username &&
      other.fromAddress == fromAddress &&
      other.fromName == fromName &&
      other.secretsSet == secretsSet;

    @override
    int get hashCode =>
        enabled.hashCode +
        (host == null ? 0 : host.hashCode) +
        (port == null ? 0 : port.hashCode) +
        security.hashCode +
        (username == null ? 0 : username.hashCode) +
        (fromAddress == null ? 0 : fromAddress.hashCode) +
        (fromName == null ? 0 : fromName.hashCode) +
        secretsSet.hashCode;

  factory SmtpSettings.fromJson(Map<String, dynamic> json) => _$SmtpSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$SmtpSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

