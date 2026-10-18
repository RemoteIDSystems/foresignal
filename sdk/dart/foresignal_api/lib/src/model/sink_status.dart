//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sink_status_clients.dart';
import 'package:foresignal_api/src/model/sink_status_denied.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sink_status.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SinkStatus {
  /// Returns a new [SinkStatus] instance.
  SinkStatus({

    required  this.sinkId,

    required  this.state,

     this.lastSeq,

     this.watermarkSeq,

     this.lastPublishedSeq,

     this.lagSeq,

     this.lastDeliveredAt,

     this.lastError,

     this.failingSince,

     this.resyncCount,

    required  this.delivered,

    required  this.dropped,

    required  this.denied,

     this.clients,
  });

  @JsonKey(
    
    name: r'sinkId',
    required: true,
    includeIfNull: false,
  )


  final String sinkId;



  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final SinkStatusStateEnum state;



  @JsonKey(
    
    name: r'lastSeq',
    required: false,
    includeIfNull: false,
  )


  final int? lastSeq;



  @JsonKey(
    
    name: r'watermarkSeq',
    required: false,
    includeIfNull: false,
  )


  final int? watermarkSeq;



  @JsonKey(
    
    name: r'lastPublishedSeq',
    required: false,
    includeIfNull: false,
  )


  final int? lastPublishedSeq;



      /// lastPublishedSeq − lastSeq
  @JsonKey(
    
    name: r'lagSeq',
    required: false,
    includeIfNull: false,
  )


  final int? lagSeq;



  @JsonKey(
    
    name: r'lastDeliveredAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastDeliveredAt;



  @JsonKey(
    
    name: r'lastError',
    required: false,
    includeIfNull: false,
  )


  final String? lastError;



  @JsonKey(
    
    name: r'failingSince',
    required: false,
    includeIfNull: false,
  )


  final DateTime? failingSince;



  @JsonKey(
    
    name: r'resyncCount',
    required: false,
    includeIfNull: false,
  )


  final int? resyncCount;



  @JsonKey(
    
    name: r'delivered',
    required: true,
    includeIfNull: false,
  )


  final int delivered;



      /// Updates conflated by rate control
  @JsonKey(
    
    name: r'dropped',
    required: true,
    includeIfNull: false,
  )


  final int dropped;



  @JsonKey(
    
    name: r'denied',
    required: true,
    includeIfNull: false,
  )


  final SinkStatusDenied denied;



  @JsonKey(
    
    name: r'clients',
    required: false,
    includeIfNull: false,
  )


  final SinkStatusClients? clients;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SinkStatus &&
      other.sinkId == sinkId &&
      other.state == state &&
      other.lastSeq == lastSeq &&
      other.watermarkSeq == watermarkSeq &&
      other.lastPublishedSeq == lastPublishedSeq &&
      other.lagSeq == lagSeq &&
      other.lastDeliveredAt == lastDeliveredAt &&
      other.lastError == lastError &&
      other.failingSince == failingSince &&
      other.resyncCount == resyncCount &&
      other.delivered == delivered &&
      other.dropped == dropped &&
      other.denied == denied &&
      other.clients == clients;

    @override
    int get hashCode =>
        sinkId.hashCode +
        state.hashCode +
        (lastSeq == null ? 0 : lastSeq.hashCode) +
        (watermarkSeq == null ? 0 : watermarkSeq.hashCode) +
        (lastPublishedSeq == null ? 0 : lastPublishedSeq.hashCode) +
        (lagSeq == null ? 0 : lagSeq.hashCode) +
        (lastDeliveredAt == null ? 0 : lastDeliveredAt.hashCode) +
        (lastError == null ? 0 : lastError.hashCode) +
        (failingSince == null ? 0 : failingSince.hashCode) +
        resyncCount.hashCode +
        delivered.hashCode +
        dropped.hashCode +
        denied.hashCode +
        (clients == null ? 0 : clients.hashCode);

  factory SinkStatus.fromJson(Map<String, dynamic> json) => _$SinkStatusFromJson(json);

  Map<String, dynamic> toJson() => _$SinkStatusToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SinkStatusStateEnum {
@JsonValue(r'RUNNING')
RUNNING(r'RUNNING'),
@JsonValue(r'RESYNCING')
RESYNCING(r'RESYNCING'),
@JsonValue(r'FAILING')
FAILING(r'FAILING'),
@JsonValue(r'STOPPED')
STOPPED(r'STOPPED');

const SinkStatusStateEnum(this.value);

final String value;

@override
String toString() => value;
}


