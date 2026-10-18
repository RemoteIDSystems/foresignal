//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/osm_and_location_location.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'osm_and_location.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OsmAndLocation {
  /// Returns a new [OsmAndLocation] instance.
  OsmAndLocation({

    required  this.deviceId,

    required  this.location,
  });

  @JsonKey(
    
    name: r'device_id',
    required: true,
    includeIfNull: false,
  )


  final String deviceId;



  @JsonKey(
    
    name: r'location',
    required: true,
    includeIfNull: false,
  )


  final OsmAndLocationLocation location;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OsmAndLocation &&
      other.deviceId == deviceId &&
      other.location == location;

    @override
    int get hashCode =>
        deviceId.hashCode +
        location.hashCode;

  factory OsmAndLocation.fromJson(Map<String, dynamic> json) => _$OsmAndLocationFromJson(json);

  Map<String, dynamic> toJson() => _$OsmAndLocationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

