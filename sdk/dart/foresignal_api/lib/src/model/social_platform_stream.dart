//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_platform_stream_last_gap.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_platform_stream.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialPlatformStream {
  /// Returns a new [SocialPlatformStream] instance.
  SocialPlatformStream({

     this.lagSeconds,

     this.cursor,

     this.connectedSince,

     this.lastGap,
  });

  @JsonKey(
    
    name: r'lagSeconds',
    required: false,
    includeIfNull: false,
  )


  final num? lagSeconds;



      /// Jetstream `time_us` or platform stream cursor
  @JsonKey(
    
    name: r'cursor',
    required: false,
    includeIfNull: false,
  )


  final String? cursor;



  @JsonKey(
    
    name: r'connectedSince',
    required: false,
    includeIfNull: false,
  )


  final DateTime? connectedSince;



  @JsonKey(
    
    name: r'lastGap',
    required: false,
    includeIfNull: false,
  )


  final SocialPlatformStreamLastGap? lastGap;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialPlatformStream &&
      other.lagSeconds == lagSeconds &&
      other.cursor == cursor &&
      other.connectedSince == connectedSince &&
      other.lastGap == lastGap;

    @override
    int get hashCode =>
        (lagSeconds == null ? 0 : lagSeconds.hashCode) +
        (cursor == null ? 0 : cursor.hashCode) +
        (connectedSince == null ? 0 : connectedSince.hashCode) +
        (lastGap == null ? 0 : lastGap.hashCode);

  factory SocialPlatformStream.fromJson(Map<String, dynamic> json) => _$SocialPlatformStreamFromJson(json);

  Map<String, dynamic> toJson() => _$SocialPlatformStreamToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

