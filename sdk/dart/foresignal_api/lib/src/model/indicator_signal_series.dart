//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/indicator_signal_series_condition.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'indicator_signal_series.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IndicatorSignalSeries {
  /// Returns a new [IndicatorSignalSeries] instance.
  IndicatorSignalSeries({

    required  this.ref,

     this.condition,

     this.changePoint = false,

     this.direction,
  });

      /// Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left, center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement, every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the platform-language denominator; complete and paged collection only)
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'condition',
    required: false,
    includeIfNull: false,
  )


  final IndicatorSignalSeriesCondition? condition;



  @JsonKey(
    defaultValue: false,
    name: r'changePoint',
    required: false,
    includeIfNull: false,
  )


  final bool? changePoint;



  @JsonKey(
    
    name: r'direction',
    required: false,
    includeIfNull: false,
  )


  final IndicatorSignalSeriesDirectionEnum? direction;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IndicatorSignalSeries &&
      other.ref == ref &&
      other.condition == condition &&
      other.changePoint == changePoint &&
      other.direction == direction;

    @override
    int get hashCode =>
        ref.hashCode +
        (condition == null ? 0 : condition.hashCode) +
        changePoint.hashCode +
        (direction == null ? 0 : direction.hashCode);

  factory IndicatorSignalSeries.fromJson(Map<String, dynamic> json) => _$IndicatorSignalSeriesFromJson(json);

  Map<String, dynamic> toJson() => _$IndicatorSignalSeriesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IndicatorSignalSeriesDirectionEnum {
@JsonValue(r'up')
up(r'up'),
@JsonValue(r'down')
down(r'down'),
@JsonValue(r'any')
any(r'any');

const IndicatorSignalSeriesDirectionEnum(this.value);

final String value;

@override
String toString() => value;
}


