//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/trigger_rule_input.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tuning_log_entry.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TuningLogEntry {
  /// Returns a new [TuningLogEntry] instance.
  TuningLogEntry({

    required  this.id,

    required  this.ruleId,

     this.suggestionId,

    required  this.before,

    required  this.after,

     this.note,

    required  this.appliedBy,

    required  this.appliedAt,

     this.undoneBy,

     this.undoneAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



  @JsonKey(
    
    name: r'suggestionId',
    required: false,
    includeIfNull: false,
  )


  final String? suggestionId;



  @JsonKey(
    
    name: r'before',
    required: true,
    includeIfNull: false,
  )


  final TriggerRuleInput before;



  @JsonKey(
    
    name: r'after',
    required: true,
    includeIfNull: false,
  )


  final TriggerRuleInput after;



  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;



  @JsonKey(
    
    name: r'appliedBy',
    required: true,
    includeIfNull: false,
  )


  final String appliedBy;



  @JsonKey(
    
    name: r'appliedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime appliedAt;



  @JsonKey(
    
    name: r'undoneBy',
    required: false,
    includeIfNull: false,
  )


  final String? undoneBy;



  @JsonKey(
    
    name: r'undoneAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? undoneAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TuningLogEntry &&
      other.id == id &&
      other.ruleId == ruleId &&
      other.suggestionId == suggestionId &&
      other.before == before &&
      other.after == after &&
      other.note == note &&
      other.appliedBy == appliedBy &&
      other.appliedAt == appliedAt &&
      other.undoneBy == undoneBy &&
      other.undoneAt == undoneAt;

    @override
    int get hashCode =>
        id.hashCode +
        ruleId.hashCode +
        (suggestionId == null ? 0 : suggestionId.hashCode) +
        before.hashCode +
        after.hashCode +
        (note == null ? 0 : note.hashCode) +
        appliedBy.hashCode +
        appliedAt.hashCode +
        (undoneBy == null ? 0 : undoneBy.hashCode) +
        (undoneAt == null ? 0 : undoneAt.hashCode);

  factory TuningLogEntry.fromJson(Map<String, dynamic> json) => _$TuningLogEntryFromJson(json);

  Map<String, dynamic> toJson() => _$TuningLogEntryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

