//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'aircraft_registration_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AircraftRegistrationPayload {
  /// Returns a new [AircraftRegistrationPayload] instance.
  AircraftRegistrationPayload({

    required  this.nNumber,

     this.serial,

     this.mfrMdlCode,

     this.manufacturer,

     this.model,

     this.typeAircraft,

     this.typeEngine,

     this.engines,

     this.seats,

     this.weightClass,

     this.yearMfr,

     this.registrantType,

     this.certIssueDate,

     this.airworthinessDate,

     this.expirationDate,

     this.statusCode,

     this.modeSHex,

     this.fractionalOwner,

     this.kitMfr,

     this.kitModel,

     this.name,
  });

  @JsonKey(
    
    name: r'nNumber',
    required: true,
    includeIfNull: false,
  )


  final String nNumber;



  @JsonKey(
    
    name: r'serial',
    required: false,
    includeIfNull: false,
  )


  final String? serial;



  @JsonKey(
    
    name: r'mfrMdlCode',
    required: false,
    includeIfNull: false,
  )


  final String? mfrMdlCode;



  @JsonKey(
    
    name: r'manufacturer',
    required: false,
    includeIfNull: false,
  )


  final String? manufacturer;



  @JsonKey(
    
    name: r'model',
    required: false,
    includeIfNull: false,
  )


  final String? model;



  @JsonKey(
    
    name: r'typeAircraft',
    required: false,
    includeIfNull: false,
  )


  final String? typeAircraft;



  @JsonKey(
    
    name: r'typeEngine',
    required: false,
    includeIfNull: false,
  )


  final String? typeEngine;



  @JsonKey(
    
    name: r'engines',
    required: false,
    includeIfNull: false,
  )


  final int? engines;



  @JsonKey(
    
    name: r'seats',
    required: false,
    includeIfNull: false,
  )


  final int? seats;



  @JsonKey(
    
    name: r'weightClass',
    required: false,
    includeIfNull: false,
  )


  final String? weightClass;



  @JsonKey(
    
    name: r'yearMfr',
    required: false,
    includeIfNull: false,
  )


  final int? yearMfr;



      /// Decoded registrant type (individual, partnership, corporation, co-owned, government, llc, non-citizen-corporation, non-citizen-co-owned)
  @JsonKey(
    
    name: r'registrantType',
    required: false,
    includeIfNull: false,
  )


  final String? registrantType;



  @JsonKey(
    
    name: r'certIssueDate',
    required: false,
    includeIfNull: false,
  )


  final DateTime? certIssueDate;



  @JsonKey(
    
    name: r'airworthinessDate',
    required: false,
    includeIfNull: false,
  )


  final DateTime? airworthinessDate;



  @JsonKey(
    
    name: r'expirationDate',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expirationDate;



  @JsonKey(
    
    name: r'statusCode',
    required: false,
    includeIfNull: false,
  )


  final String? statusCode;



      /// Lowercase ICAO 24-bit hex
  @JsonKey(
    
    name: r'modeSHex',
    required: false,
    includeIfNull: false,
  )


  final String? modeSHex;



  @JsonKey(
    
    name: r'fractionalOwner',
    required: false,
    includeIfNull: false,
  )


  final bool? fractionalOwner;



  @JsonKey(
    
    name: r'kitMfr',
    required: false,
    includeIfNull: false,
  )


  final String? kitMfr;



  @JsonKey(
    
    name: r'kitModel',
    required: false,
    includeIfNull: false,
  )


  final String? kitModel;



      /// Registrant name, present only when the source param `nonIndividualOwnerNames` is on and the registrant is a corporation, government, LLC or non-citizen corporation
  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AircraftRegistrationPayload &&
      other.nNumber == nNumber &&
      other.serial == serial &&
      other.mfrMdlCode == mfrMdlCode &&
      other.manufacturer == manufacturer &&
      other.model == model &&
      other.typeAircraft == typeAircraft &&
      other.typeEngine == typeEngine &&
      other.engines == engines &&
      other.seats == seats &&
      other.weightClass == weightClass &&
      other.yearMfr == yearMfr &&
      other.registrantType == registrantType &&
      other.certIssueDate == certIssueDate &&
      other.airworthinessDate == airworthinessDate &&
      other.expirationDate == expirationDate &&
      other.statusCode == statusCode &&
      other.modeSHex == modeSHex &&
      other.fractionalOwner == fractionalOwner &&
      other.kitMfr == kitMfr &&
      other.kitModel == kitModel &&
      other.name == name;

    @override
    int get hashCode =>
        nNumber.hashCode +
        serial.hashCode +
        mfrMdlCode.hashCode +
        (manufacturer == null ? 0 : manufacturer.hashCode) +
        (model == null ? 0 : model.hashCode) +
        (typeAircraft == null ? 0 : typeAircraft.hashCode) +
        (typeEngine == null ? 0 : typeEngine.hashCode) +
        (engines == null ? 0 : engines.hashCode) +
        (seats == null ? 0 : seats.hashCode) +
        (weightClass == null ? 0 : weightClass.hashCode) +
        (yearMfr == null ? 0 : yearMfr.hashCode) +
        (registrantType == null ? 0 : registrantType.hashCode) +
        (certIssueDate == null ? 0 : certIssueDate.hashCode) +
        (airworthinessDate == null ? 0 : airworthinessDate.hashCode) +
        (expirationDate == null ? 0 : expirationDate.hashCode) +
        (statusCode == null ? 0 : statusCode.hashCode) +
        (modeSHex == null ? 0 : modeSHex.hashCode) +
        (fractionalOwner == null ? 0 : fractionalOwner.hashCode) +
        (kitMfr == null ? 0 : kitMfr.hashCode) +
        (kitModel == null ? 0 : kitModel.hashCode) +
        (name == null ? 0 : name.hashCode);

  factory AircraftRegistrationPayload.fromJson(Map<String, dynamic> json) => _$AircraftRegistrationPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$AircraftRegistrationPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

