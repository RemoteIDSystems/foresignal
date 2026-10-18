//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'benchmark_component_result_thresholds_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BenchmarkComponentResultThresholdsInner {
  /// Returns a new [BenchmarkComponentResultThresholdsInner] instance.
  BenchmarkComponentResultThresholdsInner({

    required  this.metric,

    required  this.comparator,

    required  this.threshold,

     this.observed,

    required  this.status,

     this.warn,

     this.scenario,

     this.note,
  });

  @JsonKey(
    
    name: r'metric',
    required: true,
    includeIfNull: false,
  )


  final String metric;



  @JsonKey(
    
    name: r'comparator',
    required: true,
    includeIfNull: false,
  )


  final BenchmarkComponentResultThresholdsInnerComparatorEnum comparator;



  @JsonKey(
    
    name: r'threshold',
    required: true,
    includeIfNull: false,
  )


  final num threshold;



  @JsonKey(
    
    name: r'observed',
    required: false,
    includeIfNull: false,
  )


  final num? observed;



      /// Result against the documented thresholds
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final BenchmarkComponentResultThresholdsInnerStatusEnum status;



      /// The warn threshold; outside it the check fails (null: outside the pass threshold fails)
  @JsonKey(
    
    name: r'warn',
    required: false,
    includeIfNull: false,
  )


  final num? warn;



      /// The scenario (or `historical`) the check applies to
  @JsonKey(
    
    name: r'scenario',
    required: false,
    includeIfNull: false,
  )


  final String? scenario;



      /// What the threshold means
  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BenchmarkComponentResultThresholdsInner &&
      other.metric == metric &&
      other.comparator == comparator &&
      other.threshold == threshold &&
      other.observed == observed &&
      other.status == status &&
      other.warn == warn &&
      other.scenario == scenario &&
      other.note == note;

    @override
    int get hashCode =>
        metric.hashCode +
        comparator.hashCode +
        threshold.hashCode +
        (observed == null ? 0 : observed.hashCode) +
        status.hashCode +
        (warn == null ? 0 : warn.hashCode) +
        scenario.hashCode +
        note.hashCode;

  factory BenchmarkComponentResultThresholdsInner.fromJson(Map<String, dynamic> json) => _$BenchmarkComponentResultThresholdsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$BenchmarkComponentResultThresholdsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BenchmarkComponentResultThresholdsInnerComparatorEnum {
@JsonValue(r'le')
le(r'le'),
@JsonValue(r'ge')
ge(r'ge'),
@JsonValue(r'eq')
eq(r'eq'),
@JsonValue(r'within')
within(r'within');

const BenchmarkComponentResultThresholdsInnerComparatorEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Result against the documented thresholds
enum BenchmarkComponentResultThresholdsInnerStatusEnum {
@JsonValue(r'pass')
pass(r'pass'),
@JsonValue(r'warn')
warn(r'warn'),
@JsonValue(r'fail')
fail(r'fail');

const BenchmarkComponentResultThresholdsInnerStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


