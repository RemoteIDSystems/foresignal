//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/quantiles.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'series_forecast_forecasts_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SeriesForecastForecastsInner {
  /// Returns a new [SeriesForecastForecastsInner] instance.
  SeriesForecastForecastsInner({

    required  this.horizon,

    required  this.targetPeriod,

    required  this.quantiles,
  });

      /// Days ahead
  @JsonKey(
    
    name: r'horizon',
    required: true,
    includeIfNull: false,
  )


  final SeriesForecastForecastsInnerHorizonEnum horizon;



  @JsonKey(
    
    name: r'targetPeriod',
    required: true,
    includeIfNull: false,
  )


  final TimeRange targetPeriod;



  @JsonKey(
    
    name: r'quantiles',
    required: true,
    includeIfNull: false,
  )


  final Quantiles quantiles;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SeriesForecastForecastsInner &&
      other.horizon == horizon &&
      other.targetPeriod == targetPeriod &&
      other.quantiles == quantiles;

    @override
    int get hashCode =>
        horizon.hashCode +
        targetPeriod.hashCode +
        quantiles.hashCode;

  factory SeriesForecastForecastsInner.fromJson(Map<String, dynamic> json) => _$SeriesForecastForecastsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SeriesForecastForecastsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Days ahead
enum SeriesForecastForecastsInnerHorizonEnum {
@JsonValue(1)
number1('1'),
@JsonValue(7)
number7('7'),
@JsonValue(28)
number28('28');

const SeriesForecastForecastsInnerHorizonEnum(this.value);

final String value;

@override
String toString() => value;
}


