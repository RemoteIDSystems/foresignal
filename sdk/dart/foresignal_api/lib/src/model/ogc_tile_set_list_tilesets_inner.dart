//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_link.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_tile_set_list_tilesets_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcTileSetListTilesetsInner {
  /// Returns a new [OgcTileSetListTilesetsInner] instance.
  OgcTileSetListTilesetsInner({

     this.title,

    required  this.dataType,

     this.crs,

     this.tileMatrixSetURI,

    required  this.links,
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


  final OgcTileSetListTilesetsInnerDataTypeEnum dataType;



  @JsonKey(
    
    name: r'crs',
    required: false,
    includeIfNull: false,
  )


  final String? crs;



  @JsonKey(
    
    name: r'tileMatrixSetURI',
    required: false,
    includeIfNull: false,
  )


  final String? tileMatrixSetURI;



  @JsonKey(
    
    name: r'links',
    required: true,
    includeIfNull: false,
  )


  final List<OgcLink> links;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcTileSetListTilesetsInner &&
      other.title == title &&
      other.dataType == dataType &&
      other.crs == crs &&
      other.tileMatrixSetURI == tileMatrixSetURI &&
      other.links == links;

    @override
    int get hashCode =>
        title.hashCode +
        dataType.hashCode +
        crs.hashCode +
        tileMatrixSetURI.hashCode +
        links.hashCode;

  factory OgcTileSetListTilesetsInner.fromJson(Map<String, dynamic> json) => _$OgcTileSetListTilesetsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$OgcTileSetListTilesetsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OgcTileSetListTilesetsInnerDataTypeEnum {
@JsonValue(r'map')
map(r'map');

const OgcTileSetListTilesetsInnerDataTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


