//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'entity_key.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EntityKey {
  /// Returns a new [EntityKey] instance.
  EntityKey({

    required  this.id,

    required  this.entityId,

    required  this.keyType,

    required  this.keyValue,

    required  this.role,

    required  this.confidence,

    required  this.origin,

     this.originRef,

    required  this.validFrom,

     this.validTo,

    required  this.retracted,
  });

      /// Key row id
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final int id;



      /// ULID identifier
  @JsonKey(
    
    name: r'entityId',
    required: true,
    includeIfNull: false,
  )


  final String entityId;



      /// Key type. `callsign` values are domain-prefixed (`air:`, `sea:`, `ham:`)
  @JsonKey(
    
    name: r'keyType',
    required: true,
    includeIfNull: false,
  )


  final EntityKeyKeyTypeEnum keyType;



      /// Normalised value (e.g. icao24 lowercase hex, registration without spaces or hyphens)
  @JsonKey(
    
    name: r'keyValue',
    required: true,
    includeIfNull: false,
  )


  final String keyValue;



      /// ANCHOR keys (airframe, imo, norad, cospar) identify an entity on their own; WEAK keys can be re-assigned
  @JsonKey(
    
    name: r'role',
    required: true,
    includeIfNull: false,
  )


  final EntityKeyRoleEnum role;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'confidence',
    required: true,
    includeIfNull: false,
  )


  final num confidence;



  @JsonKey(
    
    name: r'origin',
    required: true,
    includeIfNull: false,
  )


  final EntityKeyOriginEnum origin;



      /// `sourceId:externalId` of the asserting record, or the operator token id
  @JsonKey(
    
    name: r'originRef',
    required: false,
    includeIfNull: false,
  )


  final String? originRef;



  @JsonKey(
    
    name: r'validFrom',
    required: true,
    includeIfNull: false,
  )


  final DateTime validFrom;



      /// null while the key is open
  @JsonKey(
    
    name: r'validTo',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validTo;



      /// True when an operator retracted the key
  @JsonKey(
    
    name: r'retracted',
    required: true,
    includeIfNull: false,
  )


  final bool retracted;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EntityKey &&
      other.id == id &&
      other.entityId == entityId &&
      other.keyType == keyType &&
      other.keyValue == keyValue &&
      other.role == role &&
      other.confidence == confidence &&
      other.origin == origin &&
      other.originRef == originRef &&
      other.validFrom == validFrom &&
      other.validTo == validTo &&
      other.retracted == retracted;

    @override
    int get hashCode =>
        id.hashCode +
        entityId.hashCode +
        keyType.hashCode +
        keyValue.hashCode +
        role.hashCode +
        confidence.hashCode +
        origin.hashCode +
        (originRef == null ? 0 : originRef.hashCode) +
        validFrom.hashCode +
        (validTo == null ? 0 : validTo.hashCode) +
        retracted.hashCode;

  factory EntityKey.fromJson(Map<String, dynamic> json) => _$EntityKeyFromJson(json);

  Map<String, dynamic> toJson() => _$EntityKeyToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Key type. `callsign` values are domain-prefixed (`air:`, `sea:`, `ham:`)
enum EntityKeyKeyTypeEnum {
@JsonValue(r'airframe')
airframe(r'airframe'),
@JsonValue(r'icao24')
icao24(r'icao24'),
@JsonValue(r'registration')
registration(r'registration'),
@JsonValue(r'imo')
imo(r'imo'),
@JsonValue(r'mmsi')
mmsi(r'mmsi'),
@JsonValue(r'norad')
norad(r'norad'),
@JsonValue(r'cospar')
cospar(r'cospar'),
@JsonValue(r'callsign')
callsign(r'callsign');

const EntityKeyKeyTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// ANCHOR keys (airframe, imo, norad, cospar) identify an entity on their own; WEAK keys can be re-assigned
enum EntityKeyRoleEnum {
@JsonValue(r'ANCHOR')
ANCHOR(r'ANCHOR'),
@JsonValue(r'WEAK')
WEAK(r'WEAK');

const EntityKeyRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EntityKeyOriginEnum {
@JsonValue(r'REGISTRY')
REGISTRY(r'REGISTRY'),
@JsonValue(r'CO_ASSERTED')
CO_ASSERTED(r'CO_ASSERTED'),
@JsonValue(r'OBSERVED')
OBSERVED(r'OBSERVED'),
@JsonValue(r'OPERATOR')
OPERATOR(r'OPERATOR');

const EntityKeyOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


