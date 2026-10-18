//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/hypothesis_prior_basis.dart';
import 'package:foresignal_api/src/model/icd_term.dart';
import 'package:foresignal_api/src/model/hypothesis_coincidence_baseline.dart';
import 'package:foresignal_api/src/model/confidence_level.dart';
import 'package:foresignal_api/src/model/hypothesis_confidence_basis.dart';
import 'package:foresignal_api/src/model/coverage_stats.dart';
import 'package:foresignal_api/src/model/hypothesis_cap_override.dart';
import 'package:foresignal_api/src/model/hypothesis_change.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Hypothesis {
  /// Returns a new [Hypothesis] instance.
  Hypothesis({

    required  this.id,

    required  this.setId,

    required  this.label,

    required  this.statement,

    required  this.type,

    required  this.prior,

    required  this.priorBasis,

    required  this.probability,

    required  this.estimativeTerm,

    required  this.confidence,

     this.confidenceBasis,

     this.ledgerOutcome,

    required  this.status,

     this.change,

     this.linkedFindingIds,

     this.coincidenceBaseline,

     this.alternatives,

     this.probabilityCap,

     this.capOverride,

     this.version,

     this.newsCoverage,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// ULID identifier
  @JsonKey(
    
    name: r'setId',
    required: true,
    includeIfNull: false,
  )


  final String setId;



  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final String label;



  @JsonKey(
    
    name: r'statement',
    required: true,
    includeIfNull: false,
  )


  final String statement;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final HypothesisTypeEnum type;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'prior',
    required: true,
    includeIfNull: false,
  )


  final num prior;



  @JsonKey(
    
    name: r'priorBasis',
    required: true,
    includeIfNull: false,
  )


  final HypothesisPriorBasis priorBasis;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: true,
    includeIfNull: false,
  )


  final num probability;



  @JsonKey(
    
    name: r'estimativeTerm',
    required: true,
    includeIfNull: false,
  )


  final IcdTerm estimativeTerm;



  @JsonKey(
    
    name: r'confidence',
    required: true,
    includeIfNull: false,
  )


  final ConfidenceLevel confidence;



  @JsonKey(
    
    name: r'confidenceBasis',
    required: false,
    includeIfNull: false,
  )


  final HypothesisConfidenceBasis? confidenceBasis;



      /// Outcome of the linked question this hypothesis maps to
  @JsonKey(
    
    name: r'ledgerOutcome',
    required: false,
    includeIfNull: false,
  )


  final String? ledgerOutcome;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final HypothesisStatusEnum status;



  @JsonKey(
    
    name: r'change',
    required: false,
    includeIfNull: false,
  )


  final HypothesisChange? change;



      /// Speculative links: ≥ 1 linked 119 findings
  @JsonKey(
    
    name: r'linkedFindingIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? linkedFindingIds;



  @JsonKey(
    
    name: r'coincidenceBaseline',
    required: false,
    includeIfNull: false,
  )


  final HypothesisCoincidenceBaseline? coincidenceBaseline;



      /// Speculative links: ≥ 2 other hypotheses in the same set
  @JsonKey(
    
    name: r'alternatives',
    required: false,
    includeIfNull: false,
  )


  final List<String>? alternatives;



      /// Speculative links: computed cap the updater never exceeds
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probabilityCap',
    required: false,
    includeIfNull: false,
  )


  final num? probabilityCap;



  @JsonKey(
    
    name: r'capOverride',
    required: false,
    includeIfNull: false,
  )


  final HypothesisCapOverride? capOverride;



  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;



      /// Lean and state-affiliation balance of the hypothesis's supporting news evidence; SKEWED_COVERAGE caveat when skewed
  @JsonKey(
    
    name: r'newsCoverage',
    required: false,
    includeIfNull: false,
  )


  final CoverageStats? newsCoverage;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Hypothesis &&
      other.id == id &&
      other.setId == setId &&
      other.label == label &&
      other.statement == statement &&
      other.type == type &&
      other.prior == prior &&
      other.priorBasis == priorBasis &&
      other.probability == probability &&
      other.estimativeTerm == estimativeTerm &&
      other.confidence == confidence &&
      other.confidenceBasis == confidenceBasis &&
      other.ledgerOutcome == ledgerOutcome &&
      other.status == status &&
      other.change == change &&
      other.linkedFindingIds == linkedFindingIds &&
      other.coincidenceBaseline == coincidenceBaseline &&
      other.alternatives == alternatives &&
      other.probabilityCap == probabilityCap &&
      other.capOverride == capOverride &&
      other.version == version &&
      other.newsCoverage == newsCoverage;

    @override
    int get hashCode =>
        id.hashCode +
        setId.hashCode +
        label.hashCode +
        statement.hashCode +
        type.hashCode +
        prior.hashCode +
        priorBasis.hashCode +
        probability.hashCode +
        estimativeTerm.hashCode +
        confidence.hashCode +
        confidenceBasis.hashCode +
        (ledgerOutcome == null ? 0 : ledgerOutcome.hashCode) +
        status.hashCode +
        (change == null ? 0 : change.hashCode) +
        linkedFindingIds.hashCode +
        (coincidenceBaseline == null ? 0 : coincidenceBaseline.hashCode) +
        alternatives.hashCode +
        (probabilityCap == null ? 0 : probabilityCap.hashCode) +
        (capOverride == null ? 0 : capOverride.hashCode) +
        version.hashCode +
        (newsCoverage == null ? 0 : newsCoverage.hashCode);

  factory Hypothesis.fromJson(Map<String, dynamic> json) => _$HypothesisFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum HypothesisTypeEnum {
@JsonValue(r'outcome')
outcome(r'outcome'),
@JsonValue(r'explanation')
explanation(r'explanation'),
@JsonValue(r'speculative-link')
speculativeLink(r'speculative-link'),
@JsonValue(r'other')
other(r'other');

const HypothesisTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum HypothesisStatusEnum {
@JsonValue(r'open')
open(r'open'),
@JsonValue(r'confirmed')
confirmed(r'confirmed'),
@JsonValue(r'refuted')
refuted(r'refuted'),
@JsonValue(r'expired')
expired(r'expired');

const HypothesisStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


