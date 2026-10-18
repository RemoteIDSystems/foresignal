//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tag_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TagInput {
  /// Returns a new [TagInput] instance.
  TagInput({

    required  this.name,

     this.namespace,

     this.color,

     this.description,

     this.visibility,

     this.tlp,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'namespace',
    required: false,
    includeIfNull: false,
  )


  final TagInputNamespaceEnum? namespace;



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
    bool operator ==(Object other) => identical(this, other) || other is TagInput &&
      other.name == name &&
      other.namespace == namespace &&
      other.color == color &&
      other.description == description &&
      other.visibility == visibility &&
      other.tlp == tlp;

    @override
    int get hashCode =>
        name.hashCode +
        namespace.hashCode +
        (color == null ? 0 : color.hashCode) +
        (description == null ? 0 : description.hashCode) +
        visibility.hashCode +
        tlp.hashCode;

  factory TagInput.fromJson(Map<String, dynamic> json) => _$TagInputFromJson(json);

  Map<String, dynamic> toJson() => _$TagInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TagInputNamespaceEnum {
@JsonValue(r'personal')
personal(r'personal'),
@JsonValue(r'shared')
shared(r'shared');

const TagInputNamespaceEnum(this.value);

final String value;

@override
String toString() => value;
}


