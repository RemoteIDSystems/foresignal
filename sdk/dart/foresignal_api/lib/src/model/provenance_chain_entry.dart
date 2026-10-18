//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'provenance_chain_entry.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ProvenanceChainEntry {
  /// Returns a new [ProvenanceChainEntry] instance.
  ProvenanceChainEntry({

    required  this.instanceId,

    required  this.principal,

    required  this.productRef,

     this.manifestId,

     this.role,

     this.at,
  });

  @JsonKey(
    
    name: r'instanceId',
    required: true,
    includeIfNull: false,
  )


  final String instanceId;



      /// Principal handle on that instance
  @JsonKey(
    
    name: r'principal',
    required: true,
    includeIfNull: false,
  )


  final String principal;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'productRef',
    required: true,
    includeIfNull: false,
  )


  final String productRef;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;



  @JsonKey(
    
    name: r'role',
    required: false,
    includeIfNull: false,
  )


  final ProvenanceChainEntryRoleEnum? role;



  @JsonKey(
    
    name: r'at',
    required: false,
    includeIfNull: false,
  )


  final DateTime? at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ProvenanceChainEntry &&
      other.instanceId == instanceId &&
      other.principal == principal &&
      other.productRef == productRef &&
      other.manifestId == manifestId &&
      other.role == role &&
      other.at == at;

    @override
    int get hashCode =>
        instanceId.hashCode +
        principal.hashCode +
        productRef.hashCode +
        (manifestId == null ? 0 : manifestId.hashCode) +
        role.hashCode +
        (at == null ? 0 : at.hashCode);

  factory ProvenanceChainEntry.fromJson(Map<String, dynamic> json) => _$ProvenanceChainEntryFromJson(json);

  Map<String, dynamic> toJson() => _$ProvenanceChainEntryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ProvenanceChainEntryRoleEnum {
@JsonValue(r'origin')
origin(r'origin'),
@JsonValue(r'author')
author(r'author'),
@JsonValue(r'forwarder')
forwarder(r'forwarder'),
@JsonValue(r'importer')
importer(r'importer');

const ProvenanceChainEntryRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


