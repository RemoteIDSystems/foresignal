//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'export_file.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExportFile {
  /// Returns a new [ExportFile] instance.
  ExportFile({

    required  this.name,

    required  this.url,

    required  this.bytes,

    required  this.sha256,

     this.mediaType,

    required  this.expiresAt,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'url',
    required: true,
    includeIfNull: false,
  )


  final String url;



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
    
    name: r'mediaType',
    required: false,
    includeIfNull: false,
  )


  final String? mediaType;



  @JsonKey(
    
    name: r'expiresAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime expiresAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExportFile &&
      other.name == name &&
      other.url == url &&
      other.bytes == bytes &&
      other.sha256 == sha256 &&
      other.mediaType == mediaType &&
      other.expiresAt == expiresAt;

    @override
    int get hashCode =>
        name.hashCode +
        url.hashCode +
        bytes.hashCode +
        sha256.hashCode +
        mediaType.hashCode +
        expiresAt.hashCode;

  factory ExportFile.fromJson(Map<String, dynamic> json) => _$ExportFileFromJson(json);

  Map<String, dynamic> toJson() => _$ExportFileToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

