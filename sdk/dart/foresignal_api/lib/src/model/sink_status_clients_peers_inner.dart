//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sink_status_clients_peers_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SinkStatusClientsPeersInner {
  /// Returns a new [SinkStatusClientsPeersInner] instance.
  SinkStatusClientsPeersInner({

    required  this.address,

    required  this.connectedAt,
  });

  @JsonKey(
    
    name: r'address',
    required: true,
    includeIfNull: false,
  )


  final String address;



  @JsonKey(
    
    name: r'connectedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime connectedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SinkStatusClientsPeersInner &&
      other.address == address &&
      other.connectedAt == connectedAt;

    @override
    int get hashCode =>
        address.hashCode +
        connectedAt.hashCode;

  factory SinkStatusClientsPeersInner.fromJson(Map<String, dynamic> json) => _$SinkStatusClientsPeersInnerFromJson(json);

  Map<String, dynamic> toJson() => _$SinkStatusClientsPeersInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

