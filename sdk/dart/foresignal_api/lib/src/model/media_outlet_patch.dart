//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'media_outlet_patch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MediaOutletPatch {
  /// Returns a new [MediaOutletPatch] instance.
  MediaOutletPatch({

     this.name,

     this.type,

     this.ownerOrg,

     this.parentOrg,

     this.wikidataQid,

     this.domains,

     this.mergeOutletIds,

     this.aggregates,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'type',
    required: false,
    includeIfNull: false,
  )


  final MediaOutletPatchTypeEnum? type;



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



      /// Replaces the domain list
  @JsonKey(
    
    name: r'domains',
    required: false,
    includeIfNull: false,
  )


  final List<String>? domains;



      /// Merge these outlets (and their domains and ratings) into this one
  @JsonKey(
    
    name: r'mergeOutletIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? mergeOutletIds;



      /// Wire originators this outlet syndicates (empty: any wire); used to group reprints (133)
  @JsonKey(
    
    name: r'aggregates',
    required: false,
    includeIfNull: false,
  )


  final List<String>? aggregates;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MediaOutletPatch &&
      other.name == name &&
      other.type == type &&
      other.ownerOrg == ownerOrg &&
      other.parentOrg == parentOrg &&
      other.wikidataQid == wikidataQid &&
      other.domains == domains &&
      other.mergeOutletIds == mergeOutletIds &&
      other.aggregates == aggregates;

    @override
    int get hashCode =>
        name.hashCode +
        type.hashCode +
        (ownerOrg == null ? 0 : ownerOrg.hashCode) +
        (parentOrg == null ? 0 : parentOrg.hashCode) +
        (wikidataQid == null ? 0 : wikidataQid.hashCode) +
        domains.hashCode +
        mergeOutletIds.hashCode +
        aggregates.hashCode;

  factory MediaOutletPatch.fromJson(Map<String, dynamic> json) => _$MediaOutletPatchFromJson(json);

  Map<String, dynamic> toJson() => _$MediaOutletPatchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MediaOutletPatchTypeEnum {
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

const MediaOutletPatchTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


