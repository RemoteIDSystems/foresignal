//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_comparison_revision.dart';
import 'package:foresignal_api/src/model/claim_place.dart';
import 'package:foresignal_api/src/model/social_comparison_detectability.dart';
import 'package:foresignal_api/src/model/social_physical_evidence.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/social_lead_times.dart';
import 'package:foresignal_api/src/model/social_comparison_contradiction.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:foresignal_api/src/model/detector_ref.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_comparison.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialComparison {
  /// Returns a new [SocialComparison] instance.
  SocialComparison({

    required  this.id,

    required  this.ref,

    required  this.claimId,

    required  this.narrativeId,

    required  this.signalFamily,

    required  this.place,

    required  this.window,

    required  this.status,

    required  this.physicalEvidence,

    required  this.detectability,

    required  this.leadTimes,

     this.contradiction,

    required  this.foundBy,

     this.cueId,

     this.questionId,

    required  this.revision,

     this.finalAt,

    required  this.comparator,

    required  this.manifestId,

    required  this.evaluatedAt,

     this.revisions,

     this.caveats,
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



      /// ULID identifier
  @JsonKey(
    
    name: r'claimId',
    required: true,
    includeIfNull: false,
  )


  final String claimId;



      /// ULID identifier
  @JsonKey(
    
    name: r'narrativeId',
    required: true,
    includeIfNull: false,
  )


  final String narrativeId;



  @JsonKey(
    
    name: r'signalFamily',
    required: true,
    includeIfNull: false,
  )


  final String signalFamily;



  @JsonKey(
    
    name: r'place',
    required: true,
    includeIfNull: false,
  )


  final ClaimPlace place;



  @JsonKey(
    
    name: r'window',
    required: true,
    includeIfNull: false,
  )


  final TimeRange window;



      /// `unobserved` only when `canSeeAbsence` is ok over the full window; otherwise `insufficient-coverage`; `pending` while the window (plus publication lag) is open
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SocialComparisonStatusEnum status;



  @JsonKey(
    
    name: r'physicalEvidence',
    required: true,
    includeIfNull: false,
  )


  final List<SocialPhysicalEvidence> physicalEvidence;



  @JsonKey(
    
    name: r'detectability',
    required: true,
    includeIfNull: false,
  )


  final SocialComparisonDetectability detectability;



  @JsonKey(
    
    name: r'leadTimes',
    required: true,
    includeIfNull: false,
  )


  final SocialLeadTimes leadTimes;



  @JsonKey(
    
    name: r'contradiction',
    required: false,
    includeIfNull: false,
  )


  final SocialComparisonContradiction? contradiction;



      /// `cue` when the narrative was found by looking; lead-time statistics keep the two apart
  @JsonKey(
    
    name: r'foundBy',
    required: true,
    includeIfNull: false,
  )


  final SocialComparisonFoundByEnum foundBy;



      /// ULID identifier
  @JsonKey(
    
    name: r'cueId',
    required: false,
    includeIfNull: false,
  )


  final String? cueId;



      /// 122 ledger question opened while pending
  @JsonKey(
    
    name: r'questionId',
    required: false,
    includeIfNull: false,
  )


  final String? questionId;



          // minimum: 1
  @JsonKey(
    
    name: r'revision',
    required: true,
    includeIfNull: false,
  )


  final int revision;



      /// Window end plus the family's publication lag; no revisions after
  @JsonKey(
    
    name: r'finalAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? finalAt;



  @JsonKey(
    
    name: r'comparator',
    required: true,
    includeIfNull: false,
  )


  final DetectorRef comparator;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: true,
    includeIfNull: false,
  )


  final String manifestId;



  @JsonKey(
    
    name: r'evaluatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime evaluatedAt;



      /// Detail view
  @JsonKey(
    
    name: r'revisions',
    required: false,
    includeIfNull: false,
  )


  final List<SocialComparisonRevision>? revisions;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialComparison &&
      other.id == id &&
      other.ref == ref &&
      other.claimId == claimId &&
      other.narrativeId == narrativeId &&
      other.signalFamily == signalFamily &&
      other.place == place &&
      other.window == window &&
      other.status == status &&
      other.physicalEvidence == physicalEvidence &&
      other.detectability == detectability &&
      other.leadTimes == leadTimes &&
      other.contradiction == contradiction &&
      other.foundBy == foundBy &&
      other.cueId == cueId &&
      other.questionId == questionId &&
      other.revision == revision &&
      other.finalAt == finalAt &&
      other.comparator == comparator &&
      other.manifestId == manifestId &&
      other.evaluatedAt == evaluatedAt &&
      other.revisions == revisions &&
      other.caveats == caveats;

    @override
    int get hashCode =>
        id.hashCode +
        ref.hashCode +
        claimId.hashCode +
        narrativeId.hashCode +
        signalFamily.hashCode +
        place.hashCode +
        window.hashCode +
        status.hashCode +
        physicalEvidence.hashCode +
        detectability.hashCode +
        leadTimes.hashCode +
        (contradiction == null ? 0 : contradiction.hashCode) +
        foundBy.hashCode +
        (cueId == null ? 0 : cueId.hashCode) +
        (questionId == null ? 0 : questionId.hashCode) +
        revision.hashCode +
        (finalAt == null ? 0 : finalAt.hashCode) +
        comparator.hashCode +
        manifestId.hashCode +
        evaluatedAt.hashCode +
        revisions.hashCode +
        caveats.hashCode;

  factory SocialComparison.fromJson(Map<String, dynamic> json) => _$SocialComparisonFromJson(json);

  Map<String, dynamic> toJson() => _$SocialComparisonToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `unobserved` only when `canSeeAbsence` is ok over the full window; otherwise `insufficient-coverage`; `pending` while the window (plus publication lag) is open
enum SocialComparisonStatusEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'corroborated')
corroborated(r'corroborated'),
@JsonValue(r'unobserved')
unobserved(r'unobserved'),
@JsonValue(r'contradicted')
contradicted(r'contradicted'),
@JsonValue(r'insufficient-coverage')
insufficientCoverage(r'insufficient-coverage');

const SocialComparisonStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `cue` when the narrative was found by looking; lead-time statistics keep the two apart
enum SocialComparisonFoundByEnum {
@JsonValue(r'organic')
organic(r'organic'),
@JsonValue(r'cue')
cue(r'cue');

const SocialComparisonFoundByEnum(this.value);

final String value;

@override
String toString() => value;
}


