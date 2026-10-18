//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'reference_import.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReferenceImport {
  /// Returns a new [ReferenceImport] instance.
  ReferenceImport({

    required  this.id,

    required  this.dataset,

     this.params,

    required  this.status,

     this.statusReason,

     this.progress,

     this.fileSha256,

    required  this.rowsRead,

    required  this.rowsWritten,

     this.periodFrom,

     this.periodTo,

    required  this.licence,

    required  this.egress,

     this.jobId,

     this.startedAt,

     this.finishedAt,

     this.error,

    required  this.createdBy,

    required  this.createdAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'dataset',
    required: true,
    includeIfNull: false,
  )


  final ReferenceImportDatasetEnum dataset;



  @JsonKey(
    
    name: r'params',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? params;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ReferenceImportStatusEnum status;



      /// e.g. `credentials absent` for ACLED
  @JsonKey(
    
    name: r'statusReason',
    required: false,
    includeIfNull: false,
  )


  final String? statusReason;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'progress',
    required: false,
    includeIfNull: false,
  )


  final num? progress;



  @JsonKey(
    
    name: r'fileSha256',
    required: false,
    includeIfNull: false,
  )


  final String? fileSha256;



  @JsonKey(
    
    name: r'rowsRead',
    required: true,
    includeIfNull: false,
  )


  final int rowsRead;



  @JsonKey(
    
    name: r'rowsWritten',
    required: true,
    includeIfNull: false,
  )


  final int rowsWritten;



  @JsonKey(
    
    name: r'periodFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? periodFrom;



  @JsonKey(
    
    name: r'periodTo',
    required: false,
    includeIfNull: false,
  )


  final DateTime? periodTo;



      /// e.g. `CC-BY-4.0`, `ACLED-EULA`, `public-domain`
  @JsonKey(
    
    name: r'licence',
    required: true,
    includeIfNull: false,
  )


  final String licence;



  @JsonKey(
    
    name: r'egress',
    required: true,
    includeIfNull: false,
  )


  final ReferenceImportEgressEnum egress;



      /// ULID identifier
  @JsonKey(
    
    name: r'jobId',
    required: false,
    includeIfNull: false,
  )


  final String? jobId;



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



  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;



  @JsonKey(
    
    name: r'createdBy',
    required: true,
    includeIfNull: false,
  )


  final String createdBy;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReferenceImport &&
      other.id == id &&
      other.dataset == dataset &&
      other.params == params &&
      other.status == status &&
      other.statusReason == statusReason &&
      other.progress == progress &&
      other.fileSha256 == fileSha256 &&
      other.rowsRead == rowsRead &&
      other.rowsWritten == rowsWritten &&
      other.periodFrom == periodFrom &&
      other.periodTo == periodTo &&
      other.licence == licence &&
      other.egress == egress &&
      other.jobId == jobId &&
      other.startedAt == startedAt &&
      other.finishedAt == finishedAt &&
      other.error == error &&
      other.createdBy == createdBy &&
      other.createdAt == createdAt;

    @override
    int get hashCode =>
        id.hashCode +
        dataset.hashCode +
        params.hashCode +
        status.hashCode +
        (statusReason == null ? 0 : statusReason.hashCode) +
        (progress == null ? 0 : progress.hashCode) +
        (fileSha256 == null ? 0 : fileSha256.hashCode) +
        rowsRead.hashCode +
        rowsWritten.hashCode +
        (periodFrom == null ? 0 : periodFrom.hashCode) +
        (periodTo == null ? 0 : periodTo.hashCode) +
        licence.hashCode +
        egress.hashCode +
        (jobId == null ? 0 : jobId.hashCode) +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        (error == null ? 0 : error.hashCode) +
        createdBy.hashCode +
        createdAt.hashCode;

  factory ReferenceImport.fromJson(Map<String, dynamic> json) => _$ReferenceImportFromJson(json);

  Map<String, dynamic> toJson() => _$ReferenceImportToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReferenceImportDatasetEnum {
@JsonValue(r'ucdp-ged')
ucdpGed(r'ucdp-ged'),
@JsonValue(r'acled')
acled(r'acled'),
@JsonValue(r'usgs-comcat')
usgsComcat(r'usgs-comcat'),
@JsonValue(r'gdacs')
gdacs(r'gdacs');

const ReferenceImportDatasetEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ReferenceImportStatusEnum {
@JsonValue(r'queued')
queued(r'queued'),
@JsonValue(r'running')
running(r'running'),
@JsonValue(r'done')
done(r'done'),
@JsonValue(r'failed')
failed(r'failed'),
@JsonValue(r'skipped')
skipped(r'skipped'),
@JsonValue(r'cancelled')
cancelled(r'cancelled');

const ReferenceImportStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ReferenceImportEgressEnum {
@JsonValue(r'OPEN')
OPEN(r'OPEN'),
@JsonValue(r'LOCAL_ONLY')
LOCAL_ONLY(r'LOCAL_ONLY');

const ReferenceImportEgressEnum(this.value);

final String value;

@override
String toString() => value;
}


