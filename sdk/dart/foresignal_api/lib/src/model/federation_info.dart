//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'federation_info.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FederationInfo {
  /// Returns a new [FederationInfo] instance.
  FederationInfo({

    required  this.instanceId,

     this.instanceName,

    required  this.version,

    required  this.apiVersion,

    required  this.jwksUri,

    required  this.capabilities,

    required  this.time,
  });

      /// Install id (003)
  @JsonKey(
    
    name: r'instanceId',
    required: true,
    includeIfNull: false,
  )


  final String instanceId;



  @JsonKey(
    
    name: r'instanceName',
    required: false,
    includeIfNull: false,
  )


  final String? instanceName;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final String version;



  @JsonKey(
    
    name: r'apiVersion',
    required: true,
    includeIfNull: false,
  )


  final String apiVersion;



  @JsonKey(
    
    name: r'jwksUri',
    required: true,
    includeIfNull: false,
  )


  final String jwksUri;



  @JsonKey(
    
    name: r'capabilities',
    required: true,
    includeIfNull: false,
  )


  final List<FederationInfoCapabilitiesEnum> capabilities;



  @JsonKey(
    
    name: r'time',
    required: true,
    includeIfNull: false,
  )


  final DateTime time;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FederationInfo &&
      other.instanceId == instanceId &&
      other.instanceName == instanceName &&
      other.version == version &&
      other.apiVersion == apiVersion &&
      other.jwksUri == jwksUri &&
      other.capabilities == capabilities &&
      other.time == time;

    @override
    int get hashCode =>
        instanceId.hashCode +
        (instanceName == null ? 0 : instanceName.hashCode) +
        version.hashCode +
        apiVersion.hashCode +
        jwksUri.hashCode +
        capabilities.hashCode +
        time.hashCode;

  factory FederationInfo.fromJson(Map<String, dynamic> json) => _$FederationInfoFromJson(json);

  Map<String, dynamic> toJson() => _$FederationInfoToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum FederationInfoCapabilitiesEnum {
@JsonValue(r'changes')
changes(r'changes'),
@JsonValue(r'stream')
stream(r'stream'),
@JsonValue(r'snapshot')
snapshot(r'snapshot'),
@JsonValue(r'ingest')
ingest(r'ingest');

const FederationInfoCapabilitiesEnum(this.value);

final String value;

@override
String toString() => value;
}


