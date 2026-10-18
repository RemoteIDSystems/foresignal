//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/series_point.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'series_data.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SeriesData {
  /// Returns a new [SeriesData] instance.
  SeriesData({

    required  this.ref,

    required  this.from,

    required  this.to,

     this.asOf,

    required  this.frequency,

     this.unit,

     this.family,

    required  this.valuesWithheld,

    required  this.points,

    required  this.gaps,
  });

      /// Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left, center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement, every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the platform-language denominator; complete and paged collection only)
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final DateTime to;



      /// Knowledge cutoff applied: only points with `knownAt` ≤ asOf
  @JsonKey(
    
    name: r'asOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? asOf;



  @JsonKey(
    
    name: r'frequency',
    required: true,
    includeIfNull: false,
  )


  final SeriesDataFrequencyEnum frequency;



  @JsonKey(
    
    name: r'unit',
    required: false,
    includeIfNull: false,
  )


  final String? unit;



      /// 117 series family or 119 signal family
  @JsonKey(
    
    name: r'family',
    required: false,
    includeIfNull: false,
  )


  final String? family;



      /// True when raw values are withheld (`LOCAL_ONLY` source and no local egress)
  @JsonKey(
    
    name: r'valuesWithheld',
    required: true,
    includeIfNull: false,
  )


  final bool valuesWithheld;



  @JsonKey(
    
    name: r'points',
    required: true,
    includeIfNull: false,
  )


  final List<SeriesPoint> points;



      /// Periods with no point or no coverage
  @JsonKey(
    
    name: r'gaps',
    required: true,
    includeIfNull: false,
  )


  final List<TimeRange> gaps;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SeriesData &&
      other.ref == ref &&
      other.from == from &&
      other.to == to &&
      other.asOf == asOf &&
      other.frequency == frequency &&
      other.unit == unit &&
      other.family == family &&
      other.valuesWithheld == valuesWithheld &&
      other.points == points &&
      other.gaps == gaps;

    @override
    int get hashCode =>
        ref.hashCode +
        from.hashCode +
        to.hashCode +
        (asOf == null ? 0 : asOf.hashCode) +
        frequency.hashCode +
        (unit == null ? 0 : unit.hashCode) +
        (family == null ? 0 : family.hashCode) +
        valuesWithheld.hashCode +
        points.hashCode +
        gaps.hashCode;

  factory SeriesData.fromJson(Map<String, dynamic> json) => _$SeriesDataFromJson(json);

  Map<String, dynamic> toJson() => _$SeriesDataToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SeriesDataFrequencyEnum {
@JsonValue(r'hour')
hour(r'hour'),
@JsonValue(r'day')
day(r'day'),
@JsonValue(r'week')
week(r'week'),
@JsonValue(r'month')
month(r'month'),
@JsonValue(r'irregular')
irregular(r'irregular');

const SeriesDataFrequencyEnum(this.value);

final String value;

@override
String toString() => value;
}


