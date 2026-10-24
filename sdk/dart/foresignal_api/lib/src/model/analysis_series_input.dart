//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/watch_pair.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analysis_series_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalysisSeriesInput {
  /// Returns a new [AnalysisSeriesInput] instance.
  AnalysisSeriesInput({

    required  this.ref,

     this.enabled = true,

     this.detectors,

     this.frequency,

     this.family,

     this.regionCode,

     this.watchPairs,
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
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



  @JsonKey(
    
    name: r'detectors',
    required: false,
    includeIfNull: false,
  )


  final List<AnalysisSeriesInputDetectorsEnum>? detectors;



  @JsonKey(
    
    name: r'frequency',
    required: false,
    includeIfNull: false,
  )


  final AnalysisSeriesInputFrequencyEnum? frequency;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalysisSeriesInput &&
      other.ref == ref &&
      other.enabled == enabled &&
      other.detectors == detectors &&
      other.frequency == frequency &&
      other.family == family &&
      other.regionCode == regionCode &&
      other.watchPairs == watchPairs;

    @override
    int get hashCode =>
        ref.hashCode +
        enabled.hashCode +
        detectors.hashCode +
        frequency.hashCode +
        (family == null ? 0 : family.hashCode) +
        (regionCode == null ? 0 : regionCode.hashCode) +
        watchPairs.hashCode;

  factory AnalysisSeriesInput.fromJson(Map<String, dynamic> json) => _$AnalysisSeriesInputFromJson(json);

  Map<String, dynamic> toJson() => _$AnalysisSeriesInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnalysisSeriesInputDetectorsEnum {
@JsonValue(r'changepoint-cusum')
changepointCusum(r'changepoint-cusum'),
@JsonValue(r'changepoint-bocpd')
changepointBocpd(r'changepoint-bocpd'),
@JsonValue(r'forecast')
forecast(r'forecast');

const AnalysisSeriesInputDetectorsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnalysisSeriesInputFrequencyEnum {
@JsonValue(r'hour')
hour(r'hour'),
@JsonValue(r'day')
day(r'day'),
@JsonValue(r'week')
week(r'week');

const AnalysisSeriesInputFrequencyEnum(this.value);

final String value;

@override
String toString() => value;
}


