//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'weather_context_radar_nearest_cell_motion.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WeatherContextRadarNearestCellMotion {
  /// Returns a new [WeatherContextRadarNearestCellMotion] instance.
  WeatherContextRadarNearestCellMotion({

     this.speedKmh,

     this.bearingDeg,

     this.method,
  });

  @JsonKey(
    
    name: r'speedKmh',
    required: false,
    includeIfNull: false,
  )


  final num? speedKmh;



  @JsonKey(
    
    name: r'bearingDeg',
    required: false,
    includeIfNull: false,
  )


  final int? bearingDeg;



  @JsonKey(
    
    name: r'method',
    required: false,
    includeIfNull: false,
  )


  final String? method;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WeatherContextRadarNearestCellMotion &&
      other.speedKmh == speedKmh &&
      other.bearingDeg == bearingDeg &&
      other.method == method;

    @override
    int get hashCode =>
        speedKmh.hashCode +
        bearingDeg.hashCode +
        method.hashCode;

  factory WeatherContextRadarNearestCellMotion.fromJson(Map<String, dynamic> json) => _$WeatherContextRadarNearestCellMotionFromJson(json);

  Map<String, dynamic> toJson() => _$WeatherContextRadarNearestCellMotionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

