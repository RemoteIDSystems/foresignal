//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/event_selector.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_study_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventStudyRequest {
  /// Returns a new [EventStudyRequest] instance.
  EventStudyRequest({

     this.name,

    required  this.selector,

    required  this.target,

    required  this.from,

    required  this.to,

     this.asOf,
  });

      /// Optional label
  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'selector',
    required: true,
    includeIfNull: false,
  )


  final EventSelector selector;



      /// Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left, center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement, every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the platform-language denominator; complete and paged collection only)
  @JsonKey(
    
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final String target;



      /// Start of the event range
  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;



      /// End of the event range
  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final DateTime to;



      /// Knowledge cutoff for the replay (default: submit time)
  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventStudyRequest &&
      other.name == name &&
      other.selector == selector &&
      other.target == target &&
      other.from == from &&
      other.to == to &&
      other.asOf == asOf;

    @override
    int get hashCode =>
        (name == null ? 0 : name.hashCode) +
        selector.hashCode +
        target.hashCode +
        from.hashCode +
        to.hashCode +
        (asOf == null ? 0 : asOf.hashCode);

  factory EventStudyRequest.fromJson(Map<String, dynamic> json) => _$EventStudyRequestFromJson(json);

  Map<String, dynamic> toJson() => _$EventStudyRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

