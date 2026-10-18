//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'object_acl_entry.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ObjectAclEntry {
  /// Returns a new [ObjectAclEntry] instance.
  ObjectAclEntry({

    required  this.grantee,

     this.granteeDisplay,

    required  this.role,

     this.grantedBy,

    required  this.grantedAt,

     this.expiresAt,
  });

      /// `principal:{id}`, `group:{id}` or `instance`
  @JsonKey(
    
    name: r'grantee',
    required: true,
    includeIfNull: false,
  )


  final String grantee;



      /// Handle or group name for display
  @JsonKey(
    
    name: r'granteeDisplay',
    required: false,
    includeIfNull: false,
  )


  final String? granteeDisplay;



  @JsonKey(
    
    name: r'role',
    required: true,
    includeIfNull: false,
  )


  final ObjectAclEntryRoleEnum role;



  @JsonKey(
    
    name: r'grantedBy',
    required: false,
    includeIfNull: false,
  )


  final PrincipalSummary? grantedBy;



  @JsonKey(
    
    name: r'grantedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime grantedAt;



  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ObjectAclEntry &&
      other.grantee == grantee &&
      other.granteeDisplay == granteeDisplay &&
      other.role == role &&
      other.grantedBy == grantedBy &&
      other.grantedAt == grantedAt &&
      other.expiresAt == expiresAt;

    @override
    int get hashCode =>
        grantee.hashCode +
        (granteeDisplay == null ? 0 : granteeDisplay.hashCode) +
        role.hashCode +
        grantedBy.hashCode +
        grantedAt.hashCode +
        (expiresAt == null ? 0 : expiresAt.hashCode);

  factory ObjectAclEntry.fromJson(Map<String, dynamic> json) => _$ObjectAclEntryFromJson(json);

  Map<String, dynamic> toJson() => _$ObjectAclEntryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ObjectAclEntryRoleEnum {
@JsonValue(r'viewer')
viewer(r'viewer'),
@JsonValue(r'commenter')
commenter(r'commenter'),
@JsonValue(r'editor')
editor(r'editor'),
@JsonValue(r'owner')
owner(r'owner');

const ObjectAclEntryRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


