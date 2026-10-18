//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'baseline_rebuild_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BaselineRebuildResult {
  /// Returns a new [BaselineRebuildResult] instance.
  BaselineRebuildResult({

    required  this.ruleId,

    required  this.accepted,

     this.weeksAvailable,

     this.backfillFrom,
  });

  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



  @JsonKey(
    
    name: r'accepted',
    required: true,
    includeIfNull: false,
  )


  final bool accepted;



  @JsonKey(
    
    name: r'weeksAvailable',
    required: false,
    includeIfNull: false,
  )


  final num? weeksAvailable;



  @JsonKey(
    
    name: r'backfillFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? backfillFrom;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BaselineRebuildResult &&
      other.ruleId == ruleId &&
      other.accepted == accepted &&
      other.weeksAvailable == weeksAvailable &&
      other.backfillFrom == backfillFrom;

    @override
    int get hashCode =>
        ruleId.hashCode +
        accepted.hashCode +
        weeksAvailable.hashCode +
        (backfillFrom == null ? 0 : backfillFrom.hashCode);

  factory BaselineRebuildResult.fromJson(Map<String, dynamic> json) => _$BaselineRebuildResultFromJson(json);

  Map<String, dynamic> toJson() => _$BaselineRebuildResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

