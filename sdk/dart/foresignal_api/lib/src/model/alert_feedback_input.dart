//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'alert_feedback_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AlertFeedbackInput {
  /// Returns a new [AlertFeedbackInput] instance.
  AlertFeedbackInput({

    required  this.feedback,

     this.note,
  });

  @JsonKey(
    
    name: r'feedback',
    required: true,
    includeIfNull: false,
  )


  final AlertFeedbackInputFeedbackEnum feedback;



  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AlertFeedbackInput &&
      other.feedback == feedback &&
      other.note == note;

    @override
    int get hashCode =>
        feedback.hashCode +
        (note == null ? 0 : note.hashCode);

  factory AlertFeedbackInput.fromJson(Map<String, dynamic> json) => _$AlertFeedbackInputFromJson(json);

  Map<String, dynamic> toJson() => _$AlertFeedbackInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AlertFeedbackInputFeedbackEnum {
@JsonValue(r'useful')
useful(r'useful'),
@JsonValue(r'noise')
noise(r'noise'),
@JsonValue(r'late')
late_(r'late'),
@JsonValue(r'false_positive')
falsePositive(r'false_positive');

const AlertFeedbackInputFeedbackEnum(this.value);

final String value;

@override
String toString() => value;
}


