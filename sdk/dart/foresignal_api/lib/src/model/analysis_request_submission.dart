//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analysis_request_submission.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalysisRequestSubmission {
  /// Returns a new [AnalysisRequestSubmission] instance.
  AnalysisRequestSubmission({

    required  this.forecasterId,

    required  this.questionId,

     this.orientation,

     this.horizon,
  });

      /// The external forecaster id, edp:{sourceId}:{streamId}.
  @JsonKey(
    
    name: r'forecasterId',
    required: true,
    includeIfNull: false,
  )


  final String forecasterId;



      /// The ledger question to forecast; must be open.
  @JsonKey(
    
    name: r'questionId',
    required: true,
    includeIfNull: false,
  )


  final String questionId;



      /// Whether the provider's probability is for the question as stated (same) or its negation (inverse).
  @JsonKey(
    
    name: r'orientation',
    required: false,
    includeIfNull: false,
  )


  final AnalysisRequestSubmissionOrientationEnum? orientation;



      /// Optional forecast horizon passed to the provider (for example an ISO-8601 duration).
  @JsonKey(
    
    name: r'horizon',
    required: false,
    includeIfNull: false,
  )


  final String? horizon;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalysisRequestSubmission &&
      other.forecasterId == forecasterId &&
      other.questionId == questionId &&
      other.orientation == orientation &&
      other.horizon == horizon;

    @override
    int get hashCode =>
        forecasterId.hashCode +
        questionId.hashCode +
        orientation.hashCode +
        (horizon == null ? 0 : horizon.hashCode);

  factory AnalysisRequestSubmission.fromJson(Map<String, dynamic> json) => _$AnalysisRequestSubmissionFromJson(json);

  Map<String, dynamic> toJson() => _$AnalysisRequestSubmissionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Whether the provider's probability is for the question as stated (same) or its negation (inverse).
enum AnalysisRequestSubmissionOrientationEnum {
@JsonValue(r'same')
same(r'same'),
@JsonValue(r'inverse')
inverse(r'inverse');

const AnalysisRequestSubmissionOrientationEnum(this.value);

final String value;

@override
String toString() => value;
}


