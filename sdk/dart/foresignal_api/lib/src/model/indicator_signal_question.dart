//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'indicator_signal_question.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IndicatorSignalQuestion {
  /// Returns a new [IndicatorSignalQuestion] instance.
  IndicatorSignalQuestion({

    required  this.questionId,

    required  this.outcome,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'questionId',
    required: true,
    includeIfNull: false,
  )


  final String questionId;



  @JsonKey(
    
    name: r'outcome',
    required: true,
    includeIfNull: false,
  )


  final String outcome;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IndicatorSignalQuestion &&
      other.questionId == questionId &&
      other.outcome == outcome;

    @override
    int get hashCode =>
        questionId.hashCode +
        outcome.hashCode;

  factory IndicatorSignalQuestion.fromJson(Map<String, dynamic> json) => _$IndicatorSignalQuestionFromJson(json);

  Map<String, dynamic> toJson() => _$IndicatorSignalQuestionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

