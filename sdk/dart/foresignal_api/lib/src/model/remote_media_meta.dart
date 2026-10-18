//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'remote_media_meta.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RemoteMediaMeta {
  /// Returns a new [RemoteMediaMeta] instance.
  RemoteMediaMeta({

     this.passId,

    required  this.composite,

    required  this.timestamp,

    required  this.satellite,
  });

      /// satellite-pass externalId to attach to; else the 049 join rule applies
  @JsonKey(
    
    name: r'passId',
    required: false,
    includeIfNull: false,
  )


  final String? passId;



  @JsonKey(
    
    name: r'composite',
    required: true,
    includeIfNull: false,
  )


  final String composite;



  @JsonKey(
    
    name: r'timestamp',
    required: true,
    includeIfNull: false,
  )


  final DateTime timestamp;



  @JsonKey(
    
    name: r'satellite',
    required: true,
    includeIfNull: false,
  )


  final String satellite;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RemoteMediaMeta &&
      other.passId == passId &&
      other.composite == composite &&
      other.timestamp == timestamp &&
      other.satellite == satellite;

    @override
    int get hashCode =>
        (passId == null ? 0 : passId.hashCode) +
        composite.hashCode +
        timestamp.hashCode +
        satellite.hashCode;

  factory RemoteMediaMeta.fromJson(Map<String, dynamic> json) => _$RemoteMediaMetaFromJson(json);

  Map<String, dynamic> toJson() => _$RemoteMediaMetaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

