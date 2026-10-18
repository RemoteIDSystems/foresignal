//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/weather_context_radar_nearest_cell_motion.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'weather_context_radar_nearest_cell.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WeatherContextRadarNearestCell {
  /// Returns a new [WeatherContextRadarNearestCell] instance.
  WeatherContextRadarNearestCell({

    required  this.id,

     this.distanceKm,

     this.motion,

     this.meshMaxMm,
  });

      /// radar-cell externalId
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'distanceKm',
    required: false,
    includeIfNull: false,
  )


  final num? distanceKm;



  @JsonKey(
    
    name: r'motion',
    required: false,
    includeIfNull: false,
  )


  final WeatherContextRadarNearestCellMotion? motion;



  @JsonKey(
    
    name: r'meshMaxMm',
    required: false,
    includeIfNull: false,
  )


  final num? meshMaxMm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WeatherContextRadarNearestCell &&
      other.id == id &&
      other.distanceKm == distanceKm &&
      other.motion == motion &&
      other.meshMaxMm == meshMaxMm;

    @override
    int get hashCode =>
        id.hashCode +
        distanceKm.hashCode +
        motion.hashCode +
        (meshMaxMm == null ? 0 : meshMaxMm.hashCode);

  factory WeatherContextRadarNearestCell.fromJson(Map<String, dynamic> json) => _$WeatherContextRadarNearestCellFromJson(json);

  Map<String, dynamic> toJson() => _$WeatherContextRadarNearestCellToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

