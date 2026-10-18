//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'benchmark_case_expectations_precursors_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BenchmarkCaseExpectationsPrecursorsInner {
  /// Returns a new [BenchmarkCaseExpectationsPrecursorsInner] instance.
  BenchmarkCaseExpectationsPrecursorsInner({

     this.precursorId,

     this.leader,

     this.follower,

    required  this.expected,

     this.lagHours,
  });

  @JsonKey(
    
    name: r'precursorId',
    required: false,
    includeIfNull: false,
  )


  final String? precursorId;



      /// Leading family or series ref
  @JsonKey(
    
    name: r'leader',
    required: false,
    includeIfNull: false,
  )


  final String? leader;



      /// Following family or series ref
  @JsonKey(
    
    name: r'follower',
    required: false,
    includeIfNull: false,
  )


  final String? follower;



  @JsonKey(
    
    name: r'expected',
    required: true,
    includeIfNull: false,
  )


  final BenchmarkCaseExpectationsPrecursorsInnerExpectedEnum expected;



  @JsonKey(
    
    name: r'lagHours',
    required: false,
    includeIfNull: false,
  )


  final num? lagHours;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BenchmarkCaseExpectationsPrecursorsInner &&
      other.precursorId == precursorId &&
      other.leader == leader &&
      other.follower == follower &&
      other.expected == expected &&
      other.lagHours == lagHours;

    @override
    int get hashCode =>
        (precursorId == null ? 0 : precursorId.hashCode) +
        (leader == null ? 0 : leader.hashCode) +
        (follower == null ? 0 : follower.hashCode) +
        expected.hashCode +
        (lagHours == null ? 0 : lagHours.hashCode);

  factory BenchmarkCaseExpectationsPrecursorsInner.fromJson(Map<String, dynamic> json) => _$BenchmarkCaseExpectationsPrecursorsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$BenchmarkCaseExpectationsPrecursorsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BenchmarkCaseExpectationsPrecursorsInnerExpectedEnum {
@JsonValue(r'validated')
validated(r'validated'),
@JsonValue(r'not-validated')
notValidated(r'not-validated');

const BenchmarkCaseExpectationsPrecursorsInnerExpectedEnum(this.value);

final String value;

@override
String toString() => value;
}


