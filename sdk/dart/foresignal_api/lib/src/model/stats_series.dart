//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/stats_series_series_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'stats_series.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class StatsSeries {
  /// Returns a new [StatsSeries] instance.
  StatsSeries({

    required  this.from,

    required  this.to,

    required  this.bucket,

    required  this.measure,

     this.groupBy,

     this.timeField,

    required  this.backend,

    required  this.series,

     this.retainedFrom,
  });

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



  @JsonKey(
    
    name: r'bucket',
    required: true,
    includeIfNull: false,
  )


  final String bucket;



  @JsonKey(
    
    name: r'measure',
    required: true,
    includeIfNull: false,
  )


  final StatsSeriesMeasureEnum measure;



  @JsonKey(
    
    name: r'groupBy',
    required: false,
    includeIfNull: false,
  )


  final StatsSeriesGroupByEnum? groupBy;



  @JsonKey(
    
    name: r'timeField',
    required: false,
    includeIfNull: false,
  )


  final StatsSeriesTimeFieldEnum? timeField;



  @JsonKey(
    
    name: r'backend',
    required: true,
    includeIfNull: false,
  )


  final StatsSeriesBackendEnum backend;



  @JsonKey(
    
    name: r'series',
    required: true,
    includeIfNull: false,
  )


  final List<StatsSeriesSeriesInner> series;



  @JsonKey(
    
    name: r'retainedFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? retainedFrom;





    @override
    bool operator ==(Object other) => identical(this, other) || other is StatsSeries &&
      other.from == from &&
      other.to == to &&
      other.bucket == bucket &&
      other.measure == measure &&
      other.groupBy == groupBy &&
      other.timeField == timeField &&
      other.backend == backend &&
      other.series == series &&
      other.retainedFrom == retainedFrom;

    @override
    int get hashCode =>
        from.hashCode +
        to.hashCode +
        bucket.hashCode +
        measure.hashCode +
        groupBy.hashCode +
        timeField.hashCode +
        backend.hashCode +
        series.hashCode +
        (retainedFrom == null ? 0 : retainedFrom.hashCode);

  factory StatsSeries.fromJson(Map<String, dynamic> json) => _$StatsSeriesFromJson(json);

  Map<String, dynamic> toJson() => _$StatsSeriesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum StatsSeriesMeasureEnum {
@JsonValue(r'versions')
versions(r'versions'),
@JsonValue(r'entities')
entities(r'entities'),
@JsonValue(r'firsts')
firsts(r'firsts'),
@JsonValue(r'tombstones')
tombstones(r'tombstones'),
@JsonValue(r'edges')
edges(r'edges'),
@JsonValue(r'alerts')
alerts(r'alerts');

const StatsSeriesMeasureEnum(this.value);

final String value;

@override
String toString() => value;
}


enum StatsSeriesGroupByEnum {
@JsonValue(r'none')
none(r'none'),
@JsonValue(r'source')
source_(r'source'),
@JsonValue(r'kind')
kind(r'kind');

const StatsSeriesGroupByEnum(this.value);

final String value;

@override
String toString() => value;
}


enum StatsSeriesTimeFieldEnum {
@JsonValue(r'ingested')
ingested(r'ingested'),
@JsonValue(r'effective')
effective(r'effective'),
@JsonValue(r'observed')
observed(r'observed'),
@JsonValue(r'event')
event(r'event');

const StatsSeriesTimeFieldEnum(this.value);

final String value;

@override
String toString() => value;
}


enum StatsSeriesBackendEnum {
@JsonValue(r'rollup')
rollup(r'rollup'),
@JsonValue(r'raw')
raw(r'raw'),
@JsonValue(r'mixed')
mixed(r'mixed');

const StatsSeriesBackendEnum(this.value);

final String value;

@override
String toString() => value;
}


