//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'smtp_settings_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SmtpSettingsInput {
  /// Returns a new [SmtpSettingsInput] instance.
  SmtpSettingsInput({

    required  this.enabled,

     this.host,

     this.port,

     this.security,

     this.username,

     this.password,

     this.fromAddress,

     this.fromName,
  });

  @JsonKey(
    
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



  @JsonKey(
    
    name: r'port',
    required: false,
    includeIfNull: false,
  )


  final int? port;



  @JsonKey(
    
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



  @JsonKey(
    
    name: r'password',
    required: false,
    includeIfNull: false,
  )


  final String? password;



  @JsonKey(
    
    name: r'fromAddress',
    required: false,
    includeIfNull: false,
  )


  final String? fromAddress;



  @JsonKey(
    
    name: r'fromName',
    required: false,
    includeIfNull: false,
  )


  final String? fromName;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SmtpSettingsInput &&
      other.enabled == enabled &&
      other.host == host &&
      other.port == port &&
      other.security == security &&
      other.username == username &&
      other.password == password &&
      other.fromAddress == fromAddress &&
      other.fromName == fromName;

    @override
    int get hashCode =>
        enabled.hashCode +
        (host == null ? 0 : host.hashCode) +
        (port == null ? 0 : port.hashCode) +
        security.hashCode +
        (username == null ? 0 : username.hashCode) +
        (password == null ? 0 : password.hashCode) +
        (fromAddress == null ? 0 : fromAddress.hashCode) +
        (fromName == null ? 0 : fromName.hashCode);

  factory SmtpSettingsInput.fromJson(Map<String, dynamic> json) => _$SmtpSettingsInputFromJson(json);

  Map<String, dynamic> toJson() => _$SmtpSettingsInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

