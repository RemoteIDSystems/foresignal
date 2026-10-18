//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_set_input_ledger_question_resolver.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisSetInputLedgerQuestionResolver {
  /// Returns a new [HypothesisSetInputLedgerQuestionResolver] instance.
  HypothesisSetInputLedgerQuestionResolver({

    required  this.type,

     this.params,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final HypothesisSetInputLedgerQuestionResolverTypeEnum type;



  @JsonKey(
    
    name: r'params',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? params;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisSetInputLedgerQuestionResolver &&
      other.type == type &&
      other.params == params;

    @override
    int get hashCode =>
        type.hashCode +
        params.hashCode;

  factory HypothesisSetInputLedgerQuestionResolver.fromJson(Map<String, dynamic> json) => _$HypothesisSetInputLedgerQuestionResolverFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisSetInputLedgerQuestionResolverToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum HypothesisSetInputLedgerQuestionResolverTypeEnum {
@JsonValue(r'hypothesis-outcome')
hypothesisOutcome(r'hypothesis-outcome'),
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
@JsonValue(r'manual')
manual(r'manual');

const HypothesisSetInputLedgerQuestionResolverTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


