//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_link.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_tile_matrix_set_list_tile_matrix_sets_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcTileMatrixSetListTileMatrixSetsInner {
  /// Returns a new [OgcTileMatrixSetListTileMatrixSetsInner] instance.
  OgcTileMatrixSetListTileMatrixSetsInner({

    required  this.id,

     this.title,

     this.uri,

    required  this.links,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final OgcTileMatrixSetListTileMatrixSetsInnerIdEnum id;



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
    
    name: r'links',
    required: true,
    includeIfNull: false,
  )


  final List<OgcLink> links;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcTileMatrixSetListTileMatrixSetsInner &&
      other.id == id &&
      other.title == title &&
      other.uri == uri &&
      other.links == links;

    @override
    int get hashCode =>
        id.hashCode +
        title.hashCode +
        uri.hashCode +
        links.hashCode;

  factory OgcTileMatrixSetListTileMatrixSetsInner.fromJson(Map<String, dynamic> json) => _$OgcTileMatrixSetListTileMatrixSetsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$OgcTileMatrixSetListTileMatrixSetsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OgcTileMatrixSetListTileMatrixSetsInnerIdEnum {
@JsonValue(r'WebMercatorQuad')
webMercatorQuad(r'WebMercatorQuad'),
@JsonValue(r'WorldCRS84Quad')
worldCRS84Quad(r'WorldCRS84Quad');

const OgcTileMatrixSetListTileMatrixSetsInnerIdEnum(this.value);

final String value;

@override
String toString() => value;
}


