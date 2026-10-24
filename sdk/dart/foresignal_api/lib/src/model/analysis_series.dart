//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/watch_pair.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analysis_series.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalysisSeries {
  /// Returns a new [AnalysisSeries] instance.
  AnalysisSeries({

    required  this.ref,

     this.enabled = true,

    required  this.detectors,

    required  this.frequency,

     this.family,

     this.regionCode,

     this.watchPairs,

    required  this.origin,

     this.createdAt,

     this.lastRunAt,

     this.lastError,
  });

      /// Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left, center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement, every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the platform-language denominator; complete and paged collection only)
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'detectors',
    required: true,
    includeIfNull: false,
  )


  final List<AnalysisSeriesDetectorsEnum> detectors;



  @JsonKey(
    
    name: r'frequency',
    required: true,
    includeIfNull: false,
  )


  final AnalysisSeriesFrequencyEnum frequency;



      /// Signal family from the 119 registry (`intel/signal-families.json`, operator-extensible), e.g. `ais-behaviour`, `aviation-military`, `aviation-ops`, `conflict`, `outage`, `internet-measurement`, `gnss-interference`, `nav-warning`, `attention`, `seismic`, `fire`, `weather-severe`, `cyber`, `markets`, `sanctions`, `health`, `space`
  @JsonKey(
    
    name: r'family',
    required: false,
    includeIfNull: false,
  )


  final String? family;



      /// 040 region code: ISO 3166-1 alpha-2 country, `CC-admin1`, or `ZZ` for untagged
  @JsonKey(
    
    name: r'regionCode',
    required: false,
    includeIfNull: false,
  )


  final String? regionCode;



  @JsonKey(
    
    name: r'watchPairs',
    required: false,
    includeIfNull: false,
  )


  final List<WatchPair>? watchPairs;



      /// `auto` rows come from nightly auto-registration; `operator` rows are never removed automatically
  @JsonKey(
    
    name: r'origin',
    required: true,
    includeIfNull: false,
  )


  final AnalysisSeriesOriginEnum origin;



  @JsonKey(
    
    name: r'createdAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? createdAt;



  @JsonKey(
    
    name: r'lastRunAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastRunAt;



  @JsonKey(
    
    name: r'lastError',
    required: false,
    includeIfNull: false,
  )


  final String? lastError;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalysisSeries &&
      other.ref == ref &&
      other.enabled == enabled &&
      other.detectors == detectors &&
      other.frequency == frequency &&
      other.family == family &&
      other.regionCode == regionCode &&
      other.watchPairs == watchPairs &&
      other.origin == origin &&
      other.createdAt == createdAt &&
      other.lastRunAt == lastRunAt &&
      other.lastError == lastError;

    @override
    int get hashCode =>
        ref.hashCode +
        enabled.hashCode +
        detectors.hashCode +
        frequency.hashCode +
        (family == null ? 0 : family.hashCode) +
        (regionCode == null ? 0 : regionCode.hashCode) +
        watchPairs.hashCode +
        origin.hashCode +
        createdAt.hashCode +
        (lastRunAt == null ? 0 : lastRunAt.hashCode) +
        (lastError == null ? 0 : lastError.hashCode);

  factory AnalysisSeries.fromJson(Map<String, dynamic> json) => _$AnalysisSeriesFromJson(json);

  Map<String, dynamic> toJson() => _$AnalysisSeriesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnalysisSeriesDetectorsEnum {
@JsonValue(r'changepoint-cusum')
changepointCusum(r'changepoint-cusum'),
@JsonValue(r'changepoint-bocpd')
changepointBocpd(r'changepoint-bocpd'),
@JsonValue(r'forecast')
forecast(r'forecast');

const AnalysisSeriesDetectorsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnalysisSeriesFrequencyEnum {
@JsonValue(r'hour')
hour(r'hour'),
@JsonValue(r'day')
day(r'day'),
@JsonValue(r'week')
week(r'week');

const AnalysisSeriesFrequencyEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `auto` rows come from nightly auto-registration; `operator` rows are never removed automatically
enum AnalysisSeriesOriginEnum {
@JsonValue(r'auto')
auto(r'auto'),
@JsonValue(r'operator')
operator_(r'operator');

const AnalysisSeriesOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


