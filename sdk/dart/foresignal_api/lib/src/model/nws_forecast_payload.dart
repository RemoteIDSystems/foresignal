//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/nws_forecast_payload_hazards_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'nws_forecast_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NwsForecastPayload {
  /// Returns a new [NwsForecastPayload] instance.
  NwsForecastPayload({

    required  this.place,

    required  this.wfo,

    required  this.gridX,

    required  this.gridY,

     this.popMax24h,

     this.qpf24hMm,

     this.tMaxC24h,

     this.tMinC24h,

     this.gustMaxMs24h,

     this.hazards,

    required  this.forecastUpdated,
  });

  @JsonKey(
    
    name: r'place',
    required: true,
    includeIfNull: false,
  )


  final String place;



  @JsonKey(
    
    name: r'wfo',
    required: true,
    includeIfNull: false,
  )


  final String wfo;



  @JsonKey(
    
    name: r'gridX',
    required: true,
    includeIfNull: false,
  )


  final int gridX;



  @JsonKey(
    
    name: r'gridY',
    required: true,
    includeIfNull: false,
  )


  final int gridY;



  @JsonKey(
    
    name: r'popMax24h',
    required: false,
    includeIfNull: false,
  )


  final int? popMax24h;



  @JsonKey(
    
    name: r'qpf24hMm',
    required: false,
    includeIfNull: false,
  )


  final num? qpf24hMm;



  @JsonKey(
    
    name: r'tMaxC24h',
    required: false,
    includeIfNull: false,
  )


  final num? tMaxC24h;



  @JsonKey(
    
    name: r'tMinC24h',
    required: false,
    includeIfNull: false,
  )


  final num? tMinC24h;



  @JsonKey(
    
    name: r'gustMaxMs24h',
    required: false,
    includeIfNull: false,
  )


  final num? gustMaxMs24h;



  @JsonKey(
    
    name: r'hazards',
    required: false,
    includeIfNull: false,
  )


  final List<NwsForecastPayloadHazardsInner>? hazards;



  @JsonKey(
    
    name: r'forecastUpdated',
    required: true,
    includeIfNull: false,
  )


  final DateTime forecastUpdated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NwsForecastPayload &&
      other.place == place &&
      other.wfo == wfo &&
      other.gridX == gridX &&
      other.gridY == gridY &&
      other.popMax24h == popMax24h &&
      other.qpf24hMm == qpf24hMm &&
      other.tMaxC24h == tMaxC24h &&
      other.tMinC24h == tMinC24h &&
      other.gustMaxMs24h == gustMaxMs24h &&
      other.hazards == hazards &&
      other.forecastUpdated == forecastUpdated;

    @override
    int get hashCode =>
        place.hashCode +
        wfo.hashCode +
        gridX.hashCode +
        gridY.hashCode +
        popMax24h.hashCode +
        qpf24hMm.hashCode +
        tMaxC24h.hashCode +
        tMinC24h.hashCode +
        gustMaxMs24h.hashCode +
        hazards.hashCode +
        forecastUpdated.hashCode;

  factory NwsForecastPayload.fromJson(Map<String, dynamic> json) => _$NwsForecastPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$NwsForecastPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

