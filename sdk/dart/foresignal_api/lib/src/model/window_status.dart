//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/window_status_states.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'window_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WindowStatus {
  /// Returns a new [WindowStatus] instance.
  WindowStatus({

    required  this.states,

    required  this.groups,

     this.groupsTruncated,

     this.learningUntil,

     this.lastEvaluatedAt,

     this.lowConfidence,
  });

  @JsonKey(
    
    name: r'states',
    required: true,
    includeIfNull: false,
  )


  final WindowStatusStates states;



  @JsonKey(
    
    name: r'groups',
    required: true,
    includeIfNull: false,
  )


  final int groups;



  @JsonKey(
    
    name: r'groupsTruncated',
    required: false,
    includeIfNull: false,
  )


  final bool? groupsTruncated;



      /// Anomaly rules: when every slot is expected to have minWeeks samples
  @JsonKey(
    
    name: r'learningUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? learningUntil;



  @JsonKey(
    
    name: r'lastEvaluatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastEvaluatedAt;



      /// hour-of-day anomaly rules started before minWeeks
  @JsonKey(
    
    name: r'lowConfidence',
    required: false,
    includeIfNull: false,
  )


  final bool? lowConfidence;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WindowStatus &&
      other.states == states &&
      other.groups == groups &&
      other.groupsTruncated == groupsTruncated &&
      other.learningUntil == learningUntil &&
      other.lastEvaluatedAt == lastEvaluatedAt &&
      other.lowConfidence == lowConfidence;

    @override
    int get hashCode =>
        states.hashCode +
        groups.hashCode +
        groupsTruncated.hashCode +
        (learningUntil == null ? 0 : learningUntil.hashCode) +
        (lastEvaluatedAt == null ? 0 : lastEvaluatedAt.hashCode) +
        lowConfidence.hashCode;

  factory WindowStatus.fromJson(Map<String, dynamic> json) => _$WindowStatusFromJson(json);

  Map<String, dynamic> toJson() => _$WindowStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

