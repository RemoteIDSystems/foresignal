//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_comparison_revision.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialComparisonRevision {
  /// Returns a new [SocialComparisonRevision] instance.
  SocialComparisonRevision({

    required  this.revision,

    required  this.at,

    required  this.status,

     this.previousStatus,

    required  this.reason,

     this.physicalEvidenceRefs,

     this.manifestId,
  });

          // minimum: 1
  @JsonKey(
    
    name: r'revision',
    required: true,
    includeIfNull: false,
  )


  final int revision;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SocialComparisonRevisionStatusEnum status;



  @JsonKey(
    
    name: r'previousStatus',
    required: false,
    includeIfNull: false,
  )


  final SocialComparisonRevisionPreviousStatusEnum? previousStatus;



  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;



  @JsonKey(
    
    name: r'physicalEvidenceRefs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? physicalEvidenceRefs;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialComparisonRevision &&
      other.revision == revision &&
      other.at == at &&
      other.status == status &&
      other.previousStatus == previousStatus &&
      other.reason == reason &&
      other.physicalEvidenceRefs == physicalEvidenceRefs &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        revision.hashCode +
        at.hashCode +
        status.hashCode +
        (previousStatus == null ? 0 : previousStatus.hashCode) +
        reason.hashCode +
        physicalEvidenceRefs.hashCode +
        manifestId.hashCode;

  factory SocialComparisonRevision.fromJson(Map<String, dynamic> json) => _$SocialComparisonRevisionFromJson(json);

  Map<String, dynamic> toJson() => _$SocialComparisonRevisionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SocialComparisonRevisionStatusEnum {
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

const SocialComparisonRevisionStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SocialComparisonRevisionPreviousStatusEnum {
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

const SocialComparisonRevisionPreviousStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


