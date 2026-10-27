//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 's3_target.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class S3Target {
  /// Returns a new [S3Target] instance.
  S3Target({

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
    bool operator ==(Object other) => identical(this, other) || other is S3Target &&
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

  factory S3Target.fromJson(Map<String, dynamic> json) => _$S3TargetFromJson(json);

  Map<String, dynamic> toJson() => _$S3TargetToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

