//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/near_one_of2_anchor.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'near_one_of2.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NearOneOf2 {
  /// Returns a new [NearOneOf2] instance.
  NearOneOf2({

    required  this.anchor,

    required  this.radiusKm,

     this.maxAnchorAge,

     this.minMoveMeters = 200,
  });

  @JsonKey(
    
    name: r'anchor',
    required: true,
    includeIfNull: false,
  )


  final NearOneOf2Anchor anchor;



      /// 0 = anchor point inside the target geometry
          // minimum: 0
          // maximum: 2000
  @JsonKey(
    
    name: r'radiusKm',
    required: true,
    includeIfNull: false,
  )


  final num radiusKm;



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
    bool operator ==(Object other) => identical(this, other) || other is NearOneOf2 &&
      other.anchor == anchor &&
      other.radiusKm == radiusKm &&
      other.maxAnchorAge == maxAnchorAge &&
      other.minMoveMeters == minMoveMeters;

    @override
    int get hashCode =>
        anchor.hashCode +
        radiusKm.hashCode +
        maxAnchorAge.hashCode +
        minMoveMeters.hashCode;

  factory NearOneOf2.fromJson(Map<String, dynamic> json) => _$NearOneOf2FromJson(json);

  Map<String, dynamic> toJson() => _$NearOneOf2ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

