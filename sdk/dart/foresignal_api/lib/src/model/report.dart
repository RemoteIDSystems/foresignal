//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/report_block.dart';
import 'package:foresignal_api/src/model/provenance_chain_entry.dart';
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/report_source.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Report {
  /// Returns a new [Report] instance.
  Report({

    required  this.id,

    required  this.ref,

    required  this.displayId,

    required  this.title,

     this.subtitle,

    required  this.status,

    required  this.owner,

    required  this.authors,

    required  this.tlp,

    required  this.visibility,

    required  this.source_,

     this.template,

     this.timeWindow,

     this.area,

    required  this.blocks,

     this.machineGenerated,

    required  this.version,

     this.publishedRevision,

     this.publishedAt,

     this.manifestId,

     this.provenanceChain,

     this.tags,

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
    
    name: r'displayId',
    required: true,
    includeIfNull: false,
  )


  final String displayId;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'subtitle',
    required: false,
    includeIfNull: false,
  )


  final String? subtitle;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ReportStatusEnum status;



  @JsonKey(
    
    name: r'owner',
    required: true,
    includeIfNull: false,
  )


  final PrincipalSummary owner;



  @JsonKey(
    
    name: r'authors',
    required: true,
    includeIfNull: false,
  )


  final List<PrincipalSummary> authors;



      /// At least the strictest TLP of its inputs
  @JsonKey(
    
    name: r'tlp',
    required: true,
    includeIfNull: false,
  )


  final Tlp tlp;



  @JsonKey(
    
    name: r'visibility',
    required: true,
    includeIfNull: false,
  )


  final Visibility visibility;



  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final ReportSource source_;



  @JsonKey(
    
    name: r'template',
    required: false,
    includeIfNull: false,
  )


  final ReportTemplateEnum? template;



  @JsonKey(
    
    name: r'timeWindow',
    required: false,
    includeIfNull: false,
  )


  final TimeRange? timeWindow;



  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final Geometry? area;



      /// Ordered blocks (stored as `sections`)
  @JsonKey(
    
    name: r'blocks',
    required: true,
    includeIfNull: false,
  )


  final List<ReportBlock> blocks;



      /// Any block is deterministic-generated or LLM
  @JsonKey(
    
    name: r'machineGenerated',
    required: false,
    includeIfNull: false,
  )


  final bool? machineGenerated;



      /// Optimistic-lock version of the draft
  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'publishedRevision',
    required: false,
    includeIfNull: false,
  )


  final int? publishedRevision;



  @JsonKey(
    
    name: r'publishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? publishedAt;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



      /// 134 circular-reporting chain
  @JsonKey(
    
    name: r'provenanceChain',
    required: false,
    includeIfNull: false,
  )


  final List<ProvenanceChainEntry>? provenanceChain;



  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



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
    bool operator ==(Object other) => identical(this, other) || other is Report &&
      other.id == id &&
      other.ref == ref &&
      other.displayId == displayId &&
      other.title == title &&
      other.subtitle == subtitle &&
      other.status == status &&
      other.owner == owner &&
      other.authors == authors &&
      other.tlp == tlp &&
      other.visibility == visibility &&
      other.source_ == source_ &&
      other.template == template &&
      other.timeWindow == timeWindow &&
      other.area == area &&
      other.blocks == blocks &&
      other.machineGenerated == machineGenerated &&
      other.version == version &&
      other.publishedRevision == publishedRevision &&
      other.publishedAt == publishedAt &&
      other.manifestId == manifestId &&
      other.provenanceChain == provenanceChain &&
      other.tags == tags &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        ref.hashCode +
        displayId.hashCode +
        title.hashCode +
        (subtitle == null ? 0 : subtitle.hashCode) +
        status.hashCode +
        owner.hashCode +
        authors.hashCode +
        tlp.hashCode +
        visibility.hashCode +
        source_.hashCode +
        template.hashCode +
        (timeWindow == null ? 0 : timeWindow.hashCode) +
        (area == null ? 0 : area.hashCode) +
        blocks.hashCode +
        machineGenerated.hashCode +
        version.hashCode +
        (publishedRevision == null ? 0 : publishedRevision.hashCode) +
        (publishedAt == null ? 0 : publishedAt.hashCode) +
        (manifestId == null ? 0 : manifestId.hashCode) +
        provenanceChain.hashCode +
        tags.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory Report.fromJson(Map<String, dynamic> json) => _$ReportFromJson(json);

  Map<String, dynamic> toJson() => _$ReportToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReportStatusEnum {
@JsonValue(r'draft')
draft(r'draft'),
@JsonValue(r'review')
review(r'review'),
@JsonValue(r'published')
published(r'published'),
@JsonValue(r'retracted')
retracted(r'retracted');

const ReportStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ReportTemplateEnum {
@JsonValue(r'situation-brief')
situationBrief(r'situation-brief'),
@JsonValue(r'investigation-summary')
investigationSummary(r'investigation-summary'),
@JsonValue(r'chronicle-recap')
chronicleRecap(r'chronicle-recap'),
@JsonValue(r'weekly-assessment')
weeklyAssessment(r'weekly-assessment'),
@JsonValue(r'custom')
custom(r'custom');

const ReportTemplateEnum(this.value);

final String value;

@override
String toString() => value;
}


