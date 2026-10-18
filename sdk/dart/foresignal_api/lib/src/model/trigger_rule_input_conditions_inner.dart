//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/condition.dart';
import 'package:foresignal_api/src/model/approach_condition.dart';
import 'package:foresignal_api/src/model/approach_condition_target.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'trigger_rule_input_conditions_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TriggerRuleInputConditionsInner {
  /// Returns a new [TriggerRuleInputConditionsInner] instance.
  TriggerRuleInputConditionsInner({

     this.path,

    required  this.op,

    required  this.value,

    required  this.type,

    required  this.target,

    required  this.withinMeters,

    required  this.withinMinutes,
  });

      /// Dot path into payload. Required for every operator except inWatchlist and notInWatchlist, which without a path read each source's declared identity field (descriptor entityKeys, 036); a source that does not declare the watchlist's identity never matches
  @JsonKey(
    
    name: r'path',
    required: false,
    includeIfNull: false,
  )


  final String? path;



      /// Comparison ops take numbers (== and != also strings and booleans); in/notIn a non-empty homogeneous scalar array (≤ 1000); exists a boolean; startsWith a string ≤ 256; matches a Java regex ≤ 256 without backreferences, lookaround or nested quantifiers (only (?:…) groups and the escapes \\d \\D \\w \\W \\s \\S \\n \\t \\r \\f, so PostgreSQL reads it the same way). A value of another JSON type than the operator expects, a missing path or JSON null never matches (except exists); string operators need a JSON string (the number 5 does not equal \"5\"); != and notIn need the value present. inWatchlist/notInWatchlist (036) take a watchlist id; the payload value must be a JSON string or number (compared by its JSON text after the list's normalization); anything else, a missing path or JSON null never matches either operator.
  @JsonKey(
    
    name: r'op',
    required: true,
    includeIfNull: false,
  )


  final TriggerRuleInputConditionsInnerOpEnum op;



  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: true,
  )


  final Object? value;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final TriggerRuleInputConditionsInnerTypeEnum type;



  @JsonKey(
    
    name: r'target',
    required: true,
    includeIfNull: false,
  )


  final ApproachConditionTarget target;



          // minimum: 0
  @JsonKey(
    
    name: r'withinMeters',
    required: true,
    includeIfNull: false,
  )


  final num withinMeters;



          // minimum: 0
  @JsonKey(
    
    name: r'withinMinutes',
    required: true,
    includeIfNull: false,
  )


  final num withinMinutes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TriggerRuleInputConditionsInner &&
      other.path == path &&
      other.op == op &&
      other.value == value &&
      other.type == type &&
      other.target == target &&
      other.withinMeters == withinMeters &&
      other.withinMinutes == withinMinutes;

    @override
    int get hashCode =>
        path.hashCode +
        op.hashCode +
        (value == null ? 0 : value.hashCode) +
        type.hashCode +
        target.hashCode +
        withinMeters.hashCode +
        withinMinutes.hashCode;

  factory TriggerRuleInputConditionsInner.fromJson(Map<String, dynamic> json) => _$TriggerRuleInputConditionsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$TriggerRuleInputConditionsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Comparison ops take numbers (== and != also strings and booleans); in/notIn a non-empty homogeneous scalar array (≤ 1000); exists a boolean; startsWith a string ≤ 256; matches a Java regex ≤ 256 without backreferences, lookaround or nested quantifiers (only (?:…) groups and the escapes \\d \\D \\w \\W \\s \\S \\n \\t \\r \\f, so PostgreSQL reads it the same way). A value of another JSON type than the operator expects, a missing path or JSON null never matches (except exists); string operators need a JSON string (the number 5 does not equal \"5\"); != and notIn need the value present. inWatchlist/notInWatchlist (036) take a watchlist id; the payload value must be a JSON string or number (compared by its JSON text after the list's normalization); anything else, a missing path or JSON null never matches either operator.
enum TriggerRuleInputConditionsInnerOpEnum {
@JsonValue(r'>')
greaterThan(r'>'),
@JsonValue(r'>=')
greaterThanEqual(r'>='),
@JsonValue(r'<')
lessThan(r'<'),
@JsonValue(r'<=')
lessThanEqual(r'<='),
@JsonValue(r'==')
equalEqual(r'=='),
@JsonValue(r'!=')
exclamationEqual(r'!='),
@JsonValue(r'in')
in_(r'in'),
@JsonValue(r'notIn')
notIn(r'notIn'),
@JsonValue(r'exists')
exists(r'exists'),
@JsonValue(r'startsWith')
startsWith(r'startsWith'),
@JsonValue(r'matches')
matches(r'matches'),
@JsonValue(r'inWatchlist')
inWatchlist(r'inWatchlist'),
@JsonValue(r'notInWatchlist')
notInWatchlist(r'notInWatchlist');

const TriggerRuleInputConditionsInnerOpEnum(this.value);

final String value;

@override
String toString() => value;
}


enum TriggerRuleInputConditionsInnerTypeEnum {
@JsonValue(r'approach')
approach(r'approach');

const TriggerRuleInputConditionsInnerTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


