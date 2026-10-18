//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_proposal_decision.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystProposalDecision {
  /// Returns a new [AnalystProposalDecision] instance.
  AnalystProposalDecision({

     this.reason,
  });

  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystProposalDecision &&
      other.reason == reason;

    @override
    int get hashCode =>
        reason.hashCode;

  factory AnalystProposalDecision.fromJson(Map<String, dynamic> json) => _$AnalystProposalDecisionFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystProposalDecisionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

