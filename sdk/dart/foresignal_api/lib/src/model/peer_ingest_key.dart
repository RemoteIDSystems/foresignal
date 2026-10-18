//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'peer_ingest_key.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PeerIngestKey {
  /// Returns a new [PeerIngestKey] instance.
  PeerIngestKey({

    required  this.peerId,

    required  this.key,

    required  this.keyPrefix,

    required  this.createdAt,
  });

  @JsonKey(
    
    name: r'peerId',
    required: true,
    includeIfNull: false,
  )


  final String peerId;



  @JsonKey(
    
    name: r'key',
    required: true,
    includeIfNull: false,
  )


  final String key;



  @JsonKey(
    
    name: r'keyPrefix',
    required: true,
    includeIfNull: false,
  )


  final String keyPrefix;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PeerIngestKey &&
      other.peerId == peerId &&
      other.key == key &&
      other.keyPrefix == keyPrefix &&
      other.createdAt == createdAt;

    @override
    int get hashCode =>
        peerId.hashCode +
        key.hashCode +
        keyPrefix.hashCode +
        createdAt.hashCode;

  factory PeerIngestKey.fromJson(Map<String, dynamic> json) => _$PeerIngestKeyFromJson(json);

  Map<String, dynamic> toJson() => _$PeerIngestKeyToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

