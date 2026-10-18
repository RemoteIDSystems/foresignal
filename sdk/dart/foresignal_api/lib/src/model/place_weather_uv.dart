//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sample_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_uv.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherUv {
  /// Returns a new [PlaceWeatherUv] instance.
  PlaceWeatherUv({

     this.status,

     this.maxToday,
  });

  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final SampleStatus? status;



  @JsonKey(
    
    name: r'maxToday',
    required: false,
    includeIfNull: false,
  )


  final num? maxToday;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherUv &&
      other.status == status &&
      other.maxToday == maxToday;

    @override
    int get hashCode =>
        status.hashCode +
        (maxToday == null ? 0 : maxToday.hashCode);

  factory PlaceWeatherUv.fromJson(Map<String, dynamic> json) => _$PlaceWeatherUvFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherUvToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

