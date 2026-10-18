//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/digest_alerts.dart';
import 'package:foresignal_api/src/model/digest_count.dart';
import 'package:foresignal_api/src/model/digest_health_item.dart';
import 'package:foresignal_api/src/model/digest_stats_notable_inner.dart';
import 'package:foresignal_api/src/model/digest_active.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'digest_stats.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DigestStats {
  /// Returns a new [DigestStats] instance.
  DigestStats({

    required  this.fromSeq,

    required  this.toSeq,

    required  this.gap,

     this.gapReason,

    required  this.counts,

     this.active,

     this.alerts,

     this.notable,

     this.health,

     this.healthSnapshot,

     this.sections,
  });

  @JsonKey(
    
    name: r'fromSeq',
    required: true,
    includeIfNull: false,
  )


  final int fromSeq;



  @JsonKey(
    
    name: r'toSeq',
    required: true,
    includeIfNull: false,
  )


  final int toSeq;



      /// True when fromSeq is below the retention floor of a scope source; counts are incomplete
  @JsonKey(
    
    name: r'gap',
    required: true,
    includeIfNull: false,
  )


  final bool gap;



  @JsonKey(
    
    name: r'gapReason',
    required: false,
    includeIfNull: false,
  )


  final String? gapReason;



  @JsonKey(
    
    name: r'counts',
    required: true,
    includeIfNull: false,
  )


  final List<DigestCount> counts;



  @JsonKey(
    
    name: r'active',
    required: false,
    includeIfNull: false,
  )


  final List<DigestActive>? active;



  @JsonKey(
    
    name: r'alerts',
    required: false,
    includeIfNull: false,
  )


  final DigestAlerts? alerts;



  @JsonKey(
    
    name: r'notable',
    required: false,
    includeIfNull: false,
  )


  final List<DigestStatsNotableInner>? notable;



      /// null when includeHealth is false
  @JsonKey(
    
    name: r'health',
    required: false,
    includeIfNull: false,
  )


  final List<DigestHealthItem>? health;



      /// True when health is the current snapshot rather than stored transitions (always true until health transitions are stored)
  @JsonKey(
    
    name: r'healthSnapshot',
    required: false,
    includeIfNull: false,
  )


  final bool? healthSnapshot;



      /// Sections from BriefingSectionContributor beans (e.g. 056 weather at places)
  @JsonKey(
    
    name: r'sections',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? sections;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DigestStats &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq &&
      other.gap == gap &&
      other.gapReason == gapReason &&
      other.counts == counts &&
      other.active == active &&
      other.alerts == alerts &&
      other.notable == notable &&
      other.health == health &&
      other.healthSnapshot == healthSnapshot &&
      other.sections == sections;

    @override
    int get hashCode =>
        fromSeq.hashCode +
        toSeq.hashCode +
        gap.hashCode +
        (gapReason == null ? 0 : gapReason.hashCode) +
        counts.hashCode +
        active.hashCode +
        alerts.hashCode +
        notable.hashCode +
        (health == null ? 0 : health.hashCode) +
        healthSnapshot.hashCode +
        sections.hashCode;

  factory DigestStats.fromJson(Map<String, dynamic> json) => _$DigestStatsFromJson(json);

  Map<String, dynamic> toJson() => _$DigestStatsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

