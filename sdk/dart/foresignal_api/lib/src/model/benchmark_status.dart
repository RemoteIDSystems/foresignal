//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/benchmark_status_components_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'benchmark_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BenchmarkStatus {
  /// Returns a new [BenchmarkStatus] instance.
  BenchmarkStatus({

    required  this.generatedAt,

    required  this.overall,

    required  this.components,
  });

  @JsonKey(
    
    name: r'generatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime generatedAt;



  @JsonKey(
    
    name: r'overall',
    required: true,
    includeIfNull: false,
  )


  final BenchmarkStatusOverallEnum overall;



  @JsonKey(
    
    name: r'components',
    required: true,
    includeIfNull: false,
  )


  final List<BenchmarkStatusComponentsInner> components;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BenchmarkStatus &&
      other.generatedAt == generatedAt &&
      other.overall == overall &&
      other.components == components;

    @override
    int get hashCode =>
        generatedAt.hashCode +
        overall.hashCode +
        components.hashCode;

  factory BenchmarkStatus.fromJson(Map<String, dynamic> json) => _$BenchmarkStatusFromJson(json);

  Map<String, dynamic> toJson() => _$BenchmarkStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BenchmarkStatusOverallEnum {
@JsonValue(r'pass')
pass(r'pass'),
@JsonValue(r'warn')
warn(r'warn'),
@JsonValue(r'fail')
fail(r'fail'),
@JsonValue(r'unknown')
unknown(r'unknown');

const BenchmarkStatusOverallEnum(this.value);

final String value;

@override
String toString() => value;
}


