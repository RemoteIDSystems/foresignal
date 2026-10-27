//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'aggregator_config.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AggregatorConfig {
  /// Returns a new [AggregatorConfig] instance.
  AggregatorConfig({

    required  this.enabled,

     this.mode,

     this.endpoint,

     this.config,

     this.disableSupersededSources = true,
  });

  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



  @JsonKey(
    
    name: r'mode',
    required: false,
    includeIfNull: false,
  )


  final String? mode;



      /// Required to enable, except for ground-sensors. adsb: an http or https URL of the readsb aircraft.json on this host or the LAN (it must resolve to a loopback, private or unique-local address, and is pinned); no credentials, query or fragment (R2-019). ais: the AIS decoder's NMEA feed, either tcp://host:port (its TCP server, for example AIS-catcher -S 5012; a loopback, private or unique-local address, pinned) or udp://bindHost:port (where this server listens for its datagrams, which needs config.allowSenders); the port is always explicit (R2-020). datalink: tcp://host, the host the decoders run on, with no port; each decoder's port is in config (R2-021). satellite: SatDump's web server, an http or https URL (any base path is kept; its /api is read), on this host or the LAN (R2-022). ground-sensors: not used; a value is a 400 — each sensor's address is in config (R2-023)
  @JsonKey(
    
    name: r'endpoint',
    required: false,
    includeIfNull: false,
  )


  final String? endpoint;



      /// Domain-specific settings; an unknown member is a 400 (R2-019). adsb: `band`, 1090 or 978, default 1090. ais (R2-020): `allowSenders`, a list of IPs or CIDRs in private space whose datagrams are read, required with a udp:// endpoint; `stationName`, the observer of every record, default `local`. datalink (R2-021): the decoders `acars`, `vdlm2`, `hfdl` (acars_router's TCP servers) and `aprs` (the igate's KISS TCP port), each a port on the endpoint host or a whole tcp://host:port, at least one to enable; `stationId` (ACARS observer), `stationCallsign` (APRS observer), `includeMessages` (boolean). satellite (R2-022): `location` (required with an endpoint: lat,lon[,altM] or a place id), `inboxPath` (SatDump's output directory under FORESIGNAL_INBOX_DIR), `satdumpVersion` (1.2 or 2.0), `predictNorad` (NORAD numbers of the polar satellites to predict), `goes` (east or west: the GOES the dish receives, whose ABI imagery pull is superseded). ground-sensors (R2-023): one object per sensor, each with `enabled` and at least one enabled to enable: `seismic` and `same` (`source`, the custom push source the bridge posts to), `radiosonde` (`endpoint` udp://bindHost:port, `allowSenders`, `stationCallsign`; supersedes sondehub), `airq` (`deviceType` purpleair, airrohr or airgradient, `endpoint`, `lat`, `lon`, `indoor`, `name`; supersedes openaq and sensor-community), `pws` (`station` davis or ecowitt, `endpoint`, `lat`, `lon`), `mesh` (`meshtastic` mqtt:// broker with `root`, supersedes meshtastic-mqtt and meshtastic-map; `meshcore` tcp:// companion; `bridge` boolean)
  @JsonKey(
    
    name: r'config',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? config;



      /// Default the public/mirror sources off once this aggregator is live (nothing is removed); the sources it turned off are turned back on when it stops being live or this is set to false (R2-019)
  @JsonKey(
    defaultValue: true,
    name: r'disableSupersededSources',
    required: false,
    includeIfNull: false,
  )


  final bool? disableSupersededSources;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AggregatorConfig &&
      other.enabled == enabled &&
      other.mode == mode &&
      other.endpoint == endpoint &&
      other.config == config &&
      other.disableSupersededSources == disableSupersededSources;

    @override
    int get hashCode =>
        enabled.hashCode +
        mode.hashCode +
        (endpoint == null ? 0 : endpoint.hashCode) +
        config.hashCode +
        disableSupersededSources.hashCode;

  factory AggregatorConfig.fromJson(Map<String, dynamic> json) => _$AggregatorConfigFromJson(json);

  Map<String, dynamic> toJson() => _$AggregatorConfigToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

