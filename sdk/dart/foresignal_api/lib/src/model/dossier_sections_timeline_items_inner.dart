//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dossier_sections_timeline_items_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DossierSectionsTimelineItemsInner {
  /// Returns a new [DossierSectionsTimelineItemsInner] instance.
  DossierSectionsTimelineItemsInner({

    required  this.at,

    required  this.kind,

     this.ref,

    required  this.title,

     this.severity,
  });

  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final DossierSectionsTimelineItemsInnerKindEnum kind;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final String? severity;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DossierSectionsTimelineItemsInner &&
      other.at == at &&
      other.kind == kind &&
      other.ref == ref &&
      other.title == title &&
      other.severity == severity;

    @override
    int get hashCode =>
        at.hashCode +
        kind.hashCode +
        (ref == null ? 0 : ref.hashCode) +
        title.hashCode +
        (severity == null ? 0 : severity.hashCode);

  factory DossierSectionsTimelineItemsInner.fromJson(Map<String, dynamic> json) => _$DossierSectionsTimelineItemsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$DossierSectionsTimelineItemsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DossierSectionsTimelineItemsInnerKindEnum {
@JsonValue(r'version')
version(r'version'),
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'finding')
finding(r'finding'),
@JsonValue(r'situation-log')
situationLog(r'situation-log'),
@JsonValue(r'annotation')
annotation(r'annotation'),
@JsonValue(r'other')
other(r'other');

const DossierSectionsTimelineItemsInnerKindEnum(this.value);

final String value;

@override
String toString() => value;
}


