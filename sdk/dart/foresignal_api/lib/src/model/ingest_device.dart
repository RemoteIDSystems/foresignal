//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_device.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestDevice {
  /// Returns a new [IngestDevice] instance.
  IngestDevice({

    required  this.id,

    required  this.sourceId,

    required  this.externalId,

    required  this.name,

    required  this.keyPrefix,

    required  this.createdAt,

     this.lastSeenAt,

     this.revokedAt,
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





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestDevice &&
      other.id == id &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.name == name &&
      other.keyPrefix == keyPrefix &&
      other.createdAt == createdAt &&
      other.lastSeenAt == lastSeenAt &&
      other.revokedAt == revokedAt;

    @override
    int get hashCode =>
        id.hashCode +
        sourceId.hashCode +
        externalId.hashCode +
        name.hashCode +
        keyPrefix.hashCode +
        createdAt.hashCode +
        (lastSeenAt == null ? 0 : lastSeenAt.hashCode) +
        (revokedAt == null ? 0 : revokedAt.hashCode);

  factory IngestDevice.fromJson(Map<String, dynamic> json) => _$IngestDeviceFromJson(json);

  Map<String, dynamic> toJson() => _$IngestDeviceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

