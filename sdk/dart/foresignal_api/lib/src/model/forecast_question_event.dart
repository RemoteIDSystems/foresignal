//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/forecast_question_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_question_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastQuestionEvent {
  /// Returns a new [ForecastQuestionEvent] instance.
  ForecastQuestionEvent({

    required  this.event,

    required  this.question,

    required  this.at,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final ForecastQuestionEventEventEnum event;



  @JsonKey(
    
    name: r'question',
    required: true,
    includeIfNull: false,
  )


  final ForecastQuestionSummary question;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastQuestionEvent &&
      other.event == event &&
      other.question == question &&
      other.at == at;

    @override
    int get hashCode =>
        event.hashCode +
        question.hashCode +
        at.hashCode;

  factory ForecastQuestionEvent.fromJson(Map<String, dynamic> json) => _$ForecastQuestionEventFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastQuestionEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecastQuestionEventEventEnum {
@JsonValue(r'question.opened')
questionPeriodOpened(r'question.opened'),
@JsonValue(r'question.closed')
questionPeriodClosed(r'question.closed'),
@JsonValue(r'question.resolved')
questionPeriodResolved(r'question.resolved'),
@JsonValue(r'question.annulled')
questionPeriodAnnulled(r'question.annulled');

const ForecastQuestionEventEventEnum(this.value);

final String value;

@override
String toString() => value;
}


