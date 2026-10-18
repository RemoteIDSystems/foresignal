//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_value_spec_one_of2.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingValueSpecOneOf2 {
  /// Returns a new [MappingValueSpecOneOf2] instance.
  MappingValueSpecOneOf2({

    required  this.const_,
  });

  @JsonKey(
    
    name: r'const',
    required: true,
    includeIfNull: true,
  )


  final Object? const_;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingValueSpecOneOf2 &&
      other.const_ == const_;

    @override
    int get hashCode =>
        (const_ == null ? 0 : const_.hashCode);

  factory MappingValueSpecOneOf2.fromJson(Map<String, dynamic> json) => _$MappingValueSpecOneOf2FromJson(json);

  Map<String, dynamic> toJson() => _$MappingValueSpecOneOf2ToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

