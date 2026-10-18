//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/peer_selection.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'peer_create.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PeerCreate {
  /// Returns a new [PeerCreate] instance.
  PeerCreate({

    required  this.id,

    required  this.name,

    required  this.baseUrl,

     this.mode,

     this.enabled,

     this.token,

     this.selection,

     this.sameOperator = false,

     this.reliability,

     this.exchangeKey,
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



      /// Checked by the outbound URL policy (035)
  @JsonKey(
    
    name: r'baseUrl',
    required: true,
    includeIfNull: false,
  )


  final String baseUrl;



      /// PULL: consume the peer's feed; RECEIVE: the peer (an edge) pushes here
  @JsonKey(
    
    name: r'mode',
    required: false,
    includeIfNull: false,
  )


  final PeerCreateModeEnum? mode;



  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



      /// The remote instance's token (read + federation); required for PULL
  @JsonKey(
    
    name: r'token',
    required: false,
    includeIfNull: false,
  )


  final String? token;



  @JsonKey(
    
    name: r'selection',
    required: false,
    includeIfNull: false,
  )


  final PeerSelection? selection;



      /// The operator's own hub: LOCAL_ONLY records may be pushed to it (and stay LOCAL_ONLY there)
  @JsonKey(
    defaultValue: false,
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


  final PeerCreateReliabilityEnum? reliability;



      /// The fsp_ ingest key the remote instance issued to this one (its peer row for this instance); the Bearer token of POST {baseUrl}/v1/exchange/inbox and /v1/exchange/receipts (132)
  @JsonKey(
    
    name: r'exchangeKey',
    required: false,
    includeIfNull: false,
  )


  final String? exchangeKey;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PeerCreate &&
      other.id == id &&
      other.name == name &&
      other.baseUrl == baseUrl &&
      other.mode == mode &&
      other.enabled == enabled &&
      other.token == token &&
      other.selection == selection &&
      other.sameOperator == sameOperator &&
      other.reliability == reliability &&
      other.exchangeKey == exchangeKey;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        baseUrl.hashCode +
        mode.hashCode +
        enabled.hashCode +
        token.hashCode +
        selection.hashCode +
        sameOperator.hashCode +
        reliability.hashCode +
        exchangeKey.hashCode;

  factory PeerCreate.fromJson(Map<String, dynamic> json) => _$PeerCreateFromJson(json);

  Map<String, dynamic> toJson() => _$PeerCreateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// PULL: consume the peer's feed; RECEIVE: the peer (an edge) pushes here
enum PeerCreateModeEnum {
@JsonValue(r'PULL')
PULL(r'PULL'),
@JsonValue(r'RECEIVE')
RECEIVE(r'RECEIVE');

const PeerCreateModeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Admiralty source reliability: A completely reliable, B usually reliable, C fairly reliable, D not usually reliable, E unreliable, F cannot be judged
enum PeerCreateReliabilityEnum {
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

const PeerCreateReliabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


