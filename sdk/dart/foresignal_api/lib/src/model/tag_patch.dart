//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tag_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TagPatch {
  /// Returns a new [TagPatch] instance.
  TagPatch({

     this.name,

     this.color,

     this.description,

     this.visibility,

     this.tlp,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'color',
    required: false,
    includeIfNull: false,
  )


  final String? color;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'visibility',
    required: false,
    includeIfNull: false,
  )


  final Visibility? visibility;



  @JsonKey(
    
    name: r'tlp',
    required: false,
    includeIfNull: false,
  )


  final Tlp? tlp;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TagPatch &&
      other.name == name &&
      other.color == color &&
      other.description == description &&
      other.visibility == visibility &&
      other.tlp == tlp;

    @override
    int get hashCode =>
        name.hashCode +
        (color == null ? 0 : color.hashCode) +
        (description == null ? 0 : description.hashCode) +
        visibility.hashCode +
        tlp.hashCode;

  factory TagPatch.fromJson(Map<String, dynamic> json) => _$TagPatchFromJson(json);

  Map<String, dynamic> toJson() => _$TagPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

