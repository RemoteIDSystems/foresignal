//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'archive_settings_s3.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ArchiveSettingsS3 {
  /// Returns a new [ArchiveSettingsS3] instance.
  ArchiveSettingsS3({

    required  this.endpoint,

    required  this.bucket,

     this.prefix,

     this.region,

     this.pathStyle,
  });

  @JsonKey(
    
    name: r'endpoint',
    required: true,
    includeIfNull: false,
  )


  final String endpoint;



  @JsonKey(
    
    name: r'bucket',
    required: true,
    includeIfNull: false,
  )


  final String bucket;



  @JsonKey(
    
    name: r'prefix',
    required: false,
    includeIfNull: false,
  )


  final String? prefix;



  @JsonKey(
    
    name: r'region',
    required: false,
    includeIfNull: false,
  )


  final String? region;



  @JsonKey(
    
    name: r'pathStyle',
    required: false,
    includeIfNull: false,
  )


  final bool? pathStyle;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ArchiveSettingsS3 &&
      other.endpoint == endpoint &&
      other.bucket == bucket &&
      other.prefix == prefix &&
      other.region == region &&
      other.pathStyle == pathStyle;

    @override
    int get hashCode =>
        endpoint.hashCode +
        bucket.hashCode +
        prefix.hashCode +
        region.hashCode +
        pathStyle.hashCode;

  factory ArchiveSettingsS3.fromJson(Map<String, dynamic> json) => _$ArchiveSettingsS3FromJson(json);

  Map<String, dynamic> toJson() => _$ArchiveSettingsS3ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

