//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ingest_device_setup.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_device_created.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestDeviceCreated {
  /// Returns a new [IngestDeviceCreated] instance.
  IngestDeviceCreated({

    required  this.id,

    required  this.sourceId,

    required  this.externalId,

    required  this.name,

    required  this.keyPrefix,

    required  this.createdAt,

     this.lastSeenAt,

     this.revokedAt,

    required  this.key,

    required  this.setup,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'keyPrefix',
    required: true,
    includeIfNull: false,
  )


  final String keyPrefix;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'lastSeenAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastSeenAt;



  @JsonKey(
    
    name: r'revokedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? revokedAt;



      /// `eid_…`; shown once
  @JsonKey(
    
    name: r'key',
    required: true,
    includeIfNull: false,
  )


  final String key;



  @JsonKey(
    
    name: r'setup',
    required: true,
    includeIfNull: false,
  )


  final IngestDeviceSetup setup;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestDeviceCreated &&
      other.id == id &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.name == name &&
      other.keyPrefix == keyPrefix &&
      other.createdAt == createdAt &&
      other.lastSeenAt == lastSeenAt &&
      other.revokedAt == revokedAt &&
      other.key == key &&
      other.setup == setup;

    @override
    int get hashCode =>
        id.hashCode +
        sourceId.hashCode +
        externalId.hashCode +
        name.hashCode +
        keyPrefix.hashCode +
        createdAt.hashCode +
        (lastSeenAt == null ? 0 : lastSeenAt.hashCode) +
        (revokedAt == null ? 0 : revokedAt.hashCode) +
        key.hashCode +
        setup.hashCode;

  factory IngestDeviceCreated.fromJson(Map<String, dynamic> json) => _$IngestDeviceCreatedFromJson(json);

  Map<String, dynamic> toJson() => _$IngestDeviceCreatedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

