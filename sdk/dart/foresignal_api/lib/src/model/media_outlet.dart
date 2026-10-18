//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/media_rating.dart';
import 'package:foresignal_api/src/model/outlet_consensus.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'media_outlet.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MediaOutlet {
  /// Returns a new [MediaOutlet] instance.
  MediaOutlet({

    required  this.id,

    required  this.ref,

    required  this.name,

    required  this.domains,

     this.country,

     this.languages,

    required  this.type,

     this.ownerOrg,

     this.parentOrg,

     this.wikidataQid,

     this.originatorId,

     this.aggregates,

    required  this.rated,

     this.ratings,

    required  this.consensus,

    required  this.createdAt,

     this.updatedAt,
  });

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
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'domains',
    required: true,
    includeIfNull: false,
  )


  final List<String> domains;



      /// ISO 3166-1 alpha-2
  @JsonKey(
    
    name: r'country',
    required: false,
    includeIfNull: false,
  )


  final String? country;



  @JsonKey(
    
    name: r'languages',
    required: false,
    includeIfNull: false,
  )


  final List<String>? languages;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final MediaOutletTypeEnum type;



      /// Organisation name only
  @JsonKey(
    
    name: r'ownerOrg',
    required: false,
    includeIfNull: false,
  )


  final String? ownerOrg;



  @JsonKey(
    
    name: r'parentOrg',
    required: false,
    includeIfNull: false,
  )


  final String? parentOrg;



  @JsonKey(
    
    name: r'wikidataQid',
    required: false,
    includeIfNull: false,
  )


  final String? wikidataQid;



      /// 116 originator; wires map to their own
  @JsonKey(
    
    name: r'originatorId',
    required: false,
    includeIfNull: false,
  )


  final String? originatorId;



      /// Wire originators this outlet syndicates
  @JsonKey(
    
    name: r'aggregates',
    required: false,
    includeIfNull: false,
  )


  final List<String>? aggregates;



      /// False for `unrated` outlets added from unknown domains
  @JsonKey(
    
    name: r'rated',
    required: true,
    includeIfNull: false,
  )


  final bool rated;



      /// Detail view only
  @JsonKey(
    
    name: r'ratings',
    required: false,
    includeIfNull: false,
  )


  final List<MediaRating>? ratings;



  @JsonKey(
    
    name: r'consensus',
    required: true,
    includeIfNull: false,
  )


  final List<OutletConsensus> consensus;



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
    bool operator ==(Object other) => identical(this, other) || other is MediaOutlet &&
      other.id == id &&
      other.ref == ref &&
      other.name == name &&
      other.domains == domains &&
      other.country == country &&
      other.languages == languages &&
      other.type == type &&
      other.ownerOrg == ownerOrg &&
      other.parentOrg == parentOrg &&
      other.wikidataQid == wikidataQid &&
      other.originatorId == originatorId &&
      other.aggregates == aggregates &&
      other.rated == rated &&
      other.ratings == ratings &&
      other.consensus == consensus &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        ref.hashCode +
        name.hashCode +
        domains.hashCode +
        (country == null ? 0 : country.hashCode) +
        languages.hashCode +
        type.hashCode +
        (ownerOrg == null ? 0 : ownerOrg.hashCode) +
        (parentOrg == null ? 0 : parentOrg.hashCode) +
        (wikidataQid == null ? 0 : wikidataQid.hashCode) +
        (originatorId == null ? 0 : originatorId.hashCode) +
        aggregates.hashCode +
        rated.hashCode +
        ratings.hashCode +
        consensus.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory MediaOutlet.fromJson(Map<String, dynamic> json) => _$MediaOutletFromJson(json);

  Map<String, dynamic> toJson() => _$MediaOutletToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MediaOutletTypeEnum {
@JsonValue(r'wire')
wire(r'wire'),
@JsonValue(r'national')
national(r'national'),
@JsonValue(r'regional')
regional(r'regional'),
@JsonValue(r'local')
local(r'local'),
@JsonValue(r'public-broadcaster')
publicBroadcaster(r'public-broadcaster'),
@JsonValue(r'state-affiliated')
stateAffiliated(r'state-affiliated'),
@JsonValue(r'aggregator')
aggregator(r'aggregator'),
@JsonValue(r'blog')
blog(r'blog'),
@JsonValue(r'official')
official(r'official'),
@JsonValue(r'other')
other(r'other');

const MediaOutletTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


