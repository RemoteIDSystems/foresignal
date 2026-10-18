//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_digest_next_nws.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherDigestNextNws {
  /// Returns a new [PlaceWeatherDigestNextNws] instance.
  PlaceWeatherDigestNextNws({

    required  this.status,

     this.asOf,

     this.statusText,

     this.popMax24h,

     this.qpf24hMm,

     this.gustMaxMs24h,

     this.tMaxC24h,

     this.tMinC24h,

     this.updatedAt,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final WeatherSectionStatus status;



      /// Time of the last good data; present only while status is not ok, fixed while not ok
  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



      /// Explanation for non-ok statuses
  @JsonKey(
    
    name: r'statusText',
    required: false,
    includeIfNull: false,
  )


  final String? statusText;



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
    
    name: r'gustMaxMs24h',
    required: false,
    includeIfNull: false,
  )


  final num? gustMaxMs24h;



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
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherDigestNextNws &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.popMax24h == popMax24h &&
      other.qpf24hMm == qpf24hMm &&
      other.gustMaxMs24h == gustMaxMs24h &&
      other.tMaxC24h == tMaxC24h &&
      other.tMinC24h == tMinC24h &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        popMax24h.hashCode +
        qpf24hMm.hashCode +
        gustMaxMs24h.hashCode +
        tMaxC24h.hashCode +
        tMinC24h.hashCode +
        updatedAt.hashCode;

  factory PlaceWeatherDigestNextNws.fromJson(Map<String, dynamic> json) => _$PlaceWeatherDigestNextNwsFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherDigestNextNwsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

