//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'cycle_packet_coverage.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CyclePacketCoverage {
  /// Returns a new [CyclePacketCoverage] instance.
  CyclePacketCoverage({

     this.failingSources,

     this.degradedSources,

     this.lowReportingDensityRegions,
  });

  @JsonKey(
    
    name: r'failingSources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? failingSources;



  @JsonKey(
    
    name: r'degradedSources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? degradedSources;



  @JsonKey(
    
    name: r'lowReportingDensityRegions',
    required: false,
    includeIfNull: false,
  )


  final List<String>? lowReportingDensityRegions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CyclePacketCoverage &&
      other.failingSources == failingSources &&
      other.degradedSources == degradedSources &&
      other.lowReportingDensityRegions == lowReportingDensityRegions;

    @override
    int get hashCode =>
        failingSources.hashCode +
        degradedSources.hashCode +
        lowReportingDensityRegions.hashCode;

  factory CyclePacketCoverage.fromJson(Map<String, dynamic> json) => _$CyclePacketCoverageFromJson(json);

  Map<String, dynamic> toJson() => _$CyclePacketCoverageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

