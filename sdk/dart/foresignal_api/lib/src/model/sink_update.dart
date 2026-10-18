//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/sink_filter.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sink_update.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SinkUpdate {
  /// Returns a new [SinkUpdate] instance.
  SinkUpdate({

    required  this.name,

     this.enabled,

    required  this.config,

     this.secrets,

     this.filter,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'enabled',
    required: false,
    includeIfNull: false,
  )


  final bool? enabled;



      /// Per-type settings validated against SinkType.configSchema. Common keys: `mode` (PUSH|SERVE where the type supports both), `bind` (default 127.0.0.1; loopback, RFC 1918, link-local, ULA, or 100.64.0.0/10 with `allowTailnet`), `port` (required for SERVE, no default), `allowAllInterfaces`, `maxClients` (default 8), `maxPerSecond`; broker and target URLs pass the outbound URL policy (035).
  @JsonKey(
    
    name: r'config',
    required: true,
    includeIfNull: false,
  )


  final Map<String, Object> config;



  @JsonKey(
    
    name: r'secrets',
    required: false,
    includeIfNull: false,
  )


  final Map<String, String?>? secrets;



  @JsonKey(
    
    name: r'filter',
    required: false,
    includeIfNull: false,
  )


  final SinkFilter? filter;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SinkUpdate &&
      other.name == name &&
      other.enabled == enabled &&
      other.config == config &&
      other.secrets == secrets &&
      other.filter == filter;

    @override
    int get hashCode =>
        name.hashCode +
        enabled.hashCode +
        config.hashCode +
        (secrets == null ? 0 : secrets.hashCode) +
        filter.hashCode;

  factory SinkUpdate.fromJson(Map<String, dynamic> json) => _$SinkUpdateFromJson(json);

  Map<String, dynamic> toJson() => _$SinkUpdateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

