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

part 'briefing_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BriefingInput {
  /// Returns a new [BriefingInput] instance.
  BriefingInput({

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
  });

      /// The v2.0 user who owns the edition (2.1.1). On the self-service `/v1/me/editions` operations it is ignored and forced to the caller. On `createBriefing`/`updateBriefing` it defaults to the creating user; only a caller holding `users.manage` may set it to another user, to assign an edition on their behalf. Null for a briefing created with an API token.
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


  final BriefingInputModeEnum? mode;



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





    @override
    bool operator ==(Object other) => identical(this, other) || other is BriefingInput &&
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
      other.includeCorroboration == includeCorroboration;

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
        includeCorroboration.hashCode;

  factory BriefingInput.fromJson(Map<String, dynamic> json) => _$BriefingInputFromJson(json);

  Map<String, dynamic> toJson() => _$BriefingInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BriefingInputModeEnum {
@JsonValue(r'digest')
digest(r'digest'),
@JsonValue(r'llm')
llm(r'llm');

const BriefingInputModeEnum(this.value);

final String value;

@override
String toString() => value;
}


