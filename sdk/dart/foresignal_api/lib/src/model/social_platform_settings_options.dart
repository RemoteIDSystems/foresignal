//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_platform_settings_options.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialPlatformSettingsOptions {
  /// Returns a new [SocialPlatformSettingsOptions] instance.
  SocialPlatformSettingsOptions({

     this.instances,

     this.replay,

     this.standingCounts,

     this.fullArchive,

     this.comments,

     this.maxResults = 10,

     this.fieldsParam,
  });

      /// Mastodon instance hosts
  @JsonKey(
    
    name: r'instances',
    required: false,
    includeIfNull: false,
  )


  final List<String>? instances;



      /// Bluesky Jetstream Network Replay backfill (operator API token)
  @JsonKey(
    
    name: r'replay',
    required: false,
    includeIfNull: false,
  )


  final bool? replay;



      /// X hourly counts for standing topics
  @JsonKey(
    
    name: r'standingCounts',
    required: false,
    includeIfNull: false,
  )


  final bool? standingCounts;



      /// X full-archive endpoints (upgraded access)
  @JsonKey(
    
    name: r'fullArchive',
    required: false,
    includeIfNull: false,
  )


  final bool? fullArchive;



      /// YouTube comment threads
  @JsonKey(
    
    name: r'comments',
    required: false,
    includeIfNull: false,
  )


  final bool? comments;



      /// X: max_results of a recent search (each post returned is billed)
          // minimum: 10
          // maximum: 100
  @JsonKey(
    defaultValue: 10,
    name: r'maxResults',
    required: false,
    includeIfNull: false,
  )


  final int? maxResults;



      /// X: the field parameter naming, post.fields (the live OpenAPI) or tweet.fields (the data dictionary); X's documents disagree (135)
  @JsonKey(
    
    name: r'fieldsParam',
    required: false,
    includeIfNull: false,
  )


  final SocialPlatformSettingsOptionsFieldsParamEnum? fieldsParam;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialPlatformSettingsOptions &&
      other.instances == instances &&
      other.replay == replay &&
      other.standingCounts == standingCounts &&
      other.fullArchive == fullArchive &&
      other.comments == comments &&
      other.maxResults == maxResults &&
      other.fieldsParam == fieldsParam;

    @override
    int get hashCode =>
        instances.hashCode +
        replay.hashCode +
        standingCounts.hashCode +
        fullArchive.hashCode +
        comments.hashCode +
        maxResults.hashCode +
        fieldsParam.hashCode;

  factory SocialPlatformSettingsOptions.fromJson(Map<String, dynamic> json) => _$SocialPlatformSettingsOptionsFromJson(json);

  Map<String, dynamic> toJson() => _$SocialPlatformSettingsOptionsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// X: the field parameter naming, post.fields (the live OpenAPI) or tweet.fields (the data dictionary); X's documents disagree (135)
enum SocialPlatformSettingsOptionsFieldsParamEnum {
@JsonValue(r'post')
post(r'post'),
@JsonValue(r'tweet')
tweet(r'tweet');

const SocialPlatformSettingsOptionsFieldsParamEnum(this.value);

final String value;

@override
String toString() => value;
}


