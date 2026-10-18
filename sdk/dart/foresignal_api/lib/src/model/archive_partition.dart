//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/export_licence_summary.dart';
import 'package:foresignal_api/src/model/archive_partition_files_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'archive_partition.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ArchivePartition {
  /// Returns a new [ArchivePartition] instance.
  ArchivePartition({

    required  this.tier,

    required  this.sourceId,

    required  this.date,

    required  this.path,

    required  this.files,

     this.licence,
  });

  @JsonKey(
    
    name: r'tier',
    required: true,
    includeIfNull: false,
  )


  final ArchivePartitionTierEnum tier;



  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



      /// yyyy-mm-dd of ingested_at (UTC)
  @JsonKey(
    
    name: r'date',
    required: true,
    includeIfNull: false,
  )


  final DateTime date;



      /// record_version/tier={tier}/source_id={id}/date={date}
  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final String path;



  @JsonKey(
    
    name: r'files',
    required: true,
    includeIfNull: false,
  )


  final List<ArchivePartitionFilesInner> files;



  @JsonKey(
    
    name: r'licence',
    required: false,
    includeIfNull: false,
  )


  final ExportLicenceSummary? licence;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ArchivePartition &&
      other.tier == tier &&
      other.sourceId == sourceId &&
      other.date == date &&
      other.path == path &&
      other.files == files &&
      other.licence == licence;

    @override
    int get hashCode =>
        tier.hashCode +
        sourceId.hashCode +
        date.hashCode +
        path.hashCode +
        files.hashCode +
        (licence == null ? 0 : licence.hashCode);

  factory ArchivePartition.fromJson(Map<String, dynamic> json) => _$ArchivePartitionFromJson(json);

  Map<String, dynamic> toJson() => _$ArchivePartitionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ArchivePartitionTierEnum {
@JsonValue(r'std')
std(r'std'),
@JsonValue(r'trk')
trk(r'trk');

const ArchivePartitionTierEnum(this.value);

final String value;

@override
String toString() => value;
}


