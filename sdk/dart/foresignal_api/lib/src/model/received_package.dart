//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/provenance_chain_entry.dart';
import 'package:foresignal_api/src/model/package_manifest_authors_inner.dart';
import 'package:foresignal_api/src/model/received_package_contents_inner.dart';
import 'package:foresignal_api/src/model/received_package_sender.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'received_package.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReceivedPackage {
  /// Returns a new [ReceivedPackage] instance.
  ReceivedPackage({

    required  this.id,

    required  this.packageId,

    required  this.type,

    required  this.objectRef,

    required  this.title,

    required  this.sender,

    required  this.authors,

    required  this.tlp,

    required  this.signatureStatus,

     this.keySource,

    required  this.hashesVerified,

    required  this.receivedVia,

    required  this.receivedAt,

     this.recipients,

     this.message,

     this.acknowledgedAt,

     this.provenanceChain,

     this.contents,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



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


  final ReceivedPackageTypeEnum type;



      /// `remote:{instanceId}/{type}:{id}`
  @JsonKey(
    
    name: r'objectRef',
    required: true,
    includeIfNull: false,
  )


  final String objectRef;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'sender',
    required: true,
    includeIfNull: false,
  )


  final ReceivedPackageSender sender;



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
    
    name: r'signatureStatus',
    required: true,
    includeIfNull: false,
  )


  final ReceivedPackageSignatureStatusEnum signatureStatus;



  @JsonKey(
    
    name: r'keySource',
    required: false,
    includeIfNull: false,
  )


  final ReceivedPackageKeySourceEnum? keySource;



  @JsonKey(
    
    name: r'hashesVerified',
    required: true,
    includeIfNull: false,
  )


  final bool hashesVerified;



  @JsonKey(
    
    name: r'receivedVia',
    required: true,
    includeIfNull: false,
  )


  final ReceivedPackageReceivedViaEnum receivedVia;



  @JsonKey(
    
    name: r'receivedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime receivedAt;



      /// Local principal handles
  @JsonKey(
    
    name: r'recipients',
    required: false,
    includeIfNull: false,
  )


  final List<String>? recipients;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;



  @JsonKey(
    
    name: r'acknowledgedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? acknowledgedAt;



  @JsonKey(
    
    name: r'provenanceChain',
    required: false,
    includeIfNull: false,
  )


  final List<ProvenanceChainEntry>? provenanceChain;



  @JsonKey(
    
    name: r'contents',
    required: false,
    includeIfNull: false,
  )


  final List<ReceivedPackageContentsInner>? contents;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReceivedPackage &&
      other.id == id &&
      other.packageId == packageId &&
      other.type == type &&
      other.objectRef == objectRef &&
      other.title == title &&
      other.sender == sender &&
      other.authors == authors &&
      other.tlp == tlp &&
      other.signatureStatus == signatureStatus &&
      other.keySource == keySource &&
      other.hashesVerified == hashesVerified &&
      other.receivedVia == receivedVia &&
      other.receivedAt == receivedAt &&
      other.recipients == recipients &&
      other.message == message &&
      other.acknowledgedAt == acknowledgedAt &&
      other.provenanceChain == provenanceChain &&
      other.contents == contents;

    @override
    int get hashCode =>
        id.hashCode +
        packageId.hashCode +
        type.hashCode +
        objectRef.hashCode +
        title.hashCode +
        sender.hashCode +
        authors.hashCode +
        tlp.hashCode +
        signatureStatus.hashCode +
        keySource.hashCode +
        hashesVerified.hashCode +
        receivedVia.hashCode +
        receivedAt.hashCode +
        recipients.hashCode +
        (message == null ? 0 : message.hashCode) +
        (acknowledgedAt == null ? 0 : acknowledgedAt.hashCode) +
        provenanceChain.hashCode +
        contents.hashCode;

  factory ReceivedPackage.fromJson(Map<String, dynamic> json) => _$ReceivedPackageFromJson(json);

  Map<String, dynamic> toJson() => _$ReceivedPackageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ReceivedPackageTypeEnum {
@JsonValue(r'report')
report(r'report'),
@JsonValue(r'investigation')
investigation(r'investigation'),
@JsonValue(r'chronicle-extract')
chronicleExtract(r'chronicle-extract'),
@JsonValue(r'evidence')
evidence(r'evidence');

const ReceivedPackageTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ReceivedPackageSignatureStatusEnum {
@JsonValue(r'valid')
valid(r'valid'),
@JsonValue(r'invalid')
invalid(r'invalid'),
@JsonValue(r'unknown-key')
unknownKey(r'unknown-key');

const ReceivedPackageSignatureStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ReceivedPackageKeySourceEnum {
@JsonValue(r'peer-jwks')
peerJwks(r'peer-jwks'),
@JsonValue(r'pinned')
pinned(r'pinned');

const ReceivedPackageKeySourceEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ReceivedPackageReceivedViaEnum {
@JsonValue(r'peer')
peer(r'peer'),
@JsonValue(r'import')
import_(r'import');

const ReceivedPackageReceivedViaEnum(this.value);

final String value;

@override
String toString() => value;
}


