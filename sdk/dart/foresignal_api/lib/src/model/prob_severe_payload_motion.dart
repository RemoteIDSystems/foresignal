//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'prob_severe_payload_motion.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProbSeverePayloadMotion {
  /// Returns a new [ProbSeverePayloadMotion] instance.
  ProbSeverePayloadMotion({

    required  this.speedKmh,

    required  this.bearingDeg,
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


  final int bearingDeg;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProbSeverePayloadMotion &&
      other.speedKmh == speedKmh &&
      other.bearingDeg == bearingDeg;

    @override
    int get hashCode =>
        speedKmh.hashCode +
        bearingDeg.hashCode;

  factory ProbSeverePayloadMotion.fromJson(Map<String, dynamic> json) => _$ProbSeverePayloadMotionFromJson(json);

  Map<String, dynamic> toJson() => _$ProbSeverePayloadMotionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

