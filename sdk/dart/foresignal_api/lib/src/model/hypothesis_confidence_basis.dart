//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_confidence_basis.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisConfidenceBasis {
  /// Returns a new [HypothesisConfidenceBasis] instance.
  HypothesisConfidenceBasis({

     this.observedIndicators,

     this.families,

     this.evidenceQuality,

     this.sourceReliability,

     this.coverage,

     this.reportingDensity,

     this.forecasterAgreement,

     this.brokenLinchpin,

     this.rule,

     this.newsCoverage,

     this.circularReporting,

     this.circularSupports,
  });

  @JsonKey(
    
    name: r'observedIndicators',
    required: false,
    includeIfNull: false,
  )


  final int? observedIndicators;



  @JsonKey(
    
    name: r'families',
    required: false,
    includeIfNull: false,
  )


  final int? families;



  @JsonKey(
    
    name: r'evidenceQuality',
    required: false,
    includeIfNull: false,
  )


  final String? evidenceQuality;



      /// Worst 109 reliability grade among sources
  @JsonKey(
    
    name: r'sourceReliability',
    required: false,
    includeIfNull: false,
  )


  final String? sourceReliability;



  @JsonKey(
    
    name: r'coverage',
    required: false,
    includeIfNull: false,
  )


  final String? coverage;



      /// `ok` or `LOW_REPORTING_DENSITY`
  @JsonKey(
    
    name: r'reportingDensity',
    required: false,
    includeIfNull: false,
  )


  final String? reportingDensity;



  @JsonKey(
    
    name: r'forecasterAgreement',
    required: false,
    includeIfNull: false,
  )


  final num? forecasterAgreement;



  @JsonKey(
    
    name: r'brokenLinchpin',
    required: false,
    includeIfNull: false,
  )


  final bool? brokenLinchpin;



      /// The rule that set the level
  @JsonKey(
    
    name: r'rule',
    required: false,
    includeIfNull: false,
  )


  final String? rule;



      /// `ok` or `SKEWED_COVERAGE` (133)
  @JsonKey(
    
    name: r'newsCoverage',
    required: false,
    includeIfNull: false,
  )


  final String? newsCoverage;



      /// `ok` or `CIRCULAR_REPORTING` (134): support of one origin counted once
  @JsonKey(
    
    name: r'circularReporting',
    required: false,
    includeIfNull: false,
  )


  final String? circularReporting;



      /// Live ACH rows of the set marked CIRCULAR_REPORTING (134)
  @JsonKey(
    
    name: r'circularSupports',
    required: false,
    includeIfNull: false,
  )


  final int? circularSupports;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisConfidenceBasis &&
      other.observedIndicators == observedIndicators &&
      other.families == families &&
      other.evidenceQuality == evidenceQuality &&
      other.sourceReliability == sourceReliability &&
      other.coverage == coverage &&
      other.reportingDensity == reportingDensity &&
      other.forecasterAgreement == forecasterAgreement &&
      other.brokenLinchpin == brokenLinchpin &&
      other.rule == rule &&
      other.newsCoverage == newsCoverage &&
      other.circularReporting == circularReporting &&
      other.circularSupports == circularSupports;

    @override
    int get hashCode =>
        observedIndicators.hashCode +
        families.hashCode +
        (evidenceQuality == null ? 0 : evidenceQuality.hashCode) +
        (sourceReliability == null ? 0 : sourceReliability.hashCode) +
        (coverage == null ? 0 : coverage.hashCode) +
        (reportingDensity == null ? 0 : reportingDensity.hashCode) +
        (forecasterAgreement == null ? 0 : forecasterAgreement.hashCode) +
        brokenLinchpin.hashCode +
        (rule == null ? 0 : rule.hashCode) +
        (newsCoverage == null ? 0 : newsCoverage.hashCode) +
        (circularReporting == null ? 0 : circularReporting.hashCode) +
        (circularSupports == null ? 0 : circularSupports.hashCode);

  factory HypothesisConfidenceBasis.fromJson(Map<String, dynamic> json) => _$HypothesisConfidenceBasisFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisConfidenceBasisToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

