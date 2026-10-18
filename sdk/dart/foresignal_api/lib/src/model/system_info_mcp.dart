//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/system_info_mcp_transports.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_mcp.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoMcp {
  /// Returns a new [SystemInfoMcp] instance.
  SystemInfoMcp({

     this.enabled,

     this.sessions,

     this.transports,

     this.tools,
  });

  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



  @JsonKey(
    
    name: r'sessions',
    required: false,
    includeIfNull: false,
  )


  final int? sessions;



  @JsonKey(
    
    name: r'transports',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoMcpTransports? transports;



      /// Registered tools
  @JsonKey(
    
    name: r'tools',
    required: false,
    includeIfNull: false,
  )


  final int? tools;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoMcp &&
      other.enabled == enabled &&
      other.sessions == sessions &&
      other.transports == transports &&
      other.tools == tools;

    @override
    int get hashCode =>
        enabled.hashCode +
        sessions.hashCode +
        transports.hashCode +
        tools.hashCode;

  factory SystemInfoMcp.fromJson(Map<String, dynamic> json) => _$SystemInfoMcpFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoMcpToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

