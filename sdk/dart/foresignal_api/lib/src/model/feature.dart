//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/feature_properties.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'feature.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Feature {
  /// Returns a new [Feature] instance.
  Feature({

    required  this.type,

     this.id,

    required  this.geometry,

    required  this.properties,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final FeatureTypeEnum type;



  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



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


  final FeatureProperties properties;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Feature &&
      other.type == type &&
      other.id == id &&
      other.geometry == geometry &&
      other.properties == properties;

    @override
    int get hashCode =>
        type.hashCode +
        id.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        properties.hashCode;

  factory Feature.fromJson(Map<String, dynamic> json) => _$FeatureFromJson(json);

  Map<String, dynamic> toJson() => _$FeatureToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum FeatureTypeEnum {
@JsonValue(r'Feature')
feature(r'Feature');

const FeatureTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


