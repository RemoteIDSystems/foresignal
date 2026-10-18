//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'assessment_feedback.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AssessmentFeedback {
  /// Returns a new [AssessmentFeedback] instance.
  AssessmentFeedback({

    required  this.id,

    required  this.assessmentId,

    required  this.rating,

     this.comment,

    required  this.by,

    required  this.at,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// ULID identifier
  @JsonKey(
    
    name: r'assessmentId',
    required: true,
    includeIfNull: false,
  )


  final String assessmentId;



  @JsonKey(
    
    name: r'rating',
    required: true,
    includeIfNull: false,
  )


  final AssessmentFeedbackRatingEnum rating;



  @JsonKey(
    
    name: r'comment',
    required: false,
    includeIfNull: false,
  )


  final String? comment;



  @JsonKey(
    
    name: r'by',
    required: true,
    includeIfNull: false,
  )


  final String by;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AssessmentFeedback &&
      other.id == id &&
      other.assessmentId == assessmentId &&
      other.rating == rating &&
      other.comment == comment &&
      other.by == by &&
      other.at == at;

    @override
    int get hashCode =>
        id.hashCode +
        assessmentId.hashCode +
        rating.hashCode +
        (comment == null ? 0 : comment.hashCode) +
        by.hashCode +
        at.hashCode;

  factory AssessmentFeedback.fromJson(Map<String, dynamic> json) => _$AssessmentFeedbackFromJson(json);

  Map<String, dynamic> toJson() => _$AssessmentFeedbackToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AssessmentFeedbackRatingEnum {
@JsonValue(r'useful')
useful(r'useful'),
@JsonValue(r'not_useful')
notUseful(r'not_useful'),
@JsonValue(r'wrong')
wrong(r'wrong');

const AssessmentFeedbackRatingEnum(this.value);

final String value;

@override
String toString() => value;
}


