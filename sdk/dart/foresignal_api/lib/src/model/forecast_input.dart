//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/quantiles.dart';
import 'package:foresignal_api/src/model/forecast_citations_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastInput {
  /// Returns a new [ForecastInput] instance.
  ForecastInput({

     this.probability,

     this.distribution,

     this.quantiles,

     this.rationale,

     this.citations,

     this.forecasterId,
  });

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



  @JsonKey(
    
    name: r'rationale',
    required: false,
    includeIfNull: false,
  )


  final String? rationale;



  @JsonKey(
    
    name: r'citations',
    required: false,
    includeIfNull: false,
  )


  final List<ForecastCitationsInner>? citations;



      /// Agent scope only: another registered `operator` or `external` forecaster
  @JsonKey(
    
    name: r'forecasterId',
    required: false,
    includeIfNull: false,
  )


  final String? forecasterId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastInput &&
      other.probability == probability &&
      other.distribution == distribution &&
      other.quantiles == quantiles &&
      other.rationale == rationale &&
      other.citations == citations &&
      other.forecasterId == forecasterId;

    @override
    int get hashCode =>
        (probability == null ? 0 : probability.hashCode) +
        (distribution == null ? 0 : distribution.hashCode) +
        (quantiles == null ? 0 : quantiles.hashCode) +
        (rationale == null ? 0 : rationale.hashCode) +
        citations.hashCode +
        (forecasterId == null ? 0 : forecasterId.hashCode);

  factory ForecastInput.fromJson(Map<String, dynamic> json) => _$ForecastInputFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

