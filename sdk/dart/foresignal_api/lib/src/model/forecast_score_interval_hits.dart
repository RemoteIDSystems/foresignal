//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_score_interval_hits.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastScoreIntervalHits {
  /// Returns a new [ForecastScoreIntervalHits] instance.
  ForecastScoreIntervalHits({

     this.p05p95,

     this.p25p75,
  });

  @JsonKey(
    
    name: r'p05p95',
    required: false,
    includeIfNull: false,
  )


  final bool? p05p95;



  @JsonKey(
    
    name: r'p25p75',
    required: false,
    includeIfNull: false,
  )


  final bool? p25p75;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastScoreIntervalHits &&
      other.p05p95 == p05p95 &&
      other.p25p75 == p25p75;

    @override
    int get hashCode =>
        p05p95.hashCode +
        p25p75.hashCode;

  factory ForecastScoreIntervalHits.fromJson(Map<String, dynamic> json) => _$ForecastScoreIntervalHitsFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastScoreIntervalHitsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

