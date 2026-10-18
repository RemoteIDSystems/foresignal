//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/alertmanager_webhook.dart';
import 'package:foresignal_api/src/model/osm_and_location.dart';
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/feature_collection.dart';
import 'package:foresignal_api/src/model/gps_logger_fix.dart';
import 'package:foresignal_api/src/model/overland_batch_locations_inner.dart';
import 'package:foresignal_api/src/model/ingest_records.dart';
import 'package:foresignal_api/src/model/feature.dart';
import 'package:foresignal_api/src/model/osm_and_location_location.dart';
import 'package:foresignal_api/src/model/normalized_record_preview.dart';
import 'package:foresignal_api/src/model/cloud_event.dart';
import 'package:foresignal_api/src/model/own_tracks_message.dart';
import 'package:foresignal_api/src/model/alertmanager_webhook_alerts_inner.dart';
import 'package:foresignal_api/src/model/overland_batch.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_records_format_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestRecordsFormatRequest {
  /// Returns a new [IngestRecordsFormatRequest] instance.
  IngestRecordsFormatRequest({

    required  this.type,

    required  this.features,

     this.nextCursor,

     this.attributions,

     this.watermarkSeq,

     this.asOfSeq,

     this.asOf,

     this.approximate,

     this.horizon,

     this.licenceFiltered,

    required  this.records,

    required  this.version,

     this.groupKey,

     this.truncatedAlerts,

     this.status,

     this.receiver,

     this.groupLabels,

     this.commonLabels,

     this.commonAnnotations,

     this.externalURL,

    required  this.alerts,

    required  this.specversion,

    required  this.id,

    required  this.source_,

     this.subject,

     this.time,

     this.datacontenttype,

     this.dataschema,

     this.data,

    required  this.typeUnderscore,

    required  this.lat,

    required  this.lon,

     this.tst,

     this.acc,

     this.alt,

     this.vel,

     this.cog,

     this.batt,

     this.bs,

     this.conn,

     this.tid,

     this.event,

     this.desc,

     this.wtst,

    required  this.locations,

     this.trip,

     this.current,

    required  this.deviceId,

    required  this.location,

     this.spd,

     this.dir,

     this.dev,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final String type;



  @JsonKey(
    
    name: r'features',
    required: true,
    includeIfNull: false,
  )


  final List<Feature> features;



  @JsonKey(
    
    name: r'nextCursor',
    required: false,
    includeIfNull: false,
  )


  final String? nextCursor;



      /// Foreign member: aggregated attributions
  @JsonKey(
    
    name: r'attributions',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attributions;



      /// ChangeBus.lastPublishedSeq() read before the query; open SSE with after=watermarkSeq
  @JsonKey(
    
    name: r'watermarkSeq',
    required: false,
    includeIfNull: false,
  )


  final int? watermarkSeq;



      /// Echo of the pinned snapshot seq (asOfSeq or order=id)
  @JsonKey(
    
    name: r'asOfSeq',
    required: false,
    includeIfNull: false,
  )


  final int? asOfSeq;



      /// Echo of asOf
  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



      /// asOf only: true when a source is SAMPLED/LATEST or the range is downsampled
  @JsonKey(
    
    name: r'approximate',
    required: false,
    includeIfNull: false,
  )


  final bool? approximate;



      /// asOf only: earliest supported T over the involved sources
  @JsonKey(
    
    name: r'horizon',
    required: false,
    includeIfNull: false,
  )


  final DateTime? horizon;



          // minimum: 0
  @JsonKey(
    
    name: r'licenceFiltered',
    required: false,
    includeIfNull: false,
  )


  final int? licenceFiltered;



  @JsonKey(
    
    name: r'records',
    required: true,
    includeIfNull: false,
  )


  final List<NormalizedRecordPreview> records;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final IngestRecordsFormatRequestVersionEnum version;



  @JsonKey(
    
    name: r'groupKey',
    required: false,
    includeIfNull: false,
  )


  final String? groupKey;



  @JsonKey(
    
    name: r'truncatedAlerts',
    required: false,
    includeIfNull: false,
  )


  final int? truncatedAlerts;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final IngestRecordsFormatRequestStatusEnum? status;



  @JsonKey(
    
    name: r'receiver',
    required: false,
    includeIfNull: false,
  )


  final String? receiver;



  @JsonKey(
    
    name: r'groupLabels',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? groupLabels;



  @JsonKey(
    
    name: r'commonLabels',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? commonLabels;



  @JsonKey(
    
    name: r'commonAnnotations',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? commonAnnotations;



  @JsonKey(
    
    name: r'externalURL',
    required: false,
    includeIfNull: false,
  )


  final String? externalURL;



  @JsonKey(
    
    name: r'alerts',
    required: true,
    includeIfNull: false,
  )


  final List<AlertmanagerWebhookAlertsInner> alerts;



  @JsonKey(
    
    name: r'specversion',
    required: true,
    includeIfNull: false,
  )


  final IngestRecordsFormatRequestSpecversionEnum specversion;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final String source_;



  @JsonKey(
    
    name: r'subject',
    required: false,
    includeIfNull: false,
  )


  final String? subject;



  @JsonKey(
    
    name: r'time',
    required: false,
    includeIfNull: false,
  )


  final String? time;



  @JsonKey(
    
    name: r'datacontenttype',
    required: false,
    includeIfNull: false,
  )


  final String? datacontenttype;



  @JsonKey(
    
    name: r'dataschema',
    required: false,
    includeIfNull: false,
  )


  final String? dataschema;



      /// Payload; an object becomes the record payload (with `geometry` or `lat`/`lon`), other values are wrapped as `{data}`
  @JsonKey(
    
    name: r'data',
    required: false,
    includeIfNull: false,
  )


  final Object? data;



  @JsonKey(
    
    name: r'_type',
    required: true,
    includeIfNull: false,
  )


  final IngestRecordsFormatRequestTypeUnderscoreEnum typeUnderscore;



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



      /// Epoch seconds → observedAt
  @JsonKey(
    
    name: r'tst',
    required: false,
    includeIfNull: false,
  )


  final int? tst;



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



  @JsonKey(
    
    name: r'batt',
    required: false,
    includeIfNull: false,
  )


  final num? batt;



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



      /// → externalId
  @JsonKey(
    
    name: r'dev',
    required: false,
    includeIfNull: false,
  )


  final String? dev;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestRecordsFormatRequest &&
      other.type == type &&
      other.features == features &&
      other.nextCursor == nextCursor &&
      other.attributions == attributions &&
      other.watermarkSeq == watermarkSeq &&
      other.asOfSeq == asOfSeq &&
      other.asOf == asOf &&
      other.approximate == approximate &&
      other.horizon == horizon &&
      other.licenceFiltered == licenceFiltered &&
      other.records == records &&
      other.version == version &&
      other.groupKey == groupKey &&
      other.truncatedAlerts == truncatedAlerts &&
      other.status == status &&
      other.receiver == receiver &&
      other.groupLabels == groupLabels &&
      other.commonLabels == commonLabels &&
      other.commonAnnotations == commonAnnotations &&
      other.externalURL == externalURL &&
      other.alerts == alerts &&
      other.specversion == specversion &&
      other.id == id &&
      other.source_ == source_ &&
      other.subject == subject &&
      other.time == time &&
      other.datacontenttype == datacontenttype &&
      other.dataschema == dataschema &&
      other.data == data &&
      other.typeUnderscore == typeUnderscore &&
      other.lat == lat &&
      other.lon == lon &&
      other.tst == tst &&
      other.acc == acc &&
      other.alt == alt &&
      other.vel == vel &&
      other.cog == cog &&
      other.batt == batt &&
      other.bs == bs &&
      other.conn == conn &&
      other.tid == tid &&
      other.event == event &&
      other.desc == desc &&
      other.wtst == wtst &&
      other.locations == locations &&
      other.trip == trip &&
      other.current == current &&
      other.deviceId == deviceId &&
      other.location == location &&
      other.spd == spd &&
      other.dir == dir &&
      other.dev == dev;

    @override
    int get hashCode =>
        type.hashCode +
        features.hashCode +
        (nextCursor == null ? 0 : nextCursor.hashCode) +
        attributions.hashCode +
        watermarkSeq.hashCode +
        (asOfSeq == null ? 0 : asOfSeq.hashCode) +
        (asOf == null ? 0 : asOf.hashCode) +
        approximate.hashCode +
        (horizon == null ? 0 : horizon.hashCode) +
        licenceFiltered.hashCode +
        records.hashCode +
        version.hashCode +
        groupKey.hashCode +
        truncatedAlerts.hashCode +
        status.hashCode +
        receiver.hashCode +
        groupLabels.hashCode +
        commonLabels.hashCode +
        commonAnnotations.hashCode +
        externalURL.hashCode +
        alerts.hashCode +
        specversion.hashCode +
        id.hashCode +
        source_.hashCode +
        subject.hashCode +
        time.hashCode +
        datacontenttype.hashCode +
        dataschema.hashCode +
        (data == null ? 0 : data.hashCode) +
        typeUnderscore.hashCode +
        lat.hashCode +
        lon.hashCode +
        tst.hashCode +
        acc.hashCode +
        alt.hashCode +
        vel.hashCode +
        cog.hashCode +
        batt.hashCode +
        bs.hashCode +
        conn.hashCode +
        tid.hashCode +
        event.hashCode +
        desc.hashCode +
        wtst.hashCode +
        locations.hashCode +
        (trip == null ? 0 : trip.hashCode) +
        (current == null ? 0 : current.hashCode) +
        deviceId.hashCode +
        location.hashCode +
        spd.hashCode +
        dir.hashCode +
        dev.hashCode;

  factory IngestRecordsFormatRequest.fromJson(Map<String, dynamic> json) => _$IngestRecordsFormatRequestFromJson(json);

  Map<String, dynamic> toJson() => _$IngestRecordsFormatRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IngestRecordsFormatRequestVersionEnum {
@JsonValue(r'4')
n4(r'4');

const IngestRecordsFormatRequestVersionEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IngestRecordsFormatRequestStatusEnum {
@JsonValue(r'firing')
firing(r'firing'),
@JsonValue(r'resolved')
resolved(r'resolved');

const IngestRecordsFormatRequestStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IngestRecordsFormatRequestSpecversionEnum {
@JsonValue(r'1.0')
n1period0(r'1.0');

const IngestRecordsFormatRequestSpecversionEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IngestRecordsFormatRequestTypeUnderscoreEnum {
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

const IngestRecordsFormatRequestTypeUnderscoreEnum(this.value);

final String value;

@override
String toString() => value;
}


