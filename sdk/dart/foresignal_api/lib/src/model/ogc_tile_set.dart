//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_link.dart';
import 'package:foresignal_api/src/model/ogc_tile_set_tile_matrix_set_limits_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_tile_set.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcTileSet {
  /// Returns a new [OgcTileSet] instance.
  OgcTileSet({

     this.title,

    required  this.dataType,

    required  this.crs,

     this.tileMatrixSetURI,

     this.tileMatrixSetLimits,

    required  this.links,

     this.attribution,
  });

  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'dataType',
    required: true,
    includeIfNull: false,
  )


  final OgcTileSetDataTypeEnum dataType;



  @JsonKey(
    
    name: r'crs',
    required: true,
    includeIfNull: false,
  )


  final String crs;



  @JsonKey(
    
    name: r'tileMatrixSetURI',
    required: false,
    includeIfNull: false,
  )


  final String? tileMatrixSetURI;



  @JsonKey(
    
    name: r'tileMatrixSetLimits',
    required: false,
    includeIfNull: false,
  )


  final List<OgcTileSetTileMatrixSetLimitsInner>? tileMatrixSetLimits;



  @JsonKey(
    
    name: r'links',
    required: true,
    includeIfNull: false,
  )


  final List<OgcLink> links;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final String? attribution;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcTileSet &&
      other.title == title &&
      other.dataType == dataType &&
      other.crs == crs &&
      other.tileMatrixSetURI == tileMatrixSetURI &&
      other.tileMatrixSetLimits == tileMatrixSetLimits &&
      other.links == links &&
      other.attribution == attribution;

    @override
    int get hashCode =>
        title.hashCode +
        dataType.hashCode +
        crs.hashCode +
        tileMatrixSetURI.hashCode +
        tileMatrixSetLimits.hashCode +
        links.hashCode +
        (attribution == null ? 0 : attribution.hashCode);

  factory OgcTileSet.fromJson(Map<String, dynamic> json) => _$OgcTileSetFromJson(json);

  Map<String, dynamic> toJson() => _$OgcTileSetToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OgcTileSetDataTypeEnum {
@JsonValue(r'map')
map(r'map');

const OgcTileSetDataTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


