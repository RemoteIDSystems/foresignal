//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_access_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleAccessEvent {
  /// Returns a new [ChronicleAccessEvent] instance.
  ChronicleAccessEvent({

    required  this.requestId,

    required  this.linkId,

    required  this.status,

     this.requesterHandle,

     this.message,

     this.chronicleId,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'requestId',
    required: true,
    includeIfNull: false,
  )


  final String requestId;



      /// ULID identifier
  @JsonKey(
    
    name: r'linkId',
    required: true,
    includeIfNull: false,
  )


  final String linkId;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ChronicleAccessEventStatusEnum status;



      /// Only on `requested`
  @JsonKey(
    
    name: r'requesterHandle',
    required: false,
    includeIfNull: false,
  )


  final String? requesterHandle;



      /// Only on `requested`
  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



  @JsonKey(
    
    name: r'chronicleId',
    required: false,
    includeIfNull: false,
  )


  final String? chronicleId;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleAccessEvent &&
      other.requestId == requestId &&
      other.linkId == linkId &&
      other.status == status &&
      other.requesterHandle == requesterHandle &&
      other.message == message &&
      other.chronicleId == chronicleId &&
      other.at == at;

    @override
    int get hashCode =>
        requestId.hashCode +
        linkId.hashCode +
        status.hashCode +
        (requesterHandle == null ? 0 : requesterHandle.hashCode) +
        (message == null ? 0 : message.hashCode) +
        (chronicleId == null ? 0 : chronicleId.hashCode) +
        at.hashCode;

  factory ChronicleAccessEvent.fromJson(Map<String, dynamic> json) => _$ChronicleAccessEventFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleAccessEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleAccessEventStatusEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'granted')
granted(r'granted'),
@JsonValue(r'declined')
declined(r'declined');

const ChronicleAccessEventStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


