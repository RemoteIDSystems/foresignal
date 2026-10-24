//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'series_info.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SeriesInfo {
  /// Returns a new [SeriesInfo] instance.
  SeriesInfo({

    required  this.sourceId,

    required  this.series,

    required  this.seriesName,

     this.dataset,

    required  this.unit,

    required  this.frequency,

     this.area,

     this.latestPeriod,

     this.latestValue,

     this.status,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'series',
    required: true,
    includeIfNull: false,
  )


  final String series;



  @JsonKey(
    
    name: r'seriesName',
    required: true,
    includeIfNull: false,
  )


  final String seriesName;



  @JsonKey(
    
    name: r'dataset',
    required: false,
    includeIfNull: false,
  )


  final String? dataset;



      /// UCUM unit
  @JsonKey(
    
    name: r'unit',
    required: true,
    includeIfNull: false,
  )


  final String unit;



  @JsonKey(
    
    name: r'frequency',
    required: true,
    includeIfNull: false,
  )


  final SeriesInfoFrequencyEnum frequency;



  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final String? area;



  @JsonKey(
    
    name: r'latestPeriod',
    required: false,
    includeIfNull: false,
  )


  final String? latestPeriod;



  @JsonKey(
    
    name: r'latestValue',
    required: false,
    includeIfNull: false,
  )


  final num? latestValue;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final SeriesInfoStatusEnum? status;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SeriesInfo &&
      other.sourceId == sourceId &&
      other.series == series &&
      other.seriesName == seriesName &&
      other.dataset == dataset &&
      other.unit == unit &&
      other.frequency == frequency &&
      other.area == area &&
      other.latestPeriod == latestPeriod &&
      other.latestValue == latestValue &&
      other.status == status;

    @override
    int get hashCode =>
        sourceId.hashCode +
        series.hashCode +
        seriesName.hashCode +
        (dataset == null ? 0 : dataset.hashCode) +
        unit.hashCode +
        frequency.hashCode +
        (area == null ? 0 : area.hashCode) +
        (latestPeriod == null ? 0 : latestPeriod.hashCode) +
        (latestValue == null ? 0 : latestValue.hashCode) +
        (status == null ? 0 : status.hashCode);

  factory SeriesInfo.fromJson(Map<String, dynamic> json) => _$SeriesInfoFromJson(json);

  Map<String, dynamic> toJson() => _$SeriesInfoToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SeriesInfoFrequencyEnum {
@JsonValue(r'PT15S')
PT15S(r'PT15S'),
@JsonValue(r'PT15M')
PT15M(r'PT15M'),
@JsonValue(r'PT30M')
PT30M(r'PT30M'),
@JsonValue(r'PT1H')
PT1H(r'PT1H'),
@JsonValue(r'P1D')
P1D(r'P1D'),
@JsonValue(r'P1W')
P1W(r'P1W'),
@JsonValue(r'P1M')
P1M(r'P1M'),
@JsonValue(r'P3M')
P3M(r'P3M'),
@JsonValue(r'P1Y')
P1Y(r'P1Y');

const SeriesInfoFrequencyEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SeriesInfoStatusEnum {
@JsonValue(r'final')
final_(r'final'),
@JsonValue(r'preliminary')
preliminary(r'preliminary'),
@JsonValue(r'estimated')
estimated(r'estimated'),
@JsonValue(r'forecast')
forecast(r'forecast');

const SeriesInfoStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


