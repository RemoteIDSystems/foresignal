//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'social_physical_evidence.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SocialPhysicalEvidence {
  /// Returns a new [SocialPhysicalEvidence] instance.
  SocialPhysicalEvidence({

    required  this.ref,

    required  this.family,

    required  this.originator,

    required  this.firstObservedAt,

     this.distanceKm,

     this.negates,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'family',
    required: true,
    includeIfNull: false,
  )


  final String family;



  @JsonKey(
    
    name: r'originator',
    required: true,
    includeIfNull: false,
  )


  final String originator;



  @JsonKey(
    
    name: r'firstObservedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime firstObservedAt;



          // minimum: 0
  @JsonKey(
    
    name: r'distanceKm',
    required: false,
    includeIfNull: false,
  )


  final num? distanceKm;



      /// An agency or sensor record that explicitly negates the claim (e.g. normal service)
  @JsonKey(
    
    name: r'negates',
    required: false,
    includeIfNull: false,
  )


  final bool? negates;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SocialPhysicalEvidence &&
      other.ref == ref &&
      other.family == family &&
      other.originator == originator &&
      other.firstObservedAt == firstObservedAt &&
      other.distanceKm == distanceKm &&
      other.negates == negates;

    @override
    int get hashCode =>
        ref.hashCode +
        family.hashCode +
        originator.hashCode +
        firstObservedAt.hashCode +
        (distanceKm == null ? 0 : distanceKm.hashCode) +
        negates.hashCode;

  factory SocialPhysicalEvidence.fromJson(Map<String, dynamic> json) => _$SocialPhysicalEvidenceFromJson(json);

  Map<String, dynamic> toJson() => _$SocialPhysicalEvidenceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

