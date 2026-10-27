//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/archive_files_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'archive.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Archive {
  /// Returns a new [Archive] instance.
  Archive({

    required  this.id,

    required  this.status,

    required  this.createdAt,

     this.cutoff,

    required  this.target,

     this.location,

     this.sizeBytes,

     this.itemCount,

     this.checksum,

     this.purgedFromHot = false,

     this.restoredAt,

     this.trigger,

     this.domain,

     this.sources,

     this.fromSeq,

     this.toSeq,

     this.fromTime,

     this.toTime,

     this.files,

     this.error,

     this.updatedAt,

     this.completedAt,

     this.purgedAt,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// pending (accepted), running (exporting), verifying (offloading and checking each file on the tier), complete (verified and recorded), purged (and gone from the hot store), failed (nothing was purged), restoring, restored
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final String status;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



      /// Data older than this was archived; null when the run took only what retention removed anyway
  @JsonKey(
    
    name: r'cutoff',
    required: false,
    includeIfNull: false,
  )


  final DateTime? cutoff;



      /// Archive tier target
  @JsonKey(
    
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final String target;



      /// The segment's directory (`local`) or `s3://bucket/prefix/segments/{id}`
  @JsonKey(
    
    name: r'location',
    required: false,
    includeIfNull: false,
  )


  final String? location;



      /// Compressed size of every file
  @JsonKey(
    
    name: r'sizeBytes',
    required: false,
    includeIfNull: false,
  )


  final int? sizeBytes;



      /// Rows archived (for a dry run: rows that would be)
  @JsonKey(
    
    name: r'itemCount',
    required: false,
    includeIfNull: false,
  )


  final int? itemCount;



      /// SHA-256 over each file's `key:sha256` line, in key order; every file's own SHA-256 is in `files`
  @JsonKey(
    
    name: r'checksum',
    required: false,
    includeIfNull: false,
  )


  final String? checksum;



      /// True once the archived data was purged from the hot store
  @JsonKey(
    defaultValue: false,
    name: r'purgedFromHot',
    required: false,
    includeIfNull: false,
  )


  final bool? purgedFromHot;



  @JsonKey(
    
    name: r'restoredAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? restoredAt;



      /// What started the run: runArchive, `archive.schedule`, or the retention size cap (R2-015)
  @JsonKey(
    
    name: r'trigger',
    required: false,
    includeIfNull: false,
  )


  final String? trigger;



      /// What was archived: `record_version`
  @JsonKey(
    
    name: r'domain',
    required: false,
    includeIfNull: false,
  )


  final String? domain;



      /// Sources in the segment
  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



  @JsonKey(
    
    name: r'fromSeq',
    required: false,
    includeIfNull: false,
  )


  final int? fromSeq;



  @JsonKey(
    
    name: r'toSeq',
    required: false,
    includeIfNull: false,
  )


  final int? toSeq;



      /// Earliest ingested_at archived
  @JsonKey(
    
    name: r'fromTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? fromTime;



      /// Latest ingested_at archived
  @JsonKey(
    
    name: r'toTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? toTime;



      /// One gzip-compressed NDJSON file per source, one record_version row per line
  @JsonKey(
    
    name: r'files',
    required: false,
    includeIfNull: false,
  )


  final List<ArchiveFilesInner>? files;



      /// Why it failed, or a note (rows the purge kept back, a pending Glacier thaw, rows restored)
  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;



  @JsonKey(
    
    name: r'completedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? completedAt;



  @JsonKey(
    
    name: r'purgedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? purgedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Archive &&
      other.id == id &&
      other.status == status &&
      other.createdAt == createdAt &&
      other.cutoff == cutoff &&
      other.target == target &&
      other.location == location &&
      other.sizeBytes == sizeBytes &&
      other.itemCount == itemCount &&
      other.checksum == checksum &&
      other.purgedFromHot == purgedFromHot &&
      other.restoredAt == restoredAt &&
      other.trigger == trigger &&
      other.domain == domain &&
      other.sources == sources &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq &&
      other.fromTime == fromTime &&
      other.toTime == toTime &&
      other.files == files &&
      other.error == error &&
      other.updatedAt == updatedAt &&
      other.completedAt == completedAt &&
      other.purgedAt == purgedAt;

    @override
    int get hashCode =>
        id.hashCode +
        status.hashCode +
        createdAt.hashCode +
        (cutoff == null ? 0 : cutoff.hashCode) +
        target.hashCode +
        (location == null ? 0 : location.hashCode) +
        (sizeBytes == null ? 0 : sizeBytes.hashCode) +
        (itemCount == null ? 0 : itemCount.hashCode) +
        (checksum == null ? 0 : checksum.hashCode) +
        purgedFromHot.hashCode +
        (restoredAt == null ? 0 : restoredAt.hashCode) +
        trigger.hashCode +
        domain.hashCode +
        sources.hashCode +
        (fromSeq == null ? 0 : fromSeq.hashCode) +
        (toSeq == null ? 0 : toSeq.hashCode) +
        (fromTime == null ? 0 : fromTime.hashCode) +
        (toTime == null ? 0 : toTime.hashCode) +
        files.hashCode +
        (error == null ? 0 : error.hashCode) +
        updatedAt.hashCode +
        (completedAt == null ? 0 : completedAt.hashCode) +
        (purgedAt == null ? 0 : purgedAt.hashCode);

  factory Archive.fromJson(Map<String, dynamic> json) => _$ArchiveFromJson(json);

  Map<String, dynamic> toJson() => _$ArchiveToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

