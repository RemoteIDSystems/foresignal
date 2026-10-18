//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'dcp_observation_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DcpObservationPayload {
  /// Returns a new [DcpObservationPayload] instance.
  DcpObservationPayload({

    required  this.address,

     this.name,

     this.agency,

     this.channel,

     this.signalDbm,

     this.frequencyOffsetHz,

     this.failureCode,

     this.dataAscii,

     this.dataHex,

     this.decoded,
  });

  @JsonKey(
    
    name: r'address',
    required: true,
    includeIfNull: false,
  )


  final String address;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'agency',
    required: false,
    includeIfNull: false,
  )


  final String? agency;



  @JsonKey(
    
    name: r'channel',
    required: false,
    includeIfNull: false,
  )


  final String? channel;



  @JsonKey(
    
    name: r'signalDbm',
    required: false,
    includeIfNull: false,
  )


  final num? signalDbm;



  @JsonKey(
    
    name: r'frequencyOffsetHz',
    required: false,
    includeIfNull: false,
  )


  final num? frequencyOffsetHz;



  @JsonKey(
    
    name: r'failureCode',
    required: false,
    includeIfNull: false,
  )


  final String? failureCode;



  @JsonKey(
    
    name: r'dataAscii',
    required: false,
    includeIfNull: false,
  )


  final String? dataAscii;



  @JsonKey(
    
    name: r'dataHex',
    required: false,
    includeIfNull: false,
  )


  final String? dataHex;



  @JsonKey(
    
    name: r'decoded',
    required: false,
    includeIfNull: false,
  )


  final Object? decoded;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DcpObservationPayload &&
      other.address == address &&
      other.name == name &&
      other.agency == agency &&
      other.channel == channel &&
      other.signalDbm == signalDbm &&
      other.frequencyOffsetHz == frequencyOffsetHz &&
      other.failureCode == failureCode &&
      other.dataAscii == dataAscii &&
      other.dataHex == dataHex &&
      other.decoded == decoded;

    @override
    int get hashCode =>
        address.hashCode +
        (name == null ? 0 : name.hashCode) +
        (agency == null ? 0 : agency.hashCode) +
        (channel == null ? 0 : channel.hashCode) +
        (signalDbm == null ? 0 : signalDbm.hashCode) +
        (frequencyOffsetHz == null ? 0 : frequencyOffsetHz.hashCode) +
        (failureCode == null ? 0 : failureCode.hashCode) +
        (dataAscii == null ? 0 : dataAscii.hashCode) +
        (dataHex == null ? 0 : dataHex.hashCode) +
        (decoded == null ? 0 : decoded.hashCode);

  factory DcpObservationPayload.fromJson(Map<String, dynamic> json) => _$DcpObservationPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$DcpObservationPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

