//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_layer_selector_one_of7.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleLayerSelectorOneOf7 {
  /// Returns a new [ChronicleLayerSelectorOneOf7] instance.
  ChronicleLayerSelectorOneOf7({

    required  this.type,

    required  this.productIds,
  });

  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ChronicleLayerSelectorOneOf7TypeEnum type;



  @JsonKey(
    
    name: r'productIds',
    required: true,
    includeIfNull: false,
  )


  final List<String> productIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleLayerSelectorOneOf7 &&
      other.type == type &&
      other.productIds == productIds;

    @override
    int get hashCode =>
        type.hashCode +
        productIds.hashCode;

  factory ChronicleLayerSelectorOneOf7.fromJson(Map<String, dynamic> json) => _$ChronicleLayerSelectorOneOf7FromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleLayerSelectorOneOf7ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleLayerSelectorOneOf7TypeEnum {
@JsonValue(r'raster-products')
rasterProducts(r'raster-products');

const ChronicleLayerSelectorOneOf7TypeEnum(this.value);

final String value;

@override
String toString() => value;
}


