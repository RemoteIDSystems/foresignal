//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/object_acl_entry.dart';
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/shareable_type.dart';
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'object_acl.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ObjectAcl {
  /// Returns a new [ObjectAcl] instance.
  ObjectAcl({

    required  this.ref,

    required  this.type,

    required  this.owner,

    required  this.visibility,

    required  this.tlp,

    required  this.entries,

     this.updatedAt,
  });

      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final ShareableType type;



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
    
    name: r'entries',
    required: true,
    includeIfNull: false,
  )


  final List<ObjectAclEntry> entries;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ObjectAcl &&
      other.ref == ref &&
      other.type == type &&
      other.owner == owner &&
      other.visibility == visibility &&
      other.tlp == tlp &&
      other.entries == entries &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        ref.hashCode +
        type.hashCode +
        owner.hashCode +
        visibility.hashCode +
        tlp.hashCode +
        entries.hashCode +
        updatedAt.hashCode;

  factory ObjectAcl.fromJson(Map<String, dynamic> json) => _$ObjectAclFromJson(json);

  Map<String, dynamic> toJson() => _$ObjectAclToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

