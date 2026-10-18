//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/dossier_sections_overview_facets.dart';
import 'package:foresignal_api/src/model/dossier_sections_overview_links.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dossier_sections_overview.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DossierSectionsOverview {
  /// Returns a new [DossierSectionsOverview] instance.
  DossierSectionsOverview({

    required  this.status,

     this.message,

     this.links,

     this.facets,

     this.truncated,

     this.fields,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final DossierSectionsOverviewStatusEnum status;



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
    
    name: r'fields',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? fields;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DossierSectionsOverview &&
      other.status == status &&
      other.message == message &&
      other.links == links &&
      other.facets == facets &&
      other.truncated == truncated &&
      other.fields == fields;

    @override
    int get hashCode =>
        status.hashCode +
        (message == null ? 0 : message.hashCode) +
        (links == null ? 0 : links.hashCode) +
        (facets == null ? 0 : facets.hashCode) +
        truncated.hashCode +
        fields.hashCode;

  factory DossierSectionsOverview.fromJson(Map<String, dynamic> json) => _$DossierSectionsOverviewFromJson(json);

  Map<String, dynamic> toJson() => _$DossierSectionsOverviewToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DossierSectionsOverviewStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'empty')
empty(r'empty'),
@JsonValue(r'unavailable')
unavailable(r'unavailable'),
@JsonValue(r'not-installed')
notInstalled(r'not-installed');

const DossierSectionsOverviewStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


