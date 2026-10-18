//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/tile_json_vector_layers_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tile_json.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TileJson {
  /// Returns a new [TileJson] instance.
  TileJson({

    required  this.tilejson,

     this.name,

     this.description,

     this.version,

     this.attribution,

     this.scheme,

    required  this.tiles,

     this.minzoom,

     this.maxzoom,

     this.bounds,

     this.vectorLayers,

     this.foresignalColonAggBelowZoom,

     this.foresignalColonProperties,

     this.tileSize,

     this.foresignalColonTiles2x,

     this.foresignalColonFrameKey,

     this.foresignalColonStyle,

     this.foresignalColonRun,

     this.foresignalColonExpiresAt,

     this.tms,

     this.encoding,

     this.redFactor,

     this.greenFactor,

     this.blueFactor,

     this.baseShift,

     this.units,

     this.nodata,
  });

  @JsonKey(
    
    name: r'tilejson',
    required: true,
    includeIfNull: false,
  )


  final TileJsonTilejsonEnum tilejson;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



      /// Foresignal version
  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final String? version;



      /// Distinct descriptor attributions joined with \" · \"
  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final String? attribution;



  @JsonKey(
    
    name: r'scheme',
    required: false,
    includeIfNull: false,
  )


  final TileJsonSchemeEnum? scheme;



      /// Tile URL templates
  @JsonKey(
    
    name: r'tiles',
    required: true,
    includeIfNull: false,
  )


  final List<String> tiles;



          // minimum: 0
          // maximum: 16
  @JsonKey(
    
    name: r'minzoom',
    required: false,
    includeIfNull: false,
  )


  final int? minzoom;



          // minimum: 0
          // maximum: 16
  @JsonKey(
    
    name: r'maxzoom',
    required: false,
    includeIfNull: false,
  )


  final int? maxzoom;



  @JsonKey(
    
    name: r'bounds',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bounds;



  @JsonKey(
    
    name: r'vector_layers',
    required: false,
    includeIfNull: false,
  )


  final List<TileJsonVectorLayersInner>? vectorLayers;



          // minimum: 0
          // maximum: 22
  @JsonKey(
    
    name: r'foresignal:aggBelowZoom',
    required: false,
    includeIfNull: false,
  )


  final int? foresignalColonAggBelowZoom;



      /// Feature property names
  @JsonKey(
    
    name: r'foresignal:properties',
    required: false,
    includeIfNull: false,
  )


  final List<String>? foresignalColonProperties;



      /// Raster products: 256
  @JsonKey(
    
    name: r'tileSize',
    required: false,
    includeIfNull: false,
  )


  final TileJsonTileSizeEnum? tileSize;



      /// Raster products: the `@2x` (512 px) template
  @JsonKey(
    
    name: r'foresignal:tiles2x',
    required: false,
    includeIfNull: false,
  )


  final List<String>? foresignalColonTiles2x;



      /// Raster products: resolved content-addressed frame key
  @JsonKey(
    
    name: r'foresignal:frameKey',
    required: false,
    includeIfNull: false,
  )


  final String? foresignalColonFrameKey;



      /// Raster products: versioned style `name@v`
  @JsonKey(
    
    name: r'foresignal:style',
    required: false,
    includeIfNull: false,
  )


  final String? foresignalColonStyle;



      /// Raster products: pinned forecast run
  @JsonKey(
    
    name: r'foresignal:run',
    required: false,
    includeIfNull: false,
  )


  final String? foresignalColonRun;



  @JsonKey(
    
    name: r'foresignal:expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? foresignalColonExpiresAt;



  @JsonKey(
    
    name: r'tms',
    required: false,
    includeIfNull: false,
  )


  final TileJsonTmsEnum? tms;



      /// Data tiles only
  @JsonKey(
    
    name: r'encoding',
    required: false,
    includeIfNull: false,
  )


  final TileJsonEncodingEnum? encoding;



  @JsonKey(
    
    name: r'redFactor',
    required: false,
    includeIfNull: false,
  )


  final num? redFactor;



  @JsonKey(
    
    name: r'greenFactor',
    required: false,
    includeIfNull: false,
  )


  final num? greenFactor;



  @JsonKey(
    
    name: r'blueFactor',
    required: false,
    includeIfNull: false,
  )


  final num? blueFactor;



  @JsonKey(
    
    name: r'baseShift',
    required: false,
    includeIfNull: false,
  )


  final num? baseShift;



  @JsonKey(
    
    name: r'units',
    required: false,
    includeIfNull: false,
  )


  final String? units;



  @JsonKey(
    
    name: r'nodata',
    required: false,
    includeIfNull: false,
  )


  final num? nodata;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TileJson &&
      other.tilejson == tilejson &&
      other.name == name &&
      other.description == description &&
      other.version == version &&
      other.attribution == attribution &&
      other.scheme == scheme &&
      other.tiles == tiles &&
      other.minzoom == minzoom &&
      other.maxzoom == maxzoom &&
      other.bounds == bounds &&
      other.vectorLayers == vectorLayers &&
      other.foresignalColonAggBelowZoom == foresignalColonAggBelowZoom &&
      other.foresignalColonProperties == foresignalColonProperties &&
      other.tileSize == tileSize &&
      other.foresignalColonTiles2x == foresignalColonTiles2x &&
      other.foresignalColonFrameKey == foresignalColonFrameKey &&
      other.foresignalColonStyle == foresignalColonStyle &&
      other.foresignalColonRun == foresignalColonRun &&
      other.foresignalColonExpiresAt == foresignalColonExpiresAt &&
      other.tms == tms &&
      other.encoding == encoding &&
      other.redFactor == redFactor &&
      other.greenFactor == greenFactor &&
      other.blueFactor == blueFactor &&
      other.baseShift == baseShift &&
      other.units == units &&
      other.nodata == nodata;

    @override
    int get hashCode =>
        tilejson.hashCode +
        name.hashCode +
        description.hashCode +
        version.hashCode +
        attribution.hashCode +
        scheme.hashCode +
        tiles.hashCode +
        minzoom.hashCode +
        maxzoom.hashCode +
        bounds.hashCode +
        vectorLayers.hashCode +
        (foresignalColonAggBelowZoom == null ? 0 : foresignalColonAggBelowZoom.hashCode) +
        foresignalColonProperties.hashCode +
        tileSize.hashCode +
        foresignalColonTiles2x.hashCode +
        foresignalColonFrameKey.hashCode +
        foresignalColonStyle.hashCode +
        (foresignalColonRun == null ? 0 : foresignalColonRun.hashCode) +
        (foresignalColonExpiresAt == null ? 0 : foresignalColonExpiresAt.hashCode) +
        tms.hashCode +
        (encoding == null ? 0 : encoding.hashCode) +
        (redFactor == null ? 0 : redFactor.hashCode) +
        (greenFactor == null ? 0 : greenFactor.hashCode) +
        (blueFactor == null ? 0 : blueFactor.hashCode) +
        (baseShift == null ? 0 : baseShift.hashCode) +
        (units == null ? 0 : units.hashCode) +
        (nodata == null ? 0 : nodata.hashCode);

  factory TileJson.fromJson(Map<String, dynamic> json) => _$TileJsonFromJson(json);

  Map<String, dynamic> toJson() => _$TileJsonToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TileJsonTilejsonEnum {
@JsonValue(r'3.0.0')
n3period0Period0(r'3.0.0');

const TileJsonTilejsonEnum(this.value);

final String value;

@override
String toString() => value;
}


enum TileJsonSchemeEnum {
@JsonValue(r'xyz')
xyz(r'xyz');

const TileJsonSchemeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Raster products: 256
enum TileJsonTileSizeEnum {
@JsonValue(256)
number256('256'),
@JsonValue(512)
number512('512');

const TileJsonTileSizeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum TileJsonTmsEnum {
@JsonValue(r'WebMercatorQuad')
webMercatorQuad(r'WebMercatorQuad'),
@JsonValue(r'WorldCRS84Quad')
worldCRS84Quad(r'WorldCRS84Quad');

const TileJsonTmsEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Data tiles only
enum TileJsonEncodingEnum {
@JsonValue(r'custom')
custom(r'custom'),
@JsonValue(r'terrarium')
terrarium(r'terrarium');

const TileJsonEncodingEnum(this.value);

final String value;

@override
String toString() => value;
}


