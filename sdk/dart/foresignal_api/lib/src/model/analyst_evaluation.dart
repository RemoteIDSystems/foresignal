//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/analyst_evaluation_profiles_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_evaluation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystEvaluation {
  /// Returns a new [AnalystEvaluation] instance.
  AnalystEvaluation({

    required  this.since,

    required  this.profiles,
  });

  @JsonKey(
    
    name: r'since',
    required: true,
    includeIfNull: false,
  )


  final DateTime since;



  @JsonKey(
    
    name: r'profiles',
    required: true,
    includeIfNull: false,
  )


  final List<AnalystEvaluationProfilesInner> profiles;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystEvaluation &&
      other.since == since &&
      other.profiles == profiles;

    @override
    int get hashCode =>
        since.hashCode +
        profiles.hashCode;

  factory AnalystEvaluation.fromJson(Map<String, dynamic> json) => _$AnalystEvaluationFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystEvaluationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

