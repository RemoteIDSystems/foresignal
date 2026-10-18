//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/observation_link_link_reason.dart';
import 'package:foresignal_api/src/model/originator.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'observation_link.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ObservationLink {
  /// Returns a new [ObservationLink] instance.
  ObservationLink({

    required  this.canonicalEventId,

     this.recordRef,

    required  this.sourceId,

    required  this.externalId,

    required  this.seq,

     this.version,

    required  this.originatorId,

     this.originator,

     this.upstreamEventId,

    required  this.evidenceGroupId,

    required  this.linkConfidence,

    required  this.linkReason,

    required  this.linkedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'canonicalEventId',
    required: true,
    includeIfNull: false,
  )


  final String canonicalEventId;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'recordRef',
    required: false,
    includeIfNull: false,
  )


  final String? recordRef;



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
    
    name: r'seq',
    required: true,
    includeIfNull: false,
  )


  final int seq;



  @JsonKey(
    
    name: r'version',
    required: false,
    includeIfNull: false,
  )


  final int? version;



  @JsonKey(
    
    name: r'originatorId',
    required: true,
    includeIfNull: false,
  )


  final String originatorId;



      /// Included on event detail
  @JsonKey(
    
    name: r'originator',
    required: false,
    includeIfNull: false,
  )


  final Originator? originator;



  @JsonKey(
    
    name: r'upstreamEventId',
    required: false,
    includeIfNull: false,
  )


  final String? upstreamEventId;



      /// ULID identifier
  @JsonKey(
    
    name: r'evidenceGroupId',
    required: true,
    includeIfNull: false,
  )


  final String evidenceGroupId;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'linkConfidence',
    required: true,
    includeIfNull: false,
  )


  final num linkConfidence;



  @JsonKey(
    
    name: r'linkReason',
    required: true,
    includeIfNull: false,
  )


  final ObservationLinkLinkReason linkReason;



  @JsonKey(
    
    name: r'linkedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime linkedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ObservationLink &&
      other.canonicalEventId == canonicalEventId &&
      other.recordRef == recordRef &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.seq == seq &&
      other.version == version &&
      other.originatorId == originatorId &&
      other.originator == originator &&
      other.upstreamEventId == upstreamEventId &&
      other.evidenceGroupId == evidenceGroupId &&
      other.linkConfidence == linkConfidence &&
      other.linkReason == linkReason &&
      other.linkedAt == linkedAt;

    @override
    int get hashCode =>
        canonicalEventId.hashCode +
        recordRef.hashCode +
        sourceId.hashCode +
        externalId.hashCode +
        seq.hashCode +
        version.hashCode +
        originatorId.hashCode +
        (originator == null ? 0 : originator.hashCode) +
        (upstreamEventId == null ? 0 : upstreamEventId.hashCode) +
        evidenceGroupId.hashCode +
        linkConfidence.hashCode +
        linkReason.hashCode +
        linkedAt.hashCode;

  factory ObservationLink.fromJson(Map<String, dynamic> json) => _$ObservationLinkFromJson(json);

  Map<String, dynamic> toJson() => _$ObservationLinkToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

