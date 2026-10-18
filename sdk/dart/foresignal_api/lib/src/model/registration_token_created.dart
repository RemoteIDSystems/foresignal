//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'registration_token_created.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RegistrationTokenCreated {
  /// Returns a new [RegistrationTokenCreated] instance.
  RegistrationTokenCreated({

    required  this.id,

    required  this.name,

    required  this.prefix,

     this.expiresAt,

    required  this.maxUses,

    required  this.uses,

     this.sourceIdPrefix,

    required  this.createdAt,

    required  this.token,
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
    
    name: r'prefix',
    required: true,
    includeIfNull: false,
  )


  final String prefix;



  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;



  @JsonKey(
    
    name: r'maxUses',
    required: true,
    includeIfNull: false,
  )


  final int maxUses;



  @JsonKey(
    
    name: r'uses',
    required: true,
    includeIfNull: false,
  )


  final int uses;



  @JsonKey(
    
    name: r'sourceIdPrefix',
    required: false,
    includeIfNull: false,
  )


  final String? sourceIdPrefix;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



      /// fsx_… (shown once)
  @JsonKey(
    
    name: r'token',
    required: true,
    includeIfNull: false,
  )


  final String token;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RegistrationTokenCreated &&
      other.id == id &&
      other.name == name &&
      other.prefix == prefix &&
      other.expiresAt == expiresAt &&
      other.maxUses == maxUses &&
      other.uses == uses &&
      other.sourceIdPrefix == sourceIdPrefix &&
      other.createdAt == createdAt &&
      other.token == token;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        prefix.hashCode +
        (expiresAt == null ? 0 : expiresAt.hashCode) +
        maxUses.hashCode +
        uses.hashCode +
        (sourceIdPrefix == null ? 0 : sourceIdPrefix.hashCode) +
        createdAt.hashCode +
        token.hashCode;

  factory RegistrationTokenCreated.fromJson(Map<String, dynamic> json) => _$RegistrationTokenCreatedFromJson(json);

  Map<String, dynamic> toJson() => _$RegistrationTokenCreatedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

