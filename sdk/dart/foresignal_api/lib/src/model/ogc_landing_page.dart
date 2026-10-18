//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ogc_link.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ogc_landing_page.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OgcLandingPage {
  /// Returns a new [OgcLandingPage] instance.
  OgcLandingPage({

     this.title,

     this.description,

     this.attribution,

    required  this.links,
  });

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
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final String? attribution;



      /// self, conformance, data (/v1/ogc/collections), service-desc (/v1/openapi.json)
  @JsonKey(
    
    name: r'links',
    required: true,
    includeIfNull: false,
  )


  final List<OgcLink> links;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OgcLandingPage &&
      other.title == title &&
      other.description == description &&
      other.attribution == attribution &&
      other.links == links;

    @override
    int get hashCode =>
        title.hashCode +
        description.hashCode +
        (attribution == null ? 0 : attribution.hashCode) +
        links.hashCode;

  factory OgcLandingPage.fromJson(Map<String, dynamic> json) => _$OgcLandingPageFromJson(json);

  Map<String, dynamic> toJson() => _$OgcLandingPageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

