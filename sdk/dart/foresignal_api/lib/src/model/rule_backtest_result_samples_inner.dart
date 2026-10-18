//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'rule_backtest_result_samples_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RuleBacktestResultSamplesInner {
  /// Returns a new [RuleBacktestResultSamplesInner] instance.
  RuleBacktestResultSamplesInner({

    required  this.seq,

    required  this.source_,

    required  this.externalId,

    required  this.observedAt,

     this.edge,
  });

  @JsonKey(
    
    name: r'seq',
    required: true,
    includeIfNull: false,
  )


  final int seq;



  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final String source_;



  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



  @JsonKey(
    
    name: r'observedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime observedAt;



  @JsonKey(
    
    name: r'edge',
    required: false,
    includeIfNull: false,
  )


  final RuleBacktestResultSamplesInnerEdgeEnum? edge;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RuleBacktestResultSamplesInner &&
      other.seq == seq &&
      other.source_ == source_ &&
      other.externalId == externalId &&
      other.observedAt == observedAt &&
      other.edge == edge;

    @override
    int get hashCode =>
        seq.hashCode +
        source_.hashCode +
        externalId.hashCode +
        observedAt.hashCode +
        edge.hashCode;

  factory RuleBacktestResultSamplesInner.fromJson(Map<String, dynamic> json) => _$RuleBacktestResultSamplesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$RuleBacktestResultSamplesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum RuleBacktestResultSamplesInnerEdgeEnum {
@JsonValue(r'match')
match(r'match'),
@JsonValue(r'enter')
enter(r'enter'),
@JsonValue(r'exit')
exit(r'exit'),
@JsonValue(r'window')
window(r'window');

const RuleBacktestResultSamplesInnerEdgeEnum(this.value);

final String value;

@override
String toString() => value;
}


