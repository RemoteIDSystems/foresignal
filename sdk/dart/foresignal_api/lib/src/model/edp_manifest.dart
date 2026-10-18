//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_stream.dart';
import 'package:foresignal_api/src/model/edp_source_info.dart';
import 'package:foresignal_api/src/model/edp_transport.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_manifest.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpManifest {
  /// Returns a new [EdpManifest] instance.
  EdpManifest({

    required  this.protocol,

    required  this.source_,

    required  this.transport,

    required  this.streams,
  });

      /// foresignal-edp/1.N; N must not exceed the highest minor the instance supports (GET /v1/external/protocol), else PROTOCOL_UNSUPPORTED
  @JsonKey(
    
    name: r'protocol',
    required: true,
    includeIfNull: false,
  )


  final String protocol;



  @JsonKey(
    
    name: r'source',
    required: true,
    includeIfNull: false,
  )


  final EdpSourceInfo source_;



  @JsonKey(
    
    name: r'transport',
    required: true,
    includeIfNull: false,
  )


  final EdpTransport transport;



  @JsonKey(
    
    name: r'streams',
    required: true,
    includeIfNull: false,
  )


  final List<EdpStream> streams;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpManifest &&
      other.protocol == protocol &&
      other.source_ == source_ &&
      other.transport == transport &&
      other.streams == streams;

    @override
    int get hashCode =>
        protocol.hashCode +
        source_.hashCode +
        transport.hashCode +
        streams.hashCode;

  factory EdpManifest.fromJson(Map<String, dynamic> json) => _$EdpManifestFromJson(json);

  Map<String, dynamic> toJson() => _$EdpManifestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

