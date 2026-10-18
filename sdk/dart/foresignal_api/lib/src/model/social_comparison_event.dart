//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_comparison_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialComparisonEvent {
  /// Returns a new [SocialComparisonEvent] instance.
  SocialComparisonEvent({

    required  this.event,

    required  this.comparisonId,

    required  this.claimId,

    required  this.narrativeId,

    required  this.status,

     this.previousStatus,

    required  this.revision,

    required  this.evaluatedAt,
  });

  @JsonKey(
    
    name: r'event',
    required: true,
    includeIfNull: false,
  )


  final SocialComparisonEventEventEnum event;



      /// ULID identifier
  @JsonKey(
    
    name: r'comparisonId',
    required: true,
    includeIfNull: false,
  )


  final String comparisonId;



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
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SocialComparisonEventStatusEnum status;



  @JsonKey(
    
    name: r'previousStatus',
    required: false,
    includeIfNull: false,
  )


  final SocialComparisonEventPreviousStatusEnum? previousStatus;



  @JsonKey(
    
    name: r'revision',
    required: true,
    includeIfNull: false,
  )


  final int revision;



  @JsonKey(
    
    name: r'evaluatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime evaluatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialComparisonEvent &&
      other.event == event &&
      other.comparisonId == comparisonId &&
      other.claimId == claimId &&
      other.narrativeId == narrativeId &&
      other.status == status &&
      other.previousStatus == previousStatus &&
      other.revision == revision &&
      other.evaluatedAt == evaluatedAt;

    @override
    int get hashCode =>
        event.hashCode +
        comparisonId.hashCode +
        claimId.hashCode +
        narrativeId.hashCode +
        status.hashCode +
        (previousStatus == null ? 0 : previousStatus.hashCode) +
        revision.hashCode +
        evaluatedAt.hashCode;

  factory SocialComparisonEvent.fromJson(Map<String, dynamic> json) => _$SocialComparisonEventFromJson(json);

  Map<String, dynamic> toJson() => _$SocialComparisonEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SocialComparisonEventEventEnum {
@JsonValue(r'comparison.updated')
comparisonPeriodUpdated(r'comparison.updated');

const SocialComparisonEventEventEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SocialComparisonEventStatusEnum {
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

const SocialComparisonEventStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SocialComparisonEventPreviousStatusEnum {
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

const SocialComparisonEventPreviousStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


