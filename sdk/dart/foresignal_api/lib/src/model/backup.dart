//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/backup_files_inner.dart';
import 'package:foresignal_api/src/model/import_report.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'backup.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Backup {
  /// Returns a new [Backup] instance.
  Backup({

    required  this.id,

    required  this.type,

    required  this.status,

    required  this.createdAt,

     this.sizeBytes,

     this.target,

     this.location,

     this.checksum,

     this.label,

     this.trigger,

     this.files,

     this.error,

     this.startedAt,

     this.finishedAt,

     this.restoredAt,

     this.importReport,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// `database`: a `pg_dump --format=custom` archive; `config`: the `exportSettings` bundle (no secrets); `full`: both
  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final String type;



      /// pending → running → complete or failed; a restore goes restoring → restored (a failed restore puts the previous status back with `error`)
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



  @JsonKey(
    
    name: r'sizeBytes',
    required: false,
    includeIfNull: false,
  )


  final int? sizeBytes;



  @JsonKey(
    
    name: r'target',
    required: false,
    includeIfNull: false,
  )


  final String? target;



      /// The directory or `s3://` prefix holding the files
  @JsonKey(
    
    name: r'location',
    required: false,
    includeIfNull: false,
  )


  final String? location;



      /// SHA-256 of the one file, or of the `key:sha256` lines of both files sorted by key
  @JsonKey(
    
    name: r'checksum',
    required: false,
    includeIfNull: false,
  )


  final String? checksum;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;



      /// `manual` (runBackup) or `scheduled` (backup.schedule)
  @JsonKey(
    
    name: r'trigger',
    required: false,
    includeIfNull: false,
  )


  final String? trigger;



      /// The stored files
  @JsonKey(
    
    name: r'files',
    required: false,
    includeIfNull: false,
  )


  final List<BackupFilesInner>? files;



      /// Why the backup, or its last restore, failed
  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;



  @JsonKey(
    
    name: r'startedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? startedAt;



  @JsonKey(
    
    name: r'finishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? finishedAt;



      /// When it was last restored
  @JsonKey(
    
    name: r'restoredAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? restoredAt;



      /// Present on the answer of `restoreBackup` for a `config` backup: what the import applied
  @JsonKey(
    
    name: r'importReport',
    required: false,
    includeIfNull: false,
  )


  final ImportReport? importReport;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Backup &&
      other.id == id &&
      other.type == type &&
      other.status == status &&
      other.createdAt == createdAt &&
      other.sizeBytes == sizeBytes &&
      other.target == target &&
      other.location == location &&
      other.checksum == checksum &&
      other.label == label &&
      other.trigger == trigger &&
      other.files == files &&
      other.error == error &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.restoredAt == restoredAt &&
      other.importReport == importReport;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        status.hashCode +
        createdAt.hashCode +
        (sizeBytes == null ? 0 : sizeBytes.hashCode) +
        target.hashCode +
        (location == null ? 0 : location.hashCode) +
        (checksum == null ? 0 : checksum.hashCode) +
        (label == null ? 0 : label.hashCode) +
        trigger.hashCode +
        files.hashCode +
        (error == null ? 0 : error.hashCode) +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        (restoredAt == null ? 0 : restoredAt.hashCode) +
        (importReport == null ? 0 : importReport.hashCode);

  factory Backup.fromJson(Map<String, dynamic> json) => _$BackupFromJson(json);

  Map<String, dynamic> toJson() => _$BackupToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

