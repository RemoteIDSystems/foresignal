//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/export_manifest_files_inner.dart';
import 'package:foresignal_api/src/model/export_manifest_signature.dart';
import 'package:foresignal_api/src/model/export_licence_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'export_manifest.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExportManifest {
  /// Returns a new [ExportManifest] instance.
  ExportManifest({

     this.manifestVersion,

    required  this.id,

    required  this.kind,

    required  this.format,

    required  this.instanceId,

    required  this.createdAt,

     this.watermarkSeq,

     this.atSeq,

     this.spec,

     this.rowCount,

    required  this.files,

    required  this.licence,

     this.personalUse,

     this.signature,
  });

  @JsonKey(
    
    name: r'manifestVersion',
    required: false,
    includeIfNull: false,
  )


  final ExportManifestManifestVersionEnum? manifestVersion;



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


  final ExportManifestKindEnum kind;



  @JsonKey(
    
    name: r'format',
    required: true,
    includeIfNull: false,
  )


  final ExportManifestFormatEnum format;



  @JsonKey(
    
    name: r'instanceId',
    required: true,
    includeIfNull: false,
  )


  final String instanceId;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'watermarkSeq',
    required: false,
    includeIfNull: false,
  )


  final int? watermarkSeq;



  @JsonKey(
    
    name: r'atSeq',
    required: false,
    includeIfNull: false,
  )


  final int? atSeq;



  @JsonKey(
    
    name: r'spec',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? spec;



  @JsonKey(
    
    name: r'rowCount',
    required: false,
    includeIfNull: false,
  )


  final int? rowCount;



  @JsonKey(
    
    name: r'files',
    required: true,
    includeIfNull: false,
  )


  final List<ExportManifestFilesInner> files;



  @JsonKey(
    
    name: r'licence',
    required: true,
    includeIfNull: false,
  )


  final ExportLicenceSummary licence;



  @JsonKey(
    
    name: r'personalUse',
    required: false,
    includeIfNull: false,
  )


  final bool? personalUse;



  @JsonKey(
    
    name: r'signature',
    required: false,
    includeIfNull: false,
  )


  final ExportManifestSignature? signature;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExportManifest &&
      other.manifestVersion == manifestVersion &&
      other.id == id &&
      other.kind == kind &&
      other.format == format &&
      other.instanceId == instanceId &&
      other.createdAt == createdAt &&
      other.watermarkSeq == watermarkSeq &&
      other.atSeq == atSeq &&
      other.spec == spec &&
      other.rowCount == rowCount &&
      other.files == files &&
      other.licence == licence &&
      other.personalUse == personalUse &&
      other.signature == signature;

    @override
    int get hashCode =>
        manifestVersion.hashCode +
        id.hashCode +
        kind.hashCode +
        format.hashCode +
        instanceId.hashCode +
        createdAt.hashCode +
        (watermarkSeq == null ? 0 : watermarkSeq.hashCode) +
        (atSeq == null ? 0 : atSeq.hashCode) +
        spec.hashCode +
        rowCount.hashCode +
        files.hashCode +
        licence.hashCode +
        personalUse.hashCode +
        signature.hashCode;

  factory ExportManifest.fromJson(Map<String, dynamic> json) => _$ExportManifestFromJson(json);

  Map<String, dynamic> toJson() => _$ExportManifestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExportManifestManifestVersionEnum {
@JsonValue(1)
number1('1');

const ExportManifestManifestVersionEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ExportManifestKindEnum {
@JsonValue(r'EXPORT')
EXPORT(r'EXPORT'),
@JsonValue(r'SNAPSHOT')
SNAPSHOT(r'SNAPSHOT'),
@JsonValue(r'PACK')
PACK(r'PACK');

const ExportManifestKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ExportManifestFormatEnum {
@JsonValue(r'geoparquet')
geoparquet(r'geoparquet'),
@JsonValue(r'geopackage')
geopackage(r'geopackage'),
@JsonValue(r'flatgeobuf')
flatgeobuf(r'flatgeobuf'),
@JsonValue(r'gpx')
gpx(r'gpx'),
@JsonValue(r'pmtiles')
pmtiles(r'pmtiles');

const ExportManifestFormatEnum(this.value);

final String value;

@override
String toString() => value;
}


