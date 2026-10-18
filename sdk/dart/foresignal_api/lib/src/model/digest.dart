//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/digest_alerts.dart';
import 'package:foresignal_api/src/model/digest_count.dart';
import 'package:foresignal_api/src/model/digest_health_item.dart';
import 'package:foresignal_api/src/model/place_weather_digest.dart';
import 'package:foresignal_api/src/model/digest_stats_notable_inner.dart';
import 'package:foresignal_api/src/model/digest_active.dart';
import 'package:foresignal_api/src/model/digest_all_of_next.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'digest.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Digest {
  /// Returns a new [Digest] instance.
  Digest({

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

    required  this.fromTime,

    required  this.toTime,

    required  this.next,

     this.weatherAtPlaces,
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



  @JsonKey(
    
    name: r'fromTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime fromTime;



  @JsonKey(
    
    name: r'toTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime toTime;



  @JsonKey(
    
    name: r'next',
    required: true,
    includeIfNull: false,
  )


  final DigestAllOfNext next;



      /// \"Weather at places\" section, present when `places` is passed
  @JsonKey(
    
    name: r'weatherAtPlaces',
    required: false,
    includeIfNull: false,
  )


  final List<PlaceWeatherDigest>? weatherAtPlaces;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Digest &&
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
      other.sections == sections &&
      other.fromTime == fromTime &&
      other.toTime == toTime &&
      other.next == next &&
      other.weatherAtPlaces == weatherAtPlaces;

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
        sections.hashCode +
        fromTime.hashCode +
        toTime.hashCode +
        next.hashCode +
        weatherAtPlaces.hashCode;

  factory Digest.fromJson(Map<String, dynamic> json) => _$DigestFromJson(json);

  Map<String, dynamic> toJson() => _$DigestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

