//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/glance_alerts.dart';
import 'package:foresignal_api/src/model/glance_activity.dart';
import 'package:foresignal_api/src/model/glance_briefing.dart';
import 'package:foresignal_api/src/model/glance_sources.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'glance.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Glance {
  /// Returns a new [Glance] instance.
  Glance({

    required  this.serverTime,

    required  this.status,

     this.alerts,

     this.sources,

     this.briefing,

     this.activity,

    required  this.lastSeq,

     this.links,

     this.sections,
  });

  @JsonKey(
    
    name: r'serverTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime serverTime;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final GlanceStatusEnum status;



  @JsonKey(
    
    name: r'alerts',
    required: false,
    includeIfNull: false,
  )


  final GlanceAlerts? alerts;



  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final GlanceSources? sources;



  @JsonKey(
    
    name: r'briefing',
    required: false,
    includeIfNull: false,
  )


  final GlanceBriefing? briefing;



  @JsonKey(
    
    name: r'activity',
    required: false,
    includeIfNull: false,
  )


  final GlanceActivity? activity;



  @JsonKey(
    
    name: r'lastSeq',
    required: true,
    includeIfNull: false,
  )


  final int lastSeq;



      /// Deep links from clientLinks
  @JsonKey(
    
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? links;



      /// GlanceContributor sections (e.g. weather at place, 056), each within a byte budget
  @JsonKey(
    
    name: r'sections',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? sections;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Glance &&
      other.serverTime == serverTime &&
      other.status == status &&
      other.alerts == alerts &&
      other.sources == sources &&
      other.briefing == briefing &&
      other.activity == activity &&
      other.lastSeq == lastSeq &&
      other.links == links &&
      other.sections == sections;

    @override
    int get hashCode =>
        serverTime.hashCode +
        status.hashCode +
        alerts.hashCode +
        sources.hashCode +
        (briefing == null ? 0 : briefing.hashCode) +
        activity.hashCode +
        lastSeq.hashCode +
        links.hashCode +
        sections.hashCode;

  factory Glance.fromJson(Map<String, dynamic> json) => _$GlanceFromJson(json);

  Map<String, dynamic> toJson() => _$GlanceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum GlanceStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'degraded')
degraded(r'degraded'),
@JsonValue(r'failing')
failing(r'failing');

const GlanceStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


