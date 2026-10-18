//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_cell.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherCell {
  /// Returns a new [PlaceWeatherCell] instance.
  PlaceWeatherCell({

    required  this.status,

     this.asOf,

     this.statusText,

     this.eta50Min,

     this.etaCellId,

     this.etaSpeedKmh,

     this.etaBearingDeg,
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



      /// Minutes until a ≥ 50 dBZ core enters the radius, 5-min steps (999 = none approaching)
  @JsonKey(
    
    name: r'eta50Min',
    required: false,
    includeIfNull: false,
  )


  final int? eta50Min;



  @JsonKey(
    
    name: r'etaCellId',
    required: false,
    includeIfNull: false,
  )


  final String? etaCellId;



  @JsonKey(
    
    name: r'etaSpeedKmh',
    required: false,
    includeIfNull: false,
  )


  final num? etaSpeedKmh;



  @JsonKey(
    
    name: r'etaBearingDeg',
    required: false,
    includeIfNull: false,
  )


  final int? etaBearingDeg;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherCell &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.eta50Min == eta50Min &&
      other.etaCellId == etaCellId &&
      other.etaSpeedKmh == etaSpeedKmh &&
      other.etaBearingDeg == etaBearingDeg;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        eta50Min.hashCode +
        etaCellId.hashCode +
        etaSpeedKmh.hashCode +
        etaBearingDeg.hashCode;

  factory PlaceWeatherCell.fromJson(Map<String, dynamic> json) => _$PlaceWeatherCellFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherCellToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

