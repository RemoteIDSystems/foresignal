//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_assessment_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationAssessmentRequest {
  /// Returns a new [SituationAssessmentRequest] instance.
  SituationAssessmentRequest({

     this.roles,

     this.effort,
  });

      /// Default analyst and red-team
  @JsonKey(
    
    name: r'roles',
    required: false,
    includeIfNull: false,
  )


  final Set<SituationAssessmentRequestRolesEnum>? roles;



      /// LLM effort
  @JsonKey(
    
    name: r'effort',
    required: false,
    includeIfNull: false,
  )


  final SituationAssessmentRequestEffortEnum? effort;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationAssessmentRequest &&
      other.roles == roles &&
      other.effort == effort;

    @override
    int get hashCode =>
        roles.hashCode +
        effort.hashCode;

  factory SituationAssessmentRequest.fromJson(Map<String, dynamic> json) => _$SituationAssessmentRequestFromJson(json);

  Map<String, dynamic> toJson() => _$SituationAssessmentRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SituationAssessmentRequestRolesEnum {
@JsonValue(r'analyst')
analyst(r'analyst'),
@JsonValue(r'red-team')
redTeam(r'red-team'),
@JsonValue(r'forecaster')
forecaster(r'forecaster'),
@JsonValue(r'proposer')
proposer(r'proposer');

const SituationAssessmentRequestRolesEnum(this.value);

final String value;

@override
String toString() => value;
}


/// LLM effort
enum SituationAssessmentRequestEffortEnum {
@JsonValue(r'low')
low(r'low'),
@JsonValue(r'medium')
medium(r'medium'),
@JsonValue(r'high')
high(r'high');

const SituationAssessmentRequestEffortEnum(this.value);

final String value;

@override
String toString() => value;
}


