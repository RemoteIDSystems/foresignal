//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_value_spec_one_of1.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingValueSpecOneOf1 {
  /// Returns a new [MappingValueSpecOneOf1] instance.
  MappingValueSpecOneOf1({

    required  this.template,
  });

  @JsonKey(
    
    name: r'template',
    required: true,
    includeIfNull: false,
  )


  final String template;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingValueSpecOneOf1 &&
      other.template == template;

    @override
    int get hashCode =>
        template.hashCode;

  factory MappingValueSpecOneOf1.fromJson(Map<String, dynamic> json) => _$MappingValueSpecOneOf1FromJson(json);

  Map<String, dynamic> toJson() => _$MappingValueSpecOneOf1ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

