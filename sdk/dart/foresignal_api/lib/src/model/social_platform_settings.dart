//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/budget.dart';
import 'package:foresignal_api/src/model/social_platform_price.dart';
import 'package:foresignal_api/src/model/social_platform_settings_options.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_platform_settings.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialPlatformSettings {
  /// Returns a new [SocialPlatformSettings] instance.
  SocialPlatformSettings({

    required  this.enabled,

     this.mode,

     this.channels,

     this.deletionSync,

     this.postRetention,

     this.budget,

     this.monthlyCapUsd,

     this.prices,

     this.pricesAsOf,

     this.options,
  });

  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// Adapter mode: x `counts` | `search` | `counts+search`; others null
  @JsonKey(
    
    name: r'mode',
    required: false,
    includeIfNull: false,
  )


  final String? mode;



      /// Channels polled regardless of topics: subreddits, Mastodon instances or hashtags, YouTube channels
  @JsonKey(
    
    name: r'channels',
    required: false,
    includeIfNull: false,
  )


  final List<String>? channels;



      /// Default on for X, Bluesky and Threads
  @JsonKey(
    
    name: r'deletionSync',
    required: false,
    includeIfNull: false,
  )


  final bool? deletionSync;



      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'postRetention',
    required: false,
    includeIfNull: false,
  )


  final String? postRetention;



      /// Call budget (005)
  @JsonKey(
    
    name: r'budget',
    required: false,
    includeIfNull: false,
  )


  final Budget? budget;



      /// Money cap for paid platforms (X default 25)
          // minimum: 0
  @JsonKey(
    
    name: r'monthlyCapUsd',
    required: false,
    includeIfNull: false,
  )


  final num? monthlyCapUsd;



      /// X `social.x.prices`, seeded from the pricing page with its date
  @JsonKey(
    
    name: r'prices',
    required: false,
    includeIfNull: false,
  )


  final List<SocialPlatformPrice>? prices;



  @JsonKey(
    
    name: r'pricesAsOf',
    required: false,
    includeIfNull: false,
  )


  final DateTime? pricesAsOf;



  @JsonKey(
    
    name: r'options',
    required: false,
    includeIfNull: false,
  )


  final SocialPlatformSettingsOptions? options;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialPlatformSettings &&
      other.enabled == enabled &&
      other.mode == mode &&
      other.channels == channels &&
      other.deletionSync == deletionSync &&
      other.postRetention == postRetention &&
      other.budget == budget &&
      other.monthlyCapUsd == monthlyCapUsd &&
      other.prices == prices &&
      other.pricesAsOf == pricesAsOf &&
      other.options == options;

    @override
    int get hashCode =>
        enabled.hashCode +
        (mode == null ? 0 : mode.hashCode) +
        channels.hashCode +
        deletionSync.hashCode +
        postRetention.hashCode +
        (budget == null ? 0 : budget.hashCode) +
        (monthlyCapUsd == null ? 0 : monthlyCapUsd.hashCode) +
        prices.hashCode +
        (pricesAsOf == null ? 0 : pricesAsOf.hashCode) +
        options.hashCode;

  factory SocialPlatformSettings.fromJson(Map<String, dynamic> json) => _$SocialPlatformSettingsFromJson(json);

  Map<String, dynamic> toJson() => _$SocialPlatformSettingsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

