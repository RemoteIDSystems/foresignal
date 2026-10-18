//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'trigger_test_result_window_evaluations_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TriggerTestResultWindowEvaluationsInner {
  /// Returns a new [TriggerTestResultWindowEvaluationsInner] instance.
  TriggerTestResultWindowEvaluationsInner({

    required  this.at,

    required  this.groupKey,

     this.value,

    required  this.state,

     this.reason,
  });

  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'groupKey',
    required: true,
    includeIfNull: false,
  )


  final String groupKey;



  @JsonKey(
    
    name: r'value',
    required: false,
    includeIfNull: false,
  )


  final num? value;



  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final TriggerTestResultWindowEvaluationsInnerStateEnum state;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TriggerTestResultWindowEvaluationsInner &&
      other.at == at &&
      other.groupKey == groupKey &&
      other.value == value &&
      other.state == state &&
      other.reason == reason;

    @override
    int get hashCode =>
        at.hashCode +
        groupKey.hashCode +
        (value == null ? 0 : value.hashCode) +
        state.hashCode +
        (reason == null ? 0 : reason.hashCode);

  factory TriggerTestResultWindowEvaluationsInner.fromJson(Map<String, dynamic> json) => _$TriggerTestResultWindowEvaluationsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$TriggerTestResultWindowEvaluationsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TriggerTestResultWindowEvaluationsInnerStateEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'clear')
clear(r'clear'),
@JsonValue(r'unknown')
unknown(r'unknown'),
@JsonValue(r'learning')
learning(r'learning');

const TriggerTestResultWindowEvaluationsInnerStateEnum(this.value);

final String value;

@override
String toString() => value;
}


