//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'osm_and_location_location_battery.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OsmAndLocationLocationBattery {
  /// Returns a new [OsmAndLocationLocationBattery] instance.
  OsmAndLocationLocationBattery({

     this.level,

     this.isCharging,
  });

  @JsonKey(
    
    name: r'level',
    required: false,
    includeIfNull: false,
  )


  final num? level;



  @JsonKey(
    
    name: r'is_charging',
    required: false,
    includeIfNull: false,
  )


  final bool? isCharging;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OsmAndLocationLocationBattery &&
      other.level == level &&
      other.isCharging == isCharging;

    @override
    int get hashCode =>
        level.hashCode +
        isCharging.hashCode;

  factory OsmAndLocationLocationBattery.fromJson(Map<String, dynamic> json) => _$OsmAndLocationLocationBatteryFromJson(json);

  Map<String, dynamic> toJson() => _$OsmAndLocationLocationBatteryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

