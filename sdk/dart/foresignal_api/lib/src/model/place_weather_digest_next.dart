//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/place_weather_digest_next_hrrr.dart';
import 'package:foresignal_api/src/model/place_weather_digest_next_nws.dart';
import 'package:foresignal_api/src/model/place_weather_digest_next_gfs.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_digest_next.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherDigestNext {
  /// Returns a new [PlaceWeatherDigestNext] instance.
  PlaceWeatherDigestNext({

     this.nws,

     this.gfs,

     this.hrrr,
  });

  @JsonKey(
    
    name: r'nws',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherDigestNextNws? nws;



  @JsonKey(
    
    name: r'gfs',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherDigestNextGfs? gfs;



  @JsonKey(
    
    name: r'hrrr',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherDigestNextHrrr? hrrr;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherDigestNext &&
      other.nws == nws &&
      other.gfs == gfs &&
      other.hrrr == hrrr;

    @override
    int get hashCode =>
        nws.hashCode +
        gfs.hashCode +
        hrrr.hashCode;

  factory PlaceWeatherDigestNext.fromJson(Map<String, dynamic> json) => _$PlaceWeatherDigestNextFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherDigestNextToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

