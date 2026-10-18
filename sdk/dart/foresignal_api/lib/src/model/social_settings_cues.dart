//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_platform_id.dart';
import 'package:foresignal_api/src/model/social_settings_cues_xai_x_search.dart';
import 'package:foresignal_api/src/model/social_settings_cues_triggers.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_settings_cues.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialSettingsCues {
  /// Returns a new [SocialSettingsCues] instance.
  SocialSettingsCues({

     this.enabled = true,

     this.triggers,

     this.maxPerDay = 50,

     this.maxCostUsdPerCue = 1.0,

     this.platforms,

     this.xaiXSearch,
  });

  @JsonKey(
    defaultValue: true,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



  @JsonKey(
    
    name: r'triggers',
    required: false,
    includeIfNull: false,
  )


  final SocialSettingsCuesTriggers? triggers;



          // minimum: 0
  @JsonKey(
    defaultValue: 50,
    name: r'maxPerDay',
    required: false,
    includeIfNull: false,
  )


  final int? maxPerDay;



          // minimum: 0
  @JsonKey(
    defaultValue: 1.0,
    name: r'maxCostUsdPerCue',
    required: false,
    includeIfNull: false,
  )


  final num? maxCostUsdPerCue;



      /// Search-capable adapters cues may use; default all enabled
  @JsonKey(
    
    name: r'platforms',
    required: false,
    includeIfNull: false,
  )


  final List<SocialPlatformId>? platforms;



  @JsonKey(
    
    name: r'xaiXSearch',
    required: false,
    includeIfNull: false,
  )


  final SocialSettingsCuesXaiXSearch? xaiXSearch;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialSettingsCues &&
      other.enabled == enabled &&
      other.triggers == triggers &&
      other.maxPerDay == maxPerDay &&
      other.maxCostUsdPerCue == maxCostUsdPerCue &&
      other.platforms == platforms &&
      other.xaiXSearch == xaiXSearch;

    @override
    int get hashCode =>
        enabled.hashCode +
        triggers.hashCode +
        maxPerDay.hashCode +
        maxCostUsdPerCue.hashCode +
        platforms.hashCode +
        xaiXSearch.hashCode;

  factory SocialSettingsCues.fromJson(Map<String, dynamic> json) => _$SocialSettingsCuesFromJson(json);

  Map<String, dynamic> toJson() => _$SocialSettingsCuesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

