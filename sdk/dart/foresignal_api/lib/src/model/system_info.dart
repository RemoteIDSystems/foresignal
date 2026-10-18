//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/system_info_search.dart';
import 'package:foresignal_api/src/model/system_info_streams.dart';
import 'package:foresignal_api/src/model/system_info_scheduler.dart';
import 'package:foresignal_api/src/model/system_info_history.dart';
import 'package:foresignal_api/src/model/mqtt_connections.dart';
import 'package:foresignal_api/src/model/system_info_catch_up.dart';
import 'package:foresignal_api/src/model/system_info_holds.dart';
import 'package:foresignal_api/src/model/system_info_raster_formats.dart';
import 'package:foresignal_api/src/model/system_info_limits.dart';
import 'package:foresignal_api/src/model/system_rasters.dart';
import 'package:foresignal_api/src/model/system_info_database.dart';
import 'package:foresignal_api/src/model/system_info_ingest_writer.dart';
import 'package:foresignal_api/src/model/system_info_records.dart';
import 'package:foresignal_api/src/model/system_info_security.dart';
import 'package:foresignal_api/src/model/system_info_sources.dart';
import 'package:foresignal_api/src/model/system_info_warnings_inner.dart';
import 'package:foresignal_api/src/model/system_info_llm.dart';
import 'package:foresignal_api/src/model/system_info_archive.dart';
import 'package:foresignal_api/src/model/system_info_notifications.dart';
import 'package:foresignal_api/src/model/system_info_exports.dart';
import 'package:foresignal_api/src/model/system_info_sinks.dart';
import 'package:foresignal_api/src/model/system_info_mcp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfo {
  /// Returns a new [SystemInfo] instance.
  SystemInfo({

    required  this.version,

    required  this.startedAt,

     this.uptimeSeconds,

     this.database,

     this.records,

     this.sources,

     this.scheduler,

     this.streams,

     this.notifications,

     this.llm,

     this.search,

     this.mcp,

     this.apiVersion,

     this.status,

     this.features,

     this.limits,

     this.history,

     this.catchUp,

     this.ingestWriter,

     this.warnings,

     this.security,

     this.rasters,

     this.mqtt,

     this.rasterFormats,

     this.sinks,

     this.exports,

     this.archive,

     this.holds,

     this.mode,
  });

  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final String version;



  @JsonKey(
    
    name: r'startedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime startedAt;



  @JsonKey(
    
    name: r'uptimeSeconds',
    required: false,
    includeIfNull: false,
  )


  final int? uptimeSeconds;



  @JsonKey(
    
    name: r'database',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoDatabase? database;



  @JsonKey(
    
    name: r'records',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoRecords? records;



  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoSources? sources;



  @JsonKey(
    
    name: r'scheduler',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoScheduler? scheduler;



  @JsonKey(
    
    name: r'streams',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoStreams? streams;



  @JsonKey(
    
    name: r'notifications',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoNotifications? notifications;



  @JsonKey(
    
    name: r'llm',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoLlm? llm;



  @JsonKey(
    
    name: r'search',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoSearch? search;



  @JsonKey(
    
    name: r'mcp',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoMcp? mcp;



      /// The spec's info.version
  @JsonKey(
    
    name: r'apiVersion',
    required: false,
    includeIfNull: false,
  )


  final String? apiVersion;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoStatusEnum? status;



      /// Capability flags from FeatureContributor beans (notifications, retention, later llm, search, push, mcp, weather …); 113 adds federation and federationServing, and in edge mode reports embeddings, llm, search, ask, rasters, briefings, ogc, exports and federationServing false
  @JsonKey(
    
    name: r'features',
    required: false,
    includeIfNull: false,
  )


  final Map<String, bool>? features;



  @JsonKey(
    
    name: r'limits',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoLimits? limits;



  @JsonKey(
    
    name: r'history',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoHistory? history;



  @JsonKey(
    
    name: r'catchUp',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoCatchUp? catchUp;



  @JsonKey(
    
    name: r'ingestWriter',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoIngestWriter? ingestWriter;



  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<SystemInfoWarningsInner>? warnings;



  @JsonKey(
    
    name: r'security',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoSecurity? security;



  @JsonKey(
    
    name: r'rasters',
    required: false,
    includeIfNull: false,
  )


  final SystemRasters? rasters;



  @JsonKey(
    
    name: r'mqtt',
    required: false,
    includeIfNull: false,
  )


  final MqttConnections? mqtt;



  @JsonKey(
    
    name: r'rasterFormats',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoRasterFormats? rasterFormats;



  @JsonKey(
    
    name: r'sinks',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoSinks? sinks;



  @JsonKey(
    
    name: r'exports',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoExports? exports;



  @JsonKey(
    
    name: r'archive',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoArchive? archive;



  @JsonKey(
    
    name: r'holds',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoHolds? holds;



      /// Deployment profile; in edge mode `features` reports embeddings, LLM, rasters, briefings, OGC, exports and federation serving off
  @JsonKey(
    
    name: r'mode',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoModeEnum? mode;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfo &&
      other.version == version &&
      other.startedAt == startedAt &&
      other.uptimeSeconds == uptimeSeconds &&
      other.database == database &&
      other.records == records &&
      other.sources == sources &&
      other.scheduler == scheduler &&
      other.streams == streams &&
      other.notifications == notifications &&
      other.llm == llm &&
      other.search == search &&
      other.mcp == mcp &&
      other.apiVersion == apiVersion &&
      other.status == status &&
      other.features == features &&
      other.limits == limits &&
      other.history == history &&
      other.catchUp == catchUp &&
      other.ingestWriter == ingestWriter &&
      other.warnings == warnings &&
      other.security == security &&
      other.rasters == rasters &&
      other.mqtt == mqtt &&
      other.rasterFormats == rasterFormats &&
      other.sinks == sinks &&
      other.exports == exports &&
      other.archive == archive &&
      other.holds == holds &&
      other.mode == mode;

    @override
    int get hashCode =>
        version.hashCode +
        startedAt.hashCode +
        uptimeSeconds.hashCode +
        database.hashCode +
        records.hashCode +
        sources.hashCode +
        scheduler.hashCode +
        streams.hashCode +
        notifications.hashCode +
        llm.hashCode +
        search.hashCode +
        mcp.hashCode +
        apiVersion.hashCode +
        status.hashCode +
        features.hashCode +
        limits.hashCode +
        history.hashCode +
        catchUp.hashCode +
        ingestWriter.hashCode +
        warnings.hashCode +
        security.hashCode +
        rasters.hashCode +
        mqtt.hashCode +
        rasterFormats.hashCode +
        sinks.hashCode +
        exports.hashCode +
        archive.hashCode +
        holds.hashCode +
        mode.hashCode;

  factory SystemInfo.fromJson(Map<String, dynamic> json) => _$SystemInfoFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SystemInfoStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'degraded')
degraded(r'degraded'),
@JsonValue(r'down')
down(r'down');

const SystemInfoStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Deployment profile; in edge mode `features` reports embeddings, LLM, rasters, briefings, OGC, exports and federation serving off
enum SystemInfoModeEnum {
@JsonValue(r'full')
full(r'full'),
@JsonValue(r'edge')
edge(r'edge');

const SystemInfoModeEnum(this.value);

final String value;

@override
String toString() => value;
}


