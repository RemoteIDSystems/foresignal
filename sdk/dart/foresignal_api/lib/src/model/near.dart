//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/near_one_of1.dart';
import 'package:foresignal_api/src/model/near_one_of.dart';
import 'package:foresignal_api/src/model/near_one_of2_anchor.dart';
import 'package:foresignal_api/src/model/near_one_of2.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'near.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Near {
  /// Returns a new [Near] instance.
  Near({

     this.placeIds,

     this.placeId,

    required  this.radiusKm,

    required  this.point,

    required  this.anchor,

     this.maxAnchorAge,

     this.minMoveMeters = 200,
  });

  @JsonKey(
    
    name: r'placeIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? placeIds;



  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



      /// 0 = anchor point inside the target geometry
          // minimum: 0
          // maximum: 2000
  @JsonKey(
    
    name: r'radiusKm',
    required: true,
    includeIfNull: false,
  )


  final num radiusKm;



      /// [lon, lat]
  @JsonKey(
    
    name: r'point',
    required: true,
    includeIfNull: false,
  )


  final List<num> point;



  @JsonKey(
    
    name: r'anchor',
    required: true,
    includeIfNull: false,
  )


  final NearOneOf2Anchor anchor;



      /// Rule suspends (ANCHOR_STALE) when the anchor is older; default PT30M
  @JsonKey(
    
    name: r'maxAnchorAge',
    required: false,
    includeIfNull: false,
  )


  final String? maxAnchorAge;



          // minimum: 0
  @JsonKey(
    defaultValue: 200,
    name: r'minMoveMeters',
    required: false,
    includeIfNull: false,
  )


  final num? minMoveMeters;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Near &&
      other.placeIds == placeIds &&
      other.placeId == placeId &&
      other.radiusKm == radiusKm &&
      other.point == point &&
      other.anchor == anchor &&
      other.maxAnchorAge == maxAnchorAge &&
      other.minMoveMeters == minMoveMeters;

    @override
    int get hashCode =>
        placeIds.hashCode +
        placeId.hashCode +
        radiusKm.hashCode +
        point.hashCode +
        anchor.hashCode +
        maxAnchorAge.hashCode +
        minMoveMeters.hashCode;

  factory Near.fromJson(Map<String, dynamic> json) => _$NearFromJson(json);

  Map<String, dynamic> toJson() => _$NearToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

