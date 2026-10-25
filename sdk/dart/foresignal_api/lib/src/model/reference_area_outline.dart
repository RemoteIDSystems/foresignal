//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'reference_area_outline.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReferenceAreaOutline {
  /// Returns a new [ReferenceAreaOutline] instance.
  ReferenceAreaOutline({

    required  this.kind,

    required  this.code,

    required  this.name,

     this.countryIso2,

     this.countryName,

    required  this.bbox,

    required  this.geometry,
  });

  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final ReferenceAreaOutlineKindEnum kind;



  @JsonKey(
    
    name: r'code',
    required: true,
    includeIfNull: false,
  )


  final String code;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'countryIso2',
    required: false,
    includeIfNull: false,
  )


  final String? countryIso2;



  @JsonKey(
    
    name: r'countryName',
    required: false,
    includeIfNull: false,
  )


  final String? countryName;



      /// [west, south, east, north]
  @JsonKey(
    
    name: r'bbox',
    required: true,
    includeIfNull: false,
  )


  final List<num> bbox;



  @JsonKey(
    
    name: r'geometry',
    required: true,
    includeIfNull: false,
  )


  final Geometry geometry;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReferenceAreaOutline &&
      other.kind == kind &&
      other.code == code &&
      other.name == name &&
      other.countryIso2 == countryIso2 &&
      other.countryName == countryName &&
      other.bbox == bbox &&
      other.geometry == geometry;

    @override
    int get hashCode =>
        kind.hashCode +
        code.hashCode +
        name.hashCode +
        (countryIso2 == null ? 0 : countryIso2.hashCode) +
        (countryName == null ? 0 : countryName.hashCode) +
        bbox.hashCode +
        geometry.hashCode;

  factory ReferenceAreaOutline.fromJson(Map<String, dynamic> json) => _$ReferenceAreaOutlineFromJson(json);

  Map<String, dynamic> toJson() => _$ReferenceAreaOutlineToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReferenceAreaOutlineKindEnum {
@JsonValue(r'country')
country(r'country'),
@JsonValue(r'admin1')
admin1(r'admin1');

const ReferenceAreaOutlineKindEnum(this.value);

final String value;

@override
String toString() => value;
}


