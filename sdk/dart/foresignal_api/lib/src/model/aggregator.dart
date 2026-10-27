//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'aggregator.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Aggregator {
  /// Returns a new [Aggregator] instance.
  Aggregator({

    required  this.domain,

    required  this.name,

     this.enabled = false,

     this.mode,

     this.endpoint,

     this.status,

     this.supersededSources,

     this.lastSeenAt,

     this.config,

     this.disableSupersededSources,

     this.implemented,

     this.statusReason,

     this.sourceIds,
  });

  @JsonKey(
    
    name: r'domain',
    required: true,
    includeIfNull: false,
  )


  final String domain;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    defaultValue: false,
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// Own receiver, aggregated feed, or both
  @JsonKey(
    
    name: r'mode',
    required: false,
    includeIfNull: false,
  )


  final String? mode;



      /// Local endpoint the adapter reads. For adsb, the readsb aircraft.json, for example http://readsb/data/aircraft.json (R2-019)
  @JsonKey(
    
    name: r'endpoint',
    required: false,
    includeIfNull: false,
  )


  final String? endpoint;



      /// ok when the aggregator's source is healthy, degraded when degraded or not yet run, down when failing, unconfigured when not enabled with an endpoint (R2-019)
  @JsonKey(
    
    name: r'status',
    required: false,
    includeIfNull: false,
  )


  final String? status;



      /// Public/mirror source ids this aggregator supersedes under its current configuration; defaulted off when it is live (R2-022: for satellite, only the ABI imagery of the GOES named by config.goes)
  @JsonKey(
    
    name: r'supersededSources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? supersededSources;



      /// When the aggregator's source last brought data (R2-019)
  @JsonKey(
    
    name: r'lastSeenAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastSeenAt;



      /// Domain-specific settings as stored (adsb: `band`, 1090 or 978) (R2-019)
  @JsonKey(
    
    name: r'config',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? config;



      /// Whether the superseded sources are kept off while the aggregator is live (R2-019)
  @JsonKey(
    
    name: r'disableSupersededSources',
    required: false,
    includeIfNull: false,
  )


  final bool? disableSupersededSources;



      /// Whether this server has the domain's backend; a domain without one can be configured only disabled (R2-019)
  @JsonKey(
    
    name: r'implemented',
    required: false,
    includeIfNull: false,
  )


  final bool? implemented;



      /// Why the status is what it is: the health reason of the aggregator's source, or why it is unconfigured (R2-019)
  @JsonKey(
    
    name: r'statusReason',
    required: false,
    includeIfNull: false,
  )


  final String? statusReason;



      /// The first-party sources the aggregator drives; the first one's health is its status (adsb: local-adsb-aggregator) (R2-019)
  @JsonKey(
    
    name: r'sourceIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sourceIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Aggregator &&
      other.domain == domain &&
      other.name == name &&
      other.enabled == enabled &&
      other.mode == mode &&
      other.endpoint == endpoint &&
      other.status == status &&
      other.supersededSources == supersededSources &&
      other.lastSeenAt == lastSeenAt &&
      other.config == config &&
      other.disableSupersededSources == disableSupersededSources &&
      other.implemented == implemented &&
      other.statusReason == statusReason &&
      other.sourceIds == sourceIds;

    @override
    int get hashCode =>
        domain.hashCode +
        name.hashCode +
        enabled.hashCode +
        (mode == null ? 0 : mode.hashCode) +
        (endpoint == null ? 0 : endpoint.hashCode) +
        status.hashCode +
        supersededSources.hashCode +
        (lastSeenAt == null ? 0 : lastSeenAt.hashCode) +
        config.hashCode +
        disableSupersededSources.hashCode +
        implemented.hashCode +
        (statusReason == null ? 0 : statusReason.hashCode) +
        sourceIds.hashCode;

  factory Aggregator.fromJson(Map<String, dynamic> json) => _$AggregatorFromJson(json);

  Map<String, dynamic> toJson() => _$AggregatorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

