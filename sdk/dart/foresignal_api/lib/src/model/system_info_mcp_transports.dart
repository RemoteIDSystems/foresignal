//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_mcp_transports.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoMcpTransports {
  /// Returns a new [SystemInfoMcpTransports] instance.
  SystemInfoMcpTransports({

     this.streamable,

     this.legacySse,
  });

  @JsonKey(
    
    name: r'streamable',
    required: false,
    includeIfNull: false,
  )


  final bool? streamable;



      /// `foresignal.mcp.legacy-sse.enabled`: /mcp/sse + /mcp/message
  @JsonKey(
    
    name: r'legacySse',
    required: false,
    includeIfNull: false,
  )


  final bool? legacySse;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoMcpTransports &&
      other.streamable == streamable &&
      other.legacySse == legacySse;

    @override
    int get hashCode =>
        streamable.hashCode +
        legacySse.hashCode;

  factory SystemInfoMcpTransports.fromJson(Map<String, dynamic> json) => _$SystemInfoMcpTransportsFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoMcpTransportsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

