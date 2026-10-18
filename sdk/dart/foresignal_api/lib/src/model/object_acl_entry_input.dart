//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'object_acl_entry_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ObjectAclEntryInput {
  /// Returns a new [ObjectAclEntryInput] instance.
  ObjectAclEntryInput({

    required  this.grantee,

    required  this.role,

     this.expiresAt,
  });

      /// `principal:{id}`, `group:{id}` or `instance`
  @JsonKey(
    
    name: r'grantee',
    required: true,
    includeIfNull: false,
  )


  final String grantee;



  @JsonKey(
    
    name: r'role',
    required: true,
    includeIfNull: false,
  )


  final ObjectAclEntryInputRoleEnum role;



  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ObjectAclEntryInput &&
      other.grantee == grantee &&
      other.role == role &&
      other.expiresAt == expiresAt;

    @override
    int get hashCode =>
        grantee.hashCode +
        role.hashCode +
        (expiresAt == null ? 0 : expiresAt.hashCode);

  factory ObjectAclEntryInput.fromJson(Map<String, dynamic> json) => _$ObjectAclEntryInputFromJson(json);

  Map<String, dynamic> toJson() => _$ObjectAclEntryInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ObjectAclEntryInputRoleEnum {
@JsonValue(r'viewer')
viewer(r'viewer'),
@JsonValue(r'commenter')
commenter(r'commenter'),
@JsonValue(r'editor')
editor(r'editor'),
@JsonValue(r'owner')
owner(r'owner');

const ObjectAclEntryInputRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


