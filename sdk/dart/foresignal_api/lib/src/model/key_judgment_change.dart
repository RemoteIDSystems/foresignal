//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'key_judgment_change.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class KeyJudgmentChange {
  /// Returns a new [KeyJudgmentChange] instance.
  KeyJudgmentChange({

     this.previousProbability,

     this.previousText,

    required  this.explanation,
  });

          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'previousProbability',
    required: false,
    includeIfNull: false,
  )


  final num? previousProbability;



  @JsonKey(
    
    name: r'previousText',
    required: false,
    includeIfNull: false,
  )


  final String? previousText;



  @JsonKey(
    
    name: r'explanation',
    required: true,
    includeIfNull: false,
  )


  final String explanation;





    @override
    bool operator ==(Object other) => identical(this, other) || other is KeyJudgmentChange &&
      other.previousProbability == previousProbability &&
      other.previousText == previousText &&
      other.explanation == explanation;

    @override
    int get hashCode =>
        (previousProbability == null ? 0 : previousProbability.hashCode) +
        (previousText == null ? 0 : previousText.hashCode) +
        explanation.hashCode;

  factory KeyJudgmentChange.fromJson(Map<String, dynamic> json) => _$KeyJudgmentChangeFromJson(json);

  Map<String, dynamic> toJson() => _$KeyJudgmentChangeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

