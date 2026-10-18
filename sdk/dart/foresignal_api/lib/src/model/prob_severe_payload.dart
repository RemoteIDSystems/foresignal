//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/prob_severe_payload_motion.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'prob_severe_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProbSeverePayload {
  /// Returns a new [ProbSeverePayload] instance.
  ProbSeverePayload({

    required  this.probSevere,

    required  this.probTor,

    required  this.probHail,

    required  this.probWind,

     this.meshIn,

     this.compRefDbz,

     this.vil,

     this.echoTop50Kft,

     this.flashRate,

     this.muCape,

     this.mlCape,

     this.ebShearKt,

     this.srh01km,

     this.motion,

    required  this.label,

    required  this.automated,

    required  this.notAWarning,
  });

  @JsonKey(
    
    name: r'probSevere',
    required: true,
    includeIfNull: false,
  )


  final int probSevere;



  @JsonKey(
    
    name: r'probTor',
    required: true,
    includeIfNull: false,
  )


  final int probTor;



  @JsonKey(
    
    name: r'probHail',
    required: true,
    includeIfNull: false,
  )


  final int probHail;



  @JsonKey(
    
    name: r'probWind',
    required: true,
    includeIfNull: false,
  )


  final int probWind;



      /// 0.25 in steps
  @JsonKey(
    
    name: r'meshIn',
    required: false,
    includeIfNull: false,
  )


  final num? meshIn;



  @JsonKey(
    
    name: r'compRefDbz',
    required: false,
    includeIfNull: false,
  )


  final num? compRefDbz;



  @JsonKey(
    
    name: r'vil',
    required: false,
    includeIfNull: false,
  )


  final num? vil;



  @JsonKey(
    
    name: r'echoTop50Kft',
    required: false,
    includeIfNull: false,
  )


  final num? echoTop50Kft;



  @JsonKey(
    
    name: r'flashRate',
    required: false,
    includeIfNull: false,
  )


  final num? flashRate;



  @JsonKey(
    
    name: r'muCape',
    required: false,
    includeIfNull: false,
  )


  final num? muCape;



  @JsonKey(
    
    name: r'mlCape',
    required: false,
    includeIfNull: false,
  )


  final num? mlCape;



  @JsonKey(
    
    name: r'ebShearKt',
    required: false,
    includeIfNull: false,
  )


  final num? ebShearKt;



  @JsonKey(
    
    name: r'srh01km',
    required: false,
    includeIfNull: false,
  )


  final num? srh01km;



  @JsonKey(
    
    name: r'motion',
    required: false,
    includeIfNull: false,
  )


  final ProbSeverePayloadMotion? motion;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final String label;



  @JsonKey(
    
    name: r'automated',
    required: true,
    includeIfNull: false,
  )


  final bool automated;



  @JsonKey(
    
    name: r'notAWarning',
    required: true,
    includeIfNull: false,
  )


  final bool notAWarning;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProbSeverePayload &&
      other.probSevere == probSevere &&
      other.probTor == probTor &&
      other.probHail == probHail &&
      other.probWind == probWind &&
      other.meshIn == meshIn &&
      other.compRefDbz == compRefDbz &&
      other.vil == vil &&
      other.echoTop50Kft == echoTop50Kft &&
      other.flashRate == flashRate &&
      other.muCape == muCape &&
      other.mlCape == mlCape &&
      other.ebShearKt == ebShearKt &&
      other.srh01km == srh01km &&
      other.motion == motion &&
      other.label == label &&
      other.automated == automated &&
      other.notAWarning == notAWarning;

    @override
    int get hashCode =>
        probSevere.hashCode +
        probTor.hashCode +
        probHail.hashCode +
        probWind.hashCode +
        meshIn.hashCode +
        compRefDbz.hashCode +
        vil.hashCode +
        echoTop50Kft.hashCode +
        flashRate.hashCode +
        muCape.hashCode +
        mlCape.hashCode +
        ebShearKt.hashCode +
        srh01km.hashCode +
        motion.hashCode +
        label.hashCode +
        automated.hashCode +
        notAWarning.hashCode;

  factory ProbSeverePayload.fromJson(Map<String, dynamic> json) => _$ProbSeverePayloadFromJson(json);

  Map<String, dynamic> toJson() => _$ProbSeverePayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

