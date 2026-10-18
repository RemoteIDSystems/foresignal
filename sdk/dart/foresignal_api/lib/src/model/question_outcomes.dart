//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'question_outcomes.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class QuestionOutcomes {
  /// Returns a new [QuestionOutcomes] instance.
  QuestionOutcomes({

     this.labels,

     this.unit,

     this.lower,

     this.upper,
  });

      /// Multiple (unordered) or ordered labels, in order
  @JsonKey(
    
    name: r'labels',
    required: false,
    includeIfNull: false,
  )


  final List<String>? labels;



  @JsonKey(
    
    name: r'unit',
    required: false,
    includeIfNull: false,
  )


  final String? unit;



  @JsonKey(
    
    name: r'lower',
    required: false,
    includeIfNull: false,
  )


  final num? lower;



  @JsonKey(
    
    name: r'upper',
    required: false,
    includeIfNull: false,
  )


  final num? upper;





    @override
    bool operator ==(Object other) => identical(this, other) || other is QuestionOutcomes &&
      other.labels == labels &&
      other.unit == unit &&
      other.lower == lower &&
      other.upper == upper;

    @override
    int get hashCode =>
        labels.hashCode +
        (unit == null ? 0 : unit.hashCode) +
        (lower == null ? 0 : lower.hashCode) +
        (upper == null ? 0 : upper.hashCode);

  factory QuestionOutcomes.fromJson(Map<String, dynamic> json) => _$QuestionOutcomesFromJson(json);

  Map<String, dynamic> toJson() => _$QuestionOutcomesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

