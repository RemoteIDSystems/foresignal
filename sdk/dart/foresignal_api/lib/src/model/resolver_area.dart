//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'resolver_area.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ResolverArea {
  /// Returns a new [ResolverArea] instance.
  ResolverArea({

     this.bbox,

     this.polygon,

     this.placeId,

     this.regionCodes,
  });

  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



  @JsonKey(
    
    name: r'polygon',
    required: false,
    includeIfNull: false,
  )


  final Geometry? polygon;



      /// 036 place id
  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ResolverArea &&
      other.bbox == bbox &&
      other.polygon == polygon &&
      other.placeId == placeId &&
      other.regionCodes == regionCodes;

    @override
    int get hashCode =>
        bbox.hashCode +
        polygon.hashCode +
        placeId.hashCode +
        regionCodes.hashCode;

  factory ResolverArea.fromJson(Map<String, dynamic> json) => _$ResolverAreaFromJson(json);

  Map<String, dynamic> toJson() => _$ResolverAreaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

