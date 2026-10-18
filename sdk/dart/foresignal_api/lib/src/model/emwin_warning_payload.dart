//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/emwin_warning_payload_duplicate_of.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'emwin_warning_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EmwinWarningPayload {
  /// Returns a new [EmwinWarningPayload] instance.
  EmwinWarningPayload({

    required  this.action,

    required  this.phenomena,

    required  this.significance,

    required  this.office,

    required  this.etn,

    required  this.validFrom,

    required  this.validUntil,

     this.headline,

    required  this.fallback,

    required  this.authoritativeSource,

     this.duplicateOf,

     this.receivedVia,
  });

  @JsonKey(
    
    name: r'action',
    required: true,
    includeIfNull: false,
  )


  final String action;



  @JsonKey(
    
    name: r'phenomena',
    required: true,
    includeIfNull: false,
  )


  final String phenomena;



  @JsonKey(
    
    name: r'significance',
    required: true,
    includeIfNull: false,
  )


  final String significance;



  @JsonKey(
    
    name: r'office',
    required: true,
    includeIfNull: false,
  )


  final String office;



  @JsonKey(
    
    name: r'etn',
    required: true,
    includeIfNull: false,
  )


  final int etn;



  @JsonKey(
    
    name: r'validFrom',
    required: true,
    includeIfNull: false,
  )


  final DateTime validFrom;



  @JsonKey(
    
    name: r'validUntil',
    required: true,
    includeIfNull: false,
  )


  final DateTime validUntil;



  @JsonKey(
    
    name: r'headline',
    required: false,
    includeIfNull: false,
  )


  final String? headline;



  @JsonKey(
    
    name: r'fallback',
    required: true,
    includeIfNull: false,
  )


  final bool fallback;



  @JsonKey(
    
    name: r'authoritativeSource',
    required: true,
    includeIfNull: false,
  )


  final EmwinWarningPayloadAuthoritativeSourceEnum authoritativeSource;



  @JsonKey(
    
    name: r'duplicateOf',
    required: false,
    includeIfNull: false,
  )


  final EmwinWarningPayloadDuplicateOf? duplicateOf;



  @JsonKey(
    
    name: r'receivedVia',
    required: false,
    includeIfNull: false,
  )


  final EmwinWarningPayloadReceivedViaEnum? receivedVia;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EmwinWarningPayload &&
      other.action == action &&
      other.phenomena == phenomena &&
      other.significance == significance &&
      other.office == office &&
      other.etn == etn &&
      other.validFrom == validFrom &&
      other.validUntil == validUntil &&
      other.headline == headline &&
      other.fallback == fallback &&
      other.authoritativeSource == authoritativeSource &&
      other.duplicateOf == duplicateOf &&
      other.receivedVia == receivedVia;

    @override
    int get hashCode =>
        action.hashCode +
        phenomena.hashCode +
        significance.hashCode +
        office.hashCode +
        etn.hashCode +
        validFrom.hashCode +
        validUntil.hashCode +
        (headline == null ? 0 : headline.hashCode) +
        fallback.hashCode +
        authoritativeSource.hashCode +
        (duplicateOf == null ? 0 : duplicateOf.hashCode) +
        receivedVia.hashCode;

  factory EmwinWarningPayload.fromJson(Map<String, dynamic> json) => _$EmwinWarningPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$EmwinWarningPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EmwinWarningPayloadAuthoritativeSourceEnum {
@JsonValue(r'nws-alerts')
nwsAlerts(r'nws-alerts');

const EmwinWarningPayloadAuthoritativeSourceEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EmwinWarningPayloadReceivedViaEnum {
@JsonValue(r'Received via GOES HRIT EMWIN')
receivedViaGOESHRITEMWIN(r'Received via GOES HRIT EMWIN');

const EmwinWarningPayloadReceivedViaEnum(this.value);

final String value;

@override
String toString() => value;
}


