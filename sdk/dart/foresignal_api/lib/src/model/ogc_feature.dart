//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/ogc_link.dart';
import 'package:foresignal_api/src/model/ogc_feature_properties.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_feature.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcFeature {
  /// Returns a new [OgcFeature] instance.
  OgcFeature({

    required  this.type,

    required  this.id,

    required  this.geometry,

    required  this.properties,

     this.links,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final OgcFeatureTypeEnum type;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'geometry',
    required: true,
    includeIfNull: true,
  )


  final Geometry? geometry;



  @JsonKey(
    
    name: r'properties',
    required: true,
    includeIfNull: false,
  )


  final OgcFeatureProperties properties;



  @JsonKey(
    
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final List<OgcLink>? links;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcFeature &&
      other.type == type &&
      other.id == id &&
      other.geometry == geometry &&
      other.properties == properties &&
      other.links == links;

    @override
    int get hashCode =>
        type.hashCode +
        id.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        properties.hashCode +
        links.hashCode;

  factory OgcFeature.fromJson(Map<String, dynamic> json) => _$OgcFeatureFromJson(json);

  Map<String, dynamic> toJson() => _$OgcFeatureToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OgcFeatureTypeEnum {
@JsonValue(r'Feature')
feature(r'Feature');

const OgcFeatureTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


