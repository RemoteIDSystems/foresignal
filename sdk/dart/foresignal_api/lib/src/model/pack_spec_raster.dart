//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pack_spec_raster.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PackSpecRaster {
  /// Returns a new [PackSpecRaster] instance.
  PackSpecRaster({

    required  this.productId,

     this.frame = 'latest',
  });

  @JsonKey(
    
    name: r'productId',
    required: true,
    includeIfNull: false,
  )


  final String productId;



      /// `latest` or a frame id
  @JsonKey(
    defaultValue: 'latest',
    name: r'frame',
    required: false,
    includeIfNull: false,
  )


  final String? frame;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PackSpecRaster &&
      other.productId == productId &&
      other.frame == frame;

    @override
    int get hashCode =>
        productId.hashCode +
        frame.hashCode;

  factory PackSpecRaster.fromJson(Map<String, dynamic> json) => _$PackSpecRasterFromJson(json);

  Map<String, dynamic> toJson() => _$PackSpecRasterToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

