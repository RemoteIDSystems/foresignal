//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_tile_matrix_set_list_tile_matrix_sets_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_tile_matrix_set_list.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcTileMatrixSetList {
  /// Returns a new [OgcTileMatrixSetList] instance.
  OgcTileMatrixSetList({

    required  this.tileMatrixSets,
  });

  @JsonKey(
    
    name: r'tileMatrixSets',
    required: true,
    includeIfNull: false,
  )


  final List<OgcTileMatrixSetListTileMatrixSetsInner> tileMatrixSets;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcTileMatrixSetList &&
      other.tileMatrixSets == tileMatrixSets;

    @override
    int get hashCode =>
        tileMatrixSets.hashCode;

  factory OgcTileMatrixSetList.fromJson(Map<String, dynamic> json) => _$OgcTileMatrixSetListFromJson(json);

  Map<String, dynamic> toJson() => _$OgcTileMatrixSetListToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

