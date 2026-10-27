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

     this.storageClass,
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



      /// `x-amz-storage-class` of uploads (R2-016); null uses the bucket default. With target `glacier` it must be GLACIER (the default) or DEEP_ARCHIVE; with target `s3` it may not be either, since those objects cannot be read until thawed.
  @JsonKey(
    
    name: r'storageClass',
    required: false,
    includeIfNull: false,
  )


  final ArchiveSettingsS3StorageClassEnum? storageClass;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ArchiveSettingsS3 &&
      other.endpoint == endpoint &&
      other.bucket == bucket &&
      other.prefix == prefix &&
      other.region == region &&
      other.pathStyle == pathStyle &&
      other.storageClass == storageClass;

    @override
    int get hashCode =>
        endpoint.hashCode +
        bucket.hashCode +
        prefix.hashCode +
        region.hashCode +
        pathStyle.hashCode +
        (storageClass == null ? 0 : storageClass.hashCode);

  factory ArchiveSettingsS3.fromJson(Map<String, dynamic> json) => _$ArchiveSettingsS3FromJson(json);

  Map<String, dynamic> toJson() => _$ArchiveSettingsS3ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `x-amz-storage-class` of uploads (R2-016); null uses the bucket default. With target `glacier` it must be GLACIER (the default) or DEEP_ARCHIVE; with target `s3` it may not be either, since those objects cannot be read until thawed.
enum ArchiveSettingsS3StorageClassEnum {
@JsonValue(r'STANDARD')
STANDARD(r'STANDARD'),
@JsonValue(r'REDUCED_REDUNDANCY')
REDUCED_REDUNDANCY(r'REDUCED_REDUNDANCY'),
@JsonValue(r'STANDARD_IA')
STANDARD_IA(r'STANDARD_IA'),
@JsonValue(r'ONEZONE_IA')
ONEZONE_IA(r'ONEZONE_IA'),
@JsonValue(r'INTELLIGENT_TIERING')
INTELLIGENT_TIERING(r'INTELLIGENT_TIERING'),
@JsonValue(r'GLACIER_IR')
GLACIER_IR(r'GLACIER_IR'),
@JsonValue(r'GLACIER')
GLACIER(r'GLACIER'),
@JsonValue(r'DEEP_ARCHIVE')
DEEP_ARCHIVE(r'DEEP_ARCHIVE');

const ArchiveSettingsS3StorageClassEnum(this.value);

final String value;

@override
String toString() => value;
}


