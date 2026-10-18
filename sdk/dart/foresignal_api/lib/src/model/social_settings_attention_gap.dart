//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_settings_attention_gap.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialSettingsAttentionGap {
  /// Returns a new [SocialSettingsAttentionGap] instance.
  SocialSettingsAttentionGap({

     this.minSeverity,

     this.percentile = 10,

     this.lookbackDays = 365,

     this.windowHours = 6,
  });

      /// Minimum finding severity; situation escalations and critical alerts always qualify
  @JsonKey(
    
    name: r'minSeverity',
    required: false,
    includeIfNull: false,
  )


  final SocialSettingsAttentionGapMinSeverityEnum? minSeverity;



          // minimum: 0
          // maximum: 50
  @JsonKey(
    defaultValue: 10,
    name: r'percentile',
    required: false,
    includeIfNull: false,
  )


  final num? percentile;



          // minimum: 30
  @JsonKey(
    defaultValue: 365,
    name: r'lookbackDays',
    required: false,
    includeIfNull: false,
  )


  final int? lookbackDays;



          // minimum: 1
  @JsonKey(
    defaultValue: 6,
    name: r'windowHours',
    required: false,
    includeIfNull: false,
  )


  final int? windowHours;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialSettingsAttentionGap &&
      other.minSeverity == minSeverity &&
      other.percentile == percentile &&
      other.lookbackDays == lookbackDays &&
      other.windowHours == windowHours;

    @override
    int get hashCode =>
        minSeverity.hashCode +
        percentile.hashCode +
        lookbackDays.hashCode +
        windowHours.hashCode;

  factory SocialSettingsAttentionGap.fromJson(Map<String, dynamic> json) => _$SocialSettingsAttentionGapFromJson(json);

  Map<String, dynamic> toJson() => _$SocialSettingsAttentionGapToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Minimum finding severity; situation escalations and critical alerts always qualify
enum SocialSettingsAttentionGapMinSeverityEnum {
@JsonValue(r'notable')
notable(r'notable'),
@JsonValue(r'significant')
significant(r'significant');

const SocialSettingsAttentionGapMinSeverityEnum(this.value);

final String value;

@override
String toString() => value;
}


