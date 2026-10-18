//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/event_study_result_events_inner.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/event_study_result_caar.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:foresignal_api/src/model/event_study_result_windows.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_study_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventStudyResult {
  /// Returns a new [EventStudyResult] instance.
  EventStudyResult({

    required  this.detector,

     this.definitionHash,

    required  this.target,

    required  this.frequency,

     this.transform,

    required  this.windows,

     this.range,

     this.asOfSeq,

     this.placeboSeed,

    required  this.nEvents,

     this.nClusters,

     this.nExcluded,

    required  this.events,

    required  this.caar,

    required  this.caveats,

     this.valuesWithheld,

     this.truncated,

    required  this.manifestId,
  });

  @JsonKey(
    
    name: r'detector',
    required: true,
    includeIfNull: false,
  )


  final DetectorRef detector;



  @JsonKey(
    
    name: r'definitionHash',
    required: false,
    includeIfNull: false,
  )


  final String? definitionHash;



      /// Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left, center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement, every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the platform-language denominator; complete and paged collection only)
  @JsonKey(
    
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final String target;



  @JsonKey(
    
    name: r'frequency',
    required: true,
    includeIfNull: false,
  )


  final EventStudyResultFrequencyEnum frequency;



  @JsonKey(
    
    name: r'transform',
    required: false,
    includeIfNull: false,
  )


  final EventStudyResultTransformEnum? transform;



  @JsonKey(
    
    name: r'windows',
    required: true,
    includeIfNull: false,
  )


  final EventStudyResultWindows windows;



  @JsonKey(
    
    name: r'range',
    required: false,
    includeIfNull: false,
  )


  final TimeRange? range;



  @JsonKey(
    
    name: r'asOfSeq',
    required: false,
    includeIfNull: false,
  )


  final int? asOfSeq;



      /// Deterministic seed (hash of definition and range)
  @JsonKey(
    
    name: r'placeboSeed',
    required: false,
    includeIfNull: false,
  )


  final String? placeboSeed;



  @JsonKey(
    
    name: r'nEvents',
    required: true,
    includeIfNull: false,
  )


  final int nEvents;



  @JsonKey(
    
    name: r'nClusters',
    required: false,
    includeIfNull: false,
  )


  final int? nClusters;



  @JsonKey(
    
    name: r'nExcluded',
    required: false,
    includeIfNull: false,
  )


  final int? nExcluded;



  @JsonKey(
    
    name: r'events',
    required: true,
    includeIfNull: false,
  )


  final List<EventStudyResultEventsInner> events;



  @JsonKey(
    
    name: r'caar',
    required: true,
    includeIfNull: false,
  )


  final EventStudyResultCaar caar;



      /// 103 caveats plus `FEW_EVENTS`, `LAGGED_PUBLICATION`, `DAILY_RESOLUTION_ONLY`
  @JsonKey(
    
    name: r'caveats',
    required: true,
    includeIfNull: false,
  )


  final List<Caveat> caveats;



  @JsonKey(
    
    name: r'valuesWithheld',
    required: false,
    includeIfNull: false,
  )


  final bool? valuesWithheld;



  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: true,
    includeIfNull: false,
  )


  final String manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventStudyResult &&
      other.detector == detector &&
      other.definitionHash == definitionHash &&
      other.target == target &&
      other.frequency == frequency &&
      other.transform == transform &&
      other.windows == windows &&
      other.range == range &&
      other.asOfSeq == asOfSeq &&
      other.placeboSeed == placeboSeed &&
      other.nEvents == nEvents &&
      other.nClusters == nClusters &&
      other.nExcluded == nExcluded &&
      other.events == events &&
      other.caar == caar &&
      other.caveats == caveats &&
      other.valuesWithheld == valuesWithheld &&
      other.truncated == truncated &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        detector.hashCode +
        definitionHash.hashCode +
        target.hashCode +
        frequency.hashCode +
        transform.hashCode +
        windows.hashCode +
        range.hashCode +
        asOfSeq.hashCode +
        placeboSeed.hashCode +
        nEvents.hashCode +
        nClusters.hashCode +
        nExcluded.hashCode +
        events.hashCode +
        caar.hashCode +
        caveats.hashCode +
        valuesWithheld.hashCode +
        truncated.hashCode +
        manifestId.hashCode;

  factory EventStudyResult.fromJson(Map<String, dynamic> json) => _$EventStudyResultFromJson(json);

  Map<String, dynamic> toJson() => _$EventStudyResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EventStudyResultFrequencyEnum {
@JsonValue(r'day')
day(r'day'),
@JsonValue(r'week')
week(r'week');

const EventStudyResultFrequencyEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EventStudyResultTransformEnum {
@JsonValue(r'log-return')
logReturn(r'log-return'),
@JsonValue(r'log-level-standardised')
logLevelStandardised(r'log-level-standardised'),
@JsonValue(r'weekly-difference-standardised')
weeklyDifferenceStandardised(r'weekly-difference-standardised'),
@JsonValue(r'anscombe-residual')
anscombeResidual(r'anscombe-residual');

const EventStudyResultTransformEnum(this.value);

final String value;

@override
String toString() => value;
}


