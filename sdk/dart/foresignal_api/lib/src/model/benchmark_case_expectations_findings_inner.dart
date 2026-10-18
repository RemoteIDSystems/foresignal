//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/finding_severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'benchmark_case_expectations_findings_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BenchmarkCaseExpectationsFindingsInner {
  /// Returns a new [BenchmarkCaseExpectationsFindingsInner] instance.
  BenchmarkCaseExpectationsFindingsInner({

    required  this.findingType,

    required  this.present,

     this.regionCodes,

     this.seriesRef,

     this.from,

     this.to,

     this.minSeverity,
  });

  @JsonKey(
    
    name: r'findingType',
    required: true,
    includeIfNull: false,
  )


  final String findingType;



  @JsonKey(
    
    name: r'present',
    required: true,
    includeIfNull: false,
  )


  final bool present;



      /// ISO 3166-1 alpha-2 country or ISO 3166-2 subdivision codes
  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;



      /// Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left, center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement, every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the platform-language denominator; complete and paged collection only)
  @JsonKey(
    
    name: r'seriesRef',
    required: false,
    includeIfNull: false,
  )


  final String? seriesRef;



  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



  @JsonKey(
    
    name: r'minSeverity',
    required: false,
    includeIfNull: false,
  )


  final FindingSeverity? minSeverity;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BenchmarkCaseExpectationsFindingsInner &&
      other.findingType == findingType &&
      other.present == present &&
      other.regionCodes == regionCodes &&
      other.seriesRef == seriesRef &&
      other.from == from &&
      other.to == to &&
      other.minSeverity == minSeverity;

    @override
    int get hashCode =>
        findingType.hashCode +
        present.hashCode +
        regionCodes.hashCode +
        (seriesRef == null ? 0 : seriesRef.hashCode) +
        (from == null ? 0 : from.hashCode) +
        (to == null ? 0 : to.hashCode) +
        (minSeverity == null ? 0 : minSeverity.hashCode);

  factory BenchmarkCaseExpectationsFindingsInner.fromJson(Map<String, dynamic> json) => _$BenchmarkCaseExpectationsFindingsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$BenchmarkCaseExpectationsFindingsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

