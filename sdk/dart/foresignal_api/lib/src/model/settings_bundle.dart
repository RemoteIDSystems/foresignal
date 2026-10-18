//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/custom_source_input.dart';
import 'package:foresignal_api/src/model/llm_settings.dart';
import 'package:foresignal_api/src/model/trigger_rule.dart';
import 'package:foresignal_api/src/model/security_settings.dart';
import 'package:foresignal_api/src/model/settings_bundle_watchlists_inner.dart';
import 'package:foresignal_api/src/model/watchdog_settings.dart';
import 'package:foresignal_api/src/model/push_provider.dart';
import 'package:foresignal_api/src/model/briefing.dart';
import 'package:foresignal_api/src/model/settings_bundle_channels_inner.dart';
import 'package:foresignal_api/src/model/llm_provider_input.dart';
import 'package:foresignal_api/src/model/notification_settings.dart';
import 'package:foresignal_api/src/model/source_settings_update.dart';
import 'package:foresignal_api/src/model/retention_settings.dart';
import 'package:foresignal_api/src/model/server_settings.dart';
import 'package:foresignal_api/src/model/saved_view.dart';
import 'package:foresignal_api/src/model/cors_settings.dart';
import 'package:foresignal_api/src/model/place.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'settings_bundle.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SettingsBundle {
  /// Returns a new [SettingsBundle] instance.
  SettingsBundle({

     this.exportedAt,

     this.foresignalVersion,

    required  this.sources,

     this.retention,

     this.cors,

     this.security,

     this.server,

     this.triggers,

     this.channels,

     this.briefings,

     this.customSources,

     this.llmProviders,

     this.llmSettings,

     this.notifications,

     this.watchdog,

     this.pushProviders,

     this.places,

     this.views,

     this.watchlists,

     this.warnings,
  });

  @JsonKey(
    
    name: r'exportedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? exportedAt;



  @JsonKey(
    
    name: r'foresignalVersion',
    required: false,
    includeIfNull: false,
  )


  final String? foresignalVersion;



  @JsonKey(
    
    name: r'sources',
    required: true,
    includeIfNull: false,
  )


  final Map<String, SourceSettingsUpdate> sources;



  @JsonKey(
    
    name: r'retention',
    required: false,
    includeIfNull: false,
  )


  final RetentionSettings? retention;



  @JsonKey(
    
    name: r'cors',
    required: false,
    includeIfNull: false,
  )


  final CorsSettings? cors;



  @JsonKey(
    
    name: r'security',
    required: false,
    includeIfNull: false,
  )


  final SecuritySettings? security;



  @JsonKey(
    
    name: r'server',
    required: false,
    includeIfNull: false,
  )


  final ServerSettings? server;



      /// Trigger rules with their ids; import upserts by id
  @JsonKey(
    
    name: r'triggers',
    required: false,
    includeIfNull: false,
  )


  final List<TriggerRule>? triggers;



      /// Channels without secrets: ChannelInput plus id. Import upserts by id; a channel imported without secrets keeps the ones it has.
  @JsonKey(
    
    name: r'channels',
    required: false,
    includeIfNull: false,
  )


  final List<SettingsBundleChannelsInner>? channels;



  @JsonKey(
    
    name: r'briefings',
    required: false,
    includeIfNull: false,
  )


  final List<Briefing>? briefings;



  @JsonKey(
    
    name: r'customSources',
    required: false,
    includeIfNull: false,
  )


  final List<CustomSourceInput>? customSources;



  @JsonKey(
    
    name: r'llmProviders',
    required: false,
    includeIfNull: false,
  )


  final List<LlmProviderInput>? llmProviders;



  @JsonKey(
    
    name: r'llmSettings',
    required: false,
    includeIfNull: false,
  )


  final LlmSettings? llmSettings;



  @JsonKey(
    
    name: r'notifications',
    required: false,
    includeIfNull: false,
  )


  final NotificationSettings? notifications;



  @JsonKey(
    
    name: r'watchdog',
    required: false,
    includeIfNull: false,
  )


  final WatchdogSettings? watchdog;



      /// Without secrets (032). Import upserts by id: an existing provider keeps its key; a new one is stored disabled until an admin sets its key.
  @JsonKey(
    
    name: r'pushProviders',
    required: false,
    includeIfNull: false,
  )


  final List<PushProvider>? pushProviders;



      /// Only with includePlaces=true (036)
  @JsonKey(
    
    name: r'places',
    required: false,
    includeIfNull: false,
  )


  final List<Place>? places;



      /// 036
  @JsonKey(
    
    name: r'views',
    required: false,
    includeIfNull: false,
  )


  final List<SavedView>? views;



      /// Only with includeWatchlists=true (036)
  @JsonKey(
    
    name: r'watchlists',
    required: false,
    includeIfNull: false,
  )


  final List<SettingsBundleWatchlistsInner>? watchlists;



      /// Export warnings (036): places reveal locations; watchlists identify what is followed
  @JsonKey(
    
    name: r'warnings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? warnings;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SettingsBundle &&
      other.exportedAt == exportedAt &&
      other.foresignalVersion == foresignalVersion &&
      other.sources == sources &&
      other.retention == retention &&
      other.cors == cors &&
      other.security == security &&
      other.server == server &&
      other.triggers == triggers &&
      other.channels == channels &&
      other.briefings == briefings &&
      other.customSources == customSources &&
      other.llmProviders == llmProviders &&
      other.llmSettings == llmSettings &&
      other.notifications == notifications &&
      other.watchdog == watchdog &&
      other.pushProviders == pushProviders &&
      other.places == places &&
      other.views == views &&
      other.watchlists == watchlists &&
      other.warnings == warnings;

    @override
    int get hashCode =>
        exportedAt.hashCode +
        foresignalVersion.hashCode +
        sources.hashCode +
        retention.hashCode +
        cors.hashCode +
        security.hashCode +
        server.hashCode +
        triggers.hashCode +
        channels.hashCode +
        briefings.hashCode +
        customSources.hashCode +
        llmProviders.hashCode +
        llmSettings.hashCode +
        notifications.hashCode +
        watchdog.hashCode +
        pushProviders.hashCode +
        places.hashCode +
        views.hashCode +
        watchlists.hashCode +
        warnings.hashCode;

  factory SettingsBundle.fromJson(Map<String, dynamic> json) => _$SettingsBundleFromJson(json);

  Map<String, dynamic> toJson() => _$SettingsBundleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

