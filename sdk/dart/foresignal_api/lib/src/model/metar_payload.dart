//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/metar_payload_sky_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'metar_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MetarPayload {
  /// Returns a new [MetarPayload] instance.
  MetarPayload({

    required  this.raw,

    required  this.stationId,

     this.metarType,

     this.tempC,

     this.dewpointC,

     this.windDirDeg,

     this.windVariable,

     this.windKt,

     this.gustKt,

     this.visibilitySm,

     this.visibilityPlus,

     this.altimInHg,

     this.slpMb,

     this.wx,

     this.sky,

     this.flightCategory,

     this.elevationM,
  });

  @JsonKey(
    
    name: r'raw',
    required: true,
    includeIfNull: false,
  )


  final String raw;



  @JsonKey(
    
    name: r'stationId',
    required: true,
    includeIfNull: false,
  )


  final String stationId;



  @JsonKey(
    
    name: r'metarType',
    required: false,
    includeIfNull: false,
  )


  final MetarPayloadMetarTypeEnum? metarType;



  @JsonKey(
    
    name: r'tempC',
    required: false,
    includeIfNull: false,
  )


  final num? tempC;



  @JsonKey(
    
    name: r'dewpointC',
    required: false,
    includeIfNull: false,
  )


  final num? dewpointC;



  @JsonKey(
    
    name: r'windDirDeg',
    required: false,
    includeIfNull: false,
  )


  final int? windDirDeg;



  @JsonKey(
    
    name: r'windVariable',
    required: false,
    includeIfNull: false,
  )


  final bool? windVariable;



  @JsonKey(
    
    name: r'windKt',
    required: false,
    includeIfNull: false,
  )


  final int? windKt;



  @JsonKey(
    
    name: r'gustKt',
    required: false,
    includeIfNull: false,
  )


  final int? gustKt;



  @JsonKey(
    
    name: r'visibilitySm',
    required: false,
    includeIfNull: false,
  )


  final num? visibilitySm;



  @JsonKey(
    
    name: r'visibilityPlus',
    required: false,
    includeIfNull: false,
  )


  final bool? visibilityPlus;



  @JsonKey(
    
    name: r'altimInHg',
    required: false,
    includeIfNull: false,
  )


  final num? altimInHg;



  @JsonKey(
    
    name: r'slpMb',
    required: false,
    includeIfNull: false,
  )


  final num? slpMb;



  @JsonKey(
    
    name: r'wx',
    required: false,
    includeIfNull: false,
  )


  final String? wx;



  @JsonKey(
    
    name: r'sky',
    required: false,
    includeIfNull: false,
  )


  final List<MetarPayloadSkyInner>? sky;



  @JsonKey(
    
    name: r'flightCategory',
    required: false,
    includeIfNull: false,
  )


  final MetarPayloadFlightCategoryEnum? flightCategory;



  @JsonKey(
    
    name: r'elevationM',
    required: false,
    includeIfNull: false,
  )


  final num? elevationM;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MetarPayload &&
      other.raw == raw &&
      other.stationId == stationId &&
      other.metarType == metarType &&
      other.tempC == tempC &&
      other.dewpointC == dewpointC &&
      other.windDirDeg == windDirDeg &&
      other.windVariable == windVariable &&
      other.windKt == windKt &&
      other.gustKt == gustKt &&
      other.visibilitySm == visibilitySm &&
      other.visibilityPlus == visibilityPlus &&
      other.altimInHg == altimInHg &&
      other.slpMb == slpMb &&
      other.wx == wx &&
      other.sky == sky &&
      other.flightCategory == flightCategory &&
      other.elevationM == elevationM;

    @override
    int get hashCode =>
        raw.hashCode +
        stationId.hashCode +
        metarType.hashCode +
        tempC.hashCode +
        dewpointC.hashCode +
        windDirDeg.hashCode +
        windVariable.hashCode +
        windKt.hashCode +
        gustKt.hashCode +
        visibilitySm.hashCode +
        visibilityPlus.hashCode +
        altimInHg.hashCode +
        slpMb.hashCode +
        wx.hashCode +
        sky.hashCode +
        flightCategory.hashCode +
        elevationM.hashCode;

  factory MetarPayload.fromJson(Map<String, dynamic> json) => _$MetarPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$MetarPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MetarPayloadMetarTypeEnum {
@JsonValue(r'METAR')
METAR(r'METAR'),
@JsonValue(r'SPECI')
SPECI(r'SPECI');

const MetarPayloadMetarTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum MetarPayloadFlightCategoryEnum {
@JsonValue(r'VFR')
VFR(r'VFR'),
@JsonValue(r'MVFR')
MVFR(r'MVFR'),
@JsonValue(r'IFR')
IFR(r'IFR'),
@JsonValue(r'LIFR')
LIFR(r'LIFR');

const MetarPayloadFlightCategoryEnum(this.value);

final String value;

@override
String toString() => value;
}


