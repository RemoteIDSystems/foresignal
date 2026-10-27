//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/egress_health_sources_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'egress_health.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EgressHealth {
  /// Returns a new [EgressHealth] instance.
  EgressHealth({

    required  this.reachable,

     this.exitIp,

     this.latencyMs,

     this.lastCheckedAt,

     this.consecutiveFailures = 0,

     this.backoffUntil,

     this.lastError,

     this.sources,
  });

  @JsonKey(
    
    name: r'reachable',
    required: true,
    includeIfNull: false,
  )


  final bool reachable;



      /// Public IP the route presents
  @JsonKey(
    
    name: r'exitIp',
    required: false,
    includeIfNull: false,
  )


  final String? exitIp;



  @JsonKey(
    
    name: r'latencyMs',
    required: false,
    includeIfNull: false,
  )


  final num? latencyMs;



  @JsonKey(
    
    name: r'lastCheckedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastCheckedAt;



  @JsonKey(
    defaultValue: 0,
    name: r'consecutiveFailures',
    required: false,
    includeIfNull: false,
  )


  final int? consecutiveFailures;



      /// Skipped until this time after failures
  @JsonKey(
    
    name: r'backoffUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? backoffUntil;



      /// The last failure carrying a request through the route (R2-018)
  @JsonKey(
    
    name: r'lastError',
    required: false,
    includeIfNull: false,
  )


  final String? lastError;



      /// Only on getEgressRouteHealth: the sources held off this route by a 429 or Retry-After received through it (R2-018)
  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<EgressHealthSourcesInner>? sources;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EgressHealth &&
      other.reachable == reachable &&
      other.exitIp == exitIp &&
      other.latencyMs == latencyMs &&
      other.lastCheckedAt == lastCheckedAt &&
      other.consecutiveFailures == consecutiveFailures &&
      other.backoffUntil == backoffUntil &&
      other.lastError == lastError &&
      other.sources == sources;

    @override
    int get hashCode =>
        reachable.hashCode +
        (exitIp == null ? 0 : exitIp.hashCode) +
        (latencyMs == null ? 0 : latencyMs.hashCode) +
        (lastCheckedAt == null ? 0 : lastCheckedAt.hashCode) +
        consecutiveFailures.hashCode +
        (backoffUntil == null ? 0 : backoffUntil.hashCode) +
        (lastError == null ? 0 : lastError.hashCode) +
        sources.hashCode;

  factory EgressHealth.fromJson(Map<String, dynamic> json) => _$EgressHealthFromJson(json);

  Map<String, dynamic> toJson() => _$EgressHealthToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

