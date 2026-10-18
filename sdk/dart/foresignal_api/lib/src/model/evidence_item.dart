//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/provenance_chain_entry.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'evidence_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EvidenceItem {
  /// Returns a new [EvidenceItem] instance.
  EvidenceItem({

    required  this.id,

    required  this.ref,

    required  this.title,

     this.description,

     this.attachmentIds,

     this.url,

     this.sourceDescription,

    required  this.sourceReliability,

    required  this.informationCredibility,

     this.grade,

     this.citedSourceId,

    required  this.originator,

     this.citedRefs,

     this.provenanceChain,

     this.geometry,

     this.eventTime,

     this.tags,

     this.owner,

    required  this.createdBy,

     this.visibility,

    required  this.tlp,

     this.caveats,

    required  this.createdAt,

     this.updatedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



      /// Markdown
  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'attachmentIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? attachmentIds;



      /// Link evidence; stored as metadata only and never fetched
  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'sourceDescription',
    required: false,
    includeIfNull: false,
  )


  final String? sourceDescription;



      /// Admiralty source reliability (A completely reliable … F cannot be judged)
  @JsonKey(
    
    name: r'sourceReliability',
    required: true,
    includeIfNull: false,
  )


  final EvidenceItemSourceReliabilityEnum sourceReliability;



      /// Admiralty information credibility (1 confirmed … 6 cannot be judged)
          // minimum: 1
          // maximum: 6
  @JsonKey(
    
    name: r'informationCredibility',
    required: true,
    includeIfNull: false,
  )


  final int informationCredibility;



  @JsonKey(
    
    name: r'grade',
    required: false,
    includeIfNull: false,
  )


  final String? grade;



      /// Catalogued source this evidence cites; otherwise the originator is `operator:{principal}` for 116 independence
  @JsonKey(
    
    name: r'citedSourceId',
    required: false,
    includeIfNull: false,
  )


  final String? citedSourceId;



  @JsonKey(
    
    name: r'originator',
    required: true,
    includeIfNull: false,
  )


  final String originator;



      /// Objects this evidence cites, e.g. a report
  @JsonKey(
    
    name: r'citedRefs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? citedRefs;



  @JsonKey(
    
    name: r'provenanceChain',
    required: false,
    includeIfNull: false,
  )


  final List<ProvenanceChainEntry>? provenanceChain;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



  @JsonKey(
    
    name: r'eventTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? eventTime;



      /// Tag names visible to the caller
  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



  @JsonKey(
    
    name: r'owner',
    required: false,
    includeIfNull: false,
  )


  final PrincipalSummary? owner;



  @JsonKey(
    
    name: r'createdBy',
    required: true,
    includeIfNull: false,
  )


  final PrincipalSummary createdBy;



  @JsonKey(
    
    name: r'visibility',
    required: false,
    includeIfNull: false,
  )


  final Visibility? visibility;



  @JsonKey(
    
    name: r'tlp',
    required: true,
    includeIfNull: false,
  )


  final Tlp tlp;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EvidenceItem &&
      other.id == id &&
      other.ref == ref &&
      other.title == title &&
      other.description == description &&
      other.attachmentIds == attachmentIds &&
      other.url == url &&
      other.sourceDescription == sourceDescription &&
      other.sourceReliability == sourceReliability &&
      other.informationCredibility == informationCredibility &&
      other.grade == grade &&
      other.citedSourceId == citedSourceId &&
      other.originator == originator &&
      other.citedRefs == citedRefs &&
      other.provenanceChain == provenanceChain &&
      other.geometry == geometry &&
      other.eventTime == eventTime &&
      other.tags == tags &&
      other.owner == owner &&
      other.createdBy == createdBy &&
      other.visibility == visibility &&
      other.tlp == tlp &&
      other.caveats == caveats &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        ref.hashCode +
        title.hashCode +
        (description == null ? 0 : description.hashCode) +
        attachmentIds.hashCode +
        (url == null ? 0 : url.hashCode) +
        (sourceDescription == null ? 0 : sourceDescription.hashCode) +
        sourceReliability.hashCode +
        informationCredibility.hashCode +
        grade.hashCode +
        (citedSourceId == null ? 0 : citedSourceId.hashCode) +
        originator.hashCode +
        citedRefs.hashCode +
        provenanceChain.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        (eventTime == null ? 0 : eventTime.hashCode) +
        tags.hashCode +
        owner.hashCode +
        createdBy.hashCode +
        visibility.hashCode +
        tlp.hashCode +
        caveats.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory EvidenceItem.fromJson(Map<String, dynamic> json) => _$EvidenceItemFromJson(json);

  Map<String, dynamic> toJson() => _$EvidenceItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Admiralty source reliability (A completely reliable … F cannot be judged)
enum EvidenceItemSourceReliabilityEnum {
@JsonValue(r'A')
A(r'A'),
@JsonValue(r'B')
B(r'B'),
@JsonValue(r'C')
C(r'C'),
@JsonValue(r'D')
D(r'D'),
@JsonValue(r'E')
E(r'E'),
@JsonValue(r'F')
F(r'F');

const EvidenceItemSourceReliabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


