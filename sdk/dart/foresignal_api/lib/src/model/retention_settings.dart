//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/retention_settings_stats.dart';
import 'package:foresignal_api/src/model/retention_settings_social.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'retention_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RetentionSettings {
  /// Returns a new [RetentionSettings] instance.
  RetentionSettings({

    required  this.default_,

     this.alerts,

     this.usage,

     this.briefings,

     this.ask,

     this.sources,

     this.trackWindow = '3d',

     this.provenance = '30d',

     this.notifications = '90d',

     this.kinds,

     this.audit,

     this.stats,

     this.inbox = '30d',

     this.trackRawWindow,

     this.trackDownsampleInterval,

     this.entityLinks,

     this.social,
  });

      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'default',
    required: true,
    includeIfNull: false,
  )


  final String default_;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'alerts',
    required: false,
    includeIfNull: false,
  )


  final String? alerts;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'usage',
    required: false,
    includeIfNull: false,
  )


  final String? usage;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'briefings',
    required: false,
    includeIfNull: false,
  )


  final String? briefings;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'ask',
    required: false,
    includeIfNull: false,
  )


  final String? ask;



      /// Per-source overrides (view over source_settings.retention); TRACK sources → 400 RETENTION_TIER_FIXED
  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? sources;



      /// Single window of the TRACK tier. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    defaultValue: '3d',
    name: r'trackWindow',
    required: false,
    includeIfNull: false,
  )


  final String? trackWindow;



      /// fetch_log rows with versions. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    defaultValue: '30d',
    name: r'provenance',
    required: false,
    includeIfNull: false,
  )


  final String? provenance;



      /// notification rows. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    defaultValue: '90d',
    name: r'notifications',
    required: false,
    includeIfNull: false,
  )


  final String? notifications;



      /// Per-kind windows (STANDARD only), first in the resolution order
  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? kinds;



      /// Audit log retention (default 90d)
  @JsonKey(
    
    name: r'audit',
    required: false,
    includeIfNull: false,
  )


  final String? audit;



  @JsonKey(
    
    name: r'stats',
    required: false,
    includeIfNull: false,
  )


  final RetentionSettingsStats? stats;



      /// Notification inbox retention (033). Separate from `notifications` (delivery rows, 019).
  @JsonKey(
    defaultValue: '30d',
    name: r'inbox',
    required: false,
    includeIfNull: false,
  )


  final String? inbox;



      /// TRACK history older than this is downsampled; null disables (must be < trackWindow). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'trackRawWindow',
    required: false,
    includeIfNull: false,
  )


  final String? trackRawWindow;



      /// One sample per entity per interval in downsampled leaves (30s ≤ interval ≤ trackRawWindow). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'trackDownsampleInterval',
    required: false,
    includeIfNull: false,
  )


  final String? trackDownsampleInterval;



      /// How long closed entity links are kept (098; default 365d). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'entityLinks',
    required: false,
    includeIfNull: false,
  )


  final String? entityLinks;



  @JsonKey(
    
    name: r'social',
    required: false,
    includeIfNull: false,
  )


  final RetentionSettingsSocial? social;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RetentionSettings &&
      other.default_ == default_ &&
      other.alerts == alerts &&
      other.usage == usage &&
      other.briefings == briefings &&
      other.ask == ask &&
      other.sources == sources &&
      other.trackWindow == trackWindow &&
      other.provenance == provenance &&
      other.notifications == notifications &&
      other.kinds == kinds &&
      other.audit == audit &&
      other.stats == stats &&
      other.inbox == inbox &&
      other.trackRawWindow == trackRawWindow &&
      other.trackDownsampleInterval == trackDownsampleInterval &&
      other.entityLinks == entityLinks &&
      other.social == social;

    @override
    int get hashCode =>
        default_.hashCode +
        alerts.hashCode +
        usage.hashCode +
        briefings.hashCode +
        ask.hashCode +
        sources.hashCode +
        trackWindow.hashCode +
        provenance.hashCode +
        notifications.hashCode +
        kinds.hashCode +
        audit.hashCode +
        stats.hashCode +
        inbox.hashCode +
        (trackRawWindow == null ? 0 : trackRawWindow.hashCode) +
        trackDownsampleInterval.hashCode +
        entityLinks.hashCode +
        social.hashCode;

  factory RetentionSettings.fromJson(Map<String, dynamic> json) => _$RetentionSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$RetentionSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

