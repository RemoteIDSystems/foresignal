//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edition_summary.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EditionSummary {
  /// Returns a new [EditionSummary] instance.
  EditionSummary({

    required  this.briefingId,

    required  this.name,

    required  this.enabled,

     this.outputLanguage,

    required  this.schedule,

    required  this.timezone,

     this.nextRunAt,

     this.lastRunAt,

     this.lastRunStatus,

     this.latestRunId,

     this.siteUrl,
  });

  @JsonKey(
    
    name: r'briefingId',
    required: true,
    includeIfNull: false,
  )


  final String briefingId;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// BCP 47 language tag, for example `en`, `es` or `pt-BR`
  @JsonKey(
    
    name: r'outputLanguage',
    required: false,
    includeIfNull: false,
  )


  final String? outputLanguage;



      /// 5-field cron
  @JsonKey(
    
    name: r'schedule',
    required: true,
    includeIfNull: false,
  )


  final String schedule;



  @JsonKey(
    
    name: r'timezone',
    required: true,
    includeIfNull: false,
  )


  final String timezone;



  @JsonKey(
    
    name: r'nextRunAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? nextRunAt;



  @JsonKey(
    
    name: r'lastRunAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastRunAt;



  @JsonKey(
    
    name: r'lastRunStatus',
    required: false,
    includeIfNull: false,
  )


  final String? lastRunStatus;



      /// The latest completed run, or null before the first
  @JsonKey(
    
    name: r'latestRunId',
    required: false,
    includeIfNull: false,
  )


  final String? latestRunId;



      /// The edition site (`getEditionSite`), or null when hosting is off
  @JsonKey(
    
    name: r'siteUrl',
    required: false,
    includeIfNull: false,
  )


  final String? siteUrl;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EditionSummary &&
      other.briefingId == briefingId &&
      other.name == name &&
      other.enabled == enabled &&
      other.outputLanguage == outputLanguage &&
      other.schedule == schedule &&
      other.timezone == timezone &&
      other.nextRunAt == nextRunAt &&
      other.lastRunAt == lastRunAt &&
      other.lastRunStatus == lastRunStatus &&
      other.latestRunId == latestRunId &&
      other.siteUrl == siteUrl;

    @override
    int get hashCode =>
        briefingId.hashCode +
        name.hashCode +
        enabled.hashCode +
        outputLanguage.hashCode +
        schedule.hashCode +
        timezone.hashCode +
        (nextRunAt == null ? 0 : nextRunAt.hashCode) +
        (lastRunAt == null ? 0 : lastRunAt.hashCode) +
        (lastRunStatus == null ? 0 : lastRunStatus.hashCode) +
        (latestRunId == null ? 0 : latestRunId.hashCode) +
        (siteUrl == null ? 0 : siteUrl.hashCode);

  factory EditionSummary.fromJson(Map<String, dynamic> json) => _$EditionSummaryFromJson(json);

  Map<String, dynamic> toJson() => _$EditionSummaryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

