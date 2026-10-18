//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sample_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_marine.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherMarine {
  /// Returns a new [PlaceWeatherMarine] instance.
  PlaceWeatherMarine({

     this.status,

     this.waveHeightMaxM24h,
  });

  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final SampleStatus? status;



  @JsonKey(
    
    name: r'waveHeightMaxM24h',
    required: false,
    includeIfNull: false,
  )


  final num? waveHeightMaxM24h;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherMarine &&
      other.status == status &&
      other.waveHeightMaxM24h == waveHeightMaxM24h;

    @override
    int get hashCode =>
        status.hashCode +
        (waveHeightMaxM24h == null ? 0 : waveHeightMaxM24h.hashCode);

  factory PlaceWeatherMarine.fromJson(Map<String, dynamic> json) => _$PlaceWeatherMarineFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherMarineToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

