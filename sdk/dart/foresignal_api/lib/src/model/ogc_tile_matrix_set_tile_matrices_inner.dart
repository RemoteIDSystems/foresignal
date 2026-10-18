//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_tile_matrix_set_tile_matrices_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcTileMatrixSetTileMatricesInner {
  /// Returns a new [OgcTileMatrixSetTileMatricesInner] instance.
  OgcTileMatrixSetTileMatricesInner({

    required  this.id,

    required  this.scaleDenominator,

     this.cellSize,

    required  this.pointOfOrigin,

    required  this.tileWidth,

    required  this.tileHeight,

    required  this.matrixWidth,

    required  this.matrixHeight,

     this.cornerOfOrigin,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'scaleDenominator',
    required: true,
    includeIfNull: false,
  )


  final num scaleDenominator;



  @JsonKey(
    
    name: r'cellSize',
    required: false,
    includeIfNull: false,
  )


  final num? cellSize;



  @JsonKey(
    
    name: r'pointOfOrigin',
    required: true,
    includeIfNull: false,
  )


  final List<num> pointOfOrigin;



  @JsonKey(
    
    name: r'tileWidth',
    required: true,
    includeIfNull: false,
  )


  final int tileWidth;



  @JsonKey(
    
    name: r'tileHeight',
    required: true,
    includeIfNull: false,
  )


  final int tileHeight;



  @JsonKey(
    
    name: r'matrixWidth',
    required: true,
    includeIfNull: false,
  )


  final int matrixWidth;



  @JsonKey(
    
    name: r'matrixHeight',
    required: true,
    includeIfNull: false,
  )


  final int matrixHeight;



  @JsonKey(
    
    name: r'cornerOfOrigin',
    required: false,
    includeIfNull: false,
  )


  final String? cornerOfOrigin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcTileMatrixSetTileMatricesInner &&
      other.id == id &&
      other.scaleDenominator == scaleDenominator &&
      other.cellSize == cellSize &&
      other.pointOfOrigin == pointOfOrigin &&
      other.tileWidth == tileWidth &&
      other.tileHeight == tileHeight &&
      other.matrixWidth == matrixWidth &&
      other.matrixHeight == matrixHeight &&
      other.cornerOfOrigin == cornerOfOrigin;

    @override
    int get hashCode =>
        id.hashCode +
        scaleDenominator.hashCode +
        cellSize.hashCode +
        pointOfOrigin.hashCode +
        tileWidth.hashCode +
        tileHeight.hashCode +
        matrixWidth.hashCode +
        matrixHeight.hashCode +
        cornerOfOrigin.hashCode;

  factory OgcTileMatrixSetTileMatricesInner.fromJson(Map<String, dynamic> json) => _$OgcTileMatrixSetTileMatricesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$OgcTileMatrixSetTileMatricesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

