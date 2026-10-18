//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_section_status.dart';
import 'package:foresignal_api/src/model/weather_context_radar_nearest_cell.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'weather_context_radar.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WeatherContextRadar {
  /// Returns a new [WeatherContextRadar] instance.
  WeatherContextRadar({

    required  this.status,

     this.asOf,

     this.statusText,

     this.maxDbz,

     this.nearestCell,
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



      /// Max within 10 km of the geometry
  @JsonKey(
    
    name: r'maxDbz',
    required: false,
    includeIfNull: false,
  )


  final num? maxDbz;



  @JsonKey(
    
    name: r'nearestCell',
    required: false,
    includeIfNull: false,
  )


  final WeatherContextRadarNearestCell? nearestCell;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WeatherContextRadar &&
      other.status == status &&
      other.asOf == asOf &&
      other.statusText == statusText &&
      other.maxDbz == maxDbz &&
      other.nearestCell == nearestCell;

    @override
    int get hashCode =>
        status.hashCode +
        asOf.hashCode +
        statusText.hashCode +
        maxDbz.hashCode +
        (nearestCell == null ? 0 : nearestCell.hashCode);

  factory WeatherContextRadar.fromJson(Map<String, dynamic> json) => _$WeatherContextRadarFromJson(json);

  Map<String, dynamic> toJson() => _$WeatherContextRadarToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

