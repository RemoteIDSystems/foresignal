//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_platform_stream_last_gap.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialPlatformStreamLastGap {
  /// Returns a new [SocialPlatformStreamLastGap] instance.
  SocialPlatformStreamLastGap({

    required  this.from,

    required  this.to,

    required  this.replayed,
  });

  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final DateTime to;



  @JsonKey(
    
    name: r'replayed',
    required: true,
    includeIfNull: false,
  )


  final bool replayed;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialPlatformStreamLastGap &&
      other.from == from &&
      other.to == to &&
      other.replayed == replayed;

    @override
    int get hashCode =>
        from.hashCode +
        to.hashCode +
        replayed.hashCode;

  factory SocialPlatformStreamLastGap.fromJson(Map<String, dynamic> json) => _$SocialPlatformStreamLastGapFromJson(json);

  Map<String, dynamic> toJson() => _$SocialPlatformStreamLastGapToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

