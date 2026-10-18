//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'product_links.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProductLinks {
  /// Returns a new [ProductLinks] instance.
  ProductLinks({

     this.self,

     this.frames,

     this.latest,

     this.tilejson,

     this.legend,

     this.timeline,
  });

  @JsonKey(
    
    name: r'self',
    required: false,
    includeIfNull: false,
  )


  final String? self;



  @JsonKey(
    
    name: r'frames',
    required: false,
    includeIfNull: false,
  )


  final String? frames;



  @JsonKey(
    
    name: r'latest',
    required: false,
    includeIfNull: false,
  )


  final String? latest;



  @JsonKey(
    
    name: r'tilejson',
    required: false,
    includeIfNull: false,
  )


  final String? tilejson;



  @JsonKey(
    
    name: r'legend',
    required: false,
    includeIfNull: false,
  )


  final String? legend;



  @JsonKey(
    
    name: r'timeline',
    required: false,
    includeIfNull: false,
  )


  final String? timeline;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProductLinks &&
      other.self == self &&
      other.frames == frames &&
      other.latest == latest &&
      other.tilejson == tilejson &&
      other.legend == legend &&
      other.timeline == timeline;

    @override
    int get hashCode =>
        self.hashCode +
        frames.hashCode +
        latest.hashCode +
        tilejson.hashCode +
        legend.hashCode +
        timeline.hashCode;

  factory ProductLinks.fromJson(Map<String, dynamic> json) => _$ProductLinksFromJson(json);

  Map<String, dynamic> toJson() => _$ProductLinksToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

