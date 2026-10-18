//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'series_forecast_model_backtest_results_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SeriesForecastModelBacktestResultsInner {
  /// Returns a new [SeriesForecastModelBacktestResultsInner] instance.
  SeriesForecastModelBacktestResultsInner({

    required  this.method,

    required  this.horizon,

    required  this.meanPinballLoss,

     this.intervalCoverageP05P95,
  });

  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final SeriesForecastModelBacktestResultsInnerMethodEnum method;



  @JsonKey(
    
    name: r'horizon',
    required: true,
    includeIfNull: false,
  )


  final int horizon;



  @JsonKey(
    
    name: r'meanPinballLoss',
    required: true,
    includeIfNull: false,
  )


  final num meanPinballLoss;



  @JsonKey(
    
    name: r'intervalCoverageP05P95',
    required: false,
    includeIfNull: false,
  )


  final num? intervalCoverageP05P95;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SeriesForecastModelBacktestResultsInner &&
      other.method == method &&
      other.horizon == horizon &&
      other.meanPinballLoss == meanPinballLoss &&
      other.intervalCoverageP05P95 == intervalCoverageP05P95;

    @override
    int get hashCode =>
        method.hashCode +
        horizon.hashCode +
        meanPinballLoss.hashCode +
        (intervalCoverageP05P95 == null ? 0 : intervalCoverageP05P95.hashCode);

  factory SeriesForecastModelBacktestResultsInner.fromJson(Map<String, dynamic> json) => _$SeriesForecastModelBacktestResultsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SeriesForecastModelBacktestResultsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SeriesForecastModelBacktestResultsInnerMethodEnum {
@JsonValue(r'seasonal-naive')
seasonalNaive(r'seasonal-naive'),
@JsonValue(r'ets-ann')
etsAnn(r'ets-ann'),
@JsonValue(r'ets-aaa')
etsAaa(r'ets-aaa'),
@JsonValue(r'nb-glm')
nbGlm(r'nb-glm'),
@JsonValue(r'damped-trend')
dampedTrend(r'damped-trend');

const SeriesForecastModelBacktestResultsInnerMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


