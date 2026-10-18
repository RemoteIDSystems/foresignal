//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'benchmark_status_components_inner_gating.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BenchmarkStatusComponentsInnerGating {
  /// Returns a new [BenchmarkStatusComponentsInnerGating] instance.
  BenchmarkStatusComponentsInnerGating({

     this.proposerAutoExecution,

     this.llmEnsembleWeight,

     this.findingCaveat,
  });

  @JsonKey(
    
    name: r'proposerAutoExecution',
    required: false,
    includeIfNull: false,
  )


  final BenchmarkStatusComponentsInnerGatingProposerAutoExecutionEnum? proposerAutoExecution;



  @JsonKey(
    
    name: r'llmEnsembleWeight',
    required: false,
    includeIfNull: false,
  )


  final BenchmarkStatusComponentsInnerGatingLlmEnsembleWeightEnum? llmEnsembleWeight;



      /// `BENCHMARK_FAIL` while failing
  @JsonKey(
    
    name: r'findingCaveat',
    required: false,
    includeIfNull: false,
  )


  final String? findingCaveat;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BenchmarkStatusComponentsInnerGating &&
      other.proposerAutoExecution == proposerAutoExecution &&
      other.llmEnsembleWeight == llmEnsembleWeight &&
      other.findingCaveat == findingCaveat;

    @override
    int get hashCode =>
        proposerAutoExecution.hashCode +
        llmEnsembleWeight.hashCode +
        (findingCaveat == null ? 0 : findingCaveat.hashCode);

  factory BenchmarkStatusComponentsInnerGating.fromJson(Map<String, dynamic> json) => _$BenchmarkStatusComponentsInnerGatingFromJson(json);

  Map<String, dynamic> toJson() => _$BenchmarkStatusComponentsInnerGatingToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BenchmarkStatusComponentsInnerGatingProposerAutoExecutionEnum {
@JsonValue(r'allowed')
allowed(r'allowed'),
@JsonValue(r'degraded')
degraded(r'degraded');

const BenchmarkStatusComponentsInnerGatingProposerAutoExecutionEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BenchmarkStatusComponentsInnerGatingLlmEnsembleWeightEnum {
@JsonValue(r'allowed')
allowed(r'allowed'),
@JsonValue(r'degraded')
degraded(r'degraded');

const BenchmarkStatusComponentsInnerGatingLlmEnsembleWeightEnum(this.value);

final String value;

@override
String toString() => value;
}


