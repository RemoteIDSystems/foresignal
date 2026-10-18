//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'scorecard_entry_interval_coverage.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ScorecardEntryIntervalCoverage {
  /// Returns a new [ScorecardEntryIntervalCoverage] instance.
  ScorecardEntryIntervalCoverage({

     this.p05p95,

     this.p25p75,
  });

  @JsonKey(
    
    name: r'p05p95',
    required: false,
    includeIfNull: false,
  )


  final num? p05p95;



  @JsonKey(
    
    name: r'p25p75',
    required: false,
    includeIfNull: false,
  )


  final num? p25p75;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ScorecardEntryIntervalCoverage &&
      other.p05p95 == p05p95 &&
      other.p25p75 == p25p75;

    @override
    int get hashCode =>
        (p05p95 == null ? 0 : p05p95.hashCode) +
        (p25p75 == null ? 0 : p25p75.hashCode);

  factory ScorecardEntryIntervalCoverage.fromJson(Map<String, dynamic> json) => _$ScorecardEntryIntervalCoverageFromJson(json);

  Map<String, dynamic> toJson() => _$ScorecardEntryIntervalCoverageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

