//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_evaluation_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastEvaluationRequest {
  /// Returns a new [ForecastEvaluationRequest] instance.
  ForecastEvaluationRequest({

    required  this.forecasterId,

    required  this.from,

    required  this.to,

     this.categories,

     this.maxQuestions,
  });

  @JsonKey(
    
    name: r'forecasterId',
    required: true,
    includeIfNull: false,
  )


  final String forecasterId;



  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final DateTime to;



  @JsonKey(
    
    name: r'categories',
    required: false,
    includeIfNull: false,
  )


  final List<String>? categories;



          // minimum: 1
          // maximum: 10000
  @JsonKey(
    
    name: r'maxQuestions',
    required: false,
    includeIfNull: false,
  )


  final int? maxQuestions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastEvaluationRequest &&
      other.forecasterId == forecasterId &&
      other.from == from &&
      other.to == to &&
      other.categories == categories &&
      other.maxQuestions == maxQuestions;

    @override
    int get hashCode =>
        forecasterId.hashCode +
        from.hashCode +
        to.hashCode +
        categories.hashCode +
        maxQuestions.hashCode;

  factory ForecastEvaluationRequest.fromJson(Map<String, dynamic> json) => _$ForecastEvaluationRequestFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastEvaluationRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

