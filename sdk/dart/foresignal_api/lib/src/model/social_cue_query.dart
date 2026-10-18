//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/social_platform_id.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_cue_query.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialCueQuery {
  /// Returns a new [SocialCueQuery] instance.
  SocialCueQuery({

    required  this.platform,

    required  this.query,

     this.language,

     this.estimatedCostUsd,
  });

  @JsonKey(
    
    name: r'platform',
    required: true,
    includeIfNull: false,
  )


  final SocialPlatformId platform;



      /// Exact query string sent
  @JsonKey(
    
    name: r'query',
    required: true,
    includeIfNull: false,
  )


  final String query;



  @JsonKey(
    
    name: r'language',
    required: false,
    includeIfNull: false,
  )


  final String? language;



          // minimum: 0
  @JsonKey(
    
    name: r'estimatedCostUsd',
    required: false,
    includeIfNull: false,
  )


  final num? estimatedCostUsd;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialCueQuery &&
      other.platform == platform &&
      other.query == query &&
      other.language == language &&
      other.estimatedCostUsd == estimatedCostUsd;

    @override
    int get hashCode =>
        platform.hashCode +
        query.hashCode +
        (language == null ? 0 : language.hashCode) +
        (estimatedCostUsd == null ? 0 : estimatedCostUsd.hashCode);

  factory SocialCueQuery.fromJson(Map<String, dynamic> json) => _$SocialCueQueryFromJson(json);

  Map<String, dynamic> toJson() => _$SocialCueQueryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

