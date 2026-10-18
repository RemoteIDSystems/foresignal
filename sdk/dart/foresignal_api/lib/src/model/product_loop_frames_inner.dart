//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'product_loop_frames_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProductLoopFramesInner {
  /// Returns a new [ProductLoopFramesInner] instance.
  ProductLoopFramesInner({

    required  this.productId,

    required  this.time,

     this.timeKey,

    required  this.frameKey,

    required  this.kind,

     this.run,

     this.step,

    required  this.url,

     this.bytes,

     this.expiresAt,
  });

  @JsonKey(
    
    name: r'productId',
    required: true,
    includeIfNull: false,
  )


  final String productId;



  @JsonKey(
    
    name: r'time',
    required: true,
    includeIfNull: false,
  )


  final DateTime time;



  @JsonKey(
    
    name: r'timeKey',
    required: false,
    includeIfNull: false,
  )


  final String? timeKey;



  @JsonKey(
    
    name: r'frameKey',
    required: true,
    includeIfNull: false,
  )


  final String frameKey;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final ProductLoopFramesInnerKindEnum kind;



  @JsonKey(
    
    name: r'run',
    required: false,
    includeIfNull: false,
  )


  final String? run;



  @JsonKey(
    
    name: r'step',
    required: false,
    includeIfNull: false,
  )


  final int? step;



      /// Expanded `/image` URL for this frame
  @JsonKey(
    
    name: r'url',
    required: true,
    includeIfNull: false,
  )


  final String url;



      /// Image size when already rendered
  @JsonKey(
    
    name: r'bytes',
    required: false,
    includeIfNull: false,
  )


  final int? bytes;



  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProductLoopFramesInner &&
      other.productId == productId &&
      other.time == time &&
      other.timeKey == timeKey &&
      other.frameKey == frameKey &&
      other.kind == kind &&
      other.run == run &&
      other.step == step &&
      other.url == url &&
      other.bytes == bytes &&
      other.expiresAt == expiresAt;

    @override
    int get hashCode =>
        productId.hashCode +
        time.hashCode +
        timeKey.hashCode +
        frameKey.hashCode +
        kind.hashCode +
        (run == null ? 0 : run.hashCode) +
        (step == null ? 0 : step.hashCode) +
        url.hashCode +
        (bytes == null ? 0 : bytes.hashCode) +
        (expiresAt == null ? 0 : expiresAt.hashCode);

  factory ProductLoopFramesInner.fromJson(Map<String, dynamic> json) => _$ProductLoopFramesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ProductLoopFramesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ProductLoopFramesInnerKindEnum {
@JsonValue(r'observed')
observed(r'observed'),
@JsonValue(r'forecast')
forecast(r'forecast');

const ProductLoopFramesInnerKindEnum(this.value);

final String value;

@override
String toString() => value;
}


