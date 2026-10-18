//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'claim_place.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ClaimPlace {
  /// Returns a new [ClaimPlace] instance.
  ClaimPlace({

     this.ref,

    required  this.name,

     this.geom,

    required  this.precision,

    required  this.confidence,

     this.regionCode,
  });

      /// 036 place or 040 region; null for unresolved names
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// Null when unresolved (LLM places not in the gazetteer)
  @JsonKey(
    
    name: r'geom',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geom;



  @JsonKey(
    
    name: r'precision',
    required: true,
    includeIfNull: false,
  )


  final ClaimPlacePrecisionEnum precision;



      /// By ambiguity and population
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'confidence',
    required: true,
    includeIfNull: false,
  )


  final num confidence;



      /// 040 region code of the resolved place (country or admin-1), or null (136)
  @JsonKey(
    
    name: r'regionCode',
    required: false,
    includeIfNull: false,
  )


  final String? regionCode;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ClaimPlace &&
      other.ref == ref &&
      other.name == name &&
      other.geom == geom &&
      other.precision == precision &&
      other.confidence == confidence &&
      other.regionCode == regionCode;

    @override
    int get hashCode =>
        (ref == null ? 0 : ref.hashCode) +
        name.hashCode +
        (geom == null ? 0 : geom.hashCode) +
        precision.hashCode +
        confidence.hashCode +
        (regionCode == null ? 0 : regionCode.hashCode);

  factory ClaimPlace.fromJson(Map<String, dynamic> json) => _$ClaimPlaceFromJson(json);

  Map<String, dynamic> toJson() => _$ClaimPlaceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ClaimPlacePrecisionEnum {
@JsonValue(r'point')
point(r'point'),
@JsonValue(r'locality')
locality(r'locality'),
@JsonValue(r'admin1')
admin1(r'admin1'),
@JsonValue(r'country')
country(r'country');

const ClaimPlacePrecisionEnum(this.value);

final String value;

@override
String toString() => value;
}


