//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/coverage.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'fetch_log_entry.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FetchLogEntry {
  /// Returns a new [FetchLogEntry] instance.
  FetchLogEntry({

    required  this.id,

    required  this.trigger,

    required  this.status,

    required  this.startedAt,

     this.receivedAt,

     this.ingestedAt,

     this.httpStatus,

     this.requestCount,

     this.urlRedacted,

     this.etag,

     this.bytes,

     this.bodySha256,

     this.adapterVersion,

     this.licence,

     this.principal,

     this.recordsSeen,

     this.rejected,

     this.late_,

     this.skipped,

     this.versionsWritten,

     this.liveUpdates,

     this.touches,

     this.fromSeq,

     this.toSeq,

     this.coverage,

     this.error,

     this.runId,
  });

      /// Fetch id (= `batchId` on versions)
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'trigger',
    required: true,
    includeIfNull: false,
  )


  final FetchLogEntryTriggerEnum trigger;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final FetchLogEntryStatusEnum status;



  @JsonKey(
    
    name: r'startedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime startedAt;



  @JsonKey(
    
    name: r'receivedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? receivedAt;



      /// Set only when the writer inserted the row
  @JsonKey(
    
    name: r'ingestedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? ingestedAt;



  @JsonKey(
    
    name: r'httpStatus',
    required: false,
    includeIfNull: false,
  )


  final int? httpStatus;



          // minimum: 0
  @JsonKey(
    
    name: r'requestCount',
    required: false,
    includeIfNull: false,
  )


  final int? requestCount;



      /// Last request URL with credentials redacted
  @JsonKey(
    
    name: r'urlRedacted',
    required: false,
    includeIfNull: false,
  )


  final String? urlRedacted;



  @JsonKey(
    
    name: r'etag',
    required: false,
    includeIfNull: false,
  )


  final String? etag;



  @JsonKey(
    
    name: r'bytes',
    required: false,
    includeIfNull: false,
  )


  final int? bytes;



  @JsonKey(
    
    name: r'bodySha256',
    required: false,
    includeIfNull: false,
  )


  final String? bodySha256;



  @JsonKey(
    
    name: r'adapterVersion',
    required: false,
    includeIfNull: false,
  )


  final String? adapterVersion;



  @JsonKey(
    
    name: r'licence',
    required: false,
    includeIfNull: false,
  )


  final String? licence;



      /// Pushing principal (PUSH rows)
  @JsonKey(
    
    name: r'principal',
    required: false,
    includeIfNull: false,
  )


  final String? principal;



          // minimum: 0
  @JsonKey(
    
    name: r'recordsSeen',
    required: false,
    includeIfNull: false,
  )


  final int? recordsSeen;



          // minimum: 0
  @JsonKey(
    
    name: r'rejected',
    required: false,
    includeIfNull: false,
  )


  final int? rejected;



          // minimum: 0
  @JsonKey(
    
    name: r'late',
    required: false,
    includeIfNull: false,
  )


  final int? late_;



          // minimum: 0
  @JsonKey(
    
    name: r'skipped',
    required: false,
    includeIfNull: false,
  )


  final int? skipped;



          // minimum: 0
  @JsonKey(
    
    name: r'versionsWritten',
    required: false,
    includeIfNull: false,
  )


  final int? versionsWritten;



          // minimum: 0
  @JsonKey(
    
    name: r'liveUpdates',
    required: false,
    includeIfNull: false,
  )


  final int? liveUpdates;



          // minimum: 0
  @JsonKey(
    
    name: r'touches',
    required: false,
    includeIfNull: false,
  )


  final int? touches;



  @JsonKey(
    
    name: r'fromSeq',
    required: false,
    includeIfNull: false,
  )


  final int? fromSeq;



  @JsonKey(
    
    name: r'toSeq',
    required: false,
    includeIfNull: false,
  )


  final int? toSeq;



  @JsonKey(
    
    name: r'coverage',
    required: false,
    includeIfNull: false,
  )


  final Coverage? coverage;



      /// Redacted error (≤ 2000 chars)
  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;



      /// Shared by the chunks of one chunked fetch run (094)
  @JsonKey(
    
    name: r'runId',
    required: false,
    includeIfNull: false,
  )


  final String? runId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FetchLogEntry &&
      other.id == id &&
      other.trigger == trigger &&
      other.status == status &&
      other.startedAt == startedAt &&
      other.receivedAt == receivedAt &&
      other.ingestedAt == ingestedAt &&
      other.httpStatus == httpStatus &&
      other.requestCount == requestCount &&
      other.urlRedacted == urlRedacted &&
      other.etag == etag &&
      other.bytes == bytes &&
      other.bodySha256 == bodySha256 &&
      other.adapterVersion == adapterVersion &&
      other.licence == licence &&
      other.principal == principal &&
      other.recordsSeen == recordsSeen &&
      other.rejected == rejected &&
      other.late_ == late_ &&
      other.skipped == skipped &&
      other.versionsWritten == versionsWritten &&
      other.liveUpdates == liveUpdates &&
      other.touches == touches &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq &&
      other.coverage == coverage &&
      other.error == error &&
      other.runId == runId;

    @override
    int get hashCode =>
        id.hashCode +
        trigger.hashCode +
        status.hashCode +
        startedAt.hashCode +
        (receivedAt == null ? 0 : receivedAt.hashCode) +
        (ingestedAt == null ? 0 : ingestedAt.hashCode) +
        (httpStatus == null ? 0 : httpStatus.hashCode) +
        requestCount.hashCode +
        (urlRedacted == null ? 0 : urlRedacted.hashCode) +
        (etag == null ? 0 : etag.hashCode) +
        (bytes == null ? 0 : bytes.hashCode) +
        (bodySha256 == null ? 0 : bodySha256.hashCode) +
        (adapterVersion == null ? 0 : adapterVersion.hashCode) +
        (licence == null ? 0 : licence.hashCode) +
        (principal == null ? 0 : principal.hashCode) +
        recordsSeen.hashCode +
        rejected.hashCode +
        late_.hashCode +
        skipped.hashCode +
        versionsWritten.hashCode +
        liveUpdates.hashCode +
        touches.hashCode +
        (fromSeq == null ? 0 : fromSeq.hashCode) +
        (toSeq == null ? 0 : toSeq.hashCode) +
        (coverage == null ? 0 : coverage.hashCode) +
        (error == null ? 0 : error.hashCode) +
        (runId == null ? 0 : runId.hashCode);

  factory FetchLogEntry.fromJson(Map<String, dynamic> json) => _$FetchLogEntryFromJson(json);

  Map<String, dynamic> toJson() => _$FetchLogEntryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum FetchLogEntryTriggerEnum {
@JsonValue(r'SCHEDULED')
SCHEDULED(r'SCHEDULED'),
@JsonValue(r'MANUAL')
MANUAL(r'MANUAL'),
@JsonValue(r'STREAM')
STREAM(r'STREAM'),
@JsonValue(r'PUSH')
PUSH(r'PUSH');

const FetchLogEntryTriggerEnum(this.value);

final String value;

@override
String toString() => value;
}


enum FetchLogEntryStatusEnum {
@JsonValue(r'OK')
OK(r'OK'),
@JsonValue(r'NOT_MODIFIED')
NOT_MODIFIED(r'NOT_MODIFIED'),
@JsonValue(r'FAILED')
FAILED(r'FAILED'),
@JsonValue(r'REFUSED')
REFUSED(r'REFUSED'),
@JsonValue(r'DISCARDED')
DISCARDED(r'DISCARDED');

const FetchLogEntryStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


