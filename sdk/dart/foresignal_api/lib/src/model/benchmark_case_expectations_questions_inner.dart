//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'benchmark_case_expectations_questions_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class BenchmarkCaseExpectationsQuestionsInner {
  /// Returns a new [BenchmarkCaseExpectationsQuestionsInner] instance.
  BenchmarkCaseExpectationsQuestionsInner({

    required  this.text,

    required  this.type,

    required  this.outcome,

     this.resolvedAt,
  });

  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final BenchmarkCaseExpectationsQuestionsInnerTypeEnum type;



  @JsonKey(
    
    name: r'outcome',
    required: true,
    includeIfNull: true,
  )


  final Object? outcome;



  @JsonKey(
    
    name: r'resolvedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? resolvedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is BenchmarkCaseExpectationsQuestionsInner &&
      other.text == text &&
      other.type == type &&
      other.outcome == outcome &&
      other.resolvedAt == resolvedAt;

    @override
    int get hashCode =>
        text.hashCode +
        type.hashCode +
        (outcome == null ? 0 : outcome.hashCode) +
        (resolvedAt == null ? 0 : resolvedAt.hashCode);

  factory BenchmarkCaseExpectationsQuestionsInner.fromJson(Map<String, dynamic> json) => _$BenchmarkCaseExpectationsQuestionsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$BenchmarkCaseExpectationsQuestionsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum BenchmarkCaseExpectationsQuestionsInnerTypeEnum {
@JsonValue(r'binary')
binary(r'binary'),
@JsonValue(r'numeric')
numeric(r'numeric');

const BenchmarkCaseExpectationsQuestionsInnerTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


