//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/trip_schedule.dart';
import 'package:foresignal_api/src/model/severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'trip_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TripInput {
  /// Returns a new [TripInput] instance.
  TripInput({

    required  this.name,

     this.routePlaceId,

     this.route,

     this.bufferMeters = 2000,

    required  this.schedule,

    required  this.expectedDuration,

     this.leadTime = '60m',

     this.hazardKinds,

     this.minSeverity,

     this.channelIds,

     this.anchor,

     this.notifyWhenClear = true,

     this.enabled = true,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// A 036 route place (LineString + bufferMeters); or give `route`
  @JsonKey(
    
    name: r'routePlaceId',
    required: false,
    includeIfNull: false,
  )


  final String? routePlaceId;



      /// GeoJSON LineString, at most 5,000 vertices
  @JsonKey(
    
    name: r'route',
    required: false,
    includeIfNull: false,
  )


  final Geometry? route;



          // minimum: 1
          // maximum: 50000
  @JsonKey(
    defaultValue: 2000,
    name: r'bufferMeters',
    required: false,
    includeIfNull: false,
  )


  final int? bufferMeters;



  @JsonKey(
    
    name: r'schedule',
    required: true,
    includeIfNull: false,
  )


  final TripSchedule schedule;



      /// Trip duration; the watch window is [departAt, departAt + expectedDuration]. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h).
  @JsonKey(
    
    name: r'expectedDuration',
    required: true,
    includeIfNull: false,
  )


  final String expectedDuration;



      /// Pre-trip check runs at departAt − leadTime. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h).
  @JsonKey(
    defaultValue: '60m',
    name: r'leadTime',
    required: false,
    includeIfNull: false,
  )


  final String? leadTime;



  @JsonKey(
    
    name: r'hazardKinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? hazardKinds;



  @JsonKey(
    
    name: r'minSeverity',
    required: false,
    includeIfNull: false,
  )


  final Severity? minSeverity;



  @JsonKey(
    
    name: r'channelIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? channelIds;



      /// Moving-anchor record `{sourceId}/{externalId}` (036), e.g. an OwnTracks device
  @JsonKey(
    
    name: r'anchor',
    required: false,
    includeIfNull: false,
  )


  final String? anchor;



      /// Deliver a 'route clear' briefing too
  @JsonKey(
    defaultValue: true,
    name: r'notifyWhenClear',
    required: false,
    includeIfNull: false,
  )


  final bool? notifyWhenClear;



  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TripInput &&
      other.name == name &&
      other.routePlaceId == routePlaceId &&
      other.route == route &&
      other.bufferMeters == bufferMeters &&
      other.schedule == schedule &&
      other.expectedDuration == expectedDuration &&
      other.leadTime == leadTime &&
      other.hazardKinds == hazardKinds &&
      other.minSeverity == minSeverity &&
      other.channelIds == channelIds &&
      other.anchor == anchor &&
      other.notifyWhenClear == notifyWhenClear &&
      other.enabled == enabled;

    @override
    int get hashCode =>
        name.hashCode +
        (routePlaceId == null ? 0 : routePlaceId.hashCode) +
        (route == null ? 0 : route.hashCode) +
        bufferMeters.hashCode +
        schedule.hashCode +
        expectedDuration.hashCode +
        leadTime.hashCode +
        hazardKinds.hashCode +
        (minSeverity == null ? 0 : minSeverity.hashCode) +
        channelIds.hashCode +
        (anchor == null ? 0 : anchor.hashCode) +
        notifyWhenClear.hashCode +
        enabled.hashCode;

  factory TripInput.fromJson(Map<String, dynamic> json) => _$TripInputFromJson(json);

  Map<String, dynamic> toJson() => _$TripInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

