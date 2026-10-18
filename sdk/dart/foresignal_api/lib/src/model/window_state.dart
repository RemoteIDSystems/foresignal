//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'window_state.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WindowState {
  /// Returns a new [WindowState] instance.
  WindowState({

    required  this.groupKey,

    required  this.state,

    required  this.since,

     this.lastValue,

     this.threshold,

     this.lastEvaluatedAt,

     this.lastWatermarkSeq,

     this.reason,

     this.lastAlertId,
  });

      /// Group key; `*` when the rule has no groupBy
  @JsonKey(
    
    name: r'groupKey',
    required: true,
    includeIfNull: false,
  )


  final String groupKey;



  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final WindowStateStateEnum state;



  @JsonKey(
    
    name: r'since',
    required: true,
    includeIfNull: false,
  )


  final DateTime since;



      /// Distinct count (count/absence) or bucket count (anomaly)
  @JsonKey(
    
    name: r'lastValue',
    required: false,
    includeIfNull: false,
  )


  final num? lastValue;



      /// Count threshold, or the expected value for anomaly rules
  @JsonKey(
    
    name: r'threshold',
    required: false,
    includeIfNull: false,
  )


  final num? threshold;



  @JsonKey(
    
    name: r'lastEvaluatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastEvaluatedAt;



  @JsonKey(
    
    name: r'lastWatermarkSeq',
    required: false,
    includeIfNull: false,
  )


  final int? lastWatermarkSeq;



      /// Why the state is unknown or learning (coverage gate reason, warm-up)
  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;



  @JsonKey(
    
    name: r'lastAlertId',
    required: false,
    includeIfNull: false,
  )


  final int? lastAlertId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WindowState &&
      other.groupKey == groupKey &&
      other.state == state &&
      other.since == since &&
      other.lastValue == lastValue &&
      other.threshold == threshold &&
      other.lastEvaluatedAt == lastEvaluatedAt &&
      other.lastWatermarkSeq == lastWatermarkSeq &&
      other.reason == reason &&
      other.lastAlertId == lastAlertId;

    @override
    int get hashCode =>
        groupKey.hashCode +
        state.hashCode +
        since.hashCode +
        (lastValue == null ? 0 : lastValue.hashCode) +
        (threshold == null ? 0 : threshold.hashCode) +
        (lastEvaluatedAt == null ? 0 : lastEvaluatedAt.hashCode) +
        (lastWatermarkSeq == null ? 0 : lastWatermarkSeq.hashCode) +
        (reason == null ? 0 : reason.hashCode) +
        (lastAlertId == null ? 0 : lastAlertId.hashCode);

  factory WindowState.fromJson(Map<String, dynamic> json) => _$WindowStateFromJson(json);

  Map<String, dynamic> toJson() => _$WindowStateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum WindowStateStateEnum {
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'clear')
clear(r'clear'),
@JsonValue(r'unknown')
unknown(r'unknown'),
@JsonValue(r'learning')
learning(r'learning');

const WindowStateStateEnum(this.value);

final String value;

@override
String toString() => value;
}


