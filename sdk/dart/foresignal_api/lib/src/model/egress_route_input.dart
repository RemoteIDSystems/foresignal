//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/egress_route_input_secrets.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'egress_route_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EgressRouteInput {
  /// Returns a new [EgressRouteInput] instance.
  EgressRouteInput({

    required  this.name,

    required  this.type,

     this.enabled = true,

     this.url,

     this.boundInterface,

     this.secrets,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final String type;



  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'boundInterface',
    required: false,
    includeIfNull: false,
  )


  final String? boundInterface;



  @JsonKey(
    
    name: r'secrets',
    required: false,
    includeIfNull: false,
  )


  final EgressRouteInputSecrets? secrets;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EgressRouteInput &&
      other.name == name &&
      other.type == type &&
      other.enabled == enabled &&
      other.url == url &&
      other.boundInterface == boundInterface &&
      other.secrets == secrets;

    @override
    int get hashCode =>
        name.hashCode +
        type.hashCode +
        enabled.hashCode +
        (url == null ? 0 : url.hashCode) +
        (boundInterface == null ? 0 : boundInterface.hashCode) +
        secrets.hashCode;

  factory EgressRouteInput.fromJson(Map<String, dynamic> json) => _$EgressRouteInputFromJson(json);

  Map<String, dynamic> toJson() => _$EgressRouteInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

