//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/briefing_scope.dart';
import 'package:foresignal_api/src/model/breaking_news_config.dart';
import 'package:foresignal_api/src/model/briefing_input_site.dart';
import 'package:foresignal_api/src/model/delivery_target.dart';
import 'package:foresignal_api/src/model/briefing_input_audio.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'briefing.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Briefing {
  /// Returns a new [Briefing] instance.
  Briefing({

     this.ownerUserId,

    required  this.name,

     this.enabled = true,

    required  this.schedule,

     this.timezone = 'UTC',

     this.scope,

     this.lookback,

     this.mode,

     this.instructions,

     this.channelIds,

     this.maxItemsPerKind = 5,

     this.audio,

     this.outputLanguage,

     this.deliveryTargets,

     this.site,

     this.breakingNews,

     this.includeProvenance = false,

     this.includeCorroboration = false,

    required  this.id,

    required  this.createdAt,

     this.updatedAt,

     this.nextRunAt,

     this.lastRunAt,

     this.lastRunStatus,

     this.warnings,
  });

      /// The v2.0 user who owns the edition: the creating user for a signed-in session, null for a briefing created with an API token (NP-003)
  @JsonKey(
    
    name: r'ownerUserId',
    required: false,
    includeIfNull: false,
  )


  final String? ownerUserId;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



      /// 5-field cron
  @JsonKey(
    
    name: r'schedule',
    required: true,
    includeIfNull: false,
  )


  final String schedule;



  @JsonKey(
    defaultValue: 'UTC',
    name: r'timezone',
    required: false,
    includeIfNull: false,
  )


  final String? timezone;



  @JsonKey(
    
    name: r'scope',
    required: false,
    includeIfNull: false,
  )


  final BriefingScope? scope;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'lookback',
    required: false,
    includeIfNull: false,
  )


  final String? lookback;



  @JsonKey(
    
    name: r'mode',
    required: false,
    includeIfNull: false,
  )


  final BriefingModeEnum? mode;



  @JsonKey(
    
    name: r'instructions',
    required: false,
    includeIfNull: false,
  )


  final String? instructions;



  @JsonKey(
    
    name: r'channelIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? channelIds;



          // minimum: 1
          // maximum: 50
  @JsonKey(
    defaultValue: 5,
    name: r'maxItemsPerKind',
    required: false,
    includeIfNull: false,
  )


  final int? maxItemsPerKind;



  @JsonKey(
    
    name: r'audio',
    required: false,
    includeIfNull: false,
  )


  final BriefingInputAudio? audio;



      /// BCP 47 language tag, for example `en`, `es` or `pt-BR`
  @JsonKey(
    
    name: r'outputLanguage',
    required: false,
    includeIfNull: false,
  )


  final String? outputLanguage;



      /// Where each run goes (NP-004). Empty or absent: every channel in `channelIds`, as before
  @JsonKey(
    
    name: r'deliveryTargets',
    required: false,
    includeIfNull: false,
  )


  final List<DeliveryTarget>? deliveryTargets;



  @JsonKey(
    
    name: r'site',
    required: false,
    includeIfNull: false,
  )


  final BriefingInputSite? site;



      /// Breaking-news special editions (NP-006); null for none
  @JsonKey(
    
    name: r'breakingNews',
    required: false,
    includeIfNull: false,
  )


  final BreakingNewsConfig? breakingNews;



      /// Each item carries the cross-linked evidence that put it in the edition (`provenance` on the run's citations, NP-007)
  @JsonKey(
    defaultValue: false,
    name: r'includeProvenance',
    required: false,
    includeIfNull: false,
  )


  final bool? includeProvenance;



      /// Each item carries its corroboration signal (`corroboration` on the run's citations, NP-007)
  @JsonKey(
    defaultValue: false,
    name: r'includeCorroboration',
    required: false,
    includeIfNull: false,
  )


  final bool? includeCorroboration;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;



  @JsonKey(
    
    name: r'nextRunAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? nextRunAt;



  @JsonKey(
    
    name: r'lastRunAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastRunAt;



  @JsonKey(
    
    name: r'lastRunStatus',
    required: false,
    includeIfNull: false,
  )


  final String? lastRunStatus;



      /// Non-fatal validation warnings, e.g. a channel not subscribed to `briefing`
  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? warnings;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Briefing &&
      other.ownerUserId == ownerUserId &&
      other.name == name &&
      other.enabled == enabled &&
      other.schedule == schedule &&
      other.timezone == timezone &&
      other.scope == scope &&
      other.lookback == lookback &&
      other.mode == mode &&
      other.instructions == instructions &&
      other.channelIds == channelIds &&
      other.maxItemsPerKind == maxItemsPerKind &&
      other.audio == audio &&
      other.outputLanguage == outputLanguage &&
      other.deliveryTargets == deliveryTargets &&
      other.site == site &&
      other.breakingNews == breakingNews &&
      other.includeProvenance == includeProvenance &&
      other.includeCorroboration == includeCorroboration &&
      other.id == id &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.nextRunAt == nextRunAt &&
      other.lastRunAt == lastRunAt &&
      other.lastRunStatus == lastRunStatus &&
      other.warnings == warnings;

    @override
    int get hashCode =>
        (ownerUserId == null ? 0 : ownerUserId.hashCode) +
        name.hashCode +
        enabled.hashCode +
        schedule.hashCode +
        timezone.hashCode +
        scope.hashCode +
        (lookback == null ? 0 : lookback.hashCode) +
        mode.hashCode +
        (instructions == null ? 0 : instructions.hashCode) +
        channelIds.hashCode +
        maxItemsPerKind.hashCode +
        (audio == null ? 0 : audio.hashCode) +
        outputLanguage.hashCode +
        deliveryTargets.hashCode +
        site.hashCode +
        (breakingNews == null ? 0 : breakingNews.hashCode) +
        includeProvenance.hashCode +
        includeCorroboration.hashCode +
        id.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode +
        (nextRunAt == null ? 0 : nextRunAt.hashCode) +
        (lastRunAt == null ? 0 : lastRunAt.hashCode) +
        (lastRunStatus == null ? 0 : lastRunStatus.hashCode) +
        warnings.hashCode;

  factory Briefing.fromJson(Map<String, dynamic> json) => _$BriefingFromJson(json);

  Map<String, dynamic> toJson() => _$BriefingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BriefingModeEnum {
@JsonValue(r'digest')
digest(r'digest'),
@JsonValue(r'llm')
llm(r'llm');

const BriefingModeEnum(this.value);

final String value;

@override
String toString() => value;
}


