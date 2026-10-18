//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_series_point.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastSeriesPoint {
  /// Returns a new [ForecastSeriesPoint] instance.
  ForecastSeriesPoint({

    required  this.time,

     this.tempC,

     this.windSpeedMs,

     this.windFromDeg,

     this.gustMs,

     this.popPct,

     this.precipMm,

     this.skyCoverPct,

     this.smokeUgM3,
  });

  @JsonKey(
    
    name: r'time',
    required: true,
    includeIfNull: false,
  )


  final DateTime time;



  @JsonKey(
    
    name: r'tempC',
    required: false,
    includeIfNull: false,
  )


  final num? tempC;



  @JsonKey(
    
    name: r'windSpeedMs',
    required: false,
    includeIfNull: false,
  )


  final num? windSpeedMs;



  @JsonKey(
    
    name: r'windFromDeg',
    required: false,
    includeIfNull: false,
  )


  final int? windFromDeg;



  @JsonKey(
    
    name: r'gustMs',
    required: false,
    includeIfNull: false,
  )


  final num? gustMs;



  @JsonKey(
    
    name: r'popPct',
    required: false,
    includeIfNull: false,
  )


  final int? popPct;



  @JsonKey(
    
    name: r'precipMm',
    required: false,
    includeIfNull: false,
  )


  final num? precipMm;



  @JsonKey(
    
    name: r'skyCoverPct',
    required: false,
    includeIfNull: false,
  )


  final int? skyCoverPct;



  @JsonKey(
    
    name: r'smokeUgM3',
    required: false,
    includeIfNull: false,
  )


  final num? smokeUgM3;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastSeriesPoint &&
      other.time == time &&
      other.tempC == tempC &&
      other.windSpeedMs == windSpeedMs &&
      other.windFromDeg == windFromDeg &&
      other.gustMs == gustMs &&
      other.popPct == popPct &&
      other.precipMm == precipMm &&
      other.skyCoverPct == skyCoverPct &&
      other.smokeUgM3 == smokeUgM3;

    @override
    int get hashCode =>
        time.hashCode +
        (tempC == null ? 0 : tempC.hashCode) +
        (windSpeedMs == null ? 0 : windSpeedMs.hashCode) +
        (windFromDeg == null ? 0 : windFromDeg.hashCode) +
        (gustMs == null ? 0 : gustMs.hashCode) +
        (popPct == null ? 0 : popPct.hashCode) +
        (precipMm == null ? 0 : precipMm.hashCode) +
        (skyCoverPct == null ? 0 : skyCoverPct.hashCode) +
        (smokeUgM3 == null ? 0 : smokeUgM3.hashCode);

  factory ForecastSeriesPoint.fromJson(Map<String, dynamic> json) => _$ForecastSeriesPointFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastSeriesPointToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

