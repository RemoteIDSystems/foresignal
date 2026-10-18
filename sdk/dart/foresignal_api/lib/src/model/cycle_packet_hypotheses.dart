//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/cycle_packet_item.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cycle_packet_hypotheses.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CyclePacketHypotheses {
  /// Returns a new [CyclePacketHypotheses] instance.
  CyclePacketHypotheses({

     this.revisions,

     this.bandChanges,

     this.indicatorsObserved,

     this.indicatorsAbsent,

     this.indicatorsUnknownCoverage,

     this.assumptionsBroken,

     this.assumptionsQuestioned,

     this.dueForReview,
  });

  @JsonKey(
    
    name: r'revisions',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? revisions;



  @JsonKey(
    
    name: r'bandChanges',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? bandChanges;



  @JsonKey(
    
    name: r'indicatorsObserved',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? indicatorsObserved;



  @JsonKey(
    
    name: r'indicatorsAbsent',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? indicatorsAbsent;



  @JsonKey(
    
    name: r'indicatorsUnknownCoverage',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? indicatorsUnknownCoverage;



  @JsonKey(
    
    name: r'assumptionsBroken',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? assumptionsBroken;



  @JsonKey(
    
    name: r'assumptionsQuestioned',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? assumptionsQuestioned;



  @JsonKey(
    
    name: r'dueForReview',
    required: false,
    includeIfNull: false,
  )


  final List<CyclePacketItem>? dueForReview;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CyclePacketHypotheses &&
      other.revisions == revisions &&
      other.bandChanges == bandChanges &&
      other.indicatorsObserved == indicatorsObserved &&
      other.indicatorsAbsent == indicatorsAbsent &&
      other.indicatorsUnknownCoverage == indicatorsUnknownCoverage &&
      other.assumptionsBroken == assumptionsBroken &&
      other.assumptionsQuestioned == assumptionsQuestioned &&
      other.dueForReview == dueForReview;

    @override
    int get hashCode =>
        revisions.hashCode +
        bandChanges.hashCode +
        indicatorsObserved.hashCode +
        indicatorsAbsent.hashCode +
        indicatorsUnknownCoverage.hashCode +
        assumptionsBroken.hashCode +
        assumptionsQuestioned.hashCode +
        dueForReview.hashCode;

  factory CyclePacketHypotheses.fromJson(Map<String, dynamic> json) => _$CyclePacketHypothesesFromJson(json);

  Map<String, dynamic> toJson() => _$CyclePacketHypothesesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

