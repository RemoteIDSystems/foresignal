//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/forecast_history_series_inner_points_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_history_series_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastHistorySeriesInner {
  /// Returns a new [ForecastHistorySeriesInner] instance.
  ForecastHistorySeriesInner({

    required  this.forecasterId,

    required  this.points,
  });

  @JsonKey(
    
    name: r'forecasterId',
    required: true,
    includeIfNull: false,
  )


  final String forecasterId;



  @JsonKey(
    
    name: r'points',
    required: true,
    includeIfNull: false,
  )


  final List<ForecastHistorySeriesInnerPointsInner> points;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastHistorySeriesInner &&
      other.forecasterId == forecasterId &&
      other.points == points;

    @override
    int get hashCode =>
        forecasterId.hashCode +
        points.hashCode;

  factory ForecastHistorySeriesInner.fromJson(Map<String, dynamic> json) => _$ForecastHistorySeriesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastHistorySeriesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

