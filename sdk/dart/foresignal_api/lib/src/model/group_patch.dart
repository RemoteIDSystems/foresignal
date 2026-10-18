//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'group_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GroupPatch {
  /// Returns a new [GroupPatch] instance.
  GroupPatch({

     this.name,

     this.description,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GroupPatch &&
      other.name == name &&
      other.description == description;

    @override
    int get hashCode =>
        name.hashCode +
        (description == null ? 0 : description.hashCode);

  factory GroupPatch.fromJson(Map<String, dynamic> json) => _$GroupPatchFromJson(json);

  Map<String, dynamic> toJson() => _$GroupPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

