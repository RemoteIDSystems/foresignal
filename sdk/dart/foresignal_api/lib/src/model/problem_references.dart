//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'problem_references.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProblemReferences {
  /// Returns a new [ProblemReferences] instance.
  ProblemReferences({

     this.rules,

     this.views,

     this.briefings,
  });

  @JsonKey(
    
    name: r'rules',
    required: false,
    includeIfNull: false,
  )


  final List<String>? rules;



  @JsonKey(
    
    name: r'views',
    required: false,
    includeIfNull: false,
  )


  final List<String>? views;



  @JsonKey(
    
    name: r'briefings',
    required: false,
    includeIfNull: false,
  )


  final List<String>? briefings;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProblemReferences &&
      other.rules == rules &&
      other.views == views &&
      other.briefings == briefings;

    @override
    int get hashCode =>
        rules.hashCode +
        views.hashCode +
        briefings.hashCode;

  factory ProblemReferences.fromJson(Map<String, dynamic> json) => _$ProblemReferencesFromJson(json);

  Map<String, dynamic> toJson() => _$ProblemReferencesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

