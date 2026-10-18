//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tag.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Tag {
  /// Returns a new [Tag] instance.
  Tag({

    required  this.id,

    required  this.name,

    required  this.namespace,

     this.color,

     this.description,

    required  this.owner,

    required  this.visibility,

     this.tlp,

     this.objectCount,

    required  this.createdAt,

     this.updatedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'namespace',
    required: true,
    includeIfNull: false,
  )


  final TagNamespaceEnum namespace;



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
    
    name: r'owner',
    required: true,
    includeIfNull: false,
  )


  final PrincipalSummary owner;



  @JsonKey(
    
    name: r'visibility',
    required: true,
    includeIfNull: false,
  )


  final Visibility visibility;



  @JsonKey(
    
    name: r'tlp',
    required: false,
    includeIfNull: false,
  )


  final Tlp? tlp;



      /// Objects carrying this tag that the caller can see
  @JsonKey(
    
    name: r'objectCount',
    required: false,
    includeIfNull: false,
  )


  final int? objectCount;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Tag &&
      other.id == id &&
      other.name == name &&
      other.namespace == namespace &&
      other.color == color &&
      other.description == description &&
      other.owner == owner &&
      other.visibility == visibility &&
      other.tlp == tlp &&
      other.objectCount == objectCount &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        namespace.hashCode +
        (color == null ? 0 : color.hashCode) +
        (description == null ? 0 : description.hashCode) +
        owner.hashCode +
        visibility.hashCode +
        tlp.hashCode +
        objectCount.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory Tag.fromJson(Map<String, dynamic> json) => _$TagFromJson(json);

  Map<String, dynamic> toJson() => _$TagToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TagNamespaceEnum {
@JsonValue(r'personal')
personal(r'personal'),
@JsonValue(r'shared')
shared(r'shared');

const TagNamespaceEnum(this.value);

final String value;

@override
String toString() => value;
}


