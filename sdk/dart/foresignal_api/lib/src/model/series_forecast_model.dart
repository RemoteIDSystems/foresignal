//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/series_forecast_model_backtest.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'series_forecast_model.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SeriesForecastModel {
  /// Returns a new [SeriesForecastModel] instance.
  SeriesForecastModel({

    required  this.ref,

    required  this.method,

     this.params,

    required  this.backtest,

    required  this.selectedAt,

     this.nextSelectionAt,

    required  this.version,

     this.manifestId,
  });

      /// Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left, center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement, every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the platform-language denominator; complete and paged collection only)
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final SeriesForecastModelMethodEnum method;



  @JsonKey(
    
    name: r'params',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? params;



  @JsonKey(
    
    name: r'backtest',
    required: true,
    includeIfNull: false,
  )


  final SeriesForecastModelBacktest backtest;



  @JsonKey(
    
    name: r'selectedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime selectedAt;



  @JsonKey(
    
    name: r'nextSelectionAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? nextSelectionAt;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final String version;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SeriesForecastModel &&
      other.ref == ref &&
      other.method == method &&
      other.params == params &&
      other.backtest == backtest &&
      other.selectedAt == selectedAt &&
      other.nextSelectionAt == nextSelectionAt &&
      other.version == version &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        ref.hashCode +
        method.hashCode +
        params.hashCode +
        backtest.hashCode +
        selectedAt.hashCode +
        (nextSelectionAt == null ? 0 : nextSelectionAt.hashCode) +
        version.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode);

  factory SeriesForecastModel.fromJson(Map<String, dynamic> json) => _$SeriesForecastModelFromJson(json);

  Map<String, dynamic> toJson() => _$SeriesForecastModelToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SeriesForecastModelMethodEnum {
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

const SeriesForecastModelMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


