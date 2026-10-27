//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'backup_files_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BackupFilesInner {
  /// Returns a new [BackupFilesInner] instance.
  BackupFilesInner({

    required  this.name,

    required  this.location,

    required  this.bytes,

    required  this.sha256,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// Absolute path or `s3://` URL
  @JsonKey(
    
    name: r'location',
    required: true,
    includeIfNull: false,
  )


  final String location;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is BackupFilesInner &&
      other.name == name &&
      other.location == location &&
      other.bytes == bytes &&
      other.sha256 == sha256;

    @override
    int get hashCode =>
        name.hashCode +
        location.hashCode +
        bytes.hashCode +
        sha256.hashCode;

  factory BackupFilesInner.fromJson(Map<String, dynamic> json) => _$BackupFilesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$BackupFilesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

