//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_link.dart';
import 'package:foresignal_api/src/model/ogc_collection200_response.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_collections.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcCollections {
  /// Returns a new [OgcCollections] instance.
  OgcCollections({

    required  this.links,

    required  this.collections,
  });

  @JsonKey(
    
    name: r'links',
    required: true,
    includeIfNull: false,
  )


  final List<OgcLink> links;



  @JsonKey(
    
    name: r'collections',
    required: true,
    includeIfNull: false,
  )


  final List<OgcCollection200Response> collections;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcCollections &&
      other.links == links &&
      other.collections == collections;

    @override
    int get hashCode =>
        links.hashCode +
        collections.hashCode;

  factory OgcCollections.fromJson(Map<String, dynamic> json) => _$OgcCollectionsFromJson(json);

  Map<String, dynamic> toJson() => _$OgcCollectionsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

