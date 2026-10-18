//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'escalation_step.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EscalationStep {
  /// Returns a new [EscalationStep] instance.
  EscalationStep({

    required  this.afterMinutes,

     this.channelIds,

     this.includePush = false,
  });

      /// Strictly increasing across steps
          // minimum: 1
          // maximum: 1440
  @JsonKey(
    
    name: r'afterMinutes',
    required: true,
    includeIfNull: false,
  )


  final int afterMinutes;



  @JsonKey(
    
    name: r'channelIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? channelIds;



  @JsonKey(
    defaultValue: false,
    name: r'includePush',
    required: false,
    includeIfNull: false,
  )


  final bool? includePush;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EscalationStep &&
      other.afterMinutes == afterMinutes &&
      other.channelIds == channelIds &&
      other.includePush == includePush;

    @override
    int get hashCode =>
        afterMinutes.hashCode +
        channelIds.hashCode +
        includePush.hashCode;

  factory EscalationStep.fromJson(Map<String, dynamic> json) => _$EscalationStepFromJson(json);

  Map<String, dynamic> toJson() => _$EscalationStepToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

