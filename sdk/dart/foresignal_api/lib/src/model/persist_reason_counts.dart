//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'persist_reason_counts.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PersistReasonCounts {
  /// Returns a new [PersistReasonCounts] instance.
  PersistReasonCounts({

    required  this.FIRST,

    required  this.REAPPEAR,

    required  this.SIGNIFICANT,

    required  this.TOMBSTONE,

    required  this.CHANGE,

    required  this.SAMPLE,

    required  this.KEYFRAME,

    required  this.HEARTBEAT,

    required  this.TRIGGER_EDGE,
  });

          // minimum: 0
  @JsonKey(
    
    name: r'FIRST',
    required: true,
    includeIfNull: false,
  )


  final int FIRST;



          // minimum: 0
  @JsonKey(
    
    name: r'REAPPEAR',
    required: true,
    includeIfNull: false,
  )


  final int REAPPEAR;



          // minimum: 0
  @JsonKey(
    
    name: r'SIGNIFICANT',
    required: true,
    includeIfNull: false,
  )


  final int SIGNIFICANT;



          // minimum: 0
  @JsonKey(
    
    name: r'TOMBSTONE',
    required: true,
    includeIfNull: false,
  )


  final int TOMBSTONE;



          // minimum: 0
  @JsonKey(
    
    name: r'CHANGE',
    required: true,
    includeIfNull: false,
  )


  final int CHANGE;



          // minimum: 0
  @JsonKey(
    
    name: r'SAMPLE',
    required: true,
    includeIfNull: false,
  )


  final int SAMPLE;



          // minimum: 0
  @JsonKey(
    
    name: r'KEYFRAME',
    required: true,
    includeIfNull: false,
  )


  final int KEYFRAME;



          // minimum: 0
  @JsonKey(
    
    name: r'HEARTBEAT',
    required: true,
    includeIfNull: false,
  )


  final int HEARTBEAT;



          // minimum: 0
  @JsonKey(
    
    name: r'TRIGGER_EDGE',
    required: true,
    includeIfNull: false,
  )


  final int TRIGGER_EDGE;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PersistReasonCounts &&
      other.FIRST == FIRST &&
      other.REAPPEAR == REAPPEAR &&
      other.SIGNIFICANT == SIGNIFICANT &&
      other.TOMBSTONE == TOMBSTONE &&
      other.CHANGE == CHANGE &&
      other.SAMPLE == SAMPLE &&
      other.KEYFRAME == KEYFRAME &&
      other.HEARTBEAT == HEARTBEAT &&
      other.TRIGGER_EDGE == TRIGGER_EDGE;

    @override
    int get hashCode =>
        FIRST.hashCode +
        REAPPEAR.hashCode +
        SIGNIFICANT.hashCode +
        TOMBSTONE.hashCode +
        CHANGE.hashCode +
        SAMPLE.hashCode +
        KEYFRAME.hashCode +
        HEARTBEAT.hashCode +
        TRIGGER_EDGE.hashCode;

  factory PersistReasonCounts.fromJson(Map<String, dynamic> json) => _$PersistReasonCountsFromJson(json);

  Map<String, dynamic> toJson() => _$PersistReasonCountsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

