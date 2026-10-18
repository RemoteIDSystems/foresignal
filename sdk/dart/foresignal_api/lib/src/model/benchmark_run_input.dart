//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'benchmark_run_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BenchmarkRunInput {
  /// Returns a new [BenchmarkRunInput] instance.
  BenchmarkRunInput({

    required  this.suite,

     this.components,

     this.seed,
  });

  @JsonKey(
    
    name: r'suite',
    required: true,
    includeIfNull: false,
  )


  final BenchmarkRunInputSuiteEnum suite;



      /// Components to benchmark; default all built components
  @JsonKey(
    
    name: r'components',
    required: false,
    includeIfNull: false,
  )


  final List<String>? components;



      /// Scenario seed; default the suite's fixed seed
  @JsonKey(
    
    name: r'seed',
    required: false,
    includeIfNull: false,
  )


  final int? seed;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BenchmarkRunInput &&
      other.suite == suite &&
      other.components == components &&
      other.seed == seed;

    @override
    int get hashCode =>
        suite.hashCode +
        components.hashCode +
        seed.hashCode;

  factory BenchmarkRunInput.fromJson(Map<String, dynamic> json) => _$BenchmarkRunInputFromJson(json);

  Map<String, dynamic> toJson() => _$BenchmarkRunInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BenchmarkRunInputSuiteEnum {
@JsonValue(r'synthetic')
synthetic(r'synthetic'),
@JsonValue(r'historical')
historical(r'historical'),
@JsonValue(r'all')
all(r'all');

const BenchmarkRunInputSuiteEnum(this.value);

final String value;

@override
String toString() => value;
}


