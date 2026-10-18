//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/event_selector.dart';
import 'package:foresignal_api/src/model/severity.dart';
import 'package:foresignal_api/src/model/resolver_area.dart';
import 'package:foresignal_api/src/model/situation_status.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_resolver.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastResolver {
  /// Returns a new [ForecastResolver] instance.
  ForecastResolver({

    required  this.type,

     this.selector,

     this.area,

     this.window,

     this.op,

     this.threshold,

     this.ref,

     this.period,

     this.aggregate,

     this.ruleId,

     this.situationId,

     this.status,

     this.minSeverity,

     this.setId,

     this.version,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ForecastResolverTypeEnum type;



      /// event-exists, count-threshold
  @JsonKey(
    
    name: r'selector',
    required: false,
    includeIfNull: false,
  )


  final EventSelector? selector;



      /// event-exists, count-threshold, alert-fired (optional)
  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final ResolverArea? area;



      /// event-exists, count-threshold, series-value, alert-fired
  @JsonKey(
    
    name: r'window',
    required: false,
    includeIfNull: false,
  )


  final TimeRange? window;



      /// count-threshold, series-value (omit op and threshold for a numeric question)
  @JsonKey(
    
    name: r'op',
    required: false,
    includeIfNull: false,
  )


  final ForecastResolverOpEnum? op;



      /// count-threshold, series-value (omit op and threshold for a numeric question: a series-value aggregate, or since 126 a count-threshold count; a referenceFamily selector then counts the events in its reference history, one source per region)
  @JsonKey(
    
    name: r'threshold',
    required: false,
    includeIfNull: false,
  )


  final num? threshold;



      /// series-value; count-threshold over a region rollup
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



      /// series-value: a single period, e.g. `2026-11` or `2026-W45`
  @JsonKey(
    
    name: r'period',
    required: false,
    includeIfNull: false,
  )


  final String? period;



      /// series-value
  @JsonKey(
    
    name: r'aggregate',
    required: false,
    includeIfNull: false,
  )


  final ForecastResolverAggregateEnum? aggregate;



      /// alert-fired: 017 rule id
  @JsonKey(
    
    name: r'ruleId',
    required: false,
    includeIfNull: false,
  )


  final String? ruleId;



      /// situation-status
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;



      /// situation-status: required status at `resolve_by`
  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final SituationStatus? status;



      /// situation-status: minimum severity at `resolve_by`
  @JsonKey(
    
    name: r'minSeverity',
    required: false,
    includeIfNull: false,
  )


  final Severity? minSeverity;



      /// hypothesis-outcome: 125 hypothesis set
  @JsonKey(
    
    name: r'setId',
    required: false,
    includeIfNull: false,
  )


  final String? setId;



      /// Resolver version (server-set)
  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final String? version;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastResolver &&
      other.type == type &&
      other.selector == selector &&
      other.area == area &&
      other.window == window &&
      other.op == op &&
      other.threshold == threshold &&
      other.ref == ref &&
      other.period == period &&
      other.aggregate == aggregate &&
      other.ruleId == ruleId &&
      other.situationId == situationId &&
      other.status == status &&
      other.minSeverity == minSeverity &&
      other.setId == setId &&
      other.version == version;

    @override
    int get hashCode =>
        type.hashCode +
        selector.hashCode +
        area.hashCode +
        window.hashCode +
        op.hashCode +
        threshold.hashCode +
        ref.hashCode +
        period.hashCode +
        aggregate.hashCode +
        ruleId.hashCode +
        situationId.hashCode +
        status.hashCode +
        minSeverity.hashCode +
        setId.hashCode +
        version.hashCode;

  factory ForecastResolver.fromJson(Map<String, dynamic> json) => _$ForecastResolverFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastResolverToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecastResolverTypeEnum {
@JsonValue(r'event-exists')
eventExists(r'event-exists'),
@JsonValue(r'count-threshold')
countThreshold(r'count-threshold'),
@JsonValue(r'series-value')
seriesValue(r'series-value'),
@JsonValue(r'alert-fired')
alertFired(r'alert-fired'),
@JsonValue(r'situation-status')
situationStatus(r'situation-status'),
@JsonValue(r'hypothesis-outcome')
hypothesisOutcome(r'hypothesis-outcome'),
@JsonValue(r'manual')
manual(r'manual');

const ForecastResolverTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// count-threshold, series-value (omit op and threshold for a numeric question)
enum ForecastResolverOpEnum {
@JsonValue(r'gt')
gt(r'gt'),
@JsonValue(r'gte')
gte(r'gte'),
@JsonValue(r'lt')
lt(r'lt'),
@JsonValue(r'lte')
lte(r'lte'),
@JsonValue(r'eq')
eq(r'eq');

const ForecastResolverOpEnum(this.value);

final String value;

@override
String toString() => value;
}


/// series-value
enum ForecastResolverAggregateEnum {
@JsonValue(r'last')
last(r'last'),
@JsonValue(r'mean')
mean(r'mean'),
@JsonValue(r'max')
max(r'max'),
@JsonValue(r'sum')
sum(r'sum');

const ForecastResolverAggregateEnum(this.value);

final String value;

@override
String toString() => value;
}


