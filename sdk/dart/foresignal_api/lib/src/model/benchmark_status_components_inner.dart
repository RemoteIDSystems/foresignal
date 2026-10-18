//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/benchmark_status_components_inner_gating.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'benchmark_status_components_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BenchmarkStatusComponentsInner {
  /// Returns a new [BenchmarkStatusComponentsInner] instance.
  BenchmarkStatusComponentsInner({

    required  this.component,

     this.componentVersion,

    required  this.status,

     this.runId,

     this.suite,

     this.finishedAt,

     this.versionChanged,

     this.gating,
  });

      /// Benchmarked component: `change-point`, `anomaly`, `event-study`, `co-occurrence`, `evidence`, `canonical-linking`, `precursor`, `situation-linker`, `forecaster:base-rate`, `forecaster:analog`, `forecaster:precursor` or `forecaster:ensemble`
  @JsonKey(
    
    name: r'component',
    required: true,
    includeIfNull: false,
  )


  final String component;



  @JsonKey(
    
    name: r'componentVersion',
    required: false,
    includeIfNull: false,
  )


  final String? componentVersion;



      /// `unknown` when never benchmarked
  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final BenchmarkStatusComponentsInnerStatusEnum status;



      /// ULID identifier
  @JsonKey(
    
    name: r'runId',
    required: false,
    includeIfNull: false,
  )


  final String? runId;



  @JsonKey(
    
    name: r'suite',
    required: false,
    includeIfNull: false,
  )


  final BenchmarkStatusComponentsInnerSuiteEnum? suite;



  @JsonKey(
    
    name: r'finishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? finishedAt;



      /// The component's version differs from the one last benchmarked
  @JsonKey(
    
    name: r'versionChanged',
    required: false,
    includeIfNull: false,
  )


  final bool? versionChanged;



  @JsonKey(
    
    name: r'gating',
    required: false,
    includeIfNull: false,
  )


  final BenchmarkStatusComponentsInnerGating? gating;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BenchmarkStatusComponentsInner &&
      other.component == component &&
      other.componentVersion == componentVersion &&
      other.status == status &&
      other.runId == runId &&
      other.suite == suite &&
      other.finishedAt == finishedAt &&
      other.versionChanged == versionChanged &&
      other.gating == gating;

    @override
    int get hashCode =>
        component.hashCode +
        componentVersion.hashCode +
        status.hashCode +
        (runId == null ? 0 : runId.hashCode) +
        (suite == null ? 0 : suite.hashCode) +
        (finishedAt == null ? 0 : finishedAt.hashCode) +
        versionChanged.hashCode +
        gating.hashCode;

  factory BenchmarkStatusComponentsInner.fromJson(Map<String, dynamic> json) => _$BenchmarkStatusComponentsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$BenchmarkStatusComponentsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// `unknown` when never benchmarked
enum BenchmarkStatusComponentsInnerStatusEnum {
@JsonValue(r'pass')
pass(r'pass'),
@JsonValue(r'warn')
warn(r'warn'),
@JsonValue(r'fail')
fail(r'fail'),
@JsonValue(r'unknown')
unknown(r'unknown');

const BenchmarkStatusComponentsInnerStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum BenchmarkStatusComponentsInnerSuiteEnum {
@JsonValue(r'synthetic')
synthetic(r'synthetic'),
@JsonValue(r'historical')
historical(r'historical'),
@JsonValue(r'all')
all(r'all');

const BenchmarkStatusComponentsInnerSuiteEnum(this.value);

final String value;

@override
String toString() => value;
}


