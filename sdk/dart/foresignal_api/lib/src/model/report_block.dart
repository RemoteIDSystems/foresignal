//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/report_block_one_of7.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/report_block_one_of3.dart';
import 'package:foresignal_api/src/model/report_block_one_of6_all_of_questions_inner.dart';
import 'package:foresignal_api/src/model/report_block_one_of10.dart';
import 'package:foresignal_api/src/model/report_block_one_of9.dart';
import 'package:foresignal_api/src/model/coverage_stats.dart';
import 'package:foresignal_api/src/model/report_block_one_of1.dart';
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/report_block_one_of.dart';
import 'package:foresignal_api/src/model/report_block_one_of4.dart';
import 'package:foresignal_api/src/model/report_block_one_of6.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:foresignal_api/src/model/report_block_one_of10_all_of_attributions_inner.dart';
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/report_block_one_of5.dart';
import 'package:foresignal_api/src/model/report_block_one_of2.dart';
import 'package:foresignal_api/src/model/report_block_one_of8.dart';
import 'package:foresignal_api/src/model/report_block_one_of7_all_of_items_inner.dart';
import 'package:foresignal_api/src/model/report_block_one_of1_all_of_judgments_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_block.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBlock {
  /// Returns a new [ReportBlock] instance.
  ReportBlock({

     this.id,

    required  this.type,

     this.title,

     this.textOrigin,

     this.updatedAt,

     this.updatedBy,

    required  this.markdown,

     this.citations,

    required  this.judgments,

    required  this.items,

     this.layers,

     this.area,

     this.bbox,

     this.timeRange,

     this.attachmentId,

     this.mediaId,

     this.caveat,

     this.sourceRef,

    required  this.hypothesisSetRef,

     this.snapshot,

     this.revisionIds,

    required  this.questions,

     this.caveats,

     this.coverage,

    required  this.attachmentIds,

    required  this.attributions,
  });

      /// Server-assigned; comment on a block with `report:{id}#{blockId}`
  @JsonKey(
    
    name: r'id',
    required: false,
    includeIfNull: false,
  )


  final String? id;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ReportBlockTypeEnum type;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



      /// `llm` blocks are labeled narrative from the optional 028 tool loop
  @JsonKey(
    
    name: r'textOrigin',
    required: false,
    includeIfNull: false,
  )


  final ReportBlockTextOriginEnum? textOrigin;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;



  @JsonKey(
    
    name: r'updatedBy',
    required: false,
    includeIfNull: false,
  )


  final PrincipalSummary? updatedBy;



  @JsonKey(
    
    name: r'markdown',
    required: true,
    includeIfNull: false,
  )


  final String markdown;



      /// Verified citations (LLM narrative)
  @JsonKey(
    
    name: r'citations',
    required: false,
    includeIfNull: false,
  )


  final List<String>? citations;



  @JsonKey(
    
    name: r'judgments',
    required: true,
    includeIfNull: false,
  )


  final List<ReportBlockOneOf1AllOfJudgmentsInner> judgments;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<ReportBlockOneOf7AllOfItemsInner> items;



  @JsonKey(
    
    name: r'layers',
    required: false,
    includeIfNull: false,
  )


  final List<String>? layers;



  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final Geometry? area;



  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



  @JsonKey(
    
    name: r'timeRange',
    required: false,
    includeIfNull: false,
  )


  final TimeRange? timeRange;



      /// ULID identifier
  @JsonKey(
    
    name: r'attachmentId',
    required: false,
    includeIfNull: false,
  )


  final String? attachmentId;



  @JsonKey(
    
    name: r'mediaId',
    required: false,
    includeIfNull: false,
  )


  final String? mediaId;



      /// Why the map was not rendered at publication, if it was not
  @JsonKey(
    
    name: r'caveat',
    required: false,
    includeIfNull: false,
  )


  final String? caveat;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'sourceRef',
    required: false,
    includeIfNull: false,
  )


  final String? sourceRef;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'hypothesisSetRef',
    required: true,
    includeIfNull: false,
  )


  final String hypothesisSetRef;



  @JsonKey(
    
    name: r'snapshot',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? snapshot;



  @JsonKey(
    
    name: r'revisionIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? revisionIds;



  @JsonKey(
    
    name: r'questions',
    required: true,
    includeIfNull: false,
  )


  final List<ReportBlockOneOf6AllOfQuestionsInner> questions;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;



  @JsonKey(
    
    name: r'coverage',
    required: false,
    includeIfNull: false,
  )


  final List<CoverageStats>? coverage;



  @JsonKey(
    
    name: r'attachmentIds',
    required: true,
    includeIfNull: false,
  )


  final List<String> attachmentIds;



  @JsonKey(
    
    name: r'attributions',
    required: true,
    includeIfNull: false,
  )


  final List<ReportBlockOneOf10AllOfAttributionsInner> attributions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBlock &&
      other.id == id &&
      other.type == type &&
      other.title == title &&
      other.textOrigin == textOrigin &&
      other.updatedAt == updatedAt &&
      other.updatedBy == updatedBy &&
      other.markdown == markdown &&
      other.citations == citations &&
      other.judgments == judgments &&
      other.items == items &&
      other.layers == layers &&
      other.area == area &&
      other.bbox == bbox &&
      other.timeRange == timeRange &&
      other.attachmentId == attachmentId &&
      other.mediaId == mediaId &&
      other.caveat == caveat &&
      other.sourceRef == sourceRef &&
      other.hypothesisSetRef == hypothesisSetRef &&
      other.snapshot == snapshot &&
      other.revisionIds == revisionIds &&
      other.questions == questions &&
      other.caveats == caveats &&
      other.coverage == coverage &&
      other.attachmentIds == attachmentIds &&
      other.attributions == attributions;

    @override
    int get hashCode =>
        id.hashCode +
        type.hashCode +
        (title == null ? 0 : title.hashCode) +
        textOrigin.hashCode +
        updatedAt.hashCode +
        (updatedBy == null ? 0 : updatedBy.hashCode) +
        markdown.hashCode +
        citations.hashCode +
        judgments.hashCode +
        items.hashCode +
        layers.hashCode +
        (area == null ? 0 : area.hashCode) +
        bbox.hashCode +
        (timeRange == null ? 0 : timeRange.hashCode) +
        (attachmentId == null ? 0 : attachmentId.hashCode) +
        (mediaId == null ? 0 : mediaId.hashCode) +
        (caveat == null ? 0 : caveat.hashCode) +
        (sourceRef == null ? 0 : sourceRef.hashCode) +
        hypothesisSetRef.hashCode +
        snapshot.hashCode +
        revisionIds.hashCode +
        questions.hashCode +
        caveats.hashCode +
        coverage.hashCode +
        attachmentIds.hashCode +
        attributions.hashCode;

  factory ReportBlock.fromJson(Map<String, dynamic> json) => _$ReportBlockFromJson(json);

  Map<String, dynamic> toJson() => _$ReportBlockToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReportBlockTypeEnum {
@JsonValue(r'markdown')
markdown(r'markdown'),
@JsonValue(r'bluf')
bluf(r'bluf'),
@JsonValue(r'objects')
objects(r'objects'),
@JsonValue(r'map')
map(r'map'),
@JsonValue(r'timeline')
timeline(r'timeline'),
@JsonValue(r'hypotheses')
hypotheses(r'hypotheses'),
@JsonValue(r'forecasts')
forecasts(r'forecasts'),
@JsonValue(r'evidence')
evidence(r'evidence'),
@JsonValue(r'coverage')
coverage(r'coverage'),
@JsonValue(r'attachments')
attachments(r'attachments'),
@JsonValue(r'sources')
sources(r'sources');

const ReportBlockTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `llm` blocks are labeled narrative from the optional 028 tool loop
enum ReportBlockTextOriginEnum {
@JsonValue(r'author')
author(r'author'),
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'llm')
llm(r'llm');

const ReportBlockTextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


