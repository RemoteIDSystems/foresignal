//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/tuning_suggestion_expected_effect.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tuning_suggestion_record.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TuningSuggestionRecord {
  /// Returns a new [TuningSuggestionRecord] instance.
  TuningSuggestionRecord({

    required  this.id,

    required  this.kind,

    required  this.rationale,

    required  this.patch_,

     this.expectedEffect,

     this.caveats,

    required  this.ruleId,

    required  this.ruleRev,

    required  this.status,

    required  this.createdAt,

     this.decidedBy,

     this.decidedAt,

     this.decisionReason,

     this.explanation,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final TuningSuggestionRecordKindEnum kind;



  @JsonKey(
    
    name: r'rationale',
    required: true,
    includeIfNull: false,
  )


  final String rationale;



      /// Partial TriggerRuleInput ready to apply as an ordinary rule update
  @JsonKey(
    
    name: r'patch',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> patch_;



  @JsonKey(
    
    name: r'expectedEffect',
    required: false,
    includeIfNull: false,
  )


  final TuningSuggestionExpectedEffect? expectedEffect;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;



  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



      /// The rule's definition hash when the suggestion was generated
  @JsonKey(
    
    name: r'ruleRev',
    required: true,
    includeIfNull: false,
  )


  final String ruleRev;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final TuningSuggestionRecordStatusEnum status;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'decidedBy',
    required: false,
    includeIfNull: false,
  )


  final String? decidedBy;



  @JsonKey(
    
    name: r'decidedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? decidedAt;



  @JsonKey(
    
    name: r'decisionReason',
    required: false,
    includeIfNull: false,
  )


  final String? decisionReason;



      /// Plain-language summary from the chat role when explain=true and the caller is authenticated (never edits the patch)
  @JsonKey(
    
    name: r'explanation',
    required: false,
    includeIfNull: false,
  )


  final String? explanation;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TuningSuggestionRecord &&
      other.id == id &&
      other.kind == kind &&
      other.rationale == rationale &&
      other.patch_ == patch_ &&
      other.expectedEffect == expectedEffect &&
      other.caveats == caveats &&
      other.ruleId == ruleId &&
      other.ruleRev == ruleRev &&
      other.status == status &&
      other.createdAt == createdAt &&
      other.decidedBy == decidedBy &&
      other.decidedAt == decidedAt &&
      other.decisionReason == decisionReason &&
      other.explanation == explanation;

    @override
    int get hashCode =>
        id.hashCode +
        kind.hashCode +
        rationale.hashCode +
        patch_.hashCode +
        (expectedEffect == null ? 0 : expectedEffect.hashCode) +
        caveats.hashCode +
        ruleId.hashCode +
        ruleRev.hashCode +
        status.hashCode +
        createdAt.hashCode +
        (decidedBy == null ? 0 : decidedBy.hashCode) +
        (decidedAt == null ? 0 : decidedAt.hashCode) +
        (decisionReason == null ? 0 : decisionReason.hashCode) +
        (explanation == null ? 0 : explanation.hashCode);

  factory TuningSuggestionRecord.fromJson(Map<String, dynamic> json) => _$TuningSuggestionRecordFromJson(json);

  Map<String, dynamic> toJson() => _$TuningSuggestionRecordToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TuningSuggestionRecordKindEnum {
@JsonValue(r'raise-threshold')
raiseThreshold(r'raise-threshold'),
@JsonValue(r'lower-threshold')
lowerThreshold(r'lower-threshold'),
@JsonValue(r'add-cooldown')
addCooldown(r'add-cooldown'),
@JsonValue(r'switch-to-enter')
switchToEnter(r'switch-to-enter'),
@JsonValue(r'add-edge-min-interval')
addEdgeMinInterval(r'add-edge-min-interval'),
@JsonValue(r'shrink-geofence')
shrinkGeofence(r'shrink-geofence'),
@JsonValue(r'lower-anomaly-sensitivity')
lowerAnomalySensitivity(r'lower-anomaly-sensitivity'),
@JsonValue(r'ACTIVE_WINDOW')
ACTIVE_WINDOW(r'ACTIVE_WINDOW'),
@JsonValue(r'SEVERITY_UP')
SEVERITY_UP(r'SEVERITY_UP'),
@JsonValue(r'SEVERITY_DOWN')
SEVERITY_DOWN(r'SEVERITY_DOWN');

const TuningSuggestionRecordKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum TuningSuggestionRecordStatusEnum {
@JsonValue(r'OPEN')
OPEN(r'OPEN'),
@JsonValue(r'APPLIED')
APPLIED(r'APPLIED'),
@JsonValue(r'DISMISSED')
DISMISSED(r'DISMISSED'),
@JsonValue(r'STALE')
STALE(r'STALE');

const TuningSuggestionRecordStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


