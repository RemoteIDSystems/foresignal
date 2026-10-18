//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'retention_settings_social.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RetentionSettingsSocial {
  /// Returns a new [RetentionSettingsSocial] instance.
  RetentionSettingsSocial({

     this.posts,

     this.platforms,
  });

      /// ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'posts',
    required: false,
    includeIfNull: false,
  )


  final String? posts;



      /// Network → post retention
  @JsonKey(
    
    name: r'platforms',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String>? platforms;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RetentionSettingsSocial &&
      other.posts == posts &&
      other.platforms == platforms;

    @override
    int get hashCode =>
        posts.hashCode +
        platforms.hashCode;

  factory RetentionSettingsSocial.fromJson(Map<String, dynamic> json) => _$RetentionSettingsSocialFromJson(json);

  Map<String, dynamic> toJson() => _$RetentionSettingsSocialToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

