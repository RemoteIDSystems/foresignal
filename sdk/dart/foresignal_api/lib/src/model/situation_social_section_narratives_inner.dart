//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_social_section_narratives_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationSocialSectionNarrativesInner {
  /// Returns a new [SituationSocialSectionNarrativesInner] instance.
  SituationSocialSectionNarrativesInner({

    required  this.narrativeId,

    required  this.title,

    required  this.status,

     this.velocity,

     this.originConcentration,

    required  this.evidenceGroupCount,

     this.leadOverPhysicalSeconds,

     this.foundBy,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'narrativeId',
    required: true,
    includeIfNull: false,
  )


  final String narrativeId;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SituationSocialSectionNarrativesInnerStatusEnum status;



  @JsonKey(
    
    name: r'velocity',
    required: false,
    includeIfNull: false,
  )


  final num? velocity;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'originConcentration',
    required: false,
    includeIfNull: false,
  )


  final num? originConcentration;



  @JsonKey(
    
    name: r'evidenceGroupCount',
    required: true,
    includeIfNull: false,
  )


  final int evidenceGroupCount;



      /// Observed lead time (single case)
  @JsonKey(
    
    name: r'leadOverPhysicalSeconds',
    required: false,
    includeIfNull: false,
  )


  final num? leadOverPhysicalSeconds;



  @JsonKey(
    
    name: r'foundBy',
    required: false,
    includeIfNull: false,
  )


  final SituationSocialSectionNarrativesInnerFoundByEnum? foundBy;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationSocialSectionNarrativesInner &&
      other.narrativeId == narrativeId &&
      other.title == title &&
      other.status == status &&
      other.velocity == velocity &&
      other.originConcentration == originConcentration &&
      other.evidenceGroupCount == evidenceGroupCount &&
      other.leadOverPhysicalSeconds == leadOverPhysicalSeconds &&
      other.foundBy == foundBy;

    @override
    int get hashCode =>
        narrativeId.hashCode +
        title.hashCode +
        status.hashCode +
        (velocity == null ? 0 : velocity.hashCode) +
        originConcentration.hashCode +
        evidenceGroupCount.hashCode +
        (leadOverPhysicalSeconds == null ? 0 : leadOverPhysicalSeconds.hashCode) +
        foundBy.hashCode;

  factory SituationSocialSectionNarrativesInner.fromJson(Map<String, dynamic> json) => _$SituationSocialSectionNarrativesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SituationSocialSectionNarrativesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SituationSocialSectionNarrativesInnerStatusEnum {
@JsonValue(r'unassessed')
unassessed(r'unassessed'),
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'corroborated')
corroborated(r'corroborated'),
@JsonValue(r'unobserved')
unobserved(r'unobserved'),
@JsonValue(r'contradicted')
contradicted(r'contradicted'),
@JsonValue(r'mixed')
mixed(r'mixed'),
@JsonValue(r'insufficient-coverage')
insufficientCoverage(r'insufficient-coverage');

const SituationSocialSectionNarrativesInnerStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SituationSocialSectionNarrativesInnerFoundByEnum {
@JsonValue(r'organic')
organic(r'organic'),
@JsonValue(r'cue')
cue(r'cue');

const SituationSocialSectionNarrativesInnerFoundByEnum(this.value);

final String value;

@override
String toString() => value;
}


