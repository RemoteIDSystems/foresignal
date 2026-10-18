//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'storm_track_payload_lightning_jump.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StormTrackPayloadLightningJump {
  /// Returns a new [StormTrackPayloadLightningJump] instance.
  StormTrackPayloadLightningJump({

    required  this.at,

     this.sigma,

     this.rateFpm,

    required  this.method,
  });

  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'sigma',
    required: false,
    includeIfNull: false,
  )


  final num? sigma;



  @JsonKey(
    
    name: r'rateFpm',
    required: false,
    includeIfNull: false,
  )


  final num? rateFpm;



  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final StormTrackPayloadLightningJumpMethodEnum method;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StormTrackPayloadLightningJump &&
      other.at == at &&
      other.sigma == sigma &&
      other.rateFpm == rateFpm &&
      other.method == method;

    @override
    int get hashCode =>
        at.hashCode +
        sigma.hashCode +
        rateFpm.hashCode +
        method.hashCode;

  factory StormTrackPayloadLightningJump.fromJson(Map<String, dynamic> json) => _$StormTrackPayloadLightningJumpFromJson(json);

  Map<String, dynamic> toJson() => _$StormTrackPayloadLightningJumpToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StormTrackPayloadLightningJumpMethodEnum {
@JsonValue(r'2sigma-glm-v1')
n2sigmaGlmV1(r'2sigma-glm-v1');

const StormTrackPayloadLightningJumpMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


