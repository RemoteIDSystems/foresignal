//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_event_member_join_reason.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelEventMemberJoinReason {
  /// Returns a new [IntelEventMemberJoinReason] instance.
  IntelEventMemberJoinReason({

     this.clause,

     this.distanceKm,

     this.dtMinutes,

     this.matchedKeys,

     this.regionTag,
  });

      /// Recipe clause (seed, join index, contradiction)
  @JsonKey(
    
    name: r'clause',
    required: false,
    includeIfNull: false,
  )


  final String? clause;



  @JsonKey(
    
    name: r'distanceKm',
    required: false,
    includeIfNull: false,
  )


  final num? distanceKm;



  @JsonKey(
    
    name: r'dtMinutes',
    required: false,
    includeIfNull: false,
  )


  final num? dtMinutes;



  @JsonKey(
    
    name: r'matchedKeys',
    required: false,
    includeIfNull: false,
  )


  final List<String>? matchedKeys;



  @JsonKey(
    
    name: r'regionTag',
    required: false,
    includeIfNull: false,
  )


  final String? regionTag;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelEventMemberJoinReason &&
      other.clause == clause &&
      other.distanceKm == distanceKm &&
      other.dtMinutes == dtMinutes &&
      other.matchedKeys == matchedKeys &&
      other.regionTag == regionTag;

    @override
    int get hashCode =>
        (clause == null ? 0 : clause.hashCode) +
        (distanceKm == null ? 0 : distanceKm.hashCode) +
        (dtMinutes == null ? 0 : dtMinutes.hashCode) +
        matchedKeys.hashCode +
        (regionTag == null ? 0 : regionTag.hashCode);

  factory IntelEventMemberJoinReason.fromJson(Map<String, dynamic> json) => _$IntelEventMemberJoinReasonFromJson(json);

  Map<String, dynamic> toJson() => _$IntelEventMemberJoinReasonToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

