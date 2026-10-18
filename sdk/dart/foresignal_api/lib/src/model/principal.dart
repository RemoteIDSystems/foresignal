//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'principal.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Principal {
  /// Returns a new [Principal] instance.
  Principal({

    required  this.id,

    required  this.handle,

    required  this.displayName,

    required  this.kind,

     this.email,

     this.avatarMediaId,

     this.timezone,

     this.organization,

    required  this.disabled,

     this.tokenCount,

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


  final PrincipalKindEnum kind;



      /// Used only for this principal's email notifications; returned only to the principal itself and admins
  @JsonKey(
    
    name: r'email',
    required: false,
    includeIfNull: false,
  )


  final String? email;



      /// 043 media id
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



      /// Free text; defines TLP AMBER and AMBER+STRICT boundaries
  @JsonKey(
    
    name: r'organization',
    required: false,
    includeIfNull: false,
  )


  final String? organization;



  @JsonKey(
    
    name: r'disabled',
    required: true,
    includeIfNull: false,
  )


  final bool disabled;



      /// Active tokens (admin view)
  @JsonKey(
    
    name: r'tokenCount',
    required: false,
    includeIfNull: false,
  )


  final int? tokenCount;



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
    bool operator ==(Object other) => identical(this, other) || other is Principal &&
      other.id == id &&
      other.handle == handle &&
      other.displayName == displayName &&
      other.kind == kind &&
      other.email == email &&
      other.avatarMediaId == avatarMediaId &&
      other.timezone == timezone &&
      other.organization == organization &&
      other.disabled == disabled &&
      other.tokenCount == tokenCount &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        handle.hashCode +
        displayName.hashCode +
        kind.hashCode +
        (email == null ? 0 : email.hashCode) +
        (avatarMediaId == null ? 0 : avatarMediaId.hashCode) +
        (timezone == null ? 0 : timezone.hashCode) +
        (organization == null ? 0 : organization.hashCode) +
        disabled.hashCode +
        tokenCount.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory Principal.fromJson(Map<String, dynamic> json) => _$PrincipalFromJson(json);

  Map<String, dynamic> toJson() => _$PrincipalToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrincipalKindEnum {
@JsonValue(r'person')
person(r'person'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'service')
service(r'service');

const PrincipalKindEnum(this.value);

final String value;

@override
String toString() => value;
}


