//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/icd_term.dart';
import 'package:foresignal_api/src/model/quantiles.dart';
import 'package:foresignal_api/src/model/ledger_question_brief_forecasters_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ledger_question_brief.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LedgerQuestionBrief {
  /// Returns a new [LedgerQuestionBrief] instance.
  LedgerQuestionBrief({

    required  this.id,

     this.ref,

    required  this.title,

    required  this.type,

    required  this.status,

     this.closesAt,

     this.resolveBy,

     this.resolutionCriteria,

     this.probability,

     this.estimativeTerm,

     this.distribution,

     this.quantiles,

    required  this.issuer,

     this.forecasters,

     this.knowledgeCutoff,

     this.baseRate,

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
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final LedgerQuestionBriefTypeEnum type;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final LedgerQuestionBriefStatusEnum status;



  @JsonKey(
    
    name: r'closesAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? closesAt;



  @JsonKey(
    
    name: r'resolveBy',
    required: false,
    includeIfNull: false,
  )


  final DateTime? resolveBy;



  @JsonKey(
    
    name: r'resolutionCriteria',
    required: false,
    includeIfNull: false,
  )


  final String? resolutionCriteria;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: false,
    includeIfNull: false,
  )


  final num? probability;



  @JsonKey(
    
    name: r'estimativeTerm',
    required: false,
    includeIfNull: false,
  )


  final IcdTerm? estimativeTerm;



      /// Ensemble probability per outcome (multiple, ordered)
  @JsonKey(
    
    name: r'distribution',
    required: false,
    includeIfNull: false,
  )


  final Map<String, num>? distribution;



  @JsonKey(
    
    name: r'quantiles',
    required: false,
    includeIfNull: false,
  )


  final Quantiles? quantiles;



      /// `foresignal-ensemble` or a forecaster id
  @JsonKey(
    
    name: r'issuer',
    required: true,
    includeIfNull: false,
  )


  final String issuer;



  @JsonKey(
    
    name: r'forecasters',
    required: false,
    includeIfNull: false,
  )


  final List<LedgerQuestionBriefForecastersInner>? forecasters;



  @JsonKey(
    
    name: r'knowledgeCutoff',
    required: false,
    includeIfNull: false,
  )


  final DateTime? knowledgeCutoff;



  @JsonKey(
    
    name: r'baseRate',
    required: false,
    includeIfNull: false,
  )


  final num? baseRate;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LedgerQuestionBrief &&
      other.id == id &&
      other.ref == ref &&
      other.title == title &&
      other.type == type &&
      other.status == status &&
      other.closesAt == closesAt &&
      other.resolveBy == resolveBy &&
      other.resolutionCriteria == resolutionCriteria &&
      other.probability == probability &&
      other.estimativeTerm == estimativeTerm &&
      other.distribution == distribution &&
      other.quantiles == quantiles &&
      other.issuer == issuer &&
      other.forecasters == forecasters &&
      other.knowledgeCutoff == knowledgeCutoff &&
      other.baseRate == baseRate &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        ref.hashCode +
        title.hashCode +
        type.hashCode +
        status.hashCode +
        (closesAt == null ? 0 : closesAt.hashCode) +
        (resolveBy == null ? 0 : resolveBy.hashCode) +
        (resolutionCriteria == null ? 0 : resolutionCriteria.hashCode) +
        (probability == null ? 0 : probability.hashCode) +
        (estimativeTerm == null ? 0 : estimativeTerm.hashCode) +
        (distribution == null ? 0 : distribution.hashCode) +
        (quantiles == null ? 0 : quantiles.hashCode) +
        issuer.hashCode +
        forecasters.hashCode +
        (knowledgeCutoff == null ? 0 : knowledgeCutoff.hashCode) +
        (baseRate == null ? 0 : baseRate.hashCode) +
        (updatedAt == null ? 0 : updatedAt.hashCode);

  factory LedgerQuestionBrief.fromJson(Map<String, dynamic> json) => _$LedgerQuestionBriefFromJson(json);

  Map<String, dynamic> toJson() => _$LedgerQuestionBriefToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LedgerQuestionBriefTypeEnum {
@JsonValue(r'binary')
binary(r'binary'),
@JsonValue(r'multiple')
multiple(r'multiple'),
@JsonValue(r'ordered')
ordered(r'ordered'),
@JsonValue(r'numeric')
numeric(r'numeric');

const LedgerQuestionBriefTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum LedgerQuestionBriefStatusEnum {
@JsonValue(r'open')
open(r'open'),
@JsonValue(r'closed')
closed(r'closed'),
@JsonValue(r'resolved')
resolved(r'resolved'),
@JsonValue(r'annulled')
annulled(r'annulled');

const LedgerQuestionBriefStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


