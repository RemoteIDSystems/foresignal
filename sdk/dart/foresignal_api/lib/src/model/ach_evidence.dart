//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/provenance_chain_entry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ach_evidence.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AchEvidence {
  /// Returns a new [AchEvidence] instance.
  AchEvidence({

    required  this.id,

    required  this.setId,

    required  this.source_,

     this.ref,

     this.evidenceGroupId,

    required  this.description,

    required  this.credibility,

    required  this.relevance,

     this.weight,

    required  this.ratings,

     this.nonDiagnostic,

    required  this.addedBy,

    required  this.addedAt,

     this.removedAt,

     this.evidenceItemId,

     this.provenanceChain,

     this.refs,

     this.caveats,
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
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final AchEvidenceSource_Enum source_;



      /// Object reference or id of the evidence
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



      /// ULID identifier
  @JsonKey(
    
    name: r'evidenceGroupId',
    required: false,
    includeIfNull: false,
  )


  final String? evidenceGroupId;



  @JsonKey(
    
    name: r'description',
    required: true,
    includeIfNull: false,
  )


  final String description;



  @JsonKey(
    
    name: r'credibility',
    required: true,
    includeIfNull: false,
  )


  final AchEvidenceCredibilityEnum credibility;



  @JsonKey(
    
    name: r'relevance',
    required: true,
    includeIfNull: false,
  )


  final AchEvidenceRelevanceEnum relevance;



      /// credibility × relevance (L 0.5, M 1, H 2)
  @JsonKey(
    
    name: r'weight',
    required: false,
    includeIfNull: false,
  )


  final num? weight;



      /// Per hypothesis id
  @JsonKey(
    
    name: r'ratings',
    required: true,
    includeIfNull: false,
  )


  final Map<String, AchEvidenceRatingsEnum> ratings;



  @JsonKey(
    
    name: r'nonDiagnostic',
    required: false,
    includeIfNull: false,
  )


  final bool? nonDiagnostic;



  @JsonKey(
    
    name: r'addedBy',
    required: true,
    includeIfNull: false,
  )


  final String addedBy;



  @JsonKey(
    
    name: r'addedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime addedAt;



  @JsonKey(
    
    name: r'removedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? removedAt;



      /// When `source=evidence-item`
  @JsonKey(
    
    name: r'evidenceItemId',
    required: false,
    includeIfNull: false,
  )


  final String? evidenceItemId;



      /// 134: chain used by CircularReportingDetector; circular support carries caveat CIRCULAR_REPORTING and counts as one evidence group
  @JsonKey(
    
    name: r'provenanceChain',
    required: false,
    includeIfNull: false,
  )


  final List<ProvenanceChainEntry>? provenanceChain;



      /// Every reference attached to the row: the first and the re-reports of the same 116 evidence group, which add no weight
  @JsonKey(
    
    name: r'refs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? refs;



      /// 134: CIRCULAR_REPORTING when the support's provenance chain returns to the consuming analyst, or when a further reference with the same origin attached to the row
  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<AchEvidenceCaveatsEnum>? caveats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AchEvidence &&
      other.id == id &&
      other.setId == setId &&
      other.source_ == source_ &&
      other.ref == ref &&
      other.evidenceGroupId == evidenceGroupId &&
      other.description == description &&
      other.credibility == credibility &&
      other.relevance == relevance &&
      other.weight == weight &&
      other.ratings == ratings &&
      other.nonDiagnostic == nonDiagnostic &&
      other.addedBy == addedBy &&
      other.addedAt == addedAt &&
      other.removedAt == removedAt &&
      other.evidenceItemId == evidenceItemId &&
      other.provenanceChain == provenanceChain &&
      other.refs == refs &&
      other.caveats == caveats;

    @override
    int get hashCode =>
        id.hashCode +
        setId.hashCode +
        source_.hashCode +
        (ref == null ? 0 : ref.hashCode) +
        (evidenceGroupId == null ? 0 : evidenceGroupId.hashCode) +
        description.hashCode +
        credibility.hashCode +
        relevance.hashCode +
        weight.hashCode +
        ratings.hashCode +
        nonDiagnostic.hashCode +
        addedBy.hashCode +
        addedAt.hashCode +
        (removedAt == null ? 0 : removedAt.hashCode) +
        (evidenceItemId == null ? 0 : evidenceItemId.hashCode) +
        provenanceChain.hashCode +
        refs.hashCode +
        caveats.hashCode;

  factory AchEvidence.fromJson(Map<String, dynamic> json) => _$AchEvidenceFromJson(json);

  Map<String, dynamic> toJson() => _$AchEvidenceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AchEvidenceSource_Enum {
@JsonValue(r'indicator')
indicator(r'indicator'),
@JsonValue(r'record')
record(r'record'),
@JsonValue(r'finding')
finding(r'finding'),
@JsonValue(r'intel_event')
intelEvent(r'intel_event'),
@JsonValue(r'alert')
alert(r'alert'),
@JsonValue(r'manual')
manual(r'manual'),
@JsonValue(r'evidence-item')
evidenceItem(r'evidence-item'),
@JsonValue(r'social-claim')
socialClaim(r'social-claim');

const AchEvidenceSource_Enum(this.value);

final String value;

@override
String toString() => value;
}


enum AchEvidenceCredibilityEnum {
@JsonValue(r'H')
H(r'H'),
@JsonValue(r'M')
M(r'M'),
@JsonValue(r'L')
L(r'L');

const AchEvidenceCredibilityEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AchEvidenceRelevanceEnum {
@JsonValue(r'H')
H(r'H'),
@JsonValue(r'M')
M(r'M'),
@JsonValue(r'L')
L(r'L');

const AchEvidenceRelevanceEnum(this.value);

final String value;

@override
String toString() => value;
}


/// ACH consistency rating
enum AchEvidenceRatingsEnum {
@JsonValue(r'CC')
CC(r'CC'),
@JsonValue(r'C')
C(r'C'),
@JsonValue(r'N')
N(r'N'),
@JsonValue(r'NA')
NA(r'NA'),
@JsonValue(r'I')
I(r'I'),
@JsonValue(r'II')
II(r'II');

const AchEvidenceRatingsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AchEvidenceCaveatsEnum {
@JsonValue(r'CIRCULAR_REPORTING')
CIRCULAR_REPORTING(r'CIRCULAR_REPORTING');

const AchEvidenceCaveatsEnum(this.value);

final String value;

@override
String toString() => value;
}


