//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'celestrak_gp_element.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CelestrakGpElement {
  /// Returns a new [CelestrakGpElement] instance.
  CelestrakGpElement({

    required  this.OBJECT_NAME,

    required  this.OBJECT_ID,

    required  this.EPOCH,

    required  this.MEAN_MOTION,

    required  this.ECCENTRICITY,

    required  this.INCLINATION,

    required  this.RA_OF_ASC_NODE,

    required  this.ARG_OF_PERICENTER,

    required  this.MEAN_ANOMALY,

     this.EPHEMERIS_TYPE,

     this.CLASSIFICATION_TYPE,

    required  this.NORAD_CAT_ID,

     this.ELEMENT_SET_NO,

     this.REV_AT_EPOCH,

     this.BSTAR,

     this.MEAN_MOTION_DOT,

     this.MEAN_MOTION_DDOT,
  });

  @JsonKey(
    
    name: r'OBJECT_NAME',
    required: true,
    includeIfNull: false,
  )


  final String OBJECT_NAME;



  @JsonKey(
    
    name: r'OBJECT_ID',
    required: true,
    includeIfNull: false,
  )


  final String OBJECT_ID;



      /// UTC, no zone suffix
  @JsonKey(
    
    name: r'EPOCH',
    required: true,
    includeIfNull: false,
  )


  final String EPOCH;



  @JsonKey(
    
    name: r'MEAN_MOTION',
    required: true,
    includeIfNull: false,
  )


  final num MEAN_MOTION;



  @JsonKey(
    
    name: r'ECCENTRICITY',
    required: true,
    includeIfNull: false,
  )


  final num ECCENTRICITY;



  @JsonKey(
    
    name: r'INCLINATION',
    required: true,
    includeIfNull: false,
  )


  final num INCLINATION;



  @JsonKey(
    
    name: r'RA_OF_ASC_NODE',
    required: true,
    includeIfNull: false,
  )


  final num RA_OF_ASC_NODE;



  @JsonKey(
    
    name: r'ARG_OF_PERICENTER',
    required: true,
    includeIfNull: false,
  )


  final num ARG_OF_PERICENTER;



  @JsonKey(
    
    name: r'MEAN_ANOMALY',
    required: true,
    includeIfNull: false,
  )


  final num MEAN_ANOMALY;



  @JsonKey(
    
    name: r'EPHEMERIS_TYPE',
    required: false,
    includeIfNull: false,
  )


  final int? EPHEMERIS_TYPE;



  @JsonKey(
    
    name: r'CLASSIFICATION_TYPE',
    required: false,
    includeIfNull: false,
  )


  final String? CLASSIFICATION_TYPE;



      /// Up to 9 digits; never assume 5
  @JsonKey(
    
    name: r'NORAD_CAT_ID',
    required: true,
    includeIfNull: false,
  )


  final int NORAD_CAT_ID;



  @JsonKey(
    
    name: r'ELEMENT_SET_NO',
    required: false,
    includeIfNull: false,
  )


  final int? ELEMENT_SET_NO;



  @JsonKey(
    
    name: r'REV_AT_EPOCH',
    required: false,
    includeIfNull: false,
  )


  final int? REV_AT_EPOCH;



  @JsonKey(
    
    name: r'BSTAR',
    required: false,
    includeIfNull: false,
  )


  final num? BSTAR;



  @JsonKey(
    
    name: r'MEAN_MOTION_DOT',
    required: false,
    includeIfNull: false,
  )


  final num? MEAN_MOTION_DOT;



  @JsonKey(
    
    name: r'MEAN_MOTION_DDOT',
    required: false,
    includeIfNull: false,
  )


  final num? MEAN_MOTION_DDOT;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CelestrakGpElement &&
      other.OBJECT_NAME == OBJECT_NAME &&
      other.OBJECT_ID == OBJECT_ID &&
      other.EPOCH == EPOCH &&
      other.MEAN_MOTION == MEAN_MOTION &&
      other.ECCENTRICITY == ECCENTRICITY &&
      other.INCLINATION == INCLINATION &&
      other.RA_OF_ASC_NODE == RA_OF_ASC_NODE &&
      other.ARG_OF_PERICENTER == ARG_OF_PERICENTER &&
      other.MEAN_ANOMALY == MEAN_ANOMALY &&
      other.EPHEMERIS_TYPE == EPHEMERIS_TYPE &&
      other.CLASSIFICATION_TYPE == CLASSIFICATION_TYPE &&
      other.NORAD_CAT_ID == NORAD_CAT_ID &&
      other.ELEMENT_SET_NO == ELEMENT_SET_NO &&
      other.REV_AT_EPOCH == REV_AT_EPOCH &&
      other.BSTAR == BSTAR &&
      other.MEAN_MOTION_DOT == MEAN_MOTION_DOT &&
      other.MEAN_MOTION_DDOT == MEAN_MOTION_DDOT;

    @override
    int get hashCode =>
        OBJECT_NAME.hashCode +
        OBJECT_ID.hashCode +
        EPOCH.hashCode +
        MEAN_MOTION.hashCode +
        ECCENTRICITY.hashCode +
        INCLINATION.hashCode +
        RA_OF_ASC_NODE.hashCode +
        ARG_OF_PERICENTER.hashCode +
        MEAN_ANOMALY.hashCode +
        EPHEMERIS_TYPE.hashCode +
        CLASSIFICATION_TYPE.hashCode +
        NORAD_CAT_ID.hashCode +
        ELEMENT_SET_NO.hashCode +
        REV_AT_EPOCH.hashCode +
        BSTAR.hashCode +
        MEAN_MOTION_DOT.hashCode +
        MEAN_MOTION_DDOT.hashCode;

  factory CelestrakGpElement.fromJson(Map<String, dynamic> json) => _$CelestrakGpElementFromJson(json);

  Map<String, dynamic> toJson() => _$CelestrakGpElementToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

