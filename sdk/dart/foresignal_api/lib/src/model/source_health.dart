//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/budget.dart';
import 'package:foresignal_api/src/model/health_status.dart';
import 'package:foresignal_api/src/model/source_storage.dart';
import 'package:foresignal_api/src/model/doctor_report_summary.dart';
import 'package:foresignal_api/src/model/coverage.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'source_health.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SourceHealth {
  /// Returns a new [SourceHealth] instance.
  SourceHealth({

    required  this.sourceId,

    required  this.status,

     this.statusReason,

     this.lastAttemptAt,

     this.lastSuccessAt,

     this.lastErrorAt,

     this.lastError,

     this.lastHttpStatus,

    required  this.consecutiveFailures,

     this.lastRecordsSeen,

     this.lastRecordsChanged,

     this.errorRateLastHour,

     this.backoffUntil,

     this.budget,

     this.budgetUsedToday,

     this.budgetRemainingToday,

     this.refusedLastHour,

     this.requestsLastHour,

     this.bytesLastHour,

     this.coverage,

     this.storage,

     this.lastDataAt,

     this.budgetHoursRemaining,

     this.newestFrameAt,

     this.frameAgeSeconds,

     this.details,

     this.lastDoctorReport,

     this.spentUsdThisMonth,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final HealthStatus status;



      /// null while healthy
  @JsonKey(
    
    name: r'statusReason',
    required: false,
    includeIfNull: false,
  )


  final String? statusReason;



  @JsonKey(
    
    name: r'lastAttemptAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastAttemptAt;



  @JsonKey(
    
    name: r'lastSuccessAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastSuccessAt;



  @JsonKey(
    
    name: r'lastErrorAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastErrorAt;



  @JsonKey(
    
    name: r'lastError',
    required: false,
    includeIfNull: false,
  )


  final String? lastError;



  @JsonKey(
    
    name: r'lastHttpStatus',
    required: false,
    includeIfNull: false,
  )


  final int? lastHttpStatus;



  @JsonKey(
    
    name: r'consecutiveFailures',
    required: true,
    includeIfNull: false,
  )


  final int consecutiveFailures;



  @JsonKey(
    
    name: r'lastRecordsSeen',
    required: false,
    includeIfNull: false,
  )


  final int? lastRecordsSeen;



  @JsonKey(
    
    name: r'lastRecordsChanged',
    required: false,
    includeIfNull: false,
  )


  final int? lastRecordsChanged;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'errorRateLastHour',
    required: false,
    includeIfNull: false,
  )


  final num? errorRateLastHour;



  @JsonKey(
    
    name: r'backoffUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? backoffUntil;



  @JsonKey(
    
    name: r'budget',
    required: false,
    includeIfNull: false,
  )


  final Budget? budget;



  @JsonKey(
    
    name: r'budgetUsedToday',
    required: false,
    includeIfNull: false,
  )


  final int? budgetUsedToday;



  @JsonKey(
    
    name: r'budgetRemainingToday',
    required: false,
    includeIfNull: false,
  )


  final int? budgetRemainingToday;



  @JsonKey(
    
    name: r'refusedLastHour',
    required: false,
    includeIfNull: false,
  )


  final int? refusedLastHour;



  @JsonKey(
    
    name: r'requestsLastHour',
    required: false,
    includeIfNull: false,
  )


  final int? requestsLastHour;



  @JsonKey(
    
    name: r'bytesLastHour',
    required: false,
    includeIfNull: false,
  )


  final int? bytesLastHour;



  @JsonKey(
    
    name: r'coverage',
    required: false,
    includeIfNull: false,
  )


  final Coverage? coverage;



  @JsonKey(
    
    name: r'storage',
    required: false,
    includeIfNull: false,
  )


  final SourceStorage? storage;



      /// Last OK fetch or stream flush with records (a 304 never sets it)
  @JsonKey(
    
    name: r'lastDataAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastDataAt;



      /// Hours until today's budget is exhausted at the last hour's rate; null when that rate is 0
          // minimum: 0
  @JsonKey(
    
    name: r'budgetHoursRemaining',
    required: false,
    includeIfNull: false,
  )


  final num? budgetHoursRemaining;



      /// Product sources: newest frame time (frame-age health rule)
  @JsonKey(
    
    name: r'newestFrameAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? newestFrameAt;



      /// Product sources: age of the newest frame
  @JsonKey(
    
    name: r'frameAgeSeconds',
    required: false,
    includeIfNull: false,
  )


  final int? frameAgeSeconds;



      /// Adapter-specific health details; omitted when there are none. aisstream (063): `compressionEnabled` (from its SubscriptionConfirmation), `perMessageDeflate` (whether the handshake negotiated it; present while connected) and `vessels` (vessels held in the merged state)
  @JsonKey(
    
    name: r'details',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? details;



  @JsonKey(
    
    name: r'lastDoctorReport',
    required: false,
    includeIfNull: false,
  )


  final DoctorReportSummary? lastDoctorReport;



      /// Paid adapters: money spent this UTC calendar month (135), against `budget.monthlyCapUsd`
          // minimum: 0
  @JsonKey(
    
    name: r'spentUsdThisMonth',
    required: false,
    includeIfNull: false,
  )


  final num? spentUsdThisMonth;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SourceHealth &&
      other.sourceId == sourceId &&
      other.status == status &&
      other.statusReason == statusReason &&
      other.lastAttemptAt == lastAttemptAt &&
      other.lastSuccessAt == lastSuccessAt &&
      other.lastErrorAt == lastErrorAt &&
      other.lastError == lastError &&
      other.lastHttpStatus == lastHttpStatus &&
      other.consecutiveFailures == consecutiveFailures &&
      other.lastRecordsSeen == lastRecordsSeen &&
      other.lastRecordsChanged == lastRecordsChanged &&
      other.errorRateLastHour == errorRateLastHour &&
      other.backoffUntil == backoffUntil &&
      other.budget == budget &&
      other.budgetUsedToday == budgetUsedToday &&
      other.budgetRemainingToday == budgetRemainingToday &&
      other.refusedLastHour == refusedLastHour &&
      other.requestsLastHour == requestsLastHour &&
      other.bytesLastHour == bytesLastHour &&
      other.coverage == coverage &&
      other.storage == storage &&
      other.lastDataAt == lastDataAt &&
      other.budgetHoursRemaining == budgetHoursRemaining &&
      other.newestFrameAt == newestFrameAt &&
      other.frameAgeSeconds == frameAgeSeconds &&
      other.details == details &&
      other.lastDoctorReport == lastDoctorReport &&
      other.spentUsdThisMonth == spentUsdThisMonth;

    @override
    int get hashCode =>
        sourceId.hashCode +
        status.hashCode +
        (statusReason == null ? 0 : statusReason.hashCode) +
        (lastAttemptAt == null ? 0 : lastAttemptAt.hashCode) +
        (lastSuccessAt == null ? 0 : lastSuccessAt.hashCode) +
        (lastErrorAt == null ? 0 : lastErrorAt.hashCode) +
        (lastError == null ? 0 : lastError.hashCode) +
        (lastHttpStatus == null ? 0 : lastHttpStatus.hashCode) +
        consecutiveFailures.hashCode +
        lastRecordsSeen.hashCode +
        lastRecordsChanged.hashCode +
        errorRateLastHour.hashCode +
        (backoffUntil == null ? 0 : backoffUntil.hashCode) +
        budget.hashCode +
        budgetUsedToday.hashCode +
        budgetRemainingToday.hashCode +
        refusedLastHour.hashCode +
        requestsLastHour.hashCode +
        bytesLastHour.hashCode +
        (coverage == null ? 0 : coverage.hashCode) +
        storage.hashCode +
        (lastDataAt == null ? 0 : lastDataAt.hashCode) +
        (budgetHoursRemaining == null ? 0 : budgetHoursRemaining.hashCode) +
        (newestFrameAt == null ? 0 : newestFrameAt.hashCode) +
        (frameAgeSeconds == null ? 0 : frameAgeSeconds.hashCode) +
        details.hashCode +
        (lastDoctorReport == null ? 0 : lastDoctorReport.hashCode) +
        (spentUsdThisMonth == null ? 0 : spentUsdThisMonth.hashCode);

  factory SourceHealth.fromJson(Map<String, dynamic> json) => _$SourceHealthFromJson(json);

  Map<String, dynamic> toJson() => _$SourceHealthToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

