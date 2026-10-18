//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'backtest_caveat.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BacktestCaveat {
  /// Returns a new [BacktestCaveat] instance.
  BacktestCaveat({

    required  this.code,

    required  this.text,

     this.data,
  });

  @JsonKey(
    
    name: r'code',
    required: true,
    includeIfNull: false,
  )


  final BacktestCaveatCodeEnum code;



  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;



  @JsonKey(
    
    name: r'data',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? data;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BacktestCaveat &&
      other.code == code &&
      other.text == text &&
      other.data == data;

    @override
    int get hashCode =>
        code.hashCode +
        text.hashCode +
        data.hashCode;

  factory BacktestCaveat.fromJson(Map<String, dynamic> json) => _$BacktestCaveatFromJson(json);

  Map<String, dynamic> toJson() => _$BacktestCaveatToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BacktestCaveatCodeEnum {
@JsonValue(r'RANGE_CLAMPED_BY_RETENTION')
RANGE_CLAMPED_BY_RETENTION(r'RANGE_CLAMPED_BY_RETENTION'),
@JsonValue(r'DOWNSAMPLED_RANGE')
DOWNSAMPLED_RANGE(r'DOWNSAMPLED_RANGE'),
@JsonValue(r'SAMPLED_SOURCE')
SAMPLED_SOURCE(r'SAMPLED_SOURCE'),
@JsonValue(r'COVERAGE_GAPS')
COVERAGE_GAPS(r'COVERAGE_GAPS'),
@JsonValue(r'DEFINITION_DIFFERS')
DEFINITION_DIFFERS(r'DEFINITION_DIFFERS'),
@JsonValue(r'ENTITY_RESOLUTION_CHANGED')
ENTITY_RESOLUTION_CHANGED(r'ENTITY_RESOLUTION_CHANGED'),
@JsonValue(r'WINDOW_APPROXIMATE')
WINDOW_APPROXIMATE(r'WINDOW_APPROXIMATE'),
@JsonValue(r'SMALL_SAMPLE')
SMALL_SAMPLE(r'SMALL_SAMPLE'),
@JsonValue(r'NOT_GROUND_TRUTH')
NOT_GROUND_TRUTH(r'NOT_GROUND_TRUTH');

const BacktestCaveatCodeEnum(this.value);

final String value;

@override
String toString() => value;
}


