//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_evaluation_profiles_inner_tokens_per_day_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystEvaluationProfilesInnerTokensPerDayInner {
  /// Returns a new [AnalystEvaluationProfilesInnerTokensPerDayInner] instance.
  AnalystEvaluationProfilesInnerTokensPerDayInner({

    required  this.date,

    required  this.tokens,
  });

  @JsonKey(
    
    name: r'date',
    required: true,
    includeIfNull: false,
  )


  final DateTime date;



  @JsonKey(
    
    name: r'tokens',
    required: true,
    includeIfNull: false,
  )


  final int tokens;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystEvaluationProfilesInnerTokensPerDayInner &&
      other.date == date &&
      other.tokens == tokens;

    @override
    int get hashCode =>
        date.hashCode +
        tokens.hashCode;

  factory AnalystEvaluationProfilesInnerTokensPerDayInner.fromJson(Map<String, dynamic> json) => _$AnalystEvaluationProfilesInnerTokensPerDayInnerFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystEvaluationProfilesInnerTokensPerDayInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

