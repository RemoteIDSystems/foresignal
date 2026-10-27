//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'aggregator_test_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AggregatorTestResult {
  /// Returns a new [AggregatorTestResult] instance.
  AggregatorTestResult({

    required  this.ok,

    required  this.reachable,

     this.sampleCount,

     this.message,

     this.checkedAt,
  });

  @JsonKey(
    
    name: r'ok',
    required: true,
    includeIfNull: false,
  )


  final bool ok;



  @JsonKey(
    
    name: r'reachable',
    required: true,
    includeIfNull: false,
  )


  final bool reachable;



      /// Objects seen in a sample read
  @JsonKey(
    
    name: r'sampleCount',
    required: false,
    includeIfNull: false,
  )


  final int? sampleCount;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



  @JsonKey(
    
    name: r'checkedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? checkedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AggregatorTestResult &&
      other.ok == ok &&
      other.reachable == reachable &&
      other.sampleCount == sampleCount &&
      other.message == message &&
      other.checkedAt == checkedAt;

    @override
    int get hashCode =>
        ok.hashCode +
        reachable.hashCode +
        (sampleCount == null ? 0 : sampleCount.hashCode) +
        (message == null ? 0 : message.hashCode) +
        checkedAt.hashCode;

  factory AggregatorTestResult.fromJson(Map<String, dynamic> json) => _$AggregatorTestResultFromJson(json);

  Map<String, dynamic> toJson() => _$AggregatorTestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

