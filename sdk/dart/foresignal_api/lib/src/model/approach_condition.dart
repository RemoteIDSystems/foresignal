//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/approach_condition_target.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'approach_condition.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ApproachCondition {
  /// Returns a new [ApproachCondition] instance.
  ApproachCondition({

    required  this.type,

    required  this.target,

    required  this.withinMeters,

    required  this.withinMinutes,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ApproachConditionTypeEnum type;



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
    bool operator ==(Object other) => identical(this, other) || other is ApproachCondition &&
      other.type == type &&
      other.target == target &&
      other.withinMeters == withinMeters &&
      other.withinMinutes == withinMinutes;

    @override
    int get hashCode =>
        type.hashCode +
        target.hashCode +
        withinMeters.hashCode +
        withinMinutes.hashCode;

  factory ApproachCondition.fromJson(Map<String, dynamic> json) => _$ApproachConditionFromJson(json);

  Map<String, dynamic> toJson() => _$ApproachConditionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ApproachConditionTypeEnum {
@JsonValue(r'approach')
approach(r'approach');

const ApproachConditionTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


