//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'me_token.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MeToken {
  /// Returns a new [MeToken] instance.
  MeToken({

    required  this.id,

    required  this.name,

    required  this.scope,

     this.tokenPrefix,

     this.expiresAt,
  });

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
    
    name: r'scope',
    required: true,
    includeIfNull: false,
  )


  final MeTokenScopeEnum scope;



  @JsonKey(
    
    name: r'tokenPrefix',
    required: false,
    includeIfNull: false,
  )


  final String? tokenPrefix;



  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MeToken &&
      other.id == id &&
      other.name == name &&
      other.scope == scope &&
      other.tokenPrefix == tokenPrefix &&
      other.expiresAt == expiresAt;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        scope.hashCode +
        tokenPrefix.hashCode +
        (expiresAt == null ? 0 : expiresAt.hashCode);

  factory MeToken.fromJson(Map<String, dynamic> json) => _$MeTokenFromJson(json);

  Map<String, dynamic> toJson() => _$MeTokenToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MeTokenScopeEnum {
@JsonValue(r'read')
read(r'read'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'admin')
admin(r'admin');

const MeTokenScopeEnum(this.value);

final String value;

@override
String toString() => value;
}


