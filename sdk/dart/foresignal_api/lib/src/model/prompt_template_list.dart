//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/prompt_template_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'prompt_template_list.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PromptTemplateList {
  /// Returns a new [PromptTemplateList] instance.
  PromptTemplateList({

    required  this.items,
  });

  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<PromptTemplateSummary> items;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PromptTemplateList &&
      other.items == items;

    @override
    int get hashCode =>
        items.hashCode;

  factory PromptTemplateList.fromJson(Map<String, dynamic> json) => _$PromptTemplateListFromJson(json);

  Map<String, dynamic> toJson() => _$PromptTemplateListToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

