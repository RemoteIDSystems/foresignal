//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'eta_result_target.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EtaResultTarget {
  /// Returns a new [EtaResultTarget] instance.
  EtaResultTarget({

     this.placeId,

    required  this.point,
  });

  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



  @JsonKey(
    
    name: r'point',
    required: true,
    includeIfNull: false,
  )


  final Geometry point;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EtaResultTarget &&
      other.placeId == placeId &&
      other.point == point;

    @override
    int get hashCode =>
        (placeId == null ? 0 : placeId.hashCode) +
        point.hashCode;

  factory EtaResultTarget.fromJson(Map<String, dynamic> json) => _$EtaResultTargetFromJson(json);

  Map<String, dynamic> toJson() => _$EtaResultTargetToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

