//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_tile_set_tile_matrix_set_limits_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcTileSetTileMatrixSetLimitsInner {
  /// Returns a new [OgcTileSetTileMatrixSetLimitsInner] instance.
  OgcTileSetTileMatrixSetLimitsInner({

     this.tileMatrix,

     this.minTileRow,

     this.maxTileRow,

     this.minTileCol,

     this.maxTileCol,
  });

  @JsonKey(
    
    name: r'tileMatrix',
    required: false,
    includeIfNull: false,
  )


  final String? tileMatrix;



  @JsonKey(
    
    name: r'minTileRow',
    required: false,
    includeIfNull: false,
  )


  final int? minTileRow;



  @JsonKey(
    
    name: r'maxTileRow',
    required: false,
    includeIfNull: false,
  )


  final int? maxTileRow;



  @JsonKey(
    
    name: r'minTileCol',
    required: false,
    includeIfNull: false,
  )


  final int? minTileCol;



  @JsonKey(
    
    name: r'maxTileCol',
    required: false,
    includeIfNull: false,
  )


  final int? maxTileCol;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcTileSetTileMatrixSetLimitsInner &&
      other.tileMatrix == tileMatrix &&
      other.minTileRow == minTileRow &&
      other.maxTileRow == maxTileRow &&
      other.minTileCol == minTileCol &&
      other.maxTileCol == maxTileCol;

    @override
    int get hashCode =>
        tileMatrix.hashCode +
        minTileRow.hashCode +
        maxTileRow.hashCode +
        minTileCol.hashCode +
        maxTileCol.hashCode;

  factory OgcTileSetTileMatrixSetLimitsInner.fromJson(Map<String, dynamic> json) => _$OgcTileSetTileMatrixSetLimitsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$OgcTileSetTileMatrixSetLimitsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

