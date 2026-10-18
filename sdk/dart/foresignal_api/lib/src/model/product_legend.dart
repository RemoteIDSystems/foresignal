//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'product_legend.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProductLegend {
  /// Returns a new [ProductLegend] instance.
  ProductLegend({

     this.summary,

     this.url,

     this.pngUrl,
  });

      /// Short legend text
  @JsonKey(
    
    name: r'summary',
    required: false,
    includeIfNull: false,
  )


  final String? summary;



      /// JSON legend URL
  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



      /// PNG legend URL
  @JsonKey(
    
    name: r'pngUrl',
    required: false,
    includeIfNull: false,
  )


  final String? pngUrl;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProductLegend &&
      other.summary == summary &&
      other.url == url &&
      other.pngUrl == pngUrl;

    @override
    int get hashCode =>
        (summary == null ? 0 : summary.hashCode) +
        url.hashCode +
        pngUrl.hashCode;

  factory ProductLegend.fromJson(Map<String, dynamic> json) => _$ProductLegendFromJson(json);

  Map<String, dynamic> toJson() => _$ProductLegendToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

