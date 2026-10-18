//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'anomaly_baseline_slot.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnomalyBaselineSlot {
  /// Returns a new [AnomalyBaselineSlot] instance.
  AnomalyBaselineSlot({

    required  this.groupKey,

    required  this.slot,

    required  this.n,

    required  this.mean,

    required  this.variance,

    required  this.model,
  });

  @JsonKey(
    
    name: r'groupKey',
    required: true,
    includeIfNull: false,
  )


  final String groupKey;



      /// 0–167 (hour-of-week) or 0–23 (hour-of-day)
  @JsonKey(
    
    name: r'slot',
    required: true,
    includeIfNull: false,
  )


  final int slot;



      /// Coverage-ok, unflagged samples
  @JsonKey(
    
    name: r'n',
    required: true,
    includeIfNull: false,
  )


  final int n;



  @JsonKey(
    
    name: r'mean',
    required: true,
    includeIfNull: false,
  )


  final num mean;



  @JsonKey(
    
    name: r'variance',
    required: true,
    includeIfNull: false,
  )


  final num variance;



  @JsonKey(
    
    name: r'model',
    required: true,
    includeIfNull: false,
  )


  final AnomalyBaselineSlotModelEnum model;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnomalyBaselineSlot &&
      other.groupKey == groupKey &&
      other.slot == slot &&
      other.n == n &&
      other.mean == mean &&
      other.variance == variance &&
      other.model == model;

    @override
    int get hashCode =>
        groupKey.hashCode +
        slot.hashCode +
        n.hashCode +
        mean.hashCode +
        variance.hashCode +
        model.hashCode;

  factory AnomalyBaselineSlot.fromJson(Map<String, dynamic> json) => _$AnomalyBaselineSlotFromJson(json);

  Map<String, dynamic> toJson() => _$AnomalyBaselineSlotToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnomalyBaselineSlotModelEnum {
@JsonValue(r'poisson')
poisson(r'poisson'),
@JsonValue(r'nb')
nb(r'nb');

const AnomalyBaselineSlotModelEnum(this.value);

final String value;

@override
String toString() => value;
}


