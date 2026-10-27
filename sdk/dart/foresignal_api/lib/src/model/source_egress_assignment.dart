//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'source_egress_assignment.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SourceEgressAssignment {
  /// Returns a new [SourceEgressAssignment] instance.
  SourceEgressAssignment({

     this.routeId,

     this.excludeFromRotation = false,
  });

      /// Pin the source to this route (with rotation on); null uses the rotation. A pinned route that is unusable fails the pull rather than moving it (R2-018)
  @JsonKey(
    
    name: r'routeId',
    required: false,
    includeIfNull: false,
  )


  final String? routeId;



      /// With rotation on and no pin, keep the source on the default route, or direct when there is none (R2-018)
  @JsonKey(
    defaultValue: false,
    name: r'excludeFromRotation',
    required: false,
    includeIfNull: false,
  )


  final bool? excludeFromRotation;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SourceEgressAssignment &&
      other.routeId == routeId &&
      other.excludeFromRotation == excludeFromRotation;

    @override
    int get hashCode =>
        (routeId == null ? 0 : routeId.hashCode) +
        excludeFromRotation.hashCode;

  factory SourceEgressAssignment.fromJson(Map<String, dynamic> json) => _$SourceEgressAssignmentFromJson(json);

  Map<String, dynamic> toJson() => _$SourceEgressAssignmentToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

