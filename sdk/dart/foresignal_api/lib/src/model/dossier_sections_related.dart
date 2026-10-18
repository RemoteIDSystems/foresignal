//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/related_object.dart';
import 'package:foresignal_api/src/model/dossier_sections_overview_facets.dart';
import 'package:foresignal_api/src/model/dossier_sections_overview_links.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dossier_sections_related.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DossierSectionsRelated {
  /// Returns a new [DossierSectionsRelated] instance.
  DossierSectionsRelated({

    required  this.status,

     this.message,

     this.links,

     this.facets,

     this.truncated,

     this.nearby,

     this.operational,

     this.infrastructure,

     this.historical,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final DossierSectionsRelatedStatusEnum status;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



  @JsonKey(
    
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final DossierSectionsOverviewLinks? links;



  @JsonKey(
    
    name: r'facets',
    required: false,
    includeIfNull: false,
  )


  final DossierSectionsOverviewFacets? facets;



  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;



  @JsonKey(
    
    name: r'nearby',
    required: false,
    includeIfNull: false,
  )


  final List<RelatedObject>? nearby;



  @JsonKey(
    
    name: r'operational',
    required: false,
    includeIfNull: false,
  )


  final List<RelatedObject>? operational;



  @JsonKey(
    
    name: r'infrastructure',
    required: false,
    includeIfNull: false,
  )


  final List<RelatedObject>? infrastructure;



  @JsonKey(
    
    name: r'historical',
    required: false,
    includeIfNull: false,
  )


  final List<RelatedObject>? historical;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DossierSectionsRelated &&
      other.status == status &&
      other.message == message &&
      other.links == links &&
      other.facets == facets &&
      other.truncated == truncated &&
      other.nearby == nearby &&
      other.operational == operational &&
      other.infrastructure == infrastructure &&
      other.historical == historical;

    @override
    int get hashCode =>
        status.hashCode +
        (message == null ? 0 : message.hashCode) +
        (links == null ? 0 : links.hashCode) +
        (facets == null ? 0 : facets.hashCode) +
        truncated.hashCode +
        nearby.hashCode +
        operational.hashCode +
        infrastructure.hashCode +
        historical.hashCode;

  factory DossierSectionsRelated.fromJson(Map<String, dynamic> json) => _$DossierSectionsRelatedFromJson(json);

  Map<String, dynamic> toJson() => _$DossierSectionsRelatedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DossierSectionsRelatedStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'empty')
empty(r'empty'),
@JsonValue(r'unavailable')
unavailable(r'unavailable'),
@JsonValue(r'not-installed')
notInstalled(r'not-installed');

const DossierSectionsRelatedStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


