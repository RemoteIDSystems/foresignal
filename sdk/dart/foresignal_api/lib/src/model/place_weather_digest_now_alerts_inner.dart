//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_digest_now_alerts_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherDigestNowAlertsInner {
  /// Returns a new [PlaceWeatherDigestNowAlertsInner] instance.
  PlaceWeatherDigestNowAlertsInner({

    required  this.id,

    required  this.event,

     this.severity,

     this.expires,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final String event;



  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final String? severity;



  @JsonKey(
    
    name: r'expires',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expires;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherDigestNowAlertsInner &&
      other.id == id &&
      other.event == event &&
      other.severity == severity &&
      other.expires == expires;

    @override
    int get hashCode =>
        id.hashCode +
        event.hashCode +
        severity.hashCode +
        (expires == null ? 0 : expires.hashCode);

  factory PlaceWeatherDigestNowAlertsInner.fromJson(Map<String, dynamic> json) => _$PlaceWeatherDigestNowAlertsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherDigestNowAlertsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

