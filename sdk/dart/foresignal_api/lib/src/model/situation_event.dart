//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/severity.dart';
import 'package:foresignal_api/src/model/situation_summary.dart';
import 'package:foresignal_api/src/model/situation_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationEvent {
  /// Returns a new [SituationEvent] instance.
  SituationEvent({

    required  this.event,

    required  this.situation,

    required  this.version,

     this.previousStatus,

     this.previousSeverity,

     this.mergedInto,

    required  this.at,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final SituationEventEventEnum event;



  @JsonKey(
    
    name: r'situation',
    required: true,
    includeIfNull: false,
  )


  final SituationSummary situation;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'previousStatus',
    required: false,
    includeIfNull: false,
  )


  final SituationStatus? previousStatus;



  @JsonKey(
    
    name: r'previousSeverity',
    required: false,
    includeIfNull: false,
  )


  final Severity? previousSeverity;



      /// ULID identifier
  @JsonKey(
    
    name: r'mergedInto',
    required: false,
    includeIfNull: false,
  )


  final String? mergedInto;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationEvent &&
      other.event == event &&
      other.situation == situation &&
      other.version == version &&
      other.previousStatus == previousStatus &&
      other.previousSeverity == previousSeverity &&
      other.mergedInto == mergedInto &&
      other.at == at;

    @override
    int get hashCode =>
        event.hashCode +
        situation.hashCode +
        version.hashCode +
        (previousStatus == null ? 0 : previousStatus.hashCode) +
        (previousSeverity == null ? 0 : previousSeverity.hashCode) +
        (mergedInto == null ? 0 : mergedInto.hashCode) +
        at.hashCode;

  factory SituationEvent.fromJson(Map<String, dynamic> json) => _$SituationEventFromJson(json);

  Map<String, dynamic> toJson() => _$SituationEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SituationEventEventEnum {
@JsonValue(r'situation.created')
situationPeriodCreated(r'situation.created'),
@JsonValue(r'situation.updated')
situationPeriodUpdated(r'situation.updated'),
@JsonValue(r'situation.activated')
situationPeriodActivated(r'situation.activated'),
@JsonValue(r'situation.escalated')
situationPeriodEscalated(r'situation.escalated'),
@JsonValue(r'situation.dormant')
situationPeriodDormant(r'situation.dormant'),
@JsonValue(r'situation.closed')
situationPeriodClosed(r'situation.closed'),
@JsonValue(r'situation.merged')
situationPeriodMerged(r'situation.merged');

const SituationEventEventEnum(this.value);

final String value;

@override
String toString() => value;
}


