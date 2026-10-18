//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/collection_gap.dart';
import 'package:foresignal_api/src/model/cycle_packet_forecasts.dart';
import 'package:foresignal_api/src/model/cycle_packet_outlooks.dart';
import 'package:foresignal_api/src/model/key_judgment.dart';
import 'package:foresignal_api/src/model/cycle_packet_hypotheses.dart';
import 'package:foresignal_api/src/model/cycle_packet_coverage.dart';
import 'package:foresignal_api/src/model/cycle_packet_situations.dart';
import 'package:foresignal_api/src/model/cycle_packet_item.dart';
import 'package:foresignal_api/src/model/social_versus_observed.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cycle_packet.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CyclePacket {
  /// Returns a new [CyclePacket] instance.
  CyclePacket({

    required  this.version,

    required  this.since,

    required  this.until,

     this.firstRun,

     this.situations,

     this.hypotheses,

     this.findings,

     this.forecasts,

     this.outlooks,

     this.coverage,

     this.collectionGaps,

     this.verdicts,

     this.previousJudgments,

     this.truncatedForLlm,

     this.social,

     this.cadence,
  });

  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final String version;



  @JsonKey(
    
    name: r'since',
    required: true,
    includeIfNull: false,
  )


  final DateTime since;



  @JsonKey(
    
    name: r'until',
    required: true,
    includeIfNull: false,
  )


  final DateTime until;



      /// Lookback used the cadence's natural window
  @JsonKey(
    
    name: r'firstRun',
    required: false,
    includeIfNull: false,
  )


  final bool? firstRun;



  @JsonKey(
    
    name: r'situations',
    required: false,
    includeIfNull: false,
  )


  final CyclePacketSituations? situations;



  @JsonKey(
    
    name: r'hypotheses',
    required: false,
    includeIfNull: false,
  )


  final CyclePacketHypotheses? hypotheses;



  @JsonKey(
    
    name: r'findings',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? findings;



  @JsonKey(
    
    name: r'forecasts',
    required: false,
    includeIfNull: false,
  )


  final CyclePacketForecasts? forecasts;



  @JsonKey(
    
    name: r'outlooks',
    required: false,
    includeIfNull: false,
  )


  final CyclePacketOutlooks? outlooks;



  @JsonKey(
    
    name: r'coverage',
    required: false,
    includeIfNull: false,
  )


  final CyclePacketCoverage? coverage;



  @JsonKey(
    
    name: r'collectionGaps',
    required: false,
    includeIfNull: false,
  )


  final List<CollectionGap>? collectionGaps;



  @JsonKey(
    
    name: r'verdicts',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? verdicts;



  @JsonKey(
    
    name: r'previousJudgments',
    required: false,
    includeIfNull: false,
  )


  final List<KeyJudgment>? previousJudgments;



      /// Items beyond the 30-per-section LLM cap, per section
  @JsonKey(
    
    name: r'truncatedForLlm',
    required: false,
    includeIfNull: false,
  )


  final Map<String, int>? truncatedForLlm;



      /// Narratives by status, attention gaps, out-of-sample lead times, media reuse and coordination findings (137)
  @JsonKey(
    
    name: r'social',
    required: false,
    includeIfNull: false,
  )


  final SocialVersusObserved? social;



      /// The cadence the packet was built for
  @JsonKey(
    
    name: r'cadence',
    required: false,
    includeIfNull: false,
  )


  final CyclePacketCadenceEnum? cadence;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CyclePacket &&
      other.version == version &&
      other.since == since &&
      other.until == until &&
      other.firstRun == firstRun &&
      other.situations == situations &&
      other.hypotheses == hypotheses &&
      other.findings == findings &&
      other.forecasts == forecasts &&
      other.outlooks == outlooks &&
      other.coverage == coverage &&
      other.collectionGaps == collectionGaps &&
      other.verdicts == verdicts &&
      other.previousJudgments == previousJudgments &&
      other.truncatedForLlm == truncatedForLlm &&
      other.social == social &&
      other.cadence == cadence;

    @override
    int get hashCode =>
        version.hashCode +
        since.hashCode +
        until.hashCode +
        firstRun.hashCode +
        situations.hashCode +
        hypotheses.hashCode +
        findings.hashCode +
        forecasts.hashCode +
        outlooks.hashCode +
        coverage.hashCode +
        collectionGaps.hashCode +
        verdicts.hashCode +
        previousJudgments.hashCode +
        truncatedForLlm.hashCode +
        (social == null ? 0 : social.hashCode) +
        cadence.hashCode;

  factory CyclePacket.fromJson(Map<String, dynamic> json) => _$CyclePacketFromJson(json);

  Map<String, dynamic> toJson() => _$CyclePacketToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// The cadence the packet was built for
enum CyclePacketCadenceEnum {
@JsonValue(r'daily')
daily(r'daily'),
@JsonValue(r'weekly')
weekly(r'weekly'),
@JsonValue(r'monthly')
monthly(r'monthly'),
@JsonValue(r'on-demand')
onDemand(r'on-demand');

const CyclePacketCadenceEnum(this.value);

final String value;

@override
String toString() => value;
}


