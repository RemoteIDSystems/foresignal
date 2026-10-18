//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'weather_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WeatherSettings {
  /// Returns a new [WeatherSettings] instance.
  WeatherSettings({

     this.alertContext,

     this.defaultAoi = '-106.65,25.84,-93.51,36.50',

     this.aoiBufferKm = 150,
  });

      /// Compute `context.weather` for geolocated alerts (056)
  @JsonKey(
    
    name: r'alertContext',
    required: false,
    includeIfNull: false,
  )


  final WeatherSettingsAlertContextEnum? alertContext;



      /// minLon,minLat,maxLon,maxLat used for weather ingestion when no places exist (047); a health warning is raised while it applies
  @JsonKey(
    defaultValue: '-106.65,25.84,-93.51,36.50',
    name: r'defaultAoi',
    required: false,
    includeIfNull: false,
  )


  final String? defaultAoi;



      /// Minimum buffer around each place in the places AOI (036)
          // minimum: 0
          // maximum: 1000
  @JsonKey(
    defaultValue: 150,
    name: r'aoiBufferKm',
    required: false,
    includeIfNull: false,
  )


  final num? aoiBufferKm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WeatherSettings &&
      other.alertContext == alertContext &&
      other.defaultAoi == defaultAoi &&
      other.aoiBufferKm == aoiBufferKm;

    @override
    int get hashCode =>
        alertContext.hashCode +
        defaultAoi.hashCode +
        aoiBufferKm.hashCode;

  factory WeatherSettings.fromJson(Map<String, dynamic> json) => _$WeatherSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$WeatherSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Compute `context.weather` for geolocated alerts (056)
enum WeatherSettingsAlertContextEnum {
@JsonValue(r'auto')
auto(r'auto'),
@JsonValue(r'off')
off(r'off');

const WeatherSettingsAlertContextEnum(this.value);

final String value;

@override
String toString() => value;
}


