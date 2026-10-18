//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/series_forecast_model_backtest_results_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'series_forecast_model_backtest.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SeriesForecastModelBacktest {
  /// Returns a new [SeriesForecastModelBacktest] instance.
  SeriesForecastModelBacktest({

    required  this.origins,

     this.spacing,

     this.selectionHorizon,

    required  this.results,
  });

  @JsonKey(
    
    name: r'origins',
    required: true,
    includeIfNull: false,
  )


  final int origins;



  @JsonKey(
    
    name: r'spacing',
    required: false,
    includeIfNull: false,
  )


  final String? spacing;



  @JsonKey(
    
    name: r'selectionHorizon',
    required: false,
    includeIfNull: false,
  )


  final int? selectionHorizon;



  @JsonKey(
    
    name: r'results',
    required: true,
    includeIfNull: false,
  )


  final List<SeriesForecastModelBacktestResultsInner> results;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SeriesForecastModelBacktest &&
      other.origins == origins &&
      other.spacing == spacing &&
      other.selectionHorizon == selectionHorizon &&
      other.results == results;

    @override
    int get hashCode =>
        origins.hashCode +
        spacing.hashCode +
        selectionHorizon.hashCode +
        results.hashCode;

  factory SeriesForecastModelBacktest.fromJson(Map<String, dynamic> json) => _$SeriesForecastModelBacktestFromJson(json);

  Map<String, dynamic> toJson() => _$SeriesForecastModelBacktestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

