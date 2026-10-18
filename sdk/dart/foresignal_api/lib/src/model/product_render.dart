//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'product_render.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProductRender {
  /// Returns a new [ProductRender] instance.
  ProductRender({

    required  this.defaultStyle,

    required  this.styles,

     this.defaultOpacity,
  });

      /// Versioned default style
  @JsonKey(
    
    name: r'defaultStyle',
    required: true,
    includeIfNull: false,
  )


  final String defaultStyle;



      /// Versioned styles valid for this product
  @JsonKey(
    
    name: r'styles',
    required: true,
    includeIfNull: false,
  )


  final List<String> styles;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'defaultOpacity',
    required: false,
    includeIfNull: false,
  )


  final num? defaultOpacity;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProductRender &&
      other.defaultStyle == defaultStyle &&
      other.styles == styles &&
      other.defaultOpacity == defaultOpacity;

    @override
    int get hashCode =>
        defaultStyle.hashCode +
        styles.hashCode +
        defaultOpacity.hashCode;

  factory ProductRender.fromJson(Map<String, dynamic> json) => _$ProductRenderFromJson(json);

  Map<String, dynamic> toJson() => _$ProductRenderToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

