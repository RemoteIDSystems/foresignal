//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_cue_results_inner.dart';
import 'package:foresignal_api/src/model/social_cue_trigger.dart';
import 'package:foresignal_api/src/model/social_cue_external_results_inner.dart';
import 'package:foresignal_api/src/model/social_platform_id.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:foresignal_api/src/model/social_cue_query.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_cue.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialCue {
  /// Returns a new [SocialCue] instance.
  SocialCue({

    required  this.id,

    required  this.trigger,

    required  this.backend,

    required  this.queries,

    required  this.platforms,

    required  this.window,

    required  this.status,

     this.statusReason,

    required  this.results,

     this.externalResults,

    required  this.narrativeIds,

    required  this.costUsd,

     this.maxCostUsd,

    required  this.createdBy,

    required  this.createdAt,

     this.startedAt,

     this.completedAt,

     this.manifestId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'trigger',
    required: true,
    includeIfNull: false,
  )


  final SocialCueTrigger trigger;



      /// `xai-x-search` (026 provider `xai`, ranked sample with citations) is off by default
  @JsonKey(
    
    name: r'backend',
    required: true,
    includeIfNull: false,
  )


  final SocialCueBackendEnum backend;



  @JsonKey(
    
    name: r'queries',
    required: true,
    includeIfNull: false,
  )


  final List<SocialCueQuery> queries;



  @JsonKey(
    
    name: r'platforms',
    required: true,
    includeIfNull: false,
  )


  final List<SocialPlatformId> platforms;



  @JsonKey(
    
    name: r'window',
    required: true,
    includeIfNull: false,
  )


  final TimeRange window;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final SocialCueStatusEnum status;



      /// e.g. BUDGET_EXCEEDED, PLATFORM_DISABLED
  @JsonKey(
    
    name: r'statusReason',
    required: false,
    includeIfNull: false,
  )


  final String? statusReason;



  @JsonKey(
    
    name: r'results',
    required: true,
    includeIfNull: false,
  )


  final List<SocialCueResultsInner> results;



      /// One item per EDP provider searched
  @JsonKey(
    
    name: r'externalResults',
    required: false,
    includeIfNull: false,
  )


  final List<SocialCueExternalResultsInner>? externalResults;



  @JsonKey(
    
    name: r'narrativeIds',
    required: true,
    includeIfNull: false,
  )


  final List<String> narrativeIds;



          // minimum: 0
  @JsonKey(
    
    name: r'costUsd',
    required: true,
    includeIfNull: false,
  )


  final num costUsd;



          // minimum: 0
  @JsonKey(
    
    name: r'maxCostUsd',
    required: false,
    includeIfNull: false,
  )


  final num? maxCostUsd;



      /// `system` for automatic cues, else the principal
  @JsonKey(
    
    name: r'createdBy',
    required: true,
    includeIfNull: false,
  )


  final String createdBy;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'startedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? startedAt;



  @JsonKey(
    
    name: r'completedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? completedAt;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialCue &&
      other.id == id &&
      other.trigger == trigger &&
      other.backend == backend &&
      other.queries == queries &&
      other.platforms == platforms &&
      other.window == window &&
      other.status == status &&
      other.statusReason == statusReason &&
      other.results == results &&
      other.externalResults == externalResults &&
      other.narrativeIds == narrativeIds &&
      other.costUsd == costUsd &&
      other.maxCostUsd == maxCostUsd &&
      other.createdBy == createdBy &&
      other.createdAt == createdAt &&
      other.startedAt == startedAt &&
      other.completedAt == completedAt &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        id.hashCode +
        trigger.hashCode +
        backend.hashCode +
        queries.hashCode +
        platforms.hashCode +
        window.hashCode +
        status.hashCode +
        (statusReason == null ? 0 : statusReason.hashCode) +
        results.hashCode +
        externalResults.hashCode +
        narrativeIds.hashCode +
        costUsd.hashCode +
        maxCostUsd.hashCode +
        createdBy.hashCode +
        createdAt.hashCode +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (completedAt == null ? 0 : completedAt.hashCode) +
        (manifestId == null ? 0 : manifestId.hashCode);

  factory SocialCue.fromJson(Map<String, dynamic> json) => _$SocialCueFromJson(json);

  Map<String, dynamic> toJson() => _$SocialCueToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `xai-x-search` (026 provider `xai`, ranked sample with citations) is off by default
enum SocialCueBackendEnum {
@JsonValue(r'platform-apis')
platformApis(r'platform-apis'),
@JsonValue(r'xai-x-search')
xaiXSearch(r'xai-x-search');

const SocialCueBackendEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SocialCueStatusEnum {
@JsonValue(r'queued')
queued(r'queued'),
@JsonValue(r'running')
running(r'running'),
@JsonValue(r'done')
done(r'done'),
@JsonValue(r'failed')
failed(r'failed'),
@JsonValue(r'budget-refused')
budgetRefused(r'budget-refused');

const SocialCueStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


