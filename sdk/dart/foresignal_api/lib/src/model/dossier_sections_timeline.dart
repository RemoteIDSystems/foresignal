//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/dossier_sections_timeline_items_inner.dart';
import 'package:foresignal_api/src/model/dossier_sections_overview_facets.dart';
import 'package:foresignal_api/src/model/dossier_sections_overview_links.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dossier_sections_timeline.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DossierSectionsTimeline {
  /// Returns a new [DossierSectionsTimeline] instance.
  DossierSectionsTimeline({

    required  this.status,

     this.message,

     this.links,

     this.facets,

     this.truncated,

     this.items,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final DossierSectionsTimelineStatusEnum status;



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
    
    name: r'items',
    required: false,
    includeIfNull: false,
  )


  final List<DossierSectionsTimelineItemsInner>? items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DossierSectionsTimeline &&
      other.status == status &&
      other.message == message &&
      other.links == links &&
      other.facets == facets &&
      other.truncated == truncated &&
      other.items == items;

    @override
    int get hashCode =>
        status.hashCode +
        (message == null ? 0 : message.hashCode) +
        (links == null ? 0 : links.hashCode) +
        (facets == null ? 0 : facets.hashCode) +
        truncated.hashCode +
        items.hashCode;

  factory DossierSectionsTimeline.fromJson(Map<String, dynamic> json) => _$DossierSectionsTimelineFromJson(json);

  Map<String, dynamic> toJson() => _$DossierSectionsTimelineToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DossierSectionsTimelineStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'empty')
empty(r'empty'),
@JsonValue(r'unavailable')
unavailable(r'unavailable'),
@JsonValue(r'not-installed')
notInstalled(r'not-installed');

const DossierSectionsTimelineStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


