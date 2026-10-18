//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_tile_matrix_set_tile_matrices_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_tile_matrix_set.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcTileMatrixSet {
  /// Returns a new [OgcTileMatrixSet] instance.
  OgcTileMatrixSet({

    required  this.id,

     this.title,

     this.uri,

    required  this.crs,

     this.orderedAxes,

     this.wellKnownScaleSet,

    required  this.tileMatrices,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final OgcTileMatrixSetIdEnum id;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'uri',
    required: false,
    includeIfNull: false,
  )


  final String? uri;



  @JsonKey(
    
    name: r'crs',
    required: true,
    includeIfNull: false,
  )


  final String crs;



  @JsonKey(
    
    name: r'orderedAxes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? orderedAxes;



  @JsonKey(
    
    name: r'wellKnownScaleSet',
    required: false,
    includeIfNull: false,
  )


  final String? wellKnownScaleSet;



  @JsonKey(
    
    name: r'tileMatrices',
    required: true,
    includeIfNull: false,
  )


  final List<OgcTileMatrixSetTileMatricesInner> tileMatrices;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcTileMatrixSet &&
      other.id == id &&
      other.title == title &&
      other.uri == uri &&
      other.crs == crs &&
      other.orderedAxes == orderedAxes &&
      other.wellKnownScaleSet == wellKnownScaleSet &&
      other.tileMatrices == tileMatrices;

    @override
    int get hashCode =>
        id.hashCode +
        title.hashCode +
        uri.hashCode +
        crs.hashCode +
        orderedAxes.hashCode +
        (wellKnownScaleSet == null ? 0 : wellKnownScaleSet.hashCode) +
        tileMatrices.hashCode;

  factory OgcTileMatrixSet.fromJson(Map<String, dynamic> json) => _$OgcTileMatrixSetFromJson(json);

  Map<String, dynamic> toJson() => _$OgcTileMatrixSetToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OgcTileMatrixSetIdEnum {
@JsonValue(r'WebMercatorQuad')
webMercatorQuad(r'WebMercatorQuad'),
@JsonValue(r'WorldCRS84Quad')
worldCRS84Quad(r'WorldCRS84Quad');

const OgcTileMatrixSetIdEnum(this.value);

final String value;

@override
String toString() => value;
}


