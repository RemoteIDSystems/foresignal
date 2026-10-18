//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/radar_cell_payload_motion.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'radar_cell_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RadarCellPayload {
  /// Returns a new [RadarCellPayload] instance.
  RadarCellPayload({

    required  this.maxDbz,

    required  this.areaKm2,

     this.meshMax,

     this.glmFlashes5m,

     this.motion,

     this.automated,

     this.notAWarning,
  });

  @JsonKey(
    
    name: r'maxDbz',
    required: true,
    includeIfNull: false,
  )


  final num maxDbz;



  @JsonKey(
    
    name: r'areaKm2',
    required: true,
    includeIfNull: false,
  )


  final num areaKm2;



  @JsonKey(
    
    name: r'meshMax',
    required: false,
    includeIfNull: false,
  )


  final num? meshMax;



  @JsonKey(
    
    name: r'glmFlashes5m',
    required: false,
    includeIfNull: false,
  )


  final int? glmFlashes5m;



  @JsonKey(
    
    name: r'motion',
    required: false,
    includeIfNull: false,
  )


  final RadarCellPayloadMotion? motion;



  @JsonKey(
    
    name: r'automated',
    required: false,
    includeIfNull: false,
  )


  final bool? automated;



  @JsonKey(
    
    name: r'notAWarning',
    required: false,
    includeIfNull: false,
  )


  final bool? notAWarning;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RadarCellPayload &&
      other.maxDbz == maxDbz &&
      other.areaKm2 == areaKm2 &&
      other.meshMax == meshMax &&
      other.glmFlashes5m == glmFlashes5m &&
      other.motion == motion &&
      other.automated == automated &&
      other.notAWarning == notAWarning;

    @override
    int get hashCode =>
        maxDbz.hashCode +
        areaKm2.hashCode +
        (meshMax == null ? 0 : meshMax.hashCode) +
        glmFlashes5m.hashCode +
        motion.hashCode +
        automated.hashCode +
        notAWarning.hashCode;

  factory RadarCellPayload.fromJson(Map<String, dynamic> json) => _$RadarCellPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$RadarCellPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

