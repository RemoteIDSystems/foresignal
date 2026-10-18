//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/series_forecast_forecasts_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'series_forecast.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SeriesForecast {
  /// Returns a new [SeriesForecast] instance.
  SeriesForecast({

    required  this.ref,

    required  this.method,

    required  this.version,

    required  this.madeAt,

    required  this.asOfSeq,

     this.manifestId,

    required  this.units,

     this.unit,

    required  this.forecasts,

     this.questionIds,

     this.caveats,
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


  final SeriesForecastMethodEnum method;



  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final String version;



  @JsonKey(
    
    name: r'madeAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime madeAt;



  @JsonKey(
    
    name: r'asOfSeq',
    required: true,
    includeIfNull: false,
  )


  final int asOfSeq;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



      /// `standardised` for LOCAL_ONLY series when the caller may not see local values
  @JsonKey(
    
    name: r'units',
    required: true,
    includeIfNull: false,
  )


  final SeriesForecastUnitsEnum units;



  @JsonKey(
    
    name: r'unit',
    required: false,
    includeIfNull: false,
  )


  final String? unit;



  @JsonKey(
    
    name: r'forecasts',
    required: true,
    includeIfNull: false,
  )


  final List<SeriesForecastForecastsInner> forecasts;



      /// Ledger questions opened for this series
  @JsonKey(
    
    name: r'questionIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? questionIds;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SeriesForecast &&
      other.ref == ref &&
      other.method == method &&
      other.version == version &&
      other.madeAt == madeAt &&
      other.asOfSeq == asOfSeq &&
      other.manifestId == manifestId &&
      other.units == units &&
      other.unit == unit &&
      other.forecasts == forecasts &&
      other.questionIds == questionIds &&
      other.caveats == caveats;

    @override
    int get hashCode =>
        ref.hashCode +
        method.hashCode +
        version.hashCode +
        madeAt.hashCode +
        asOfSeq.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode) +
        units.hashCode +
        (unit == null ? 0 : unit.hashCode) +
        forecasts.hashCode +
        questionIds.hashCode +
        caveats.hashCode;

  factory SeriesForecast.fromJson(Map<String, dynamic> json) => _$SeriesForecastFromJson(json);

  Map<String, dynamic> toJson() => _$SeriesForecastToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SeriesForecastMethodEnum {
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

const SeriesForecastMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `standardised` for LOCAL_ONLY series when the caller may not see local values
enum SeriesForecastUnitsEnum {
@JsonValue(r'native')
native_(r'native'),
@JsonValue(r'standardised')
standardised(r'standardised');

const SeriesForecastUnitsEnum(this.value);

final String value;

@override
String toString() => value;
}


