//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/quantiles.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_history_series_inner_points_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastHistorySeriesInnerPointsInner {
  /// Returns a new [ForecastHistorySeriesInnerPointsInner] instance.
  ForecastHistorySeriesInnerPointsInner({

    required  this.date,

     this.probability,

     this.distribution,

     this.quantiles,

    required  this.forecastId,
  });

  @JsonKey(
    
    name: r'date',
    required: true,
    includeIfNull: false,
  )


  final DateTime date;



      /// Binary questions
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: false,
    includeIfNull: false,
  )


  final num? probability;



      /// Multiple/ordered: outcome label → probability, summing to 1 ± 1e-6
  @JsonKey(
    
    name: r'distribution',
    required: false,
    includeIfNull: false,
  )


  final Map<String, num>? distribution;



      /// Numeric: p05, p10, p25, p50, p75, p90, p95, monotone
  @JsonKey(
    
    name: r'quantiles',
    required: false,
    includeIfNull: false,
  )


  final Quantiles? quantiles;



      /// ULID identifier
  @JsonKey(
    
    name: r'forecastId',
    required: true,
    includeIfNull: false,
  )


  final String forecastId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastHistorySeriesInnerPointsInner &&
      other.date == date &&
      other.probability == probability &&
      other.distribution == distribution &&
      other.quantiles == quantiles &&
      other.forecastId == forecastId;

    @override
    int get hashCode =>
        date.hashCode +
        (probability == null ? 0 : probability.hashCode) +
        (distribution == null ? 0 : distribution.hashCode) +
        (quantiles == null ? 0 : quantiles.hashCode) +
        forecastId.hashCode;

  factory ForecastHistorySeriesInnerPointsInner.fromJson(Map<String, dynamic> json) => _$ForecastHistorySeriesInnerPointsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastHistorySeriesInnerPointsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

