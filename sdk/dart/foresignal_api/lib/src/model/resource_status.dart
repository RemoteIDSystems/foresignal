//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/resource_status_database.dart';
import 'package:foresignal_api/src/model/resource_gauge.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'resource_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ResourceStatus {
  /// Returns a new [ResourceStatus] instance.
  ResourceStatus({

    required  this.status,

    required  this.disk,

     this.memory,

     this.database,

     this.warnings,

    required  this.checkedAt,
  });

      /// Worst of the gauges
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final String status;



      /// The most severe of the instance's data volumes (the data directory and the raster/media store, de-duplicated by file system). The Postgres data volume belongs to the database container and is gauged by `database.sizeBytes` instead.
  @JsonKey(
    
    name: r'disk',
    required: true,
    includeIfNull: false,
  )


  final ResourceGauge disk;



      /// Physical memory as the JVM sees it (inside a container, its limit), or the heap when that is unavailable
  @JsonKey(
    
    name: r'memory',
    required: false,
    includeIfNull: false,
  )


  final ResourceGauge? memory;



  @JsonKey(
    
    name: r'database',
    required: false,
    includeIfNull: false,
  )


  final ResourceStatusDatabase? database;



      /// Human-readable breach messages
  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? warnings;



      /// When the reading was taken; readings are reused for up to 30 s
  @JsonKey(
    
    name: r'checkedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime checkedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ResourceStatus &&
      other.status == status &&
      other.disk == disk &&
      other.memory == memory &&
      other.database == database &&
      other.warnings == warnings &&
      other.checkedAt == checkedAt;

    @override
    int get hashCode =>
        status.hashCode +
        disk.hashCode +
        memory.hashCode +
        database.hashCode +
        warnings.hashCode +
        checkedAt.hashCode;

  factory ResourceStatus.fromJson(Map<String, dynamic> json) => _$ResourceStatusFromJson(json);

  Map<String, dynamic> toJson() => _$ResourceStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

