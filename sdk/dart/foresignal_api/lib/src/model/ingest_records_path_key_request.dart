//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/overland_batch_locations_inner.dart';
import 'package:foresignal_api/src/model/ingest_records.dart';
import 'package:foresignal_api/src/model/osm_and_location.dart';
import 'package:foresignal_api/src/model/osm_and_location_location.dart';
import 'package:foresignal_api/src/model/normalized_record_preview.dart';
import 'package:foresignal_api/src/model/gps_logger_fix.dart';
import 'package:foresignal_api/src/model/own_tracks_message.dart';
import 'package:foresignal_api/src/model/overland_batch.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_records_path_key_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestRecordsPathKeyRequest {
  /// Returns a new [IngestRecordsPathKeyRequest] instance.
  IngestRecordsPathKeyRequest({

    required  this.deviceId,

    required  this.location,

    required  this.lat,

    required  this.lon,

     this.time,

     this.acc,

     this.alt,

     this.spd,

     this.dir,

     this.batt,

     this.dev,

    required  this.typeUnderscore,

     this.tst,

     this.vel,

     this.cog,

     this.bs,

     this.conn,

     this.tid,

     this.event,

     this.desc,

     this.wtst,

    required  this.locations,

     this.trip,

     this.current,

    required  this.records,
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



  @JsonKey(
    
    name: r'lat',
    required: true,
    includeIfNull: false,
  )


  final num lat;



  @JsonKey(
    
    name: r'lon',
    required: true,
    includeIfNull: false,
  )


  final num lon;



  @JsonKey(
    
    name: r'time',
    required: false,
    includeIfNull: false,
  )


  final String? time;



      /// Accuracy (m) → quality.accuracyMeters
  @JsonKey(
    
    name: r'acc',
    required: false,
    includeIfNull: false,
  )


  final num? acc;



  @JsonKey(
    
    name: r'alt',
    required: false,
    includeIfNull: false,
  )


  final num? alt;



      /// m/s
  @JsonKey(
    
    name: r'spd',
    required: false,
    includeIfNull: false,
  )


  final num? spd;



  @JsonKey(
    
    name: r'dir',
    required: false,
    includeIfNull: false,
  )


  final num? dir;



  @JsonKey(
    
    name: r'batt',
    required: false,
    includeIfNull: false,
  )


  final int? batt;



      /// → externalId
  @JsonKey(
    
    name: r'dev',
    required: false,
    includeIfNull: false,
  )


  final String? dev;



  @JsonKey(
    
    name: r'_type',
    required: true,
    includeIfNull: false,
  )


  final IngestRecordsPathKeyRequestTypeUnderscoreEnum typeUnderscore;



      /// Epoch seconds → observedAt
  @JsonKey(
    
    name: r'tst',
    required: false,
    includeIfNull: false,
  )


  final int? tst;



      /// km/h → speedMs
  @JsonKey(
    
    name: r'vel',
    required: false,
    includeIfNull: false,
  )


  final num? vel;



  @JsonKey(
    
    name: r'cog',
    required: false,
    includeIfNull: false,
  )


  final num? cog;



      /// Battery status → charging
  @JsonKey(
    
    name: r'bs',
    required: false,
    includeIfNull: false,
  )


  final int? bs;



  @JsonKey(
    
    name: r'conn',
    required: false,
    includeIfNull: false,
  )


  final String? conn;



  @JsonKey(
    
    name: r'tid',
    required: false,
    includeIfNull: false,
  )


  final String? tid;



  @JsonKey(
    
    name: r'event',
    required: false,
    includeIfNull: false,
  )


  final String? event;



  @JsonKey(
    
    name: r'desc',
    required: false,
    includeIfNull: false,
  )


  final String? desc;



  @JsonKey(
    
    name: r'wtst',
    required: false,
    includeIfNull: false,
  )


  final int? wtst;



  @JsonKey(
    
    name: r'locations',
    required: true,
    includeIfNull: false,
  )


  final List<OverlandBatchLocationsInner> locations;



      /// Ignored
  @JsonKey(
    
    name: r'trip',
    required: false,
    includeIfNull: false,
  )


  final Object? trip;



      /// Ignored
  @JsonKey(
    
    name: r'current',
    required: false,
    includeIfNull: false,
  )


  final Object? current;



  @JsonKey(
    
    name: r'records',
    required: true,
    includeIfNull: false,
  )


  final List<NormalizedRecordPreview> records;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestRecordsPathKeyRequest &&
      other.deviceId == deviceId &&
      other.location == location &&
      other.lat == lat &&
      other.lon == lon &&
      other.time == time &&
      other.acc == acc &&
      other.alt == alt &&
      other.spd == spd &&
      other.dir == dir &&
      other.batt == batt &&
      other.dev == dev &&
      other.typeUnderscore == typeUnderscore &&
      other.tst == tst &&
      other.vel == vel &&
      other.cog == cog &&
      other.bs == bs &&
      other.conn == conn &&
      other.tid == tid &&
      other.event == event &&
      other.desc == desc &&
      other.wtst == wtst &&
      other.locations == locations &&
      other.trip == trip &&
      other.current == current &&
      other.records == records;

    @override
    int get hashCode =>
        deviceId.hashCode +
        location.hashCode +
        lat.hashCode +
        lon.hashCode +
        time.hashCode +
        acc.hashCode +
        alt.hashCode +
        spd.hashCode +
        dir.hashCode +
        batt.hashCode +
        dev.hashCode +
        typeUnderscore.hashCode +
        tst.hashCode +
        vel.hashCode +
        cog.hashCode +
        bs.hashCode +
        conn.hashCode +
        tid.hashCode +
        event.hashCode +
        desc.hashCode +
        wtst.hashCode +
        locations.hashCode +
        (trip == null ? 0 : trip.hashCode) +
        (current == null ? 0 : current.hashCode) +
        records.hashCode;

  factory IngestRecordsPathKeyRequest.fromJson(Map<String, dynamic> json) => _$IngestRecordsPathKeyRequestFromJson(json);

  Map<String, dynamic> toJson() => _$IngestRecordsPathKeyRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IngestRecordsPathKeyRequestTypeUnderscoreEnum {
@JsonValue(r'location')
location(r'location'),
@JsonValue(r'transition')
transition(r'transition'),
@JsonValue(r'waypoint')
waypoint(r'waypoint'),
@JsonValue(r'lwt')
lwt(r'lwt'),
@JsonValue(r'card')
card(r'card'),
@JsonValue(r'status')
status(r'status'),
@JsonValue(r'encrypted')
encrypted(r'encrypted');

const IngestRecordsPathKeyRequestTypeUnderscoreEnum(this.value);

final String value;

@override
String toString() => value;
}


