//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'lead_time_stats_filters.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LeadTimeStatsFilters {
  /// Returns a new [LeadTimeStatsFilters] instance.
  LeadTimeStatsFilters({

     this.claimType,

     this.platform,

     this.region,

     this.foundBy,

     this.since,
  });

  @JsonKey(
    
    name: r'claimType',
    required: false,
    includeIfNull: false,
  )


  final String? claimType;



  @JsonKey(
    
    name: r'platform',
    required: false,
    includeIfNull: false,
  )


  final String? platform;



  @JsonKey(
    
    name: r'region',
    required: false,
    includeIfNull: false,
  )


  final String? region;



  @JsonKey(
    
    name: r'foundBy',
    required: false,
    includeIfNull: false,
  )


  final LeadTimeStatsFiltersFoundByEnum? foundBy;



  @JsonKey(
    
    name: r'since',
    required: false,
    includeIfNull: false,
  )


  final DateTime? since;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LeadTimeStatsFilters &&
      other.claimType == claimType &&
      other.platform == platform &&
      other.region == region &&
      other.foundBy == foundBy &&
      other.since == since;

    @override
    int get hashCode =>
        (claimType == null ? 0 : claimType.hashCode) +
        (platform == null ? 0 : platform.hashCode) +
        (region == null ? 0 : region.hashCode) +
        (foundBy == null ? 0 : foundBy.hashCode) +
        (since == null ? 0 : since.hashCode);

  factory LeadTimeStatsFilters.fromJson(Map<String, dynamic> json) => _$LeadTimeStatsFiltersFromJson(json);

  Map<String, dynamic> toJson() => _$LeadTimeStatsFiltersToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum LeadTimeStatsFiltersFoundByEnum {
@JsonValue(r'organic')
organic(r'organic'),
@JsonValue(r'cue')
cue(r'cue');

const LeadTimeStatsFiltersFoundByEnum(this.value);

final String value;

@override
String toString() => value;
}


