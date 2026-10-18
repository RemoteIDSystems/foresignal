//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/peer_selection.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'peer.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Peer {
  /// Returns a new [Peer] instance.
  Peer({

    required  this.id,

    required  this.name,

    required  this.baseUrl,

    required  this.mode,

    required  this.enabled,

    required  this.state,

     this.healthReason,

     this.lastSeq,

     this.lagSeq,

     this.lastError,

     this.pinnedKid,

     this.remoteInstanceId,

    required  this.selection,

     this.sameOperator,

     this.reliability,

     this.tokenSet,

     this.ingestKeyPrefix,

     this.sourceIds,

    required  this.createdAt,

    required  this.updatedAt,

     this.exchangeKeySet,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'baseUrl',
    required: true,
    includeIfNull: false,
  )


  final String baseUrl;



  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final PeerModeEnum mode;



  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// Runtime state: connecting, streaming, resyncing, failing or stopped for PULL peers; receiving for RECEIVE peers
  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final String state;



  @JsonKey(
    
    name: r'healthReason',
    required: false,
    includeIfNull: false,
  )


  final PeerHealthReasonEnum? healthReason;



  @JsonKey(
    
    name: r'lastSeq',
    required: false,
    includeIfNull: false,
  )


  final int? lastSeq;



  @JsonKey(
    
    name: r'lagSeq',
    required: false,
    includeIfNull: false,
  )


  final int? lagSeq;



  @JsonKey(
    
    name: r'lastError',
    required: false,
    includeIfNull: false,
  )


  final String? lastError;



  @JsonKey(
    
    name: r'pinnedKid',
    required: false,
    includeIfNull: false,
  )


  final String? pinnedKid;



  @JsonKey(
    
    name: r'remoteInstanceId',
    required: false,
    includeIfNull: false,
  )


  final String? remoteInstanceId;



  @JsonKey(
    
    name: r'selection',
    required: true,
    includeIfNull: false,
  )


  final PeerSelection selection;



  @JsonKey(
    
    name: r'sameOperator',
    required: false,
    includeIfNull: false,
  )


  final bool? sameOperator;



      /// Admiralty source reliability: A completely reliable, B usually reliable, C fairly reliable, D not usually reliable, E unreliable, F cannot be judged
  @JsonKey(
    
    name: r'reliability',
    required: false,
    includeIfNull: false,
  )


  final PeerReliabilityEnum? reliability;



  @JsonKey(
    
    name: r'tokenSet',
    required: false,
    includeIfNull: false,
  )


  final bool? tokenSet;



      /// Prefix of the current fsp_ ingest key (RECEIVE peers)
  @JsonKey(
    
    name: r'ingestKeyPrefix',
    required: false,
    includeIfNull: false,
  )


  final String? ingestKeyPrefix;



      /// Dynamic sources: peer-{id}, peer-{id}-trk, peer-{id}-events
  @JsonKey(
    
    name: r'sourceIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sourceIds;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;



      /// An exchange key is stored (132)
  @JsonKey(
    
    name: r'exchangeKeySet',
    required: false,
    includeIfNull: false,
  )


  final bool? exchangeKeySet;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Peer &&
      other.id == id &&
      other.name == name &&
      other.baseUrl == baseUrl &&
      other.mode == mode &&
      other.enabled == enabled &&
      other.state == state &&
      other.healthReason == healthReason &&
      other.lastSeq == lastSeq &&
      other.lagSeq == lagSeq &&
      other.lastError == lastError &&
      other.pinnedKid == pinnedKid &&
      other.remoteInstanceId == remoteInstanceId &&
      other.selection == selection &&
      other.sameOperator == sameOperator &&
      other.reliability == reliability &&
      other.tokenSet == tokenSet &&
      other.ingestKeyPrefix == ingestKeyPrefix &&
      other.sourceIds == sourceIds &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt &&
      other.exchangeKeySet == exchangeKeySet;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        baseUrl.hashCode +
        mode.hashCode +
        enabled.hashCode +
        state.hashCode +
        (healthReason == null ? 0 : healthReason.hashCode) +
        (lastSeq == null ? 0 : lastSeq.hashCode) +
        (lagSeq == null ? 0 : lagSeq.hashCode) +
        (lastError == null ? 0 : lastError.hashCode) +
        (pinnedKid == null ? 0 : pinnedKid.hashCode) +
        (remoteInstanceId == null ? 0 : remoteInstanceId.hashCode) +
        selection.hashCode +
        sameOperator.hashCode +
        reliability.hashCode +
        tokenSet.hashCode +
        (ingestKeyPrefix == null ? 0 : ingestKeyPrefix.hashCode) +
        sourceIds.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode +
        exchangeKeySet.hashCode;

  factory Peer.fromJson(Map<String, dynamic> json) => _$PeerFromJson(json);

  Map<String, dynamic> toJson() => _$PeerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PeerModeEnum {
@JsonValue(r'PULL')
PULL(r'PULL'),
@JsonValue(r'RECEIVE')
RECEIVE(r'RECEIVE');

const PeerModeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PeerHealthReasonEnum {
@JsonValue(r'PEER_KEY_MISMATCH')
PEER_KEY_MISMATCH(r'PEER_KEY_MISMATCH'),
@JsonValue(r'PEER_UNREACHABLE')
PEER_UNREACHABLE(r'PEER_UNREACHABLE'),
@JsonValue(r'PEER_RESYNC')
PEER_RESYNC(r'PEER_RESYNC');

const PeerHealthReasonEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Admiralty source reliability: A completely reliable, B usually reliable, C fairly reliable, D not usually reliable, E unreliable, F cannot be judged
enum PeerReliabilityEnum {
@JsonValue(r'A')
A(r'A'),
@JsonValue(r'B')
B(r'B'),
@JsonValue(r'C')
C(r'C'),
@JsonValue(r'D')
D(r'D'),
@JsonValue(r'E')
E(r'E'),
@JsonValue(r'F')
F(r'F');

const PeerReliabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


