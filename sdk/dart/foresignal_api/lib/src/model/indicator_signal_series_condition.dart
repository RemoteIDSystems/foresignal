//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'indicator_signal_series_condition.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IndicatorSignalSeriesCondition {
  /// Returns a new [IndicatorSignalSeriesCondition] instance.
  IndicatorSignalSeriesCondition({

    required  this.op,

    required  this.value,
  });

  @JsonKey(
    
    name: r'op',
    required: true,
    includeIfNull: false,
  )


  final IndicatorSignalSeriesConditionOpEnum op;



  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: false,
  )


  final num value;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IndicatorSignalSeriesCondition &&
      other.op == op &&
      other.value == value;

    @override
    int get hashCode =>
        op.hashCode +
        value.hashCode;

  factory IndicatorSignalSeriesCondition.fromJson(Map<String, dynamic> json) => _$IndicatorSignalSeriesConditionFromJson(json);

  Map<String, dynamic> toJson() => _$IndicatorSignalSeriesConditionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IndicatorSignalSeriesConditionOpEnum {
@JsonValue(r'gt')
gt(r'gt'),
@JsonValue(r'gte')
gte(r'gte'),
@JsonValue(r'lt')
lt(r'lt'),
@JsonValue(r'lte')
lte(r'lte');

const IndicatorSignalSeriesConditionOpEnum(this.value);

final String value;

@override
String toString() => value;
}


