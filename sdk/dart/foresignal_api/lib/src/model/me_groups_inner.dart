//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'me_groups_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MeGroupsInner {
  /// Returns a new [MeGroupsInner] instance.
  MeGroupsInner({

    required  this.id,

    required  this.name,

    required  this.role,
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
    
    name: r'role',
    required: true,
    includeIfNull: false,
  )


  final MeGroupsInnerRoleEnum role;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MeGroupsInner &&
      other.id == id &&
      other.name == name &&
      other.role == role;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        role.hashCode;

  factory MeGroupsInner.fromJson(Map<String, dynamic> json) => _$MeGroupsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$MeGroupsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MeGroupsInnerRoleEnum {
@JsonValue(r'member')
member(r'member'),
@JsonValue(r'manager')
manager(r'manager');

const MeGroupsInnerRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


