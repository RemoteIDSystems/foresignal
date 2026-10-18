//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/icd_term.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_submitted_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastSubmittedEvent {
  /// Returns a new [ForecastSubmittedEvent] instance.
  ForecastSubmittedEvent({

    required  this.event,

    required  this.questionId,

    required  this.forecastId,

    required  this.forecasterId,

    required  this.madeAt,

     this.probability,

     this.estimativeTerm,

     this.private,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final ForecastSubmittedEventEventEnum event;



      /// ULID identifier
  @JsonKey(
    
    name: r'questionId',
    required: true,
    includeIfNull: false,
  )


  final String questionId;



      /// ULID identifier
  @JsonKey(
    
    name: r'forecastId',
    required: true,
    includeIfNull: false,
  )


  final String forecastId;



  @JsonKey(
    
    name: r'forecasterId',
    required: true,
    includeIfNull: false,
  )


  final String forecasterId;



  @JsonKey(
    
    name: r'madeAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime madeAt;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: false,
    includeIfNull: false,
  )


  final num? probability;



  @JsonKey(
    
    name: r'estimativeTerm',
    required: false,
    includeIfNull: false,
  )


  final IcdTerm? estimativeTerm;



  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastSubmittedEvent &&
      other.event == event &&
      other.questionId == questionId &&
      other.forecastId == forecastId &&
      other.forecasterId == forecasterId &&
      other.madeAt == madeAt &&
      other.probability == probability &&
      other.estimativeTerm == estimativeTerm &&
      other.private == private;

    @override
    int get hashCode =>
        event.hashCode +
        questionId.hashCode +
        forecastId.hashCode +
        forecasterId.hashCode +
        madeAt.hashCode +
        (probability == null ? 0 : probability.hashCode) +
        (estimativeTerm == null ? 0 : estimativeTerm.hashCode) +
        private.hashCode;

  factory ForecastSubmittedEvent.fromJson(Map<String, dynamic> json) => _$ForecastSubmittedEventFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastSubmittedEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecastSubmittedEventEventEnum {
@JsonValue(r'forecast.submitted')
forecastPeriodSubmitted(r'forecast.submitted');

const ForecastSubmittedEventEventEnum(this.value);

final String value;

@override
String toString() => value;
}


