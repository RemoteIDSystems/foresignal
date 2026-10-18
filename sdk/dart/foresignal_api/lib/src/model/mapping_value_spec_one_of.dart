//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'mapping_value_spec_one_of.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MappingValueSpecOneOf {
  /// Returns a new [MappingValueSpecOneOf] instance.
  MappingValueSpecOneOf({

    required  this.path,
  });

  @JsonKey(
    
    name: r'path',
    required: true,
    includeIfNull: false,
  )


  final String path;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MappingValueSpecOneOf &&
      other.path == path;

    @override
    int get hashCode =>
        path.hashCode;

  factory MappingValueSpecOneOf.fromJson(Map<String, dynamic> json) => _$MappingValueSpecOneOfFromJson(json);

  Map<String, dynamic> toJson() => _$MappingValueSpecOneOfToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

