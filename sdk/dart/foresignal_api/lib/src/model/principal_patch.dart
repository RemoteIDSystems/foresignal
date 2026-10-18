//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'principal_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrincipalPatch {
  /// Returns a new [PrincipalPatch] instance.
  PrincipalPatch({

     this.displayName,

     this.email,

     this.avatarMediaId,

     this.timezone,

     this.organization,

     this.disabled,
  });

  @JsonKey(
    
    name: r'displayName',
    required: false,
    includeIfNull: false,
  )


  final String? displayName;



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



      /// Disabled principals' tokens stop working
  @JsonKey(
    
    name: r'disabled',
    required: false,
    includeIfNull: false,
  )


  final bool? disabled;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrincipalPatch &&
      other.displayName == displayName &&
      other.email == email &&
      other.avatarMediaId == avatarMediaId &&
      other.timezone == timezone &&
      other.organization == organization &&
      other.disabled == disabled;

    @override
    int get hashCode =>
        displayName.hashCode +
        (email == null ? 0 : email.hashCode) +
        (avatarMediaId == null ? 0 : avatarMediaId.hashCode) +
        (timezone == null ? 0 : timezone.hashCode) +
        (organization == null ? 0 : organization.hashCode) +
        disabled.hashCode;

  factory PrincipalPatch.fromJson(Map<String, dynamic> json) => _$PrincipalPatchFromJson(json);

  Map<String, dynamic> toJson() => _$PrincipalPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

