//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stats_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StatsRequest {
  /// Returns a new [StatsRequest] instance.
  StatsRequest({

     this.geometry,

     this.place,

     this.bbox,

     this.time = 'latest',

     this.run,

     this.thresholds,

     this.units,
  });

  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



      /// Place id (requires a token)
  @JsonKey(
    
    name: r'place',
    required: false,
    includeIfNull: false,
  )


  final String? place;



      /// minLon,minLat,maxLon,maxLat
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final String? bbox;



      /// `latest`, `now`, ISO-8601 or relative offset
  @JsonKey(
    defaultValue: 'latest',
    name: r'time',
    required: false,
    includeIfNull: false,
  )


  final String? time;



      /// Forecast run (`latest`, `previous`, runIso); required with a concrete forecast time
  @JsonKey(
    
    name: r'run',
    required: false,
    includeIfNull: false,
  )


  final String? run;



      /// Thresholds (physical units) for areaKm2 above each
  @JsonKey(
    
    name: r'thresholds',
    required: false,
    includeIfNull: false,
  )


  final List<num>? thresholds;



  @JsonKey(
    
    name: r'units',
    required: false,
    includeIfNull: false,
  )


  final StatsRequestUnitsEnum? units;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StatsRequest &&
      other.geometry == geometry &&
      other.place == place &&
      other.bbox == bbox &&
      other.time == time &&
      other.run == run &&
      other.thresholds == thresholds &&
      other.units == units;

    @override
    int get hashCode =>
        geometry.hashCode +
        place.hashCode +
        bbox.hashCode +
        time.hashCode +
        run.hashCode +
        thresholds.hashCode +
        units.hashCode;

  factory StatsRequest.fromJson(Map<String, dynamic> json) => _$StatsRequestFromJson(json);

  Map<String, dynamic> toJson() => _$StatsRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StatsRequestUnitsEnum {
@JsonValue(r'metric')
metric(r'metric'),
@JsonValue(r'imperial')
imperial(r'imperial');

const StatsRequestUnitsEnum(this.value);

final String value;

@override
String toString() => value;
}


