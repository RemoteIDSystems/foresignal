//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'package_received_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PackageReceivedEvent {
  /// Returns a new [PackageReceivedEvent] instance.
  PackageReceivedEvent({

    required  this.receivedId,

    required  this.packageId,

    required  this.type,

    required  this.senderInstanceId,

     this.title,

    required  this.tlp,

     this.signatureStatus,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'receivedId',
    required: true,
    includeIfNull: false,
  )


  final String receivedId;



      /// ULID identifier
  @JsonKey(
    
    name: r'packageId',
    required: true,
    includeIfNull: false,
  )


  final String packageId;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final PackageReceivedEventTypeEnum type;



  @JsonKey(
    
    name: r'senderInstanceId',
    required: true,
    includeIfNull: false,
  )


  final String senderInstanceId;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'tlp',
    required: true,
    includeIfNull: false,
  )


  final Tlp tlp;



  @JsonKey(
    
    name: r'signatureStatus',
    required: false,
    includeIfNull: false,
  )


  final PackageReceivedEventSignatureStatusEnum? signatureStatus;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PackageReceivedEvent &&
      other.receivedId == receivedId &&
      other.packageId == packageId &&
      other.type == type &&
      other.senderInstanceId == senderInstanceId &&
      other.title == title &&
      other.tlp == tlp &&
      other.signatureStatus == signatureStatus &&
      other.at == at;

    @override
    int get hashCode =>
        receivedId.hashCode +
        packageId.hashCode +
        type.hashCode +
        senderInstanceId.hashCode +
        title.hashCode +
        tlp.hashCode +
        signatureStatus.hashCode +
        at.hashCode;

  factory PackageReceivedEvent.fromJson(Map<String, dynamic> json) => _$PackageReceivedEventFromJson(json);

  Map<String, dynamic> toJson() => _$PackageReceivedEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PackageReceivedEventTypeEnum {
@JsonValue(r'report')
report(r'report'),
@JsonValue(r'investigation')
investigation(r'investigation'),
@JsonValue(r'chronicle-extract')
chronicleExtract(r'chronicle-extract'),
@JsonValue(r'evidence')
evidence(r'evidence');

const PackageReceivedEventTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PackageReceivedEventSignatureStatusEnum {
@JsonValue(r'valid')
valid(r'valid'),
@JsonValue(r'invalid')
invalid(r'invalid'),
@JsonValue(r'unknown-key')
unknownKey(r'unknown-key');

const PackageReceivedEventSignatureStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


