//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sensor.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sensor_discovery_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SensorDiscoveryResult {
  /// Returns a new [SensorDiscoveryResult] instance.
  SensorDiscoveryResult({

     this.bbox,

     this.placeId,

    required  this.from,

    required  this.to,

    required  this.items,

     this.truncated,

     this.note = 'Foresignal cannot task public satellites or third-party sensors',
  });

  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final DateTime to;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<Sensor> items;



  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;



  @JsonKey(
    defaultValue: 'Foresignal cannot task public satellites or third-party sensors',
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SensorDiscoveryResult &&
      other.bbox == bbox &&
      other.placeId == placeId &&
      other.from == from &&
      other.to == to &&
      other.items == items &&
      other.truncated == truncated &&
      other.note == note;

    @override
    int get hashCode =>
        (bbox == null ? 0 : bbox.hashCode) +
        (placeId == null ? 0 : placeId.hashCode) +
        from.hashCode +
        to.hashCode +
        items.hashCode +
        truncated.hashCode +
        note.hashCode;

  factory SensorDiscoveryResult.fromJson(Map<String, dynamic> json) => _$SensorDiscoveryResultFromJson(json);

  Map<String, dynamic> toJson() => _$SensorDiscoveryResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

