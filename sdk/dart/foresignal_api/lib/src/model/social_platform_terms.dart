//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_platform_terms.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialPlatformTerms {
  /// Returns a new [SocialPlatformTerms] instance.
  SocialPlatformTerms({

     this.summary,

     this.url,

     this.costs,
  });

  @JsonKey(
    
    name: r'summary',
    required: false,
    includeIfNull: false,
  )


  final String? summary;



  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'costs',
    required: false,
    includeIfNull: false,
  )


  final String? costs;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialPlatformTerms &&
      other.summary == summary &&
      other.url == url &&
      other.costs == costs;

    @override
    int get hashCode =>
        summary.hashCode +
        (url == null ? 0 : url.hashCode) +
        (costs == null ? 0 : costs.hashCode);

  factory SocialPlatformTerms.fromJson(Map<String, dynamic> json) => _$SocialPlatformTermsFromJson(json);

  Map<String, dynamic> toJson() => _$SocialPlatformTermsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

