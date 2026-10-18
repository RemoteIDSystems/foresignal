//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'assessment_feedback_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AssessmentFeedbackInput {
  /// Returns a new [AssessmentFeedbackInput] instance.
  AssessmentFeedbackInput({

    required  this.rating,

     this.comment,
  });

  @JsonKey(
    
    name: r'rating',
    required: true,
    includeIfNull: false,
  )


  final AssessmentFeedbackInputRatingEnum rating;



  @JsonKey(
    
    name: r'comment',
    required: false,
    includeIfNull: false,
  )


  final String? comment;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AssessmentFeedbackInput &&
      other.rating == rating &&
      other.comment == comment;

    @override
    int get hashCode =>
        rating.hashCode +
        (comment == null ? 0 : comment.hashCode);

  factory AssessmentFeedbackInput.fromJson(Map<String, dynamic> json) => _$AssessmentFeedbackInputFromJson(json);

  Map<String, dynamic> toJson() => _$AssessmentFeedbackInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AssessmentFeedbackInputRatingEnum {
@JsonValue(r'useful')
useful(r'useful'),
@JsonValue(r'not_useful')
notUseful(r'not_useful'),
@JsonValue(r'wrong')
wrong(r'wrong');

const AssessmentFeedbackInputRatingEnum(this.value);

final String value;

@override
String toString() => value;
}


