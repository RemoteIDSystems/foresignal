//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ingest_outcome_rejections_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_outcome.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestOutcome {
  /// Returns a new [IngestOutcome] instance.
  IngestOutcome({

    required  this.seen,

    required  this.changed,

    required  this.rejected,

     this.fromSeq,

     this.toSeq,

     this.rejections,

    required  this.persisted,

     this.live,

     this.touched,

     this.touchesThrottled,

     this.late_,

     this.skipped,

     this.byReason,

     this.versionBytes,
  });

  @JsonKey(
    
    name: r'seen',
    required: true,
    includeIfNull: false,
  )


  final int seen;



      /// Deprecated alias of `persisted`
  @Deprecated('changed has been deprecated')
  @JsonKey(
    
    name: r'changed',
    required: true,
    includeIfNull: false,
  )


  final int changed;



  @JsonKey(
    
    name: r'rejected',
    required: true,
    includeIfNull: false,
  )


  final int rejected;



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
    
    name: r'rejections',
    required: false,
    includeIfNull: false,
  )


  final List<IngestOutcomeRejectionsInner>? rejections;



      /// Versions written
          // minimum: 0
  @JsonKey(
    
    name: r'persisted',
    required: true,
    includeIfNull: false,
  )


  final int persisted;



      /// Observations written as live-only updates, without a version
          // minimum: 0
  @JsonKey(
    
    name: r'live',
    required: false,
    includeIfNull: false,
  )


  final int? live;



      /// Observations written as touches (unchanged content, refreshed observedAt, lastReceivedAt and validity)
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



      /// Persisted versions per persist reason; every reason is present, zeros included: FIRST, REAPPEAR, SIGNIFICANT, TOMBSTONE, CHANGE, SAMPLE, KEYFRAME, HEARTBEAT, TRIGGER_EDGE
  @JsonKey(
    
    name: r'byReason',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? byReason;



          // minimum: 0
  @JsonKey(
    
    name: r'versionBytes',
    required: false,
    includeIfNull: false,
  )


  final int? versionBytes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestOutcome &&
      other.seen == seen &&
      other.changed == changed &&
      other.rejected == rejected &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq &&
      other.rejections == rejections &&
      other.persisted == persisted &&
      other.live == live &&
      other.touched == touched &&
      other.touchesThrottled == touchesThrottled &&
      other.late_ == late_ &&
      other.skipped == skipped &&
      other.byReason == byReason &&
      other.versionBytes == versionBytes;

    @override
    int get hashCode =>
        seen.hashCode +
        changed.hashCode +
        rejected.hashCode +
        (fromSeq == null ? 0 : fromSeq.hashCode) +
        (toSeq == null ? 0 : toSeq.hashCode) +
        rejections.hashCode +
        persisted.hashCode +
        live.hashCode +
        touched.hashCode +
        touchesThrottled.hashCode +
        late_.hashCode +
        skipped.hashCode +
        byReason.hashCode +
        versionBytes.hashCode;

  factory IngestOutcome.fromJson(Map<String, dynamic> json) => _$IngestOutcomeFromJson(json);

  Map<String, dynamic> toJson() => _$IngestOutcomeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

