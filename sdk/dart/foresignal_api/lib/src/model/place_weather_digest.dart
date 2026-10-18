//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/place_weather_digest_period.dart';
import 'package:foresignal_api/src/model/place_weather_digest_next.dart';
import 'package:foresignal_api/src/model/place_weather_digest_now.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_digest.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherDigest {
  /// Returns a new [PlaceWeatherDigest] instance.
  PlaceWeatherDigest({

    required  this.placeId,

    required  this.place,

    required  this.timezone,

     this.now,

     this.period,

     this.next,
  });

  @JsonKey(
    
    name: r'placeId',
    required: true,
    includeIfNull: false,
  )


  final String placeId;



  @JsonKey(
    
    name: r'place',
    required: true,
    includeIfNull: false,
  )


  final String place;



  @JsonKey(
    
    name: r'timezone',
    required: true,
    includeIfNull: false,
  )


  final String timezone;



  @JsonKey(
    
    name: r'now',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherDigestNow? now;



  @JsonKey(
    
    name: r'period',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherDigestPeriod? period;



  @JsonKey(
    
    name: r'next',
    required: false,
    includeIfNull: false,
  )


  final PlaceWeatherDigestNext? next;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherDigest &&
      other.placeId == placeId &&
      other.place == place &&
      other.timezone == timezone &&
      other.now == now &&
      other.period == period &&
      other.next == next;

    @override
    int get hashCode =>
        placeId.hashCode +
        place.hashCode +
        timezone.hashCode +
        now.hashCode +
        period.hashCode +
        next.hashCode;

  factory PlaceWeatherDigest.fromJson(Map<String, dynamic> json) => _$PlaceWeatherDigestFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherDigestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

