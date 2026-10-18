//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'wx_area_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WxAreaPayload {
  /// Returns a new [WxAreaPayload] instance.
  WxAreaPayload({

    required  this.field,

    required  this.threshold,

    required  this.aoi,

    required  this.status,

     this.asOf,

     this.areaKm2,

     this.maxValue,

     this.coveragePct,

     this.automated,

     this.notAWarning,
  });

      /// rotation, qpe1h, ffg added by 053
  @JsonKey(
    
    name: r'field',
    required: true,
    includeIfNull: false,
  )


  final WxAreaPayloadFieldEnum field;



  @JsonKey(
    
    name: r'threshold',
    required: true,
    includeIfNull: false,
  )


  final num threshold;



      /// Place id or `aoi`
  @JsonKey(
    
    name: r'aoi',
    required: true,
    includeIfNull: false,
  )


  final String aoi;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final WeatherSectionStatus status;



  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



      /// 2 significant figures
  @JsonKey(
    
    name: r'areaKm2',
    required: false,
    includeIfNull: false,
  )


  final num? areaKm2;



  @JsonKey(
    
    name: r'maxValue',
    required: false,
    includeIfNull: false,
  )


  final num? maxValue;



  @JsonKey(
    
    name: r'coveragePct',
    required: false,
    includeIfNull: false,
  )


  final int? coveragePct;



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
    bool operator ==(Object other) => identical(this, other) || other is WxAreaPayload &&
      other.field == field &&
      other.threshold == threshold &&
      other.aoi == aoi &&
      other.status == status &&
      other.asOf == asOf &&
      other.areaKm2 == areaKm2 &&
      other.maxValue == maxValue &&
      other.coveragePct == coveragePct &&
      other.automated == automated &&
      other.notAWarning == notAWarning;

    @override
    int get hashCode =>
        field.hashCode +
        threshold.hashCode +
        aoi.hashCode +
        status.hashCode +
        asOf.hashCode +
        areaKm2.hashCode +
        maxValue.hashCode +
        coveragePct.hashCode +
        automated.hashCode +
        notAWarning.hashCode;

  factory WxAreaPayload.fromJson(Map<String, dynamic> json) => _$WxAreaPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$WxAreaPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// rotation, qpe1h, ffg added by 053
enum WxAreaPayloadFieldEnum {
@JsonValue(r'refl')
refl(r'refl'),
@JsonValue(r'mesh')
mesh(r'mesh'),
@JsonValue(r'precip')
precip(r'precip'),
@JsonValue(r'rotation')
rotation(r'rotation'),
@JsonValue(r'qpe1h')
qpe1h(r'qpe1h'),
@JsonValue(r'ffg')
ffg(r'ffg');

const WxAreaPayloadFieldEnum(this.value);

final String value;

@override
String toString() => value;
}


