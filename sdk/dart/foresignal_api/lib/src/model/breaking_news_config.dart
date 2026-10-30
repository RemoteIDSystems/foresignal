//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'breaking_news_config.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BreakingNewsConfig {
  /// Returns a new [BreakingNewsConfig] instance.
  BreakingNewsConfig({

     this.enabled = false,

     this.ruleIds,

     this.minSeverity,

     this.cooldownMinutes = 60,
  });

  @JsonKey(
    defaultValue: false,
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// Trigger rules whose alerts fire a breaking edition
  @JsonKey(
    
    name: r'ruleIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? ruleIds;



  @JsonKey(
    
    name: r'minSeverity',
    required: false,
    includeIfNull: false,
  )


  final Severity? minSeverity;



      /// The least time between two breaking runs of the edition
          // minimum: 1
          // maximum: 10080
  @JsonKey(
    defaultValue: 60,
    name: r'cooldownMinutes',
    required: false,
    includeIfNull: false,
  )


  final int? cooldownMinutes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BreakingNewsConfig &&
      other.enabled == enabled &&
      other.ruleIds == ruleIds &&
      other.minSeverity == minSeverity &&
      other.cooldownMinutes == cooldownMinutes;

    @override
    int get hashCode =>
        enabled.hashCode +
        ruleIds.hashCode +
        minSeverity.hashCode +
        cooldownMinutes.hashCode;

  factory BreakingNewsConfig.fromJson(Map<String, dynamic> json) => _$BreakingNewsConfigFromJson(json);

  Map<String, dynamic> toJson() => _$BreakingNewsConfigToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

