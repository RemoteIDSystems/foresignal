//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sink_status_clients_peers_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sink_status_clients.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SinkStatusClients {
  /// Returns a new [SinkStatusClients] instance.
  SinkStatusClients({

    required  this.connected,

    required  this.max,

     this.rejected,

     this.peers,
  });

  @JsonKey(
    
    name: r'connected',
    required: true,
    includeIfNull: false,
  )


  final int connected;



  @JsonKey(
    
    name: r'max',
    required: true,
    includeIfNull: false,
  )


  final int max;



      /// Connections closed because the peer address was not allowed or the maxClients cap was reached
  @JsonKey(
    
    name: r'rejected',
    required: false,
    includeIfNull: false,
  )


  final int? rejected;



  @JsonKey(
    
    name: r'peers',
    required: false,
    includeIfNull: false,
  )


  final List<SinkStatusClientsPeersInner>? peers;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SinkStatusClients &&
      other.connected == connected &&
      other.max == max &&
      other.rejected == rejected &&
      other.peers == peers;

    @override
    int get hashCode =>
        connected.hashCode +
        max.hashCode +
        rejected.hashCode +
        peers.hashCode;

  factory SinkStatusClients.fromJson(Map<String, dynamic> json) => _$SinkStatusClientsFromJson(json);

  Map<String, dynamic> toJson() => _$SinkStatusClientsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

