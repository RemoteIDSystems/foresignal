//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'approach_condition_target.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ApproachConditionTarget {
  /// Returns a new [ApproachConditionTarget] instance.
  ApproachConditionTarget({

     this.placeId,

     this.point,

     this.record,
  });

  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



  @JsonKey(
    
    name: r'point',
    required: false,
    includeIfNull: false,
  )


  final Geometry? point;



      /// `{sourceId}/{externalId}`
  @JsonKey(
    
    name: r'record',
    required: false,
    includeIfNull: false,
  )


  final String? record;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ApproachConditionTarget &&
      other.placeId == placeId &&
      other.point == point &&
      other.record == record;

    @override
    int get hashCode =>
        (placeId == null ? 0 : placeId.hashCode) +
        (point == null ? 0 : point.hashCode) +
        (record == null ? 0 : record.hashCode);

  factory ApproachConditionTarget.fromJson(Map<String, dynamic> json) => _$ApproachConditionTargetFromJson(json);

  Map<String, dynamic> toJson() => _$ApproachConditionTargetToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

