//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'fire_detection_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FireDetectionPayload {
  /// Returns a new [FireDetectionPayload] instance.
  FireDetectionPayload({

     this.maskCode,

     this.confidence,

     this.confidenceClass,

     this.instrument,

     this.brightK,

     this.bright2K,

     this.scanKm,

     this.trackKm,

     this.daynight,

     this.version,

     this.type,

     this.temporallyFiltered,

     this.frpMw,

     this.tempK,

     this.areaKm2,

    required  this.satellite,

     this.method,

     this.ecosystem,

     this.source_,

     this.label,
  });

  @JsonKey(
    
    name: r'maskCode',
    required: false,
    includeIfNull: false,
  )


  final int? maskCode;



      /// goes-fdc: `processed`, `saturated`, `cloudy` or `high`. nasa-firms: the raw FIRMS value, `l`, `n` or `h` (VIIRS) or an integer 0-100 (MODIS)
  @JsonKey(
    
    name: r'confidence',
    required: false,
    includeIfNull: false,
  )


  final Object? confidence;



      /// nasa-firms: VIIRS letters mapped; MODIS below 30 low, 30-79 nominal, from 80 high
  @JsonKey(
    
    name: r'confidenceClass',
    required: false,
    includeIfNull: false,
  )


  final FireDetectionPayloadConfidenceClassEnum? confidenceClass;



      /// nasa-firms: VIIRS, MODIS or OLI, from the product
  @JsonKey(
    
    name: r'instrument',
    required: false,
    includeIfNull: false,
  )


  final String? instrument;



      /// nasa-firms: bright_ti4 (VIIRS) or brightness (MODIS), K
  @JsonKey(
    
    name: r'brightK',
    required: false,
    includeIfNull: false,
  )


  final num? brightK;



      /// nasa-firms: bright_ti5 (VIIRS) or bright_t31 (MODIS), K
  @JsonKey(
    
    name: r'bright2K',
    required: false,
    includeIfNull: false,
  )


  final num? bright2K;



  @JsonKey(
    
    name: r'scanKm',
    required: false,
    includeIfNull: false,
  )


  final num? scanKm;



  @JsonKey(
    
    name: r'trackKm',
    required: false,
    includeIfNull: false,
  )


  final num? trackKm;



      /// nasa-firms: D or N
  @JsonKey(
    
    name: r'daynight',
    required: false,
    includeIfNull: false,
  )


  final String? daynight;



      /// nasa-firms: collection and processing, for example 2.0NRT
  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final String? version;



      /// nasa-firms MODIS only, when present: 0 vegetation fire, 1 active volcano, 2 other static land source, 3 offshore
  @JsonKey(
    
    name: r'type',
    required: false,
    includeIfNull: false,
  )


  final int? type;



  @JsonKey(
    
    name: r'temporallyFiltered',
    required: false,
    includeIfNull: false,
  )


  final bool? temporallyFiltered;



      /// Fire radiative power, MW. goes-fdc and hms-fire: 2 significant figures; nasa-firms: the FIRMS frp column as sent
  @JsonKey(
    
    name: r'frpMw',
    required: false,
    includeIfNull: false,
  )


  final num? frpMw;



      /// Rounded to 10 K
  @JsonKey(
    
    name: r'tempK',
    required: false,
    includeIfNull: false,
  )


  final num? tempK;



  @JsonKey(
    
    name: r'areaKm2',
    required: false,
    includeIfNull: false,
  )


  final num? areaKm2;



  @JsonKey(
    
    name: r'satellite',
    required: true,
    includeIfNull: false,
  )


  final String satellite;



  @JsonKey(
    
    name: r'method',
    required: false,
    includeIfNull: false,
  )


  final String? method;



      /// hms-fire only
  @JsonKey(
    
    name: r'ecosystem',
    required: false,
    includeIfNull: false,
  )


  final String? ecosystem;



      /// `HMS` for hms-fire; the FIRMS product (for example `VIIRS_NOAA20_NRT`) for nasa-firms
  @JsonKey(
    
    name: r'source',
    required: false,
    includeIfNull: false,
  )


  final String? source_;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FireDetectionPayload &&
      other.maskCode == maskCode &&
      other.confidence == confidence &&
      other.confidenceClass == confidenceClass &&
      other.instrument == instrument &&
      other.brightK == brightK &&
      other.bright2K == bright2K &&
      other.scanKm == scanKm &&
      other.trackKm == trackKm &&
      other.daynight == daynight &&
      other.version == version &&
      other.type == type &&
      other.temporallyFiltered == temporallyFiltered &&
      other.frpMw == frpMw &&
      other.tempK == tempK &&
      other.areaKm2 == areaKm2 &&
      other.satellite == satellite &&
      other.method == method &&
      other.ecosystem == ecosystem &&
      other.source_ == source_ &&
      other.label == label;

    @override
    int get hashCode =>
        maskCode.hashCode +
        (confidence == null ? 0 : confidence.hashCode) +
        confidenceClass.hashCode +
        instrument.hashCode +
        brightK.hashCode +
        bright2K.hashCode +
        scanKm.hashCode +
        trackKm.hashCode +
        daynight.hashCode +
        version.hashCode +
        type.hashCode +
        temporallyFiltered.hashCode +
        frpMw.hashCode +
        tempK.hashCode +
        areaKm2.hashCode +
        satellite.hashCode +
        method.hashCode +
        ecosystem.hashCode +
        source_.hashCode +
        label.hashCode;

  factory FireDetectionPayload.fromJson(Map<String, dynamic> json) => _$FireDetectionPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$FireDetectionPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// nasa-firms: VIIRS letters mapped; MODIS below 30 low, 30-79 nominal, from 80 high
enum FireDetectionPayloadConfidenceClassEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'nominal')
nominal(r'nominal'),
@JsonValue(r'high')
high(r'high');

const FireDetectionPayloadConfidenceClassEnum(this.value);

final String value;

@override
String toString() => value;
}


