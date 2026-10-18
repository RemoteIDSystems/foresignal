//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_social_section_attention_gap.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationSocialSectionAttentionGap {
  /// Returns a new [SituationSocialSectionAttentionGap] instance.
  SituationSocialSectionAttentionGap({

    required  this.findingId,

    required  this.expected,

    required  this.observed,

     this.platforms,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'findingId',
    required: true,
    includeIfNull: false,
  )


  final String findingId;



  @JsonKey(
    
    name: r'expected',
    required: true,
    includeIfNull: false,
  )


  final num expected;



  @JsonKey(
    
    name: r'observed',
    required: true,
    includeIfNull: false,
  )


  final num observed;



  @JsonKey(
    
    name: r'platforms',
    required: false,
    includeIfNull: false,
  )


  final List<String>? platforms;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationSocialSectionAttentionGap &&
      other.findingId == findingId &&
      other.expected == expected &&
      other.observed == observed &&
      other.platforms == platforms;

    @override
    int get hashCode =>
        findingId.hashCode +
        expected.hashCode +
        observed.hashCode +
        platforms.hashCode;

  factory SituationSocialSectionAttentionGap.fromJson(Map<String, dynamic> json) => _$SituationSocialSectionAttentionGapFromJson(json);

  Map<String, dynamic> toJson() => _$SituationSocialSectionAttentionGapToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

