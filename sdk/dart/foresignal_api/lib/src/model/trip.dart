//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/trip_schedule.dart';
import 'package:foresignal_api/src/model/severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'trip.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Trip {
  /// Returns a new [Trip] instance.
  Trip({

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

    required  this.id,

    required  this.corridor,

     this.nextOccurrence,

     this.createdBy,

    required  this.createdAt,

    required  this.updatedAt,
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



      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// ST_Buffer(route, bufferMeters), materialised on save
  @JsonKey(
    
    name: r'corridor',
    required: true,
    includeIfNull: false,
  )


  final Geometry corridor;



  @JsonKey(
    
    name: r'nextOccurrence',
    required: false,
    includeIfNull: false,
  )


  final DateTime? nextOccurrence;



  @JsonKey(
    
    name: r'createdBy',
    required: false,
    includeIfNull: false,
  )


  final String? createdBy;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Trip &&
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
      other.enabled == enabled &&
      other.id == id &&
      other.corridor == corridor &&
      other.nextOccurrence == nextOccurrence &&
      other.createdBy == createdBy &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

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
        enabled.hashCode +
        id.hashCode +
        corridor.hashCode +
        (nextOccurrence == null ? 0 : nextOccurrence.hashCode) +
        createdBy.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory Trip.fromJson(Map<String, dynamic> json) => _$TripFromJson(json);

  Map<String, dynamic> toJson() => _$TripToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

