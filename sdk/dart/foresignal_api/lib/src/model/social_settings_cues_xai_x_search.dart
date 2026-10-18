//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_settings_cues_xai_x_search.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialSettingsCuesXaiXSearch {
  /// Returns a new [SocialSettingsCuesXaiXSearch] instance.
  SocialSettingsCuesXaiXSearch({

     this.enabled = false,

     this.maxResults = 20,
  });

  @JsonKey(
    defaultValue: false,
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



          // minimum: 1
  @JsonKey(
    defaultValue: 20,
    name: r'maxResults',
    required: false,
    includeIfNull: false,
  )


  final int? maxResults;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialSettingsCuesXaiXSearch &&
      other.enabled == enabled &&
      other.maxResults == maxResults;

    @override
    int get hashCode =>
        enabled.hashCode +
        maxResults.hashCode;

  factory SocialSettingsCuesXaiXSearch.fromJson(Map<String, dynamic> json) => _$SocialSettingsCuesXaiXSearchFromJson(json);

  Map<String, dynamic> toJson() => _$SocialSettingsCuesXaiXSearchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

