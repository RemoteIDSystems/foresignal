//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_feedback_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystFeedbackInput {
  /// Returns a new [AnalystFeedbackInput] instance.
  AnalystFeedbackInput({

    required  this.rating,

     this.comment,
  });

  @JsonKey(
    
    name: r'rating',
    required: true,
    includeIfNull: false,
  )


  final AnalystFeedbackInputRatingEnum rating;



  @JsonKey(
    
    name: r'comment',
    required: false,
    includeIfNull: false,
  )


  final String? comment;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystFeedbackInput &&
      other.rating == rating &&
      other.comment == comment;

    @override
    int get hashCode =>
        rating.hashCode +
        comment.hashCode;

  factory AnalystFeedbackInput.fromJson(Map<String, dynamic> json) => _$AnalystFeedbackInputFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystFeedbackInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnalystFeedbackInputRatingEnum {
@JsonValue(r'useful')
useful(r'useful'),
@JsonValue(r'not_useful')
notUseful(r'not_useful'),
@JsonValue(r'wrong')
wrong(r'wrong');

const AnalystFeedbackInputRatingEnum(this.value);

final String value;

@override
String toString() => value;
}


