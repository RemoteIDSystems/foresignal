//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_link.dart';
import 'package:foresignal_api/src/model/ogc_collection_extent.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_collection.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcCollection {
  /// Returns a new [OgcCollection] instance.
  OgcCollection({

    required  this.id,

     this.title,

     this.description,

    required  this.itemType,

     this.crs,

     this.attribution,

     this.extent,

    required  this.links,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'itemType',
    required: true,
    includeIfNull: false,
  )


  final OgcCollectionItemTypeEnum itemType;



  @JsonKey(
    
    name: r'crs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? crs;



      /// 109 aggregate for the collection's sources
  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final String? attribution;



  @JsonKey(
    
    name: r'extent',
    required: false,
    includeIfNull: false,
  )


  final OgcCollectionExtent? extent;



  @JsonKey(
    
    name: r'links',
    required: true,
    includeIfNull: false,
  )


  final List<OgcLink> links;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcCollection &&
      other.id == id &&
      other.title == title &&
      other.description == description &&
      other.itemType == itemType &&
      other.crs == crs &&
      other.attribution == attribution &&
      other.extent == extent &&
      other.links == links;

    @override
    int get hashCode =>
        id.hashCode +
        title.hashCode +
        (description == null ? 0 : description.hashCode) +
        itemType.hashCode +
        crs.hashCode +
        (attribution == null ? 0 : attribution.hashCode) +
        extent.hashCode +
        links.hashCode;

  factory OgcCollection.fromJson(Map<String, dynamic> json) => _$OgcCollectionFromJson(json);

  Map<String, dynamic> toJson() => _$OgcCollectionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OgcCollectionItemTypeEnum {
@JsonValue(r'feature')
feature(r'feature');

const OgcCollectionItemTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


