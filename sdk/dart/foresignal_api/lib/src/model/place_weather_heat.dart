//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sample_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_heat.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherHeat {
  /// Returns a new [PlaceWeatherHeat] instance.
  PlaceWeatherHeat({

     this.status,

     this.riskMaxDay1,

     this.riskMax7d,

     this.peakDay,
  });

  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final SampleStatus? status;



          // minimum: 0
          // maximum: 4
  @JsonKey(
    
    name: r'riskMaxDay1',
    required: false,
    includeIfNull: false,
  )


  final int? riskMaxDay1;



          // minimum: 0
          // maximum: 4
  @JsonKey(
    
    name: r'riskMax7d',
    required: false,
    includeIfNull: false,
  )


  final int? riskMax7d;



  @JsonKey(
    
    name: r'peakDay',
    required: false,
    includeIfNull: false,
  )


  final DateTime? peakDay;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherHeat &&
      other.status == status &&
      other.riskMaxDay1 == riskMaxDay1 &&
      other.riskMax7d == riskMax7d &&
      other.peakDay == peakDay;

    @override
    int get hashCode =>
        status.hashCode +
        (riskMaxDay1 == null ? 0 : riskMaxDay1.hashCode) +
        (riskMax7d == null ? 0 : riskMax7d.hashCode) +
        (peakDay == null ? 0 : peakDay.hashCode);

  factory PlaceWeatherHeat.fromJson(Map<String, dynamic> json) => _$PlaceWeatherHeatFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherHeatToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

