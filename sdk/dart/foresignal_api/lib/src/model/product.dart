//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/product_links.dart';
import 'package:foresignal_api/src/model/external_catalogue_entry.dart';
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/product_spatial.dart';
import 'package:foresignal_api/src/model/product_render.dart';
import 'package:foresignal_api/src/model/product_temporal.dart';
import 'package:foresignal_api/src/model/product_legend.dart';
import 'package:foresignal_api/src/model/product_status.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'product.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Product {
  /// Returns a new [Product] instance.
  Product({

    required  this.id,

    required  this.title,

    required  this.kind,

    required  this.category,

    required  this.source_,

     this.dynamic_,

     this.units,

     this.dtype,

     this.coverage,

     this.coverageMeaning,

     this.codes,

     this.temporal,

     this.spatial,

     this.render,

     this.legend,

     this.attribution,

     this.status,

     this.external_,

     this.links,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



      /// raster: rendered tiles; field: model vector/scalar field (also tiles); external: client-direct catalogue entry
  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final ProductKindEnum kind;



  @JsonKey(
    
    name: r'category',
    required: true,
    includeIfNull: false,
  )


  final ProductCategoryEnum category;



      /// Owning source id
  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final String source_;



      /// Registered at runtime (SatDump products)
  @JsonKey(
    
    name: r'dynamic',
    required: false,
    includeIfNull: false,
  )


  final bool? dynamic_;



  @JsonKey(
    
    name: r'units',
    required: false,
    includeIfNull: false,
  )


  final String? units;



      /// Stored sample type
  @JsonKey(
    
    name: r'dtype',
    required: false,
    includeIfNull: false,
  )


  final ProductDtypeEnum? dtype;



  @JsonKey(
    
    name: r'coverage',
    required: false,
    includeIfNull: false,
  )


  final ProductCoverageEnum? coverage;



      /// One-line meaning of the coverage
  @JsonKey(
    
    name: r'coverageMeaning',
    required: false,
    includeIfNull: false,
  )


  final String? coverageMeaning;



      /// Categorical and special raw codes → meaning, e.g. {\"0\": \"noCoverage\", \"1\": \"noEcho\"}
  @JsonKey(
    
    name: r'codes',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? codes;



  @JsonKey(
    
    name: r'temporal',
    required: false,
    includeIfNull: false,
  )


  final ProductTemporal? temporal;



  @JsonKey(
    
    name: r'spatial',
    required: false,
    includeIfNull: false,
  )


  final ProductSpatial? spatial;



  @JsonKey(
    
    name: r'render',
    required: false,
    includeIfNull: false,
  )


  final ProductRender? render;



  @JsonKey(
    
    name: r'legend',
    required: false,
    includeIfNull: false,
  )


  final ProductLegend? legend;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final Attribution? attribution;



  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final ProductStatus? status;



  @JsonKey(
    
    name: r'external',
    required: false,
    includeIfNull: false,
  )


  final ExternalCatalogueEntry? external_;



  @JsonKey(
    
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final ProductLinks? links;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Product &&
      other.id == id &&
      other.title == title &&
      other.kind == kind &&
      other.category == category &&
      other.source_ == source_ &&
      other.dynamic_ == dynamic_ &&
      other.units == units &&
      other.dtype == dtype &&
      other.coverage == coverage &&
      other.coverageMeaning == coverageMeaning &&
      other.codes == codes &&
      other.temporal == temporal &&
      other.spatial == spatial &&
      other.render == render &&
      other.legend == legend &&
      other.attribution == attribution &&
      other.status == status &&
      other.external_ == external_ &&
      other.links == links;

    @override
    int get hashCode =>
        id.hashCode +
        title.hashCode +
        kind.hashCode +
        category.hashCode +
        source_.hashCode +
        dynamic_.hashCode +
        (units == null ? 0 : units.hashCode) +
        (dtype == null ? 0 : dtype.hashCode) +
        coverage.hashCode +
        coverageMeaning.hashCode +
        codes.hashCode +
        temporal.hashCode +
        spatial.hashCode +
        render.hashCode +
        legend.hashCode +
        attribution.hashCode +
        status.hashCode +
        (external_ == null ? 0 : external_.hashCode) +
        links.hashCode;

  factory Product.fromJson(Map<String, dynamic> json) => _$ProductFromJson(json);

  Map<String, dynamic> toJson() => _$ProductToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// raster: rendered tiles; field: model vector/scalar field (also tiles); external: client-direct catalogue entry
enum ProductKindEnum {
@JsonValue(r'raster')
raster(r'raster'),
@JsonValue(r'field')
field(r'field'),
@JsonValue(r'external')
external_(r'external');

const ProductKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ProductCategoryEnum {
@JsonValue(r'radar')
radar(r'radar'),
@JsonValue(r'satellite')
satellite(r'satellite'),
@JsonValue(r'model')
model(r'model'),
@JsonValue(r'lightning')
lightning(r'lightning'),
@JsonValue(r'imagery')
imagery(r'imagery');

const ProductCategoryEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Stored sample type
enum ProductDtypeEnum {
@JsonValue(r'u8')
u8(r'u8'),
@JsonValue(r'u16')
u16(r'u16'),
@JsonValue(r'i16')
i16(r'i16'),
@JsonValue(r'f32')
f32(r'f32'),
@JsonValue(r'rgba8')
rgba8(r'rgba8');

const ProductDtypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ProductCoverageEnum {
@JsonValue(r'CONUS')
CONUS(r'CONUS'),
@JsonValue(r'global')
global(r'global'),
@JsonValue(r'station-disc')
stationDisc(r'station-disc'),
@JsonValue(r'swath')
swath(r'swath');

const ProductCoverageEnum(this.value);

final String value;

@override
String toString() => value;
}


