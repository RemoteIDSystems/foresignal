//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/benchmark_case_expectations_questions_inner.dart';
import 'package:foresignal_api/src/model/benchmark_case_expectations_precursors_inner.dart';
import 'package:foresignal_api/src/model/benchmark_case_expectations_findings_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'benchmark_case_expectations.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BenchmarkCaseExpectations {
  /// Returns a new [BenchmarkCaseExpectations] instance.
  BenchmarkCaseExpectations({

     this.findings,

     this.precursors,

     this.questions,

     this.notes,
  });

  @JsonKey(
    
    name: r'findings',
    required: false,
    includeIfNull: false,
  )


  final List<BenchmarkCaseExpectationsFindingsInner>? findings;



  @JsonKey(
    
    name: r'precursors',
    required: false,
    includeIfNull: false,
  )


  final List<BenchmarkCaseExpectationsPrecursorsInner>? precursors;



  @JsonKey(
    
    name: r'questions',
    required: false,
    includeIfNull: false,
  )


  final List<BenchmarkCaseExpectationsQuestionsInner>? questions;



  @JsonKey(
    
    name: r'notes',
    required: false,
    includeIfNull: false,
  )


  final String? notes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BenchmarkCaseExpectations &&
      other.findings == findings &&
      other.precursors == precursors &&
      other.questions == questions &&
      other.notes == notes;

    @override
    int get hashCode =>
        findings.hashCode +
        precursors.hashCode +
        questions.hashCode +
        (notes == null ? 0 : notes.hashCode);

  factory BenchmarkCaseExpectations.fromJson(Map<String, dynamic> json) => _$BenchmarkCaseExpectationsFromJson(json);

  Map<String, dynamic> toJson() => _$BenchmarkCaseExpectationsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

