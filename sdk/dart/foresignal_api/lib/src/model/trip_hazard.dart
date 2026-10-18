//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/severity.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'trip_hazard.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TripHazard {
  /// Returns a new [TripHazard] instance.
  TripHazard({

    required  this.ref,

    required  this.sourceId,

    required  this.externalId,

    required  this.kind,

     this.title,

     this.severity,

    required  this.section,

    required  this.distanceAlongRouteM,

     this.distanceFromRouteM,

     this.validFrom,

     this.validUntil,

     this.ahead,

     this.etaSeconds,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final String kind;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'severity',
    required: false,
    includeIfNull: false,
  )


  final Severity? severity;



      /// on route, near route (within 2 × buffer), or a forecast hazard whose validFrom falls in the window
  @JsonKey(
    
    name: r'section',
    required: true,
    includeIfNull: false,
  )


  final TripHazardSectionEnum section;



  @JsonKey(
    
    name: r'distanceAlongRouteM',
    required: true,
    includeIfNull: false,
  )


  final num distanceAlongRouteM;



  @JsonKey(
    
    name: r'distanceFromRouteM',
    required: false,
    includeIfNull: false,
  )


  final num? distanceFromRouteM;



  @JsonKey(
    
    name: r'validFrom',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validFrom;



  @JsonKey(
    
    name: r'validUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validUntil;



      /// With an anchor: hazard is ahead of the anchor's projection
  @JsonKey(
    
    name: r'ahead',
    required: false,
    includeIfNull: false,
  )


  final bool? ahead;



  @JsonKey(
    
    name: r'etaSeconds',
    required: false,
    includeIfNull: false,
  )


  final num? etaSeconds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TripHazard &&
      other.ref == ref &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.kind == kind &&
      other.title == title &&
      other.severity == severity &&
      other.section == section &&
      other.distanceAlongRouteM == distanceAlongRouteM &&
      other.distanceFromRouteM == distanceFromRouteM &&
      other.validFrom == validFrom &&
      other.validUntil == validUntil &&
      other.ahead == ahead &&
      other.etaSeconds == etaSeconds;

    @override
    int get hashCode =>
        ref.hashCode +
        sourceId.hashCode +
        externalId.hashCode +
        kind.hashCode +
        (title == null ? 0 : title.hashCode) +
        (severity == null ? 0 : severity.hashCode) +
        section.hashCode +
        distanceAlongRouteM.hashCode +
        distanceFromRouteM.hashCode +
        (validFrom == null ? 0 : validFrom.hashCode) +
        (validUntil == null ? 0 : validUntil.hashCode) +
        (ahead == null ? 0 : ahead.hashCode) +
        (etaSeconds == null ? 0 : etaSeconds.hashCode);

  factory TripHazard.fromJson(Map<String, dynamic> json) => _$TripHazardFromJson(json);

  Map<String, dynamic> toJson() => _$TripHazardToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// on route, near route (within 2 × buffer), or a forecast hazard whose validFrom falls in the window
enum TripHazardSectionEnum {
@JsonValue(r'on-route')
onRoute(r'on-route'),
@JsonValue(r'near-route')
nearRoute(r'near-route'),
@JsonValue(r'timing')
timing(r'timing');

const TripHazardSectionEnum(this.value);

final String value;

@override
String toString() => value;
}


