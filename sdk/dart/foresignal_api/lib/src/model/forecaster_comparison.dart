//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/stat_interval.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecaster_comparison.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecasterComparison {
  /// Returns a new [ForecasterComparison] instance.
  ForecasterComparison({

    required  this.a,

    required  this.b,

     this.category,

    required  this.nShared,

    required  this.label,

    required  this.metric,

     this.meanScoreA,

     this.meanScoreB,

     this.meanDifference,

     this.interval,

    required  this.scorerVersion,

    required  this.computedAt,
  });

  @JsonKey(
    
    name: r'a',
    required: true,
    includeIfNull: false,
  )


  final String a;



  @JsonKey(
    
    name: r'b',
    required: true,
    includeIfNull: false,
  )


  final String b;



  @JsonKey(
    
    name: r'category',
    required: false,
    includeIfNull: false,
  )


  final String? category;



  @JsonKey(
    
    name: r'nShared',
    required: true,
    includeIfNull: false,
  )


  final int nShared;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final ForecasterComparisonLabelEnum label;



  @JsonKey(
    
    name: r'metric',
    required: true,
    includeIfNull: false,
  )


  final ForecasterComparisonMetricEnum metric;



  @JsonKey(
    
    name: r'meanScoreA',
    required: false,
    includeIfNull: false,
  )


  final num? meanScoreA;



  @JsonKey(
    
    name: r'meanScoreB',
    required: false,
    includeIfNull: false,
  )


  final num? meanScoreB;



      /// mean(A) − mean(B); negative favours A
  @JsonKey(
    
    name: r'meanDifference',
    required: false,
    includeIfNull: false,
  )


  final num? meanDifference;



      /// Paired bootstrap 95% interval (1,000 resamples, deterministic seed)
  @JsonKey(
    
    name: r'interval',
    required: false,
    includeIfNull: false,
  )


  final StatInterval? interval;



  @JsonKey(
    
    name: r'scorerVersion',
    required: true,
    includeIfNull: false,
  )


  final String scorerVersion;



  @JsonKey(
    
    name: r'computedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime computedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecasterComparison &&
      other.a == a &&
      other.b == b &&
      other.category == category &&
      other.nShared == nShared &&
      other.label == label &&
      other.metric == metric &&
      other.meanScoreA == meanScoreA &&
      other.meanScoreB == meanScoreB &&
      other.meanDifference == meanDifference &&
      other.interval == interval &&
      other.scorerVersion == scorerVersion &&
      other.computedAt == computedAt;

    @override
    int get hashCode =>
        a.hashCode +
        b.hashCode +
        (category == null ? 0 : category.hashCode) +
        nShared.hashCode +
        label.hashCode +
        metric.hashCode +
        (meanScoreA == null ? 0 : meanScoreA.hashCode) +
        (meanScoreB == null ? 0 : meanScoreB.hashCode) +
        (meanDifference == null ? 0 : meanDifference.hashCode) +
        (interval == null ? 0 : interval.hashCode) +
        scorerVersion.hashCode +
        computedAt.hashCode;

  factory ForecasterComparison.fromJson(Map<String, dynamic> json) => _$ForecasterComparisonFromJson(json);

  Map<String, dynamic> toJson() => _$ForecasterComparisonToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ForecasterComparisonLabelEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'insufficient')
insufficient(r'insufficient');

const ForecasterComparisonLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ForecasterComparisonMetricEnum {
@JsonValue(r'brier')
brier(r'brier');

const ForecasterComparisonMetricEnum(this.value);

final String value;

@override
String toString() => value;
}


