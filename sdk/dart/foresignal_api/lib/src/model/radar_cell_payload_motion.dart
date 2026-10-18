//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'radar_cell_payload_motion.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RadarCellPayloadMotion {
  /// Returns a new [RadarCellPayloadMotion] instance.
  RadarCellPayloadMotion({

     this.speedKmh,

     this.bearingDeg,

    required  this.method,
  });

  @JsonKey(
    
    name: r'speedKmh',
    required: false,
    includeIfNull: false,
  )


  final num? speedKmh;



  @JsonKey(
    
    name: r'bearingDeg',
    required: false,
    includeIfNull: false,
  )


  final int? bearingDeg;



  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final RadarCellPayloadMotionMethodEnum method;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RadarCellPayloadMotion &&
      other.speedKmh == speedKmh &&
      other.bearingDeg == bearingDeg &&
      other.method == method;

    @override
    int get hashCode =>
        speedKmh.hashCode +
        bearingDeg.hashCode +
        method.hashCode;

  factory RadarCellPayloadMotion.fromJson(Map<String, dynamic> json) => _$RadarCellPayloadMotionFromJson(json);

  Map<String, dynamic> toJson() => _$RadarCellPayloadMotionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RadarCellPayloadMotionMethodEnum {
@JsonValue(r'centroid-v1')
centroidV1(r'centroid-v1');

const RadarCellPayloadMotionMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


