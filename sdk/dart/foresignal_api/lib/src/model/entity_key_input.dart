//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'entity_key_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EntityKeyInput {
  /// Returns a new [EntityKeyInput] instance.
  EntityKeyInput({

    required  this.keyType,

    required  this.keyValue,

     this.confidence = 1.0,

     this.validFrom,

     this.validTo,

     this.force = false,
  });

  @JsonKey(
    
    name: r'keyType',
    required: true,
    includeIfNull: false,
  )


  final EntityKeyInputKeyTypeEnum keyType;



      /// Raw value; normalised and validated like observed keys (400 ENTITY_KEY_INVALID)
  @JsonKey(
    
    name: r'keyValue',
    required: true,
    includeIfNull: false,
  )


  final String keyValue;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    defaultValue: 1.0,
    name: r'confidence',
    required: false,
    includeIfNull: false,
  )


  final num? confidence;



      /// Default now
  @JsonKey(
    
    name: r'validFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validFrom;



  @JsonKey(
    
    name: r'validTo',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validTo;



      /// Close the key where it is open on another entity and open it here; without it that case is 409 ENTITY_KEY_CONFLICT
  @JsonKey(
    defaultValue: false,
    name: r'force',
    required: false,
    includeIfNull: false,
  )


  final bool? force;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EntityKeyInput &&
      other.keyType == keyType &&
      other.keyValue == keyValue &&
      other.confidence == confidence &&
      other.validFrom == validFrom &&
      other.validTo == validTo &&
      other.force == force;

    @override
    int get hashCode =>
        keyType.hashCode +
        keyValue.hashCode +
        confidence.hashCode +
        validFrom.hashCode +
        (validTo == null ? 0 : validTo.hashCode) +
        force.hashCode;

  factory EntityKeyInput.fromJson(Map<String, dynamic> json) => _$EntityKeyInputFromJson(json);

  Map<String, dynamic> toJson() => _$EntityKeyInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EntityKeyInputKeyTypeEnum {
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
callsign(r'callsign'),
@JsonValue(r'lei')
lei(r'lei'),
@JsonValue(r'company-number')
companyNumber(r'company-number'),
@JsonValue(r'tax-id')
taxId(r'tax-id'),
@JsonValue(r'officer-id')
officerId(r'officer-id'),
@JsonValue(r'person-key')
personKey(r'person-key');

const EntityKeyInputKeyTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


