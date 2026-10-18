//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'storm_track_payload_motion.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StormTrackPayloadMotion {
  /// Returns a new [StormTrackPayloadMotion] instance.
  StormTrackPayloadMotion({

    required  this.speedKmh,

    required  this.bearingDeg,

    required  this.method,
  });

  @JsonKey(
    
    name: r'speedKmh',
    required: true,
    includeIfNull: false,
  )


  final num speedKmh;



  @JsonKey(
    
    name: r'bearingDeg',
    required: true,
    includeIfNull: false,
  )


  final num bearingDeg;



  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final StormTrackPayloadMotionMethodEnum method;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StormTrackPayloadMotion &&
      other.speedKmh == speedKmh &&
      other.bearingDeg == bearingDeg &&
      other.method == method;

    @override
    int get hashCode =>
        speedKmh.hashCode +
        bearingDeg.hashCode +
        method.hashCode;

  factory StormTrackPayloadMotion.fromJson(Map<String, dynamic> json) => _$StormTrackPayloadMotionFromJson(json);

  Map<String, dynamic> toJson() => _$StormTrackPayloadMotionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StormTrackPayloadMotionMethodEnum {
@JsonValue(r'titan-lite-v1')
titanLiteV1(r'titan-lite-v1'),
@JsonValue(r'centroid-v1')
centroidV1(r'centroid-v1');

const StormTrackPayloadMotionMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


