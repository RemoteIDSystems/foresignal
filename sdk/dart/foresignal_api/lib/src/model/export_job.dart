//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/export_file.dart';
import 'package:foresignal_api/src/model/export_licence_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'export_job.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExportJob {
  /// Returns a new [ExportJob] instance.
  ExportJob({

    required  this.id,

    required  this.kind,

    required  this.format,

     this.spec,

    required  this.status,

    required  this.progress,

     this.principal,

     this.rowCount,

     this.bytes,

     this.manifestSha256,

     this.licenceSummary,

     this.watermarkSeq,

     this.personalUse,

    required  this.createdAt,

     this.startedAt,

     this.finishedAt,

     this.expiresAt,

     this.error,

     this.downloadUrl,

     this.manifestUrl,

     this.files,

     this.productId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final ExportJobKindEnum kind;



  @JsonKey(
    
    name: r'format',
    required: true,
    includeIfNull: false,
  )


  final ExportJobFormatEnum format;



      /// The submitted ExportSpec, SnapshotInput or PackSpec
  @JsonKey(
    
    name: r'spec',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? spec;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ExportJobStatusEnum status;



          // minimum: 0
          // maximum: 100
  @JsonKey(
    
    name: r'progress',
    required: true,
    includeIfNull: false,
  )


  final int progress;



      /// Token id that created the job
  @JsonKey(
    
    name: r'principal',
    required: false,
    includeIfNull: false,
  )


  final String? principal;



  @JsonKey(
    
    name: r'rowCount',
    required: false,
    includeIfNull: false,
  )


  final int? rowCount;



  @JsonKey(
    
    name: r'bytes',
    required: false,
    includeIfNull: false,
  )


  final int? bytes;



  @JsonKey(
    
    name: r'manifestSha256',
    required: false,
    includeIfNull: false,
  )


  final String? manifestSha256;



  @JsonKey(
    
    name: r'licenceSummary',
    required: false,
    includeIfNull: false,
  )


  final ExportLicenceSummary? licenceSummary;



  @JsonKey(
    
    name: r'watermarkSeq',
    required: false,
    includeIfNull: false,
  )


  final int? watermarkSeq;



  @JsonKey(
    
    name: r'personalUse',
    required: false,
    includeIfNull: false,
  )


  final bool? personalUse;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'startedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? startedAt;



  @JsonKey(
    
    name: r'finishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? finishedAt;



      /// Default created + exports.ttl (7 d); null for snapshots without expiresAt
  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;



      /// /v1/exports/{id}/download once DONE
  @JsonKey(
    
    name: r'downloadUrl',
    required: false,
    includeIfNull: false,
  )


  final String? downloadUrl;



  @JsonKey(
    
    name: r'manifestUrl',
    required: false,
    includeIfNull: false,
  )


  final String? manifestUrl;



  @JsonKey(
    
    name: r'files',
    required: false,
    includeIfNull: false,
  )


  final List<ExportFile>? files;



  @JsonKey(
    
    name: r'productId',
    required: false,
    includeIfNull: false,
  )


  final String? productId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExportJob &&
      other.id == id &&
      other.kind == kind &&
      other.format == format &&
      other.spec == spec &&
      other.status == status &&
      other.progress == progress &&
      other.principal == principal &&
      other.rowCount == rowCount &&
      other.bytes == bytes &&
      other.manifestSha256 == manifestSha256 &&
      other.licenceSummary == licenceSummary &&
      other.watermarkSeq == watermarkSeq &&
      other.personalUse == personalUse &&
      other.createdAt == createdAt &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.expiresAt == expiresAt &&
      other.error == error &&
      other.downloadUrl == downloadUrl &&
      other.manifestUrl == manifestUrl &&
      other.files == files &&
      other.productId == productId;

    @override
    int get hashCode =>
        id.hashCode +
        kind.hashCode +
        format.hashCode +
        spec.hashCode +
        status.hashCode +
        progress.hashCode +
        principal.hashCode +
        (rowCount == null ? 0 : rowCount.hashCode) +
        (bytes == null ? 0 : bytes.hashCode) +
        (manifestSha256 == null ? 0 : manifestSha256.hashCode) +
        (licenceSummary == null ? 0 : licenceSummary.hashCode) +
        (watermarkSeq == null ? 0 : watermarkSeq.hashCode) +
        personalUse.hashCode +
        createdAt.hashCode +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        (expiresAt == null ? 0 : expiresAt.hashCode) +
        (error == null ? 0 : error.hashCode) +
        (downloadUrl == null ? 0 : downloadUrl.hashCode) +
        (manifestUrl == null ? 0 : manifestUrl.hashCode) +
        files.hashCode +
        (productId == null ? 0 : productId.hashCode);

  factory ExportJob.fromJson(Map<String, dynamic> json) => _$ExportJobFromJson(json);

  Map<String, dynamic> toJson() => _$ExportJobToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExportJobKindEnum {
@JsonValue(r'EXPORT')
EXPORT(r'EXPORT'),
@JsonValue(r'SNAPSHOT')
SNAPSHOT(r'SNAPSHOT'),
@JsonValue(r'PACK')
PACK(r'PACK'),
@JsonValue(r'RASTER')
RASTER(r'RASTER'),
@JsonValue(r'PLACE_PACK')
PLACE_PACK(r'PLACE_PACK');

const ExportJobKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ExportJobFormatEnum {
@JsonValue(r'geoparquet')
geoparquet(r'geoparquet'),
@JsonValue(r'geopackage')
geopackage(r'geopackage'),
@JsonValue(r'flatgeobuf')
flatgeobuf(r'flatgeobuf'),
@JsonValue(r'gpx')
gpx(r'gpx'),
@JsonValue(r'pmtiles')
pmtiles(r'pmtiles'),
@JsonValue(r'mbtiles')
mbtiles(r'mbtiles'),
@JsonValue(r'cog')
cog(r'cog'),
@JsonValue(r'zip')
zip(r'zip'),
@JsonValue(r'json-bundle')
jsonBundle(r'json-bundle');

const ExportJobFormatEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ExportJobStatusEnum {
@JsonValue(r'QUEUED')
QUEUED(r'QUEUED'),
@JsonValue(r'RUNNING')
RUNNING(r'RUNNING'),
@JsonValue(r'DONE')
DONE(r'DONE'),
@JsonValue(r'FAILED')
FAILED(r'FAILED'),
@JsonValue(r'CANCELLED')
CANCELLED(r'CANCELLED'),
@JsonValue(r'EXPIRED')
EXPIRED(r'EXPIRED');

const ExportJobStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


