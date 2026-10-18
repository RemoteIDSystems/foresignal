//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'near_one_of1.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NearOneOf1 {
  /// Returns a new [NearOneOf1] instance.
  NearOneOf1({

    required  this.point,

    required  this.radiusKm,
  });

      /// [lon, lat]
  @JsonKey(
    
    name: r'point',
    required: true,
    includeIfNull: false,
  )


  final List<num> point;



          // minimum: 0.1
          // maximum: 2000
  @JsonKey(
    
    name: r'radiusKm',
    required: true,
    includeIfNull: false,
  )


  final num radiusKm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NearOneOf1 &&
      other.point == point &&
      other.radiusKm == radiusKm;

    @override
    int get hashCode =>
        point.hashCode +
        radiusKm.hashCode;

  factory NearOneOf1.fromJson(Map<String, dynamic> json) => _$NearOneOf1FromJson(json);

  Map<String, dynamic> toJson() => _$NearOneOf1ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

