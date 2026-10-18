//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/mapping_value_spec_one_of1.dart';
import 'package:foresignal_api/src/model/mapping_value_spec_one_of2.dart';
import 'package:foresignal_api/src/model/mapping_value_spec_one_of.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_value_spec.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingValueSpec {
  /// Returns a new [MappingValueSpec] instance.
  MappingValueSpec({

    required  this.path,

    required  this.template,

    required  this.const_,
  });

  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final String path;



  @JsonKey(
    
    name: r'template',
    required: true,
    includeIfNull: false,
  )


  final String template;



  @JsonKey(
    
    name: r'const',
    required: true,
    includeIfNull: true,
  )


  final Object? const_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingValueSpec &&
      other.path == path &&
      other.template == template &&
      other.const_ == const_;

    @override
    int get hashCode =>
        path.hashCode +
        template.hashCode +
        (const_ == null ? 0 : const_.hashCode);

  factory MappingValueSpec.fromJson(Map<String, dynamic> json) => _$MappingValueSpecFromJson(json);

  Map<String, dynamic> toJson() => _$MappingValueSpecToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

