//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/access_request_offer_share.dart';
import 'package:foresignal_api/src/model/access_request_grant.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'access_request.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AccessRequest {
  /// Returns a new [AccessRequest] instance.
  AccessRequest({

    required  this.id,

    required  this.linkId,

    required  this.direction,

     this.chronicleId,

    required  this.requesterHandle,

     this.requesterInstanceId,

     this.message,

     this.offerShare,

    required  this.status,

    required  this.createdAt,

     this.decidedAt,

     this.grant,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// ULID identifier
  @JsonKey(
    
    name: r'linkId',
    required: true,
    includeIfNull: false,
  )


  final String linkId;



  @JsonKey(
    
    name: r'direction',
    required: true,
    includeIfNull: false,
  )


  final AccessRequestDirectionEnum direction;



      /// Target chronicle (incoming requests only)
  @JsonKey(
    
    name: r'chronicleId',
    required: false,
    includeIfNull: false,
  )


  final String? chronicleId;



  @JsonKey(
    
    name: r'requesterHandle',
    required: true,
    includeIfNull: false,
  )


  final String requesterHandle;



  @JsonKey(
    
    name: r'requesterInstanceId',
    required: false,
    includeIfNull: false,
  )


  final String? requesterInstanceId;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



  @JsonKey(
    
    name: r'offerShare',
    required: false,
    includeIfNull: false,
  )


  final AccessRequestOfferShare? offerShare;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final AccessRequestStatusEnum status;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'decidedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? decidedAt;



  @JsonKey(
    
    name: r'grant',
    required: false,
    includeIfNull: false,
  )


  final AccessRequestGrant? grant;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AccessRequest &&
      other.id == id &&
      other.linkId == linkId &&
      other.direction == direction &&
      other.chronicleId == chronicleId &&
      other.requesterHandle == requesterHandle &&
      other.requesterInstanceId == requesterInstanceId &&
      other.message == message &&
      other.offerShare == offerShare &&
      other.status == status &&
      other.createdAt == createdAt &&
      other.decidedAt == decidedAt &&
      other.grant == grant;

    @override
    int get hashCode =>
        id.hashCode +
        linkId.hashCode +
        direction.hashCode +
        (chronicleId == null ? 0 : chronicleId.hashCode) +
        requesterHandle.hashCode +
        (requesterInstanceId == null ? 0 : requesterInstanceId.hashCode) +
        (message == null ? 0 : message.hashCode) +
        (offerShare == null ? 0 : offerShare.hashCode) +
        status.hashCode +
        createdAt.hashCode +
        (decidedAt == null ? 0 : decidedAt.hashCode) +
        (grant == null ? 0 : grant.hashCode);

  factory AccessRequest.fromJson(Map<String, dynamic> json) => _$AccessRequestFromJson(json);

  Map<String, dynamic> toJson() => _$AccessRequestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AccessRequestDirectionEnum {
@JsonValue(r'incoming')
incoming(r'incoming'),
@JsonValue(r'outgoing')
outgoing(r'outgoing');

const AccessRequestDirectionEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AccessRequestStatusEnum {
@JsonValue(r'pending')
pending(r'pending'),
@JsonValue(r'granted')
granted(r'granted'),
@JsonValue(r'declined')
declined(r'declined');

const AccessRequestStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


