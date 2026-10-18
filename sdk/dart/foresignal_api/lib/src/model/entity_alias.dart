//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'entity_alias.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EntityAlias {
  /// Returns a new [EntityAlias] instance.
  EntityAlias({

    required  this.id,

    required  this.alias,

    required  this.aliasType,

    required  this.validFrom,

     this.validTo,

    required  this.origin,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



  @JsonKey(
    
    name: r'alias',
    required: true,
    includeIfNull: false,
  )


  final String alias;



  @JsonKey(
    
    name: r'aliasType',
    required: true,
    includeIfNull: false,
  )


  final EntityAliasAliasTypeEnum aliasType;



  @JsonKey(
    
    name: r'validFrom',
    required: true,
    includeIfNull: false,
  )


  final DateTime validFrom;



  @JsonKey(
    
    name: r'validTo',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validTo;



  @JsonKey(
    
    name: r'origin',
    required: true,
    includeIfNull: false,
  )


  final EntityAliasOriginEnum origin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EntityAlias &&
      other.id == id &&
      other.alias == alias &&
      other.aliasType == aliasType &&
      other.validFrom == validFrom &&
      other.validTo == validTo &&
      other.origin == origin;

    @override
    int get hashCode =>
        id.hashCode +
        alias.hashCode +
        aliasType.hashCode +
        validFrom.hashCode +
        (validTo == null ? 0 : validTo.hashCode) +
        origin.hashCode;

  factory EntityAlias.fromJson(Map<String, dynamic> json) => _$EntityAliasFromJson(json);

  Map<String, dynamic> toJson() => _$EntityAliasToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EntityAliasAliasTypeEnum {
@JsonValue(r'NAME')
NAME(r'NAME'),
@JsonValue(r'FORMER_NAME')
FORMER_NAME(r'FORMER_NAME'),
@JsonValue(r'OPERATOR_LABEL')
OPERATOR_LABEL(r'OPERATOR_LABEL');

const EntityAliasAliasTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EntityAliasOriginEnum {
@JsonValue(r'REGISTRY')
REGISTRY(r'REGISTRY'),
@JsonValue(r'CO_ASSERTED')
CO_ASSERTED(r'CO_ASSERTED'),
@JsonValue(r'OBSERVED')
OBSERVED(r'OBSERVED'),
@JsonValue(r'OPERATOR')
OPERATOR(r'OPERATOR');

const EntityAliasOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


