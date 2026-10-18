//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/product_loop_frames_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'product_loop.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProductLoop {
  /// Returns a new [ProductLoop] instance.
  ProductLoop({

    required  this.crs,

    required  this.bounds,

    required  this.coordinates,

    required  this.width,

    required  this.height,

     this.products,

     this.runs,

    required  this.frames,

     this.legendUrl,

     this.attribution,

    required  this.serverTime,
  });

  @JsonKey(
    
    name: r'crs',
    required: true,
    includeIfNull: false,
  )


  final ProductLoopCrsEnum crs;



      /// Snapped bounds [w, s, e, n]
  @JsonKey(
    
    name: r'bounds',
    required: true,
    includeIfNull: false,
  )


  final List<num> bounds;



      /// Corner coordinates [[tl], [tr], [br], [bl]] as [lon, lat] for image overlays
  @JsonKey(
    
    name: r'coordinates',
    required: true,
    includeIfNull: false,
  )


  final List<List<num>> coordinates;



  @JsonKey(
    
    name: r'width',
    required: true,
    includeIfNull: false,
  )


  final int width;



  @JsonKey(
    
    name: r'height',
    required: true,
    includeIfNull: false,
  )


  final int height;



  @JsonKey(
    
    name: r'products',
    required: false,
    includeIfNull: false,
  )


  final List<String>? products;



      /// Forecast run used per product (never mixed)
  @JsonKey(
    
    name: r'runs',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? runs;



  @JsonKey(
    
    name: r'frames',
    required: true,
    includeIfNull: false,
  )


  final List<ProductLoopFramesInner> frames;



  @JsonKey(
    
    name: r'legendUrl',
    required: false,
    includeIfNull: false,
  )


  final String? legendUrl;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final List<Attribution>? attribution;



  @JsonKey(
    
    name: r'serverTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime serverTime;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProductLoop &&
      other.crs == crs &&
      other.bounds == bounds &&
      other.coordinates == coordinates &&
      other.width == width &&
      other.height == height &&
      other.products == products &&
      other.runs == runs &&
      other.frames == frames &&
      other.legendUrl == legendUrl &&
      other.attribution == attribution &&
      other.serverTime == serverTime;

    @override
    int get hashCode =>
        crs.hashCode +
        bounds.hashCode +
        coordinates.hashCode +
        width.hashCode +
        height.hashCode +
        products.hashCode +
        runs.hashCode +
        frames.hashCode +
        (legendUrl == null ? 0 : legendUrl.hashCode) +
        attribution.hashCode +
        serverTime.hashCode;

  factory ProductLoop.fromJson(Map<String, dynamic> json) => _$ProductLoopFromJson(json);

  Map<String, dynamic> toJson() => _$ProductLoopToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ProductLoopCrsEnum {
@JsonValue(r'EPSG:3857')
ePSGColon3857(r'EPSG:3857');

const ProductLoopCrsEnum(this.value);

final String value;

@override
String toString() => value;
}


