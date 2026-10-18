//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'finding_base_rate.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class FindingBaseRate {
  /// Returns a new [FindingBaseRate] instance.
  FindingBaseRate({

    required  this.value,

    required  this.unit,

    required  this.method,

     this.n,

     this.period,

    required  this.text,
  });

  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: true,
  )


  final num? value;



      /// e.g. `alarms-per-year`, `placebo-share`, `expected-joint-days-per-year`
  @JsonKey(
    
    name: r'unit',
    required: true,
    includeIfNull: false,
  )


  final String unit;



  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final FindingBaseRateMethodEnum method;



      /// Sample size behind the rate
  @JsonKey(
    
    name: r'n',
    required: false,
    includeIfNull: false,
  )


  final int? n;



  @JsonKey(
    
    name: r'period',
    required: false,
    includeIfNull: false,
  )


  final TimeRange? period;



      /// Plain-language statement, e.g. `a change this large was flagged once in the previous 700 days`
  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;





    @override
    bool operator ==(Object other) => identical(this, other) || other is FindingBaseRate &&
      other.value == value &&
      other.unit == unit &&
      other.method == method &&
      other.n == n &&
      other.period == period &&
      other.text == text;

    @override
    int get hashCode =>
        (value == null ? 0 : value.hashCode) +
        unit.hashCode +
        method.hashCode +
        (n == null ? 0 : n.hashCode) +
        (period == null ? 0 : period.hashCode) +
        text.hashCode;

  factory FindingBaseRate.fromJson(Map<String, dynamic> json) => _$FindingBaseRateFromJson(json);

  Map<String, dynamic> toJson() => _$FindingBaseRateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum FindingBaseRateMethodEnum {
@JsonValue(r'detector-false-alarm-rate')
detectorFalseAlarmRate(r'detector-false-alarm-rate'),
@JsonValue(r'placebo-distribution')
placeboDistribution(r'placebo-distribution'),
@JsonValue(r'independence-baseline')
independenceBaseline(r'independence-baseline'),
@JsonValue(r'precursor-base-rate')
precursorBaseRate(r'precursor-base-rate'),
@JsonValue(r'other')
other(r'other');

const FindingBaseRateMethodEnum(this.value);

final String value;

@override
String toString() => value;
}


