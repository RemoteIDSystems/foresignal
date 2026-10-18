//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'projected_impact_question_window.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProjectedImpactQuestionWindow {
  /// Returns a new [ProjectedImpactQuestionWindow] instance.
  ProjectedImpactQuestionWindow({

     this.start,

     this.end,
  });

  @JsonKey(
    
    name: r'start',
    required: false,
    includeIfNull: false,
  )


  final DateTime? start;



  @JsonKey(
    
    name: r'end',
    required: false,
    includeIfNull: false,
  )


  final DateTime? end;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProjectedImpactQuestionWindow &&
      other.start == start &&
      other.end == end;

    @override
    int get hashCode =>
        start.hashCode +
        end.hashCode;

  factory ProjectedImpactQuestionWindow.fromJson(Map<String, dynamic> json) => _$ProjectedImpactQuestionWindowFromJson(json);

  Map<String, dynamic> toJson() => _$ProjectedImpactQuestionWindowToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

