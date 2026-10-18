//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/lead_time_stats_filters.dart';
import 'package:foresignal_api/src/model/caveat.dart';
import 'package:foresignal_api/src/model/lead_time_stats_groups_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'lead_time_stats.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LeadTimeStats {
  /// Returns a new [LeadTimeStats] instance.
  LeadTimeStats({

    required  this.generatedAt,

    required  this.trainingCutoff,

     this.filters,

    required  this.groups,

     this.caveats,
  });

  @JsonKey(
    
    name: r'generatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime generatedAt;



      /// Comparisons evaluated after this are out of sample
  @JsonKey(
    
    name: r'trainingCutoff',
    required: true,
    includeIfNull: false,
  )


  final DateTime trainingCutoff;



  @JsonKey(
    
    name: r'filters',
    required: false,
    includeIfNull: false,
  )


  final LeadTimeStatsFilters? filters;



  @JsonKey(
    
    name: r'groups',
    required: true,
    includeIfNull: false,
  )


  final List<LeadTimeStatsGroupsInner> groups;



  @JsonKey(
    
    name: r'caveats',
    required: false,
    includeIfNull: false,
  )


  final List<Caveat>? caveats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LeadTimeStats &&
      other.generatedAt == generatedAt &&
      other.trainingCutoff == trainingCutoff &&
      other.filters == filters &&
      other.groups == groups &&
      other.caveats == caveats;

    @override
    int get hashCode =>
        generatedAt.hashCode +
        trainingCutoff.hashCode +
        filters.hashCode +
        groups.hashCode +
        caveats.hashCode;

  factory LeadTimeStats.fromJson(Map<String, dynamic> json) => _$LeadTimeStatsFromJson(json);

  Map<String, dynamic> toJson() => _$LeadTimeStatsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

