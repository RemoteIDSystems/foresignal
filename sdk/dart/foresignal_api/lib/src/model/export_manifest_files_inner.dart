//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'export_manifest_files_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExportManifestFilesInner {
  /// Returns a new [ExportManifestFilesInner] instance.
  ExportManifestFilesInner({

    required  this.name,

     this.mediaType,

    required  this.bytes,

    required  this.sha256,

     this.rows,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'mediaType',
    required: false,
    includeIfNull: false,
  )


  final String? mediaType;



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
    
    name: r'rows',
    required: false,
    includeIfNull: false,
  )


  final int? rows;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExportManifestFilesInner &&
      other.name == name &&
      other.mediaType == mediaType &&
      other.bytes == bytes &&
      other.sha256 == sha256 &&
      other.rows == rows;

    @override
    int get hashCode =>
        name.hashCode +
        mediaType.hashCode +
        bytes.hashCode +
        sha256.hashCode +
        (rows == null ? 0 : rows.hashCode);

  factory ExportManifestFilesInner.fromJson(Map<String, dynamic> json) => _$ExportManifestFilesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ExportManifestFilesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

