//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'egress_route_input_secrets.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EgressRouteInputSecrets {
  /// Returns a new [EgressRouteInputSecrets] instance.
  EgressRouteInputSecrets({

     this.username,

     this.password,

     this.wireguardConfig,
  });

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



      /// A WireGuard profile to import
  @JsonKey(
    
    name: r'wireguardConfig',
    required: false,
    includeIfNull: false,
  )


  final String? wireguardConfig;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EgressRouteInputSecrets &&
      other.username == username &&
      other.password == password &&
      other.wireguardConfig == wireguardConfig;

    @override
    int get hashCode =>
        username.hashCode +
        password.hashCode +
        wireguardConfig.hashCode;

  factory EgressRouteInputSecrets.fromJson(Map<String, dynamic> json) => _$EgressRouteInputSecretsFromJson(json);

  Map<String, dynamic> toJson() => _$EgressRouteInputSecretsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

