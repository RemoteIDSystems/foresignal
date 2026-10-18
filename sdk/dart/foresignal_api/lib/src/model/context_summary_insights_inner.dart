//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/icd_term.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'context_summary_insights_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ContextSummaryInsightsInner {
  /// Returns a new [ContextSummaryInsightsInner] instance.
  ContextSummaryInsightsInner({

    required  this.ref,

    required  this.kind,

    required  this.title,

     this.probability,

     this.estimativeTerm,

     this.issuer,

     this.baseRate,

     this.knowledgeCutoff,

     this.at,

     this.textOrigin,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final ContextSummaryInsightsInnerKindEnum kind;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



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



      /// `official`, `foresignal-ensemble` or a forecaster id
  @JsonKey(
    
    name: r'issuer',
    required: false,
    includeIfNull: false,
  )


  final String? issuer;



  @JsonKey(
    
    name: r'baseRate',
    required: false,
    includeIfNull: false,
  )


  final num? baseRate;



  @JsonKey(
    
    name: r'knowledgeCutoff',
    required: false,
    includeIfNull: false,
  )


  final DateTime? knowledgeCutoff;



  @JsonKey(
    
    name: r'at',
    required: false,
    includeIfNull: false,
  )


  final DateTime? at;



  @JsonKey(
    
    name: r'textOrigin',
    required: false,
    includeIfNull: false,
  )


  final ContextSummaryInsightsInnerTextOriginEnum? textOrigin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ContextSummaryInsightsInner &&
      other.ref == ref &&
      other.kind == kind &&
      other.title == title &&
      other.probability == probability &&
      other.estimativeTerm == estimativeTerm &&
      other.issuer == issuer &&
      other.baseRate == baseRate &&
      other.knowledgeCutoff == knowledgeCutoff &&
      other.at == at &&
      other.textOrigin == textOrigin;

    @override
    int get hashCode =>
        ref.hashCode +
        kind.hashCode +
        title.hashCode +
        (probability == null ? 0 : probability.hashCode) +
        (estimativeTerm == null ? 0 : estimativeTerm.hashCode) +
        (issuer == null ? 0 : issuer.hashCode) +
        (baseRate == null ? 0 : baseRate.hashCode) +
        (knowledgeCutoff == null ? 0 : knowledgeCutoff.hashCode) +
        (at == null ? 0 : at.hashCode) +
        textOrigin.hashCode;

  factory ContextSummaryInsightsInner.fromJson(Map<String, dynamic> json) => _$ContextSummaryInsightsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ContextSummaryInsightsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ContextSummaryInsightsInnerKindEnum {
@JsonValue(r'finding')
finding(r'finding'),
@JsonValue(r'forecast')
forecast(r'forecast'),
@JsonValue(r'band-change')
bandChange(r'band-change');

const ContextSummaryInsightsInnerKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ContextSummaryInsightsInnerTextOriginEnum {
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'llm')
llm(r'llm');

const ContextSummaryInsightsInnerTextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


