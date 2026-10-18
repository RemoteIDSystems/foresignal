//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ach_evidence_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AchEvidenceInput {
  /// Returns a new [AchEvidenceInput] instance.
  AchEvidenceInput({

    required  this.source_,

     this.ref,

    required  this.description,

    required  this.credibility,

    required  this.relevance,

     this.ratings,

     this.citations,
  });

  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final AchEvidenceInputSource_Enum source_;



      /// Object reference (required unless `manual`)
  @JsonKey(
    
    name: r'ref',
    required: false,
    includeIfNull: false,
  )


  final String? ref;



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


  final AchEvidenceInputCredibilityEnum credibility;



  @JsonKey(
    
    name: r'relevance',
    required: true,
    includeIfNull: false,
  )


  final AchEvidenceInputRelevanceEnum relevance;



      /// Per hypothesis id; required for manual evidence
  @JsonKey(
    
    name: r'ratings',
    required: false,
    includeIfNull: false,
  )


  final Map<String, AchEvidenceInputRatingsEnum>? ratings;



  @JsonKey(
    
    name: r'citations',
    required: false,
    includeIfNull: false,
  )


  final List<String>? citations;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AchEvidenceInput &&
      other.source_ == source_ &&
      other.ref == ref &&
      other.description == description &&
      other.credibility == credibility &&
      other.relevance == relevance &&
      other.ratings == ratings &&
      other.citations == citations;

    @override
    int get hashCode =>
        source_.hashCode +
        (ref == null ? 0 : ref.hashCode) +
        description.hashCode +
        credibility.hashCode +
        relevance.hashCode +
        ratings.hashCode +
        citations.hashCode;

  factory AchEvidenceInput.fromJson(Map<String, dynamic> json) => _$AchEvidenceInputFromJson(json);

  Map<String, dynamic> toJson() => _$AchEvidenceInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AchEvidenceInputSource_Enum {
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

const AchEvidenceInputSource_Enum(this.value);

final String value;

@override
String toString() => value;
}


enum AchEvidenceInputCredibilityEnum {
@JsonValue(r'H')
H(r'H'),
@JsonValue(r'M')
M(r'M'),
@JsonValue(r'L')
L(r'L');

const AchEvidenceInputCredibilityEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AchEvidenceInputRelevanceEnum {
@JsonValue(r'H')
H(r'H'),
@JsonValue(r'M')
M(r'M'),
@JsonValue(r'L')
L(r'L');

const AchEvidenceInputRelevanceEnum(this.value);

final String value;

@override
String toString() => value;
}


/// ACH consistency rating
enum AchEvidenceInputRatingsEnum {
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

const AchEvidenceInputRatingsEnum(this.value);

final String value;

@override
String toString() => value;
}


