//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/persist_reason_counts.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'fetch_outcome.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FetchOutcome {
  /// Returns a new [FetchOutcome] instance.
  FetchOutcome({

    required  this.sourceId,

     this.startedAt,

     this.durationMs,

    required  this.success,

    required  this.recordsSeen,

    required  this.recordsChanged,

     this.fromSeq,

     this.toSeq,

     this.error,

     this.fetchId,

     this.persisted,

     this.live,

     this.touched,

     this.touchesThrottled,

     this.late_,

     this.skipped,

     this.rejected,

     this.byReason,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'startedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? startedAt;



  @JsonKey(
    
    name: r'durationMs',
    required: false,
    includeIfNull: false,
  )


  final int? durationMs;



  @JsonKey(
    
    name: r'success',
    required: true,
    includeIfNull: false,
  )


  final bool success;



  @JsonKey(
    
    name: r'recordsSeen',
    required: true,
    includeIfNull: false,
  )


  final int recordsSeen;



      /// Alias of `persisted` (kept for v1.1 clients)
  @JsonKey(
    
    name: r'recordsChanged',
    required: true,
    includeIfNull: false,
  )


  final int recordsChanged;



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
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;



      /// fetch_log row of this fetch
  @JsonKey(
    
    name: r'fetchId',
    required: false,
    includeIfNull: false,
  )


  final int? fetchId;



          // minimum: 0
  @JsonKey(
    
    name: r'persisted',
    required: false,
    includeIfNull: false,
  )


  final int? persisted;



      /// Live (non-persisted) updates
          // minimum: 0
  @JsonKey(
    
    name: r'live',
    required: false,
    includeIfNull: false,
  )


  final int? live;



      /// Touch rows written
          // minimum: 0
  @JsonKey(
    
    name: r'touched',
    required: false,
    includeIfNull: false,
  )


  final int? touched;



          // minimum: 0
  @JsonKey(
    
    name: r'touchesThrottled',
    required: false,
    includeIfNull: false,
  )


  final int? touchesThrottled;



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
    
    name: r'rejected',
    required: false,
    includeIfNull: false,
  )


  final int? rejected;



  @JsonKey(
    
    name: r'byReason',
    required: false,
    includeIfNull: false,
  )


  final PersistReasonCounts? byReason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FetchOutcome &&
      other.sourceId == sourceId &&
      other.startedAt == startedAt &&
      other.durationMs == durationMs &&
      other.success == success &&
      other.recordsSeen == recordsSeen &&
      other.recordsChanged == recordsChanged &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq &&
      other.error == error &&
      other.fetchId == fetchId &&
      other.persisted == persisted &&
      other.live == live &&
      other.touched == touched &&
      other.touchesThrottled == touchesThrottled &&
      other.late_ == late_ &&
      other.skipped == skipped &&
      other.rejected == rejected &&
      other.byReason == byReason;

    @override
    int get hashCode =>
        sourceId.hashCode +
        startedAt.hashCode +
        durationMs.hashCode +
        success.hashCode +
        recordsSeen.hashCode +
        recordsChanged.hashCode +
        (fromSeq == null ? 0 : fromSeq.hashCode) +
        (toSeq == null ? 0 : toSeq.hashCode) +
        (error == null ? 0 : error.hashCode) +
        (fetchId == null ? 0 : fetchId.hashCode) +
        persisted.hashCode +
        live.hashCode +
        touched.hashCode +
        touchesThrottled.hashCode +
        late_.hashCode +
        skipped.hashCode +
        rejected.hashCode +
        byReason.hashCode;

  factory FetchOutcome.fromJson(Map<String, dynamic> json) => _$FetchOutcomeFromJson(json);

  Map<String, dynamic> toJson() => _$FetchOutcomeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

