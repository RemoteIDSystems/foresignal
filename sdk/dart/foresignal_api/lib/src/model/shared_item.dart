//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_envelope.dart';
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'shared_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SharedItem {
  /// Returns a new [SharedItem] instance.
  SharedItem({

    required  this.envelope,

    required  this.role,

    required  this.via,

     this.groupId,

     this.sharedBy,

    required  this.sharedAt,
  });

  @JsonKey(
    
    name: r'envelope',
    required: true,
    includeIfNull: false,
  )


  final ObjectEnvelope envelope;



  @JsonKey(
    
    name: r'role',
    required: true,
    includeIfNull: false,
  )


  final SharedItemRoleEnum role;



  @JsonKey(
    
    name: r'via',
    required: true,
    includeIfNull: false,
  )


  final SharedItemViaEnum via;



      /// ULID identifier
  @JsonKey(
    
    name: r'groupId',
    required: false,
    includeIfNull: false,
  )


  final String? groupId;



  @JsonKey(
    
    name: r'sharedBy',
    required: false,
    includeIfNull: false,
  )


  final PrincipalSummary? sharedBy;



  @JsonKey(
    
    name: r'sharedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime sharedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SharedItem &&
      other.envelope == envelope &&
      other.role == role &&
      other.via == via &&
      other.groupId == groupId &&
      other.sharedBy == sharedBy &&
      other.sharedAt == sharedAt;

    @override
    int get hashCode =>
        envelope.hashCode +
        role.hashCode +
        via.hashCode +
        (groupId == null ? 0 : groupId.hashCode) +
        (sharedBy == null ? 0 : sharedBy.hashCode) +
        sharedAt.hashCode;

  factory SharedItem.fromJson(Map<String, dynamic> json) => _$SharedItemFromJson(json);

  Map<String, dynamic> toJson() => _$SharedItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SharedItemRoleEnum {
@JsonValue(r'viewer')
viewer(r'viewer'),
@JsonValue(r'commenter')
commenter(r'commenter'),
@JsonValue(r'editor')
editor(r'editor'),
@JsonValue(r'owner')
owner(r'owner');

const SharedItemRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SharedItemViaEnum {
@JsonValue(r'principal')
principal(r'principal'),
@JsonValue(r'group')
group(r'group'),
@JsonValue(r'instance')
instance(r'instance');

const SharedItemViaEnum(this.value);

final String value;

@override
String toString() => value;
}


