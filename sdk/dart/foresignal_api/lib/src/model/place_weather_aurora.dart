//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sample_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_aurora.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherAurora {
  /// Returns a new [PlaceWeatherAurora] instance.
  PlaceWeatherAurora({

     this.status,

     this.probMaxNearby,
  });

  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final SampleStatus? status;



  @JsonKey(
    
    name: r'probMaxNearby',
    required: false,
    includeIfNull: false,
  )


  final num? probMaxNearby;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherAurora &&
      other.status == status &&
      other.probMaxNearby == probMaxNearby;

    @override
    int get hashCode =>
        status.hashCode +
        (probMaxNearby == null ? 0 : probMaxNearby.hashCode);

  factory PlaceWeatherAurora.fromJson(Map<String, dynamic> json) => _$PlaceWeatherAuroraFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherAuroraToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

