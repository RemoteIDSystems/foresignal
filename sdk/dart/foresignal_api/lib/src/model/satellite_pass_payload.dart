//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/satellite_pass_payload_rf.dart';
import 'package:foresignal_api/src/model/satellite_pass_payload_products_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'satellite_pass_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SatellitePassPayload {
  /// Returns a new [SatellitePassPayload] instance.
  SatellitePassPayload({

    required  this.status,

    required  this.satellite,

    required  this.satKey,

     this.norad,

     this.pipelineId,

     this.frequencyHz,

    required  this.aos,

     this.los,

     this.maxElevationDeg,

     this.rf,

     this.instruments,

     this.products,

     this.tle,

     this.satdumpVersion,

    required  this.title,

     this.attribution,

     this.geometrySource,

     this.tca,

     this.aosAzimuthDeg,

     this.losAzimuthDeg,

     this.place,

     this.imminent,
  });

      /// `predicted` (064: Orekit from `celestrak-gp` elements), then `receiving`, `recorded`, `decoded` or `failed` (049); `missed` (064) when a predicted pass had no session by LOS + 20 min while the station was healthy (otherwise the predicted pass is tombstoned)
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SatellitePassPayloadStatusEnum status;



  @JsonKey(
    
    name: r'satellite',
    required: true,
    includeIfNull: false,
  )


  final String satellite;



  @JsonKey(
    
    name: r'satKey',
    required: true,
    includeIfNull: false,
  )


  final String satKey;



  @JsonKey(
    
    name: r'norad',
    required: false,
    includeIfNull: false,
  )


  final int? norad;



  @JsonKey(
    
    name: r'pipelineId',
    required: false,
    includeIfNull: false,
  )


  final String? pipelineId;



  @JsonKey(
    
    name: r'frequencyHz',
    required: false,
    includeIfNull: false,
  )


  final int? frequencyHz;



  @JsonKey(
    
    name: r'aos',
    required: true,
    includeIfNull: false,
  )


  final DateTime aos;



  @JsonKey(
    
    name: r'los',
    required: false,
    includeIfNull: false,
  )


  final DateTime? los;



  @JsonKey(
    
    name: r'maxElevationDeg',
    required: false,
    includeIfNull: false,
  )


  final num? maxElevationDeg;



  @JsonKey(
    
    name: r'rf',
    required: false,
    includeIfNull: false,
  )


  final SatellitePassPayloadRf? rf;



  @JsonKey(
    
    name: r'instruments',
    required: false,
    includeIfNull: false,
  )


  final List<String>? instruments;



  @JsonKey(
    
    name: r'products',
    required: false,
    includeIfNull: false,
  )


  final List<SatellitePassPayloadProductsInner>? products;



  @JsonKey(
    
    name: r'tle',
    required: false,
    includeIfNull: false,
  )


  final String? tle;



  @JsonKey(
    
    name: r'satdumpVersion',
    required: false,
    includeIfNull: false,
  )


  final String? satdumpVersion;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final Attribution? attribution;



      /// Present when the record geometry is not the station point: `footprint` (the alpha-mask outline of the pass's polar GeoTIFF overlays, 054) or `swath` (an approximate SGP4 swath from the TLE, used when no GeoTIFF was usable; never tiled)
  @JsonKey(
    
    name: r'geometrySource',
    required: false,
    includeIfNull: false,
  )


  final SatellitePassPayloadGeometrySourceEnum? geometrySource;



      /// Predicted time of maximum elevation (064, predicted passes)
  @JsonKey(
    
    name: r'tca',
    required: false,
    includeIfNull: false,
  )


  final DateTime? tca;



      /// Predicted azimuth at AOS, degrees (064)
  @JsonKey(
    
    name: r'aosAzimuthDeg',
    required: false,
    includeIfNull: false,
  )


  final num? aosAzimuthDeg;



      /// Predicted azimuth at LOS, degrees (064)
  @JsonKey(
    
    name: r'losAzimuthDeg',
    required: false,
    includeIfNull: false,
  )


  final num? losAzimuthDeg;



      /// The place id (064 `satellite-passes`, externalId `place:{placeId}:{norad}:{aosEpochMinute}`)
  @JsonKey(
    
    name: r'place',
    required: false,
    includeIfNull: false,
  )


  final String? place;



      /// 064 `satellite-passes`: true from 15 minutes before AOS until LOS (a new version near AOS, so `forecastHorizon` rules, which match `validFrom` = AOS on new versions only, and plain `imminent == true` rules can fire)
  @JsonKey(
    
    name: r'imminent',
    required: false,
    includeIfNull: false,
  )


  final bool? imminent;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SatellitePassPayload &&
      other.status == status &&
      other.satellite == satellite &&
      other.satKey == satKey &&
      other.norad == norad &&
      other.pipelineId == pipelineId &&
      other.frequencyHz == frequencyHz &&
      other.aos == aos &&
      other.los == los &&
      other.maxElevationDeg == maxElevationDeg &&
      other.rf == rf &&
      other.instruments == instruments &&
      other.products == products &&
      other.tle == tle &&
      other.satdumpVersion == satdumpVersion &&
      other.title == title &&
      other.attribution == attribution &&
      other.geometrySource == geometrySource &&
      other.tca == tca &&
      other.aosAzimuthDeg == aosAzimuthDeg &&
      other.losAzimuthDeg == losAzimuthDeg &&
      other.place == place &&
      other.imminent == imminent;

    @override
    int get hashCode =>
        status.hashCode +
        satellite.hashCode +
        satKey.hashCode +
        norad.hashCode +
        pipelineId.hashCode +
        frequencyHz.hashCode +
        aos.hashCode +
        (los == null ? 0 : los.hashCode) +
        maxElevationDeg.hashCode +
        rf.hashCode +
        instruments.hashCode +
        products.hashCode +
        (tle == null ? 0 : tle.hashCode) +
        (satdumpVersion == null ? 0 : satdumpVersion.hashCode) +
        title.hashCode +
        attribution.hashCode +
        geometrySource.hashCode +
        tca.hashCode +
        aosAzimuthDeg.hashCode +
        losAzimuthDeg.hashCode +
        place.hashCode +
        imminent.hashCode;

  factory SatellitePassPayload.fromJson(Map<String, dynamic> json) => _$SatellitePassPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$SatellitePassPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `predicted` (064: Orekit from `celestrak-gp` elements), then `receiving`, `recorded`, `decoded` or `failed` (049); `missed` (064) when a predicted pass had no session by LOS + 20 min while the station was healthy (otherwise the predicted pass is tombstoned)
enum SatellitePassPayloadStatusEnum {
@JsonValue(r'predicted')
predicted(r'predicted'),
@JsonValue(r'receiving')
receiving(r'receiving'),
@JsonValue(r'recorded')
recorded(r'recorded'),
@JsonValue(r'decoded')
decoded(r'decoded'),
@JsonValue(r'failed')
failed(r'failed'),
@JsonValue(r'missed')
missed(r'missed');

const SatellitePassPayloadStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Present when the record geometry is not the station point: `footprint` (the alpha-mask outline of the pass's polar GeoTIFF overlays, 054) or `swath` (an approximate SGP4 swath from the TLE, used when no GeoTIFF was usable; never tiled)
enum SatellitePassPayloadGeometrySourceEnum {
@JsonValue(r'footprint')
footprint(r'footprint'),
@JsonValue(r'swath')
swath(r'swath');

const SatellitePassPayloadGeometrySourceEnum(this.value);

final String value;

@override
String toString() => value;
}


