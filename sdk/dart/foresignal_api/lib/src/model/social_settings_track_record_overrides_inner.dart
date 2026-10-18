//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_settings_track_record_overrides_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialSettingsTrackRecordOverridesInner {
  /// Returns a new [SocialSettingsTrackRecordOverridesInner] instance.
  SocialSettingsTrackRecordOverridesInner({

    required  this.platform,

     this.account,

     this.channel,

    required  this.reliability,

     this.note,
  });

      /// Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the platform an EDP provider (024, 138) declares
  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final String platform;



      /// Author key (`authorKey` of the account, as posts expose it; 136 keys overrides by author key, not handle)
  @JsonKey(
    
    name: r'account',
    required: false,
    includeIfNull: false,
  )


  final String? account;



  @JsonKey(
    
    name: r'channel',
    required: false,
    includeIfNull: false,
  )


  final String? channel;



  @JsonKey(
    
    name: r'reliability',
    required: true,
    includeIfNull: false,
  )


  final SocialSettingsTrackRecordOverridesInnerReliabilityEnum reliability;



  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialSettingsTrackRecordOverridesInner &&
      other.platform == platform &&
      other.account == account &&
      other.channel == channel &&
      other.reliability == reliability &&
      other.note == note;

    @override
    int get hashCode =>
        platform.hashCode +
        (account == null ? 0 : account.hashCode) +
        (channel == null ? 0 : channel.hashCode) +
        reliability.hashCode +
        (note == null ? 0 : note.hashCode);

  factory SocialSettingsTrackRecordOverridesInner.fromJson(Map<String, dynamic> json) => _$SocialSettingsTrackRecordOverridesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SocialSettingsTrackRecordOverridesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SocialSettingsTrackRecordOverridesInnerReliabilityEnum {
@JsonValue(r'A')
A(r'A'),
@JsonValue(r'B')
B(r'B'),
@JsonValue(r'C')
C(r'C'),
@JsonValue(r'D')
D(r'D'),
@JsonValue(r'E')
E(r'E'),
@JsonValue(r'F')
F(r'F');

const SocialSettingsTrackRecordOverridesInnerReliabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


