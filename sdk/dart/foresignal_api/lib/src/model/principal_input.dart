//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'principal_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrincipalInput {
  /// Returns a new [PrincipalInput] instance.
  PrincipalInput({

    required  this.handle,

    required  this.displayName,

    required  this.kind,

     this.email,

     this.avatarMediaId,

     this.timezone,

     this.organization,
  });

  @JsonKey(
    
    name: r'handle',
    required: true,
    includeIfNull: false,
  )


  final String handle;



  @JsonKey(
    
    name: r'displayName',
    required: true,
    includeIfNull: false,
  )


  final String displayName;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final PrincipalInputKindEnum kind;



  @JsonKey(
    
    name: r'email',
    required: false,
    includeIfNull: false,
  )


  final String? email;



  @JsonKey(
    
    name: r'avatarMediaId',
    required: false,
    includeIfNull: false,
  )


  final String? avatarMediaId;



  @JsonKey(
    
    name: r'timezone',
    required: false,
    includeIfNull: false,
  )


  final String? timezone;



  @JsonKey(
    
    name: r'organization',
    required: false,
    includeIfNull: false,
  )


  final String? organization;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrincipalInput &&
      other.handle == handle &&
      other.displayName == displayName &&
      other.kind == kind &&
      other.email == email &&
      other.avatarMediaId == avatarMediaId &&
      other.timezone == timezone &&
      other.organization == organization;

    @override
    int get hashCode =>
        handle.hashCode +
        displayName.hashCode +
        kind.hashCode +
        (email == null ? 0 : email.hashCode) +
        (avatarMediaId == null ? 0 : avatarMediaId.hashCode) +
        (timezone == null ? 0 : timezone.hashCode) +
        (organization == null ? 0 : organization.hashCode);

  factory PrincipalInput.fromJson(Map<String, dynamic> json) => _$PrincipalInputFromJson(json);

  Map<String, dynamic> toJson() => _$PrincipalInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrincipalInputKindEnum {
@JsonValue(r'person')
person(r'person'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'service')
service(r'service');

const PrincipalInputKindEnum(this.value);

final String value;

@override
String toString() => value;
}


