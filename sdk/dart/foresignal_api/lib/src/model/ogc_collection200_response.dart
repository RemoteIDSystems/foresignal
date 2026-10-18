//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_link.dart';
import 'package:foresignal_api/src/model/ogc_coverage_collection_data_queries.dart';
import 'package:foresignal_api/src/model/ogc_coverage_collection.dart';
import 'package:foresignal_api/src/model/ogc_collection.dart';
import 'package:foresignal_api/src/model/ogc_coverage_collection_extent.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_collection200_response.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcCollection200Response {
  /// Returns a new [OgcCollection200Response] instance.
  OgcCollection200Response({

    required  this.id,

     this.title,

     this.description,

    required  this.itemType,

     this.crs,

     this.attribution,

     this.extent,

    required  this.links,

     this.dataQueries,

     this.parameterNames,

     this.outputFormats,
  });

      /// Product id
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


  final OgcCollection200ResponseItemTypeEnum itemType;



  @JsonKey(
    
    name: r'crs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? crs;



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


  final OgcCoverageCollectionExtent? extent;



  @JsonKey(
    
    name: r'links',
    required: true,
    includeIfNull: false,
  )


  final List<OgcLink> links;



  @JsonKey(
    
    name: r'data_queries',
    required: false,
    includeIfNull: false,
  )


  final OgcCoverageCollectionDataQueries? dataQueries;



      /// EDR parameters with units and nodata
  @JsonKey(
    
    name: r'parameter_names',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Map<String, Object>>? parameterNames;



  @JsonKey(
    
    name: r'output_formats',
    required: false,
    includeIfNull: false,
  )


  final List<String>? outputFormats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcCollection200Response &&
      other.id == id &&
      other.title == title &&
      other.description == description &&
      other.itemType == itemType &&
      other.crs == crs &&
      other.attribution == attribution &&
      other.extent == extent &&
      other.links == links &&
      other.dataQueries == dataQueries &&
      other.parameterNames == parameterNames &&
      other.outputFormats == outputFormats;

    @override
    int get hashCode =>
        id.hashCode +
        title.hashCode +
        (description == null ? 0 : description.hashCode) +
        itemType.hashCode +
        crs.hashCode +
        (attribution == null ? 0 : attribution.hashCode) +
        extent.hashCode +
        links.hashCode +
        dataQueries.hashCode +
        parameterNames.hashCode +
        outputFormats.hashCode;

  factory OgcCollection200Response.fromJson(Map<String, dynamic> json) => _$OgcCollection200ResponseFromJson(json);

  Map<String, dynamic> toJson() => _$OgcCollection200ResponseToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OgcCollection200ResponseItemTypeEnum {
@JsonValue(r'coverage')
coverage(r'coverage');

const OgcCollection200ResponseItemTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


