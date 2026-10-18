//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/precursor_test_result_lag.dart';
import 'package:foresignal_api/src/model/precursor_test_result_reverse.dart';
import 'package:foresignal_api/src/model/backtest_caveat.dart';
import 'package:foresignal_api/src/model/precursor_test_result_hit_rate_ci.dart';
import 'package:foresignal_api/src/model/precursor_test_result_multiple_testing.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_test_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorTestResult {
  /// Returns a new [PrecursorTestResult] instance.
  PrecursorTestResult({

    required  this.testerVersion,

    required  this.nAntecedent,

     this.nConsequent,

    required  this.hits,

     this.hitRate,

     this.hitRateCi,

     this.lag,

     this.baseRate,

     this.lift,

     this.pValue,

     this.permutations,

     this.seed,

     this.reverse,

     this.coOccurrenceNotPrecedence,

    required  this.verdict,

     this.multipleTesting,

    required  this.statement,

    required  this.caveats,
  });

  @JsonKey(
    
    name: r'testerVersion',
    required: true,
    includeIfNull: false,
  )


  final String testerVersion;



  @JsonKey(
    
    name: r'nAntecedent',
    required: true,
    includeIfNull: false,
  )


  final int nAntecedent;



  @JsonKey(
    
    name: r'nConsequent',
    required: false,
    includeIfNull: false,
  )


  final int? nConsequent;



  @JsonKey(
    
    name: r'hits',
    required: true,
    includeIfNull: false,
  )


  final int hits;



  @JsonKey(
    
    name: r'hitRate',
    required: false,
    includeIfNull: false,
  )


  final num? hitRate;



  @JsonKey(
    
    name: r'hitRateCi',
    required: false,
    includeIfNull: false,
  )


  final PrecursorTestResultHitRateCi? hitRateCi;



  @JsonKey(
    
    name: r'lag',
    required: false,
    includeIfNull: false,
  )


  final PrecursorTestResultLag? lag;



      /// Mean hit rate of 200 circular time-shifted copies
  @JsonKey(
    
    name: r'baseRate',
    required: false,
    includeIfNull: false,
  )


  final num? baseRate;



  @JsonKey(
    
    name: r'lift',
    required: false,
    includeIfNull: false,
  )


  final num? lift;



      /// (1 + #shifted ≥ observed) ÷ 201
  @JsonKey(
    
    name: r'pValue',
    required: false,
    includeIfNull: false,
  )


  final num? pValue;



  @JsonKey(
    
    name: r'permutations',
    required: false,
    includeIfNull: false,
  )


  final PrecursorTestResultPermutationsEnum? permutations;



      /// Deterministic seed (hash of definition + range)
  @JsonKey(
    
    name: r'seed',
    required: false,
    includeIfNull: false,
  )


  final String? seed;



  @JsonKey(
    
    name: r'reverse',
    required: false,
    includeIfNull: false,
  )


  final PrecursorTestResultReverse? reverse;



  @JsonKey(
    
    name: r'coOccurrenceNotPrecedence',
    required: false,
    includeIfNull: false,
  )


  final bool? coOccurrenceNotPrecedence;



  @JsonKey(
    
    name: r'verdict',
    required: true,
    includeIfNull: false,
  )


  final PrecursorTestResultVerdictEnum verdict;



  @JsonKey(
    
    name: r'multipleTesting',
    required: false,
    includeIfNull: false,
  )


  final PrecursorTestResultMultipleTesting? multipleTesting;



  @JsonKey(
    
    name: r'statement',
    required: true,
    includeIfNull: false,
  )


  final PrecursorTestResultStatementEnum statement;



  @JsonKey(
    
    name: r'caveats',
    required: true,
    includeIfNull: false,
  )


  final List<BacktestCaveat> caveats;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorTestResult &&
      other.testerVersion == testerVersion &&
      other.nAntecedent == nAntecedent &&
      other.nConsequent == nConsequent &&
      other.hits == hits &&
      other.hitRate == hitRate &&
      other.hitRateCi == hitRateCi &&
      other.lag == lag &&
      other.baseRate == baseRate &&
      other.lift == lift &&
      other.pValue == pValue &&
      other.permutations == permutations &&
      other.seed == seed &&
      other.reverse == reverse &&
      other.coOccurrenceNotPrecedence == coOccurrenceNotPrecedence &&
      other.verdict == verdict &&
      other.multipleTesting == multipleTesting &&
      other.statement == statement &&
      other.caveats == caveats;

    @override
    int get hashCode =>
        testerVersion.hashCode +
        nAntecedent.hashCode +
        nConsequent.hashCode +
        hits.hashCode +
        (hitRate == null ? 0 : hitRate.hashCode) +
        hitRateCi.hashCode +
        lag.hashCode +
        (baseRate == null ? 0 : baseRate.hashCode) +
        (lift == null ? 0 : lift.hashCode) +
        (pValue == null ? 0 : pValue.hashCode) +
        permutations.hashCode +
        seed.hashCode +
        reverse.hashCode +
        coOccurrenceNotPrecedence.hashCode +
        verdict.hashCode +
        multipleTesting.hashCode +
        statement.hashCode +
        caveats.hashCode;

  factory PrecursorTestResult.fromJson(Map<String, dynamic> json) => _$PrecursorTestResultFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorTestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorTestResultPermutationsEnum {
@JsonValue(200)
number200('200');

const PrecursorTestResultPermutationsEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PrecursorTestResultVerdictEnum {
@JsonValue(r'insufficient')
insufficient(r'insufficient'),
@JsonValue(r'no-evidence-of-precedence')
noEvidenceOfPrecedence(r'no-evidence-of-precedence'),
@JsonValue(r'weak')
weak(r'weak'),
@JsonValue(r'notable')
notable(r'notable');

const PrecursorTestResultVerdictEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PrecursorTestResultStatementEnum {
@JsonValue(r'association in stored data; not causation')
associationInStoredDataSemicolonNotCausation(r'association in stored data; not causation');

const PrecursorTestResultStatementEnum(this.value);

final String value;

@override
String toString() => value;
}


