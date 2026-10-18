//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/osm_and_location_location_battery.dart';
import 'package:foresignal_api/src/model/osm_and_location_location_coords.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'osm_and_location_location.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OsmAndLocationLocation {
  /// Returns a new [OsmAndLocationLocation] instance.
  OsmAndLocationLocation({

     this.timestamp,

    required  this.coords,

     this.battery,

     this.isMoving,

     this.event,
  });

      /// ISO-8601
  @JsonKey(
    
    name: r'timestamp',
    required: false,
    includeIfNull: false,
  )


  final String? timestamp;



  @JsonKey(
    
    name: r'coords',
    required: true,
    includeIfNull: false,
  )


  final OsmAndLocationLocationCoords coords;



  @JsonKey(
    
    name: r'battery',
    required: false,
    includeIfNull: false,
  )


  final OsmAndLocationLocationBattery? battery;



  @JsonKey(
    
    name: r'is_moving',
    required: false,
    includeIfNull: false,
  )


  final bool? isMoving;



  @JsonKey(
    
    name: r'event',
    required: false,
    includeIfNull: false,
  )


  final String? event;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OsmAndLocationLocation &&
      other.timestamp == timestamp &&
      other.coords == coords &&
      other.battery == battery &&
      other.isMoving == isMoving &&
      other.event == event;

    @override
    int get hashCode =>
        timestamp.hashCode +
        coords.hashCode +
        battery.hashCode +
        isMoving.hashCode +
        event.hashCode;

  factory OsmAndLocationLocation.fromJson(Map<String, dynamic> json) => _$OsmAndLocationLocationFromJson(json);

  Map<String, dynamic> toJson() => _$OsmAndLocationLocationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

