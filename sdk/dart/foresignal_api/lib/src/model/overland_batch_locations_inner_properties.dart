//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'overland_batch_locations_inner_properties.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OverlandBatchLocationsInnerProperties {
  /// Returns a new [OverlandBatchLocationsInnerProperties] instance.
  OverlandBatchLocationsInnerProperties({

     this.timestamp,

     this.horizontalAccuracy,

     this.altitude,

     this.speed,

     this.course,

     this.motion,

     this.batteryLevel,

     this.batteryState,

     this.wifi,

     this.deviceId,
  });

  @JsonKey(
    
    name: r'timestamp',
    required: false,
    includeIfNull: false,
  )


  final DateTime? timestamp;



  @JsonKey(
    
    name: r'horizontal_accuracy',
    required: false,
    includeIfNull: false,
  )


  final num? horizontalAccuracy;



  @JsonKey(
    
    name: r'altitude',
    required: false,
    includeIfNull: false,
  )


  final num? altitude;



      /// m/s
  @JsonKey(
    
    name: r'speed',
    required: false,
    includeIfNull: false,
  )


  final num? speed;



  @JsonKey(
    
    name: r'course',
    required: false,
    includeIfNull: false,
  )


  final num? course;



  @JsonKey(
    
    name: r'motion',
    required: false,
    includeIfNull: false,
  )


  final List<String>? motion;



  @JsonKey(
    
    name: r'battery_level',
    required: false,
    includeIfNull: false,
  )


  final num? batteryLevel;



  @JsonKey(
    
    name: r'battery_state',
    required: false,
    includeIfNull: false,
  )


  final String? batteryState;



  @JsonKey(
    
    name: r'wifi',
    required: false,
    includeIfNull: false,
  )


  final String? wifi;



      /// → externalId (a device key forces its bound id)
  @JsonKey(
    
    name: r'device_id',
    required: false,
    includeIfNull: false,
  )


  final String? deviceId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OverlandBatchLocationsInnerProperties &&
      other.timestamp == timestamp &&
      other.horizontalAccuracy == horizontalAccuracy &&
      other.altitude == altitude &&
      other.speed == speed &&
      other.course == course &&
      other.motion == motion &&
      other.batteryLevel == batteryLevel &&
      other.batteryState == batteryState &&
      other.wifi == wifi &&
      other.deviceId == deviceId;

    @override
    int get hashCode =>
        timestamp.hashCode +
        horizontalAccuracy.hashCode +
        altitude.hashCode +
        speed.hashCode +
        course.hashCode +
        motion.hashCode +
        batteryLevel.hashCode +
        batteryState.hashCode +
        wifi.hashCode +
        deviceId.hashCode;

  factory OverlandBatchLocationsInnerProperties.fromJson(Map<String, dynamic> json) => _$OverlandBatchLocationsInnerPropertiesFromJson(json);

  Map<String, dynamic> toJson() => _$OverlandBatchLocationsInnerPropertiesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

