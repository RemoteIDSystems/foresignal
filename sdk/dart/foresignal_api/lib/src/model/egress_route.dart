//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/egress_health.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'egress_route.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EgressRoute {
  /// Returns a new [EgressRoute] instance.
  EgressRoute({

    required  this.id,

    required  this.name,

    required  this.type,

     this.enabled = true,

     this.url,

     this.boundInterface,

     this.exitIp,

     this.health,

     this.secretsSet,

     this.createdAt,

     this.updatedAt,

     this.available,

     this.unavailableReason,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



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
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// Proxy URL (scheme://host:port); credentials are not shown here
  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



      /// Host interface or source IP to bind (type interface)
  @JsonKey(
    
    name: r'boundInterface',
    required: false,
    includeIfNull: false,
  )


  final String? boundInterface;



      /// Last observed public exit IP from a test call
  @JsonKey(
    
    name: r'exitIp',
    required: false,
    includeIfNull: false,
  )


  final String? exitIp;



  @JsonKey(
    
    name: r'health',
    required: false,
    includeIfNull: false,
  )


  final EgressHealth? health;



      /// Names of secrets that have a stored value
  @JsonKey(
    
    name: r'secretsSet',
    required: false,
    includeIfNull: false,
  )


  final List<String>? secretsSet;



  @JsonKey(
    
    name: r'createdAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;



      /// Whether the server can send through this route now: false for a wireguard route (the server cannot bring a tunnel up), a disabled route, an interface it cannot find, or secrets it cannot open (R2-018)
  @JsonKey(
    
    name: r'available',
    required: false,
    includeIfNull: false,
  )


  final bool? available;



      /// Why the route is unavailable; null when available
  @JsonKey(
    
    name: r'unavailableReason',
    required: false,
    includeIfNull: false,
  )


  final String? unavailableReason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EgressRoute &&
      other.id == id &&
      other.name == name &&
      other.type == type &&
      other.enabled == enabled &&
      other.url == url &&
      other.boundInterface == boundInterface &&
      other.exitIp == exitIp &&
      other.health == health &&
      other.secretsSet == secretsSet &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.available == available &&
      other.unavailableReason == unavailableReason;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        type.hashCode +
        enabled.hashCode +
        (url == null ? 0 : url.hashCode) +
        (boundInterface == null ? 0 : boundInterface.hashCode) +
        (exitIp == null ? 0 : exitIp.hashCode) +
        (health == null ? 0 : health.hashCode) +
        secretsSet.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode +
        available.hashCode +
        (unavailableReason == null ? 0 : unavailableReason.hashCode);

  factory EgressRoute.fromJson(Map<String, dynamic> json) => _$EgressRouteFromJson(json);

  Map<String, dynamic> toJson() => _$EgressRouteToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

