//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_forecast_item_all_of_question.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpForecastItemAllOfQuestion {
  /// Returns a new [EdpForecastItemAllOfQuestion] instance.
  EdpForecastItemAllOfQuestion({

    required  this.externalId,

    required  this.title,

     this.closeTime,

     this.resolutionCriteria,
  });

  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'closeTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? closeTime;



  @JsonKey(
    
    name: r'resolutionCriteria',
    required: false,
    includeIfNull: false,
  )


  final String? resolutionCriteria;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpForecastItemAllOfQuestion &&
      other.externalId == externalId &&
      other.title == title &&
      other.closeTime == closeTime &&
      other.resolutionCriteria == resolutionCriteria;

    @override
    int get hashCode =>
        externalId.hashCode +
        title.hashCode +
        (closeTime == null ? 0 : closeTime.hashCode) +
        (resolutionCriteria == null ? 0 : resolutionCriteria.hashCode);

  factory EdpForecastItemAllOfQuestion.fromJson(Map<String, dynamic> json) => _$EdpForecastItemAllOfQuestionFromJson(json);

  Map<String, dynamic> toJson() => _$EdpForecastItemAllOfQuestionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

