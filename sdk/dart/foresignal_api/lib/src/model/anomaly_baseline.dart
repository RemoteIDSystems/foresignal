//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/anomaly_baseline_slot.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'anomaly_baseline.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnomalyBaseline {
  /// Returns a new [AnomalyBaseline] instance.
  AnomalyBaseline({

    required  this.ruleId,

    required  this.slotMode,

    required  this.bucket,

    required  this.weeksAvailable,

     this.minWeeks,

    required  this.learning,

     this.learningUntil,

     this.lowConfidence,

     this.samplePath,

    required  this.slots,
  });

  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



  @JsonKey(
    
    name: r'slotMode',
    required: true,
    includeIfNull: false,
  )


  final AnomalyBaselineSlotModeEnum slotMode;



  @JsonKey(
    
    name: r'bucket',
    required: true,
    includeIfNull: false,
  )


  final AnomalyBaselineBucketEnum bucket;



  @JsonKey(
    
    name: r'weeksAvailable',
    required: true,
    includeIfNull: false,
  )


  final num weeksAvailable;



  @JsonKey(
    
    name: r'minWeeks',
    required: false,
    includeIfNull: false,
  )


  final int? minWeeks;



  @JsonKey(
    
    name: r'learning',
    required: true,
    includeIfNull: false,
  )


  final bool learning;



  @JsonKey(
    
    name: r'learningUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? learningUntil;



  @JsonKey(
    
    name: r'lowConfidence',
    required: false,
    includeIfNull: false,
  )


  final bool? lowConfidence;



      /// Which store the samples come from
  @JsonKey(
    
    name: r'samplePath',
    required: false,
    includeIfNull: false,
  )


  final AnomalyBaselineSamplePathEnum? samplePath;



  @JsonKey(
    
    name: r'slots',
    required: true,
    includeIfNull: false,
  )


  final List<AnomalyBaselineSlot> slots;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnomalyBaseline &&
      other.ruleId == ruleId &&
      other.slotMode == slotMode &&
      other.bucket == bucket &&
      other.weeksAvailable == weeksAvailable &&
      other.minWeeks == minWeeks &&
      other.learning == learning &&
      other.learningUntil == learningUntil &&
      other.lowConfidence == lowConfidence &&
      other.samplePath == samplePath &&
      other.slots == slots;

    @override
    int get hashCode =>
        ruleId.hashCode +
        slotMode.hashCode +
        bucket.hashCode +
        weeksAvailable.hashCode +
        minWeeks.hashCode +
        learning.hashCode +
        (learningUntil == null ? 0 : learningUntil.hashCode) +
        lowConfidence.hashCode +
        samplePath.hashCode +
        slots.hashCode;

  factory AnomalyBaseline.fromJson(Map<String, dynamic> json) => _$AnomalyBaselineFromJson(json);

  Map<String, dynamic> toJson() => _$AnomalyBaselineToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AnomalyBaselineSlotModeEnum {
@JsonValue(r'hour-of-week')
hourOfWeek(r'hour-of-week'),
@JsonValue(r'hour-of-day')
hourOfDay(r'hour-of-day');

const AnomalyBaselineSlotModeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AnomalyBaselineBucketEnum {
@JsonValue(r'15m')
n15m(r'15m'),
@JsonValue(r'1h')
n1h(r'1h');

const AnomalyBaselineBucketEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Which store the samples come from
enum AnomalyBaselineSamplePathEnum {
@JsonValue(r'activity_sample')
activitySample(r'activity_sample'),
@JsonValue(r'activity_rollup')
activityRollup(r'activity_rollup');

const AnomalyBaselineSamplePathEnum(this.value);

final String value;

@override
String toString() => value;
}


