//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'doctor_report_probe.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DoctorReportProbe {
  /// Returns a new [DoctorReportProbe] instance.
  DoctorReportProbe({

    required  this.skipped,

     this.status,

     this.httpStatus,

     this.contentType,

     this.bytes,

     this.url,

     this.durationMs,

     this.skipReason,

     this.records,

     this.requests,

     this.error,
  });

  @JsonKey(
    
    name: r'skipped',
    required: true,
    includeIfNull: false,
  )


  final bool skipped;



      /// ok, http-error, parse-error or failed (no response)
  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final String? status;



  @JsonKey(
    
    name: r'httpStatus',
    required: false,
    includeIfNull: false,
  )


  final int? httpStatus;



  @JsonKey(
    
    name: r'contentType',
    required: false,
    includeIfNull: false,
  )


  final String? contentType;



  @JsonKey(
    
    name: r'bytes',
    required: false,
    includeIfNull: false,
  )


  final int? bytes;



      /// Redacted
  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'durationMs',
    required: false,
    includeIfNull: false,
  )


  final int? durationMs;



      /// Why no probe was sent (budget and backoff give the finding PROBE_SKIPPED_BUDGET)
  @JsonKey(
    
    name: r'skipReason',
    required: false,
    includeIfNull: false,
  )


  final DoctorReportProbeSkipReasonEnum? skipReason;



      /// Records the adapter parsed from the probe (at most 100 counted)
  @JsonKey(
    
    name: r'records',
    required: false,
    includeIfNull: false,
  )


  final int? records;



      /// Upstream requests the probe made
  @JsonKey(
    
    name: r'requests',
    required: false,
    includeIfNull: false,
  )


  final int? requests;



      /// The request failure, redacted
  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DoctorReportProbe &&
      other.skipped == skipped &&
      other.status == status &&
      other.httpStatus == httpStatus &&
      other.contentType == contentType &&
      other.bytes == bytes &&
      other.url == url &&
      other.durationMs == durationMs &&
      other.skipReason == skipReason &&
      other.records == records &&
      other.requests == requests &&
      other.error == error;

    @override
    int get hashCode =>
        skipped.hashCode +
        (status == null ? 0 : status.hashCode) +
        (httpStatus == null ? 0 : httpStatus.hashCode) +
        (contentType == null ? 0 : contentType.hashCode) +
        (bytes == null ? 0 : bytes.hashCode) +
        (url == null ? 0 : url.hashCode) +
        (durationMs == null ? 0 : durationMs.hashCode) +
        (skipReason == null ? 0 : skipReason.hashCode) +
        (records == null ? 0 : records.hashCode) +
        (requests == null ? 0 : requests.hashCode) +
        (error == null ? 0 : error.hashCode);

  factory DoctorReportProbe.fromJson(Map<String, dynamic> json) => _$DoctorReportProbeFromJson(json);

  Map<String, dynamic> toJson() => _$DoctorReportProbeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Why no probe was sent (budget and backoff give the finding PROBE_SKIPPED_BUDGET)
enum DoctorReportProbeSkipReasonEnum {
@JsonValue(r'budget')
budget(r'budget'),
@JsonValue(r'backoff')
backoff(r'backoff'),
@JsonValue(r'no-upstream')
noUpstream(r'no-upstream'),
@JsonValue(r'not-requested')
notRequested(r'not-requested');

const DoctorReportProbeSkipReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


