//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_link_decision_candidates_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationLinkDecisionCandidatesInner {
  /// Returns a new [SituationLinkDecisionCandidatesInner] instance.
  SituationLinkDecisionCandidatesInner({

    required  this.situationId,

    required  this.score,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: true,
    includeIfNull: false,
  )


  final String situationId;



  @JsonKey(
    
    name: r'score',
    required: true,
    includeIfNull: false,
  )


  final num score;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationLinkDecisionCandidatesInner &&
      other.situationId == situationId &&
      other.score == score;

    @override
    int get hashCode =>
        situationId.hashCode +
        score.hashCode;

  factory SituationLinkDecisionCandidatesInner.fromJson(Map<String, dynamic> json) => _$SituationLinkDecisionCandidatesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SituationLinkDecisionCandidatesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

