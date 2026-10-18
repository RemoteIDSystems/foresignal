//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_archive.dart';
import 'package:foresignal_api/src/model/chronicle_area.dart';
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/chronicle_layer.dart';
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Chronicle {
  /// Returns a new [Chronicle] instance.
  Chronicle({

    required  this.id,

    required  this.ref,

    required  this.displayId,

    required  this.name,

     this.description,

    required  this.owner,

    required  this.visibility,

    required  this.tlp,

    required  this.status,

     this.area,

     this.startedAt,

     this.stoppedAt,

     this.captureFromSeq,

     this.captureToSeq,

    required  this.retention,

    required  this.sizeCapBytes,

    required  this.bytesUsed,

     this.capState,

    required  this.workspaceId,

     this.situationIds,

     this.hypothesisSetIds,

     this.investigationIds,

     this.topicIds,

    required  this.origin,

     this.proposalId,

    required  this.layers,

     this.archive,

     this.discoverability,

     this.revealTitle,

     this.revealOwner,

     this.unreadCount,

     this.tags,

    required  this.version,

    required  this.createdAt,

     this.updatedAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'displayId',
    required: true,
    includeIfNull: false,
  )


  final String displayId;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'owner',
    required: true,
    includeIfNull: false,
  )


  final PrincipalSummary owner;



  @JsonKey(
    
    name: r'visibility',
    required: true,
    includeIfNull: false,
  )


  final Visibility visibility;



  @JsonKey(
    
    name: r'tlp',
    required: true,
    includeIfNull: false,
  )


  final Tlp tlp;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final ChronicleStatusEnum status;



  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final ChronicleArea? area;



  @JsonKey(
    
    name: r'startedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? startedAt;



  @JsonKey(
    
    name: r'stoppedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? stoppedAt;



  @JsonKey(
    
    name: r'captureFromSeq',
    required: false,
    includeIfNull: false,
  )


  final int? captureFromSeq;



  @JsonKey(
    
    name: r'captureToSeq',
    required: false,
    includeIfNull: false,
  )


  final int? captureToSeq;



      /// `forever` (default) or an ISO-8601 duration kept after stop
  @JsonKey(
    
    name: r'retention',
    required: true,
    includeIfNull: false,
  )


  final String retention;



  @JsonKey(
    
    name: r'sizeCapBytes',
    required: true,
    includeIfNull: false,
  )


  final int sizeCapBytes;



  @JsonKey(
    
    name: r'bytesUsed',
    required: true,
    includeIfNull: false,
  )


  final int bytesUsed;



      /// `warning` at 90% of the cap; `exceeded` switches full and downsampled layers to events-only
  @JsonKey(
    
    name: r'capState',
    required: false,
    includeIfNull: false,
  )


  final ChronicleCapStateEnum? capState;



      /// ULID identifier
  @JsonKey(
    
    name: r'workspaceId',
    required: true,
    includeIfNull: false,
  )


  final String workspaceId;



  @JsonKey(
    
    name: r'situationIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? situationIds;



  @JsonKey(
    
    name: r'hypothesisSetIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? hypothesisSetIds;



  @JsonKey(
    
    name: r'investigationIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? investigationIds;



      /// 117 attention topic ids
  @JsonKey(
    
    name: r'topicIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? topicIds;



  @JsonKey(
    
    name: r'origin',
    required: true,
    includeIfNull: false,
  )


  final ChronicleOriginEnum origin;



      /// ULID identifier
  @JsonKey(
    
    name: r'proposalId',
    required: false,
    includeIfNull: false,
  )


  final String? proposalId;



  @JsonKey(
    
    name: r'layers',
    required: true,
    includeIfNull: false,
  )


  final List<ChronicleLayer> layers;



  @JsonKey(
    
    name: r'archive',
    required: false,
    includeIfNull: false,
  )


  final ChronicleArchive? archive;



      /// Owner's discovery setting, capped by TLP (134): the effective value
  @JsonKey(
    
    name: r'discoverability',
    required: false,
    includeIfNull: false,
  )


  final ChronicleDiscoverabilityEnum? discoverability;



      /// Show the title in opaque links (134)
  @JsonKey(
    
    name: r'revealTitle',
    required: false,
    includeIfNull: false,
  )


  final bool? revealTitle;



      /// Show the owner handle in opaque links (134)
  @JsonKey(
    
    name: r'revealOwner',
    required: false,
    includeIfNull: false,
  )


  final bool? revealOwner;



      /// Unread thread items for the caller
  @JsonKey(
    
    name: r'unreadCount',
    required: false,
    includeIfNull: false,
  )


  final int? unreadCount;



      /// Caller's visible tags
  @JsonKey(
    
    name: r'tags',
    required: false,
    includeIfNull: false,
  )


  final List<String>? tags;



      /// Optimistic-lock version; sent as ETag
  @JsonKey(
    
    name: r'version',
    required: true,
    includeIfNull: false,
  )


  final int version;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Chronicle &&
      other.id == id &&
      other.ref == ref &&
      other.displayId == displayId &&
      other.name == name &&
      other.description == description &&
      other.owner == owner &&
      other.visibility == visibility &&
      other.tlp == tlp &&
      other.status == status &&
      other.area == area &&
      other.startedAt == startedAt &&
      other.stoppedAt == stoppedAt &&
      other.captureFromSeq == captureFromSeq &&
      other.captureToSeq == captureToSeq &&
      other.retention == retention &&
      other.sizeCapBytes == sizeCapBytes &&
      other.bytesUsed == bytesUsed &&
      other.capState == capState &&
      other.workspaceId == workspaceId &&
      other.situationIds == situationIds &&
      other.hypothesisSetIds == hypothesisSetIds &&
      other.investigationIds == investigationIds &&
      other.topicIds == topicIds &&
      other.origin == origin &&
      other.proposalId == proposalId &&
      other.layers == layers &&
      other.archive == archive &&
      other.discoverability == discoverability &&
      other.revealTitle == revealTitle &&
      other.revealOwner == revealOwner &&
      other.unreadCount == unreadCount &&
      other.tags == tags &&
      other.version == version &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        ref.hashCode +
        displayId.hashCode +
        name.hashCode +
        (description == null ? 0 : description.hashCode) +
        owner.hashCode +
        visibility.hashCode +
        tlp.hashCode +
        status.hashCode +
        (area == null ? 0 : area.hashCode) +
        (startedAt == null ? 0 : startedAt.hashCode) +
        (stoppedAt == null ? 0 : stoppedAt.hashCode) +
        (captureFromSeq == null ? 0 : captureFromSeq.hashCode) +
        (captureToSeq == null ? 0 : captureToSeq.hashCode) +
        retention.hashCode +
        sizeCapBytes.hashCode +
        bytesUsed.hashCode +
        capState.hashCode +
        workspaceId.hashCode +
        situationIds.hashCode +
        hypothesisSetIds.hashCode +
        investigationIds.hashCode +
        topicIds.hashCode +
        origin.hashCode +
        (proposalId == null ? 0 : proposalId.hashCode) +
        layers.hashCode +
        (archive == null ? 0 : archive.hashCode) +
        discoverability.hashCode +
        revealTitle.hashCode +
        revealOwner.hashCode +
        unreadCount.hashCode +
        tags.hashCode +
        version.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory Chronicle.fromJson(Map<String, dynamic> json) => _$ChronicleFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleStatusEnum {
@JsonValue(r'proposed')
proposed(r'proposed'),
@JsonValue(r'active')
active(r'active'),
@JsonValue(r'paused')
paused(r'paused'),
@JsonValue(r'stopped')
stopped(r'stopped'),
@JsonValue(r'archived')
archived(r'archived');

const ChronicleStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `warning` at 90% of the cap; `exceeded` switches full and downsampled layers to events-only
enum ChronicleCapStateEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'warning')
warning(r'warning'),
@JsonValue(r'exceeded')
exceeded(r'exceeded');

const ChronicleCapStateEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ChronicleOriginEnum {
@JsonValue(r'operator')
operator_(r'operator'),
@JsonValue(r'agent')
agent(r'agent'),
@JsonValue(r'analyst')
analyst(r'analyst');

const ChronicleOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Owner's discovery setting, capped by TLP (134): the effective value
enum ChronicleDiscoverabilityEnum {
@JsonValue(r'none')
none(r'none'),
@JsonValue(r'instance')
instance(r'instance'),
@JsonValue(r'network')
network(r'network');

const ChronicleDiscoverabilityEnum(this.value);

final String value;

@override
String toString() => value;
}


