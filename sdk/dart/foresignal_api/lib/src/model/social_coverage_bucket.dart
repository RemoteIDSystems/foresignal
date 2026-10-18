//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_coverage_bucket.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialCoverageBucket {
  /// Returns a new [SocialCoverageBucket] instance.
  SocialCoverageBucket({

    required  this.hour,

    required  this.postsSeen,

     this.denominator,

    required  this.complete,
  });

  @JsonKey(
    
    name: r'hour',
    required: true,
    includeIfNull: false,
  )


  final DateTime hour;



      /// Posts attributed to the region (platform geo, 136 text-derived places)
          // minimum: 0
  @JsonKey(
    
    name: r'postsSeen',
    required: true,
    includeIfNull: false,
  )


  final int postsSeen;



      /// All posts seen on complete streams for the platform
          // minimum: 0
  @JsonKey(
    
    name: r'denominator',
    required: false,
    includeIfNull: false,
  )


  final int? denominator;



      /// Complete-stream collection was healthy for the whole hour
  @JsonKey(
    
    name: r'complete',
    required: true,
    includeIfNull: false,
  )


  final bool complete;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialCoverageBucket &&
      other.hour == hour &&
      other.postsSeen == postsSeen &&
      other.denominator == denominator &&
      other.complete == complete;

    @override
    int get hashCode =>
        hour.hashCode +
        postsSeen.hashCode +
        (denominator == null ? 0 : denominator.hashCode) +
        complete.hashCode;

  factory SocialCoverageBucket.fromJson(Map<String, dynamic> json) => _$SocialCoverageBucketFromJson(json);

  Map<String, dynamic> toJson() => _$SocialCoverageBucketToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

