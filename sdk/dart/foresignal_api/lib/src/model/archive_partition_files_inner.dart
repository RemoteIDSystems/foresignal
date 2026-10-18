//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'archive_partition_files_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ArchivePartitionFilesInner {
  /// Returns a new [ArchivePartitionFilesInner] instance.
  ArchivePartitionFilesInner({

    required  this.name,

    required  this.fromSeq,

    required  this.toSeq,

    required  this.rows,

    required  this.bytes,

    required  this.sha256,

     this.local,

     this.uploadedAt,

     this.s3Key,
  });

      /// part-{fromSeq}-{toSeq}.parquet
  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'fromSeq',
    required: true,
    includeIfNull: false,
  )


  final int fromSeq;



  @JsonKey(
    
    name: r'toSeq',
    required: true,
    includeIfNull: false,
  )


  final int toSeq;



  @JsonKey(
    
    name: r'rows',
    required: true,
    includeIfNull: false,
  )


  final int rows;



  @JsonKey(
    
    name: r'bytes',
    required: true,
    includeIfNull: false,
  )


  final int bytes;



  @JsonKey(
    
    name: r'sha256',
    required: true,
    includeIfNull: false,
  )


  final String sha256;



  @JsonKey(
    
    name: r'local',
    required: false,
    includeIfNull: false,
  )


  final bool? local;



  @JsonKey(
    
    name: r'uploadedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? uploadedAt;



  @JsonKey(
    
    name: r's3Key',
    required: false,
    includeIfNull: false,
  )


  final String? s3Key;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ArchivePartitionFilesInner &&
      other.name == name &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq &&
      other.rows == rows &&
      other.bytes == bytes &&
      other.sha256 == sha256 &&
      other.local == local &&
      other.uploadedAt == uploadedAt &&
      other.s3Key == s3Key;

    @override
    int get hashCode =>
        name.hashCode +
        fromSeq.hashCode +
        toSeq.hashCode +
        rows.hashCode +
        bytes.hashCode +
        sha256.hashCode +
        local.hashCode +
        (uploadedAt == null ? 0 : uploadedAt.hashCode) +
        (s3Key == null ? 0 : s3Key.hashCode);

  factory ArchivePartitionFilesInner.fromJson(Map<String, dynamic> json) => _$ArchivePartitionFilesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ArchivePartitionFilesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

