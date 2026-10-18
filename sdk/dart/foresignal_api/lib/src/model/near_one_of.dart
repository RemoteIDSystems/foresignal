//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'near_one_of.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class NearOneOf {
  /// Returns a new [NearOneOf] instance.
  NearOneOf({

     this.placeIds,

     this.placeId,

     this.radiusKm,
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



          // minimum: 0
  @JsonKey(
    
    name: r'radiusKm',
    required: false,
    includeIfNull: false,
  )


  final num? radiusKm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is NearOneOf &&
      other.placeIds == placeIds &&
      other.placeId == placeId &&
      other.radiusKm == radiusKm;

    @override
    int get hashCode =>
        placeIds.hashCode +
        placeId.hashCode +
        (radiusKm == null ? 0 : radiusKm.hashCode);

  factory NearOneOf.fromJson(Map<String, dynamic> json) => _$NearOneOfFromJson(json);

  Map<String, dynamic> toJson() => _$NearOneOfToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

