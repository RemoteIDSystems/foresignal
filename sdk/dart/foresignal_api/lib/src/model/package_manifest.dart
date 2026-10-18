//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/provenance_chain_entry.dart';
import 'package:foresignal_api/src/model/package_manifest_authors_inner.dart';
import 'package:foresignal_api/src/model/package_manifest_sender.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'package_manifest.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PackageManifest {
  /// Returns a new [PackageManifest] instance.
  PackageManifest({

    required  this.packageId,

    required  this.type,

    required  this.objectRef,

    required  this.sender,

    required  this.authors,

    required  this.tlp,

    required  this.createdAt,

    required  this.recipients,

    required  this.contentHashes,

     this.message,

     this.provenanceChain,

     this.title,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'packageId',
    required: true,
    includeIfNull: false,
  )


  final String packageId;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final PackageManifestTypeEnum type;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'objectRef',
    required: true,
    includeIfNull: false,
  )


  final String objectRef;



  @JsonKey(
    
    name: r'sender',
    required: true,
    includeIfNull: false,
  )


  final PackageManifestSender sender;



  @JsonKey(
    
    name: r'authors',
    required: true,
    includeIfNull: false,
  )


  final List<PackageManifestAuthorsInner> authors;



  @JsonKey(
    
    name: r'tlp',
    required: true,
    includeIfNull: false,
  )


  final Tlp tlp;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



      /// Named recipients, or `*` for GREEN/CLEAR
  @JsonKey(
    
    name: r'recipients',
    required: true,
    includeIfNull: true,
  )


  final Object? recipients;



      /// Path under `content/` → sha256
  @JsonKey(
    
    name: r'contentHashes',
    required: true,
    includeIfNull: false,
  )


  final Map<String, String> contentHashes;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



  @JsonKey(
    
    name: r'provenanceChain',
    required: false,
    includeIfNull: false,
  )


  final List<ProvenanceChainEntry>? provenanceChain;



      /// The packaged object's title
  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PackageManifest &&
      other.packageId == packageId &&
      other.type == type &&
      other.objectRef == objectRef &&
      other.sender == sender &&
      other.authors == authors &&
      other.tlp == tlp &&
      other.createdAt == createdAt &&
      other.recipients == recipients &&
      other.contentHashes == contentHashes &&
      other.message == message &&
      other.provenanceChain == provenanceChain &&
      other.title == title;

    @override
    int get hashCode =>
        packageId.hashCode +
        type.hashCode +
        objectRef.hashCode +
        sender.hashCode +
        authors.hashCode +
        tlp.hashCode +
        createdAt.hashCode +
        (recipients == null ? 0 : recipients.hashCode) +
        contentHashes.hashCode +
        (message == null ? 0 : message.hashCode) +
        provenanceChain.hashCode +
        (title == null ? 0 : title.hashCode);

  factory PackageManifest.fromJson(Map<String, dynamic> json) => _$PackageManifestFromJson(json);

  Map<String, dynamic> toJson() => _$PackageManifestToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PackageManifestTypeEnum {
@JsonValue(r'report')
report(r'report'),
@JsonValue(r'investigation')
investigation(r'investigation'),
@JsonValue(r'chronicle-extract')
chronicleExtract(r'chronicle-extract'),
@JsonValue(r'evidence')
evidence(r'evidence');

const PackageManifestTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


