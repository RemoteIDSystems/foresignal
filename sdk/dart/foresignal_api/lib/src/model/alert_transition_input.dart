//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'alert_transition_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AlertTransitionInput {
  /// Returns a new [AlertTransitionInput] instance.
  AlertTransitionInput({

     this.note,

     this.feedback,
  });

  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;



  @JsonKey(
    
    name: r'feedback',
    required: false,
    includeIfNull: false,
  )


  final AlertTransitionInputFeedbackEnum? feedback;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AlertTransitionInput &&
      other.note == note &&
      other.feedback == feedback;

    @override
    int get hashCode =>
        (note == null ? 0 : note.hashCode) +
        (feedback == null ? 0 : feedback.hashCode);

  factory AlertTransitionInput.fromJson(Map<String, dynamic> json) => _$AlertTransitionInputFromJson(json);

  Map<String, dynamic> toJson() => _$AlertTransitionInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AlertTransitionInputFeedbackEnum {
@JsonValue(r'useful')
useful(r'useful'),
@JsonValue(r'noise')
noise(r'noise'),
@JsonValue(r'late')
late_(r'late'),
@JsonValue(r'false_positive')
falsePositive(r'false_positive');

const AlertTransitionInputFeedbackEnum(this.value);

final String value;

@override
String toString() => value;
}


