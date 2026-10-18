//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_tile_set_list_tilesets_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_tile_set_list.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcTileSetList {
  /// Returns a new [OgcTileSetList] instance.
  OgcTileSetList({

    required  this.tilesets,
  });

  @JsonKey(
    
    name: r'tilesets',
    required: true,
    includeIfNull: false,
  )


  final List<OgcTileSetListTilesetsInner> tilesets;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcTileSetList &&
      other.tilesets == tilesets;

    @override
    int get hashCode =>
        tilesets.hashCode;

  factory OgcTileSetList.fromJson(Map<String, dynamic> json) => _$OgcTileSetListFromJson(json);

  Map<String, dynamic> toJson() => _$OgcTileSetListToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

