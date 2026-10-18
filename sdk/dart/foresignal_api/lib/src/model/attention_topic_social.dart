//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attention_topic_social_channel.dart';
import 'package:foresignal_api/src/model/attention_topic_social_accounts_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'attention_topic_social.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AttentionTopicSocial {
  /// Returns a new [AttentionTopicSocial] instance.
  AttentionTopicSocial({

     this.terms,

     this.hashtags,

     this.accounts,

     this.channels,

     this.exclude,

     this.platforms,

     this.placeIds,

     this.regionCodes,

     this.entityRefs,

     this.mode,
  });

      /// Terms per language (BCP 47 → phrases); at most 50 in total
  @JsonKey(
    
    name: r'terms',
    required: false,
    includeIfNull: false,
  )


  final Map<String, List<String>>? terms;



  @JsonKey(
    
    name: r'hashtags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? hashtags;



  @JsonKey(
    
    name: r'accounts',
    required: false,
    includeIfNull: false,
  )


  final List<AttentionTopicSocialAccountsInner>? accounts;



  @JsonKey(
    
    name: r'channels',
    required: false,
    includeIfNull: false,
  )


  final List<AttentionTopicSocialChannel>? channels;



      /// Exclude terms
  @JsonKey(
    
    name: r'exclude',
    required: false,
    includeIfNull: false,
  )


  final List<String>? exclude;



      /// Subset of networks; empty = all enabled
  @JsonKey(
    
    name: r'platforms',
    required: false,
    includeIfNull: false,
  )


  final List<String>? platforms;



      /// 036 places whose names and aliases are added as terms
  @JsonKey(
    
    name: r'placeIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? placeIds;



      /// 040 regions whose names and aliases are added as terms
  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



      /// 098 entities whose names and identifiers are terms
  @JsonKey(
    
    name: r'entityRefs',
    required: false,
    includeIfNull: false,
  )


  final List<String>? entityRefs;



      /// `standing` collects continuously; `cue-only` only on cues (137)
  @JsonKey(
    
    name: r'mode',
    required: false,
    includeIfNull: false,
  )


  final AttentionTopicSocialModeEnum? mode;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AttentionTopicSocial &&
      other.terms == terms &&
      other.hashtags == hashtags &&
      other.accounts == accounts &&
      other.channels == channels &&
      other.exclude == exclude &&
      other.platforms == platforms &&
      other.placeIds == placeIds &&
      other.regionCodes == regionCodes &&
      other.entityRefs == entityRefs &&
      other.mode == mode;

    @override
    int get hashCode =>
        terms.hashCode +
        hashtags.hashCode +
        accounts.hashCode +
        channels.hashCode +
        exclude.hashCode +
        platforms.hashCode +
        placeIds.hashCode +
        regionCodes.hashCode +
        entityRefs.hashCode +
        mode.hashCode;

  factory AttentionTopicSocial.fromJson(Map<String, dynamic> json) => _$AttentionTopicSocialFromJson(json);

  Map<String, dynamic> toJson() => _$AttentionTopicSocialToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `standing` collects continuously; `cue-only` only on cues (137)
enum AttentionTopicSocialModeEnum {
@JsonValue(r'standing')
standing(r'standing'),
@JsonValue(r'cue-only')
cueOnly(r'cue-only');

const AttentionTopicSocialModeEnum(this.value);

final String value;

@override
String toString() => value;
}


