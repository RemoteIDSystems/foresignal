//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/media_links.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'media.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Media {
  /// Returns a new [Media] instance.
  Media({

    required  this.id,

    required  this.sha256,

    required  this.contentType,

    required  this.width,

    required  this.height,

    required  this.bytes,

    required  this.role,

     this.sourceId,

     this.recordSourceId,

     this.recordExternalId,

     this.recordRef,

     this.licence,

    required  this.createdAt,

     this.expiresAt,

     this.variantOf,

     this.variantWidth,

     this.links,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'sha256',
    required: true,
    includeIfNull: false,
  )


  final String sha256;



  @JsonKey(
    
    name: r'contentType',
    required: true,
    includeIfNull: false,
  )


  final MediaContentTypeEnum contentType;



      /// Pixels; 0 for audio
  @JsonKey(
    
    name: r'width',
    required: true,
    includeIfNull: false,
  )


  final int width;



      /// Pixels; 0 for audio
  @JsonKey(
    
    name: r'height',
    required: true,
    includeIfNull: false,
  )


  final int height;



  @JsonKey(
    
    name: r'bytes',
    required: true,
    includeIfNull: false,
  )


  final int bytes;



      /// `document` (141): a generated non-image document such as CZML, stored with an expiry; `audio` (141): synthesized speech of a briefing run, expiring with the briefing-run retention. Neither has thumbnails
  @JsonKey(
    
    name: r'role',
    required: true,
    includeIfNull: false,
  )


  final MediaRoleEnum role;



  @JsonKey(
    
    name: r'sourceId',
    required: false,
    includeIfNull: false,
  )


  final String? sourceId;



  @JsonKey(
    
    name: r'recordSourceId',
    required: false,
    includeIfNull: false,
  )


  final String? recordSourceId;



  @JsonKey(
    
    name: r'recordExternalId',
    required: false,
    includeIfNull: false,
  )


  final String? recordExternalId;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'recordRef',
    required: false,
    includeIfNull: false,
  )


  final String? recordRef;



  @JsonKey(
    
    name: r'licence',
    required: false,
    includeIfNull: false,
  )


  final String? licence;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



      /// Null: follows the owning record's retention
  @JsonKey(
    
    name: r'expiresAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? expiresAt;



      /// ULID identifier
  @JsonKey(
    
    name: r'variantOf',
    required: false,
    includeIfNull: false,
  )


  final String? variantOf;



  @JsonKey(
    
    name: r'variantWidth',
    required: false,
    includeIfNull: false,
  )


  final int? variantWidth;



  @JsonKey(
    
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final MediaLinks? links;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Media &&
      other.id == id &&
      other.sha256 == sha256 &&
      other.contentType == contentType &&
      other.width == width &&
      other.height == height &&
      other.bytes == bytes &&
      other.role == role &&
      other.sourceId == sourceId &&
      other.recordSourceId == recordSourceId &&
      other.recordExternalId == recordExternalId &&
      other.recordRef == recordRef &&
      other.licence == licence &&
      other.createdAt == createdAt &&
      other.expiresAt == expiresAt &&
      other.variantOf == variantOf &&
      other.variantWidth == variantWidth &&
      other.links == links;

    @override
    int get hashCode =>
        id.hashCode +
        sha256.hashCode +
        contentType.hashCode +
        width.hashCode +
        height.hashCode +
        bytes.hashCode +
        role.hashCode +
        (sourceId == null ? 0 : sourceId.hashCode) +
        (recordSourceId == null ? 0 : recordSourceId.hashCode) +
        (recordExternalId == null ? 0 : recordExternalId.hashCode) +
        (recordRef == null ? 0 : recordRef.hashCode) +
        (licence == null ? 0 : licence.hashCode) +
        createdAt.hashCode +
        (expiresAt == null ? 0 : expiresAt.hashCode) +
        (variantOf == null ? 0 : variantOf.hashCode) +
        (variantWidth == null ? 0 : variantWidth.hashCode) +
        links.hashCode;

  factory Media.fromJson(Map<String, dynamic> json) => _$MediaFromJson(json);

  Map<String, dynamic> toJson() => _$MediaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum MediaContentTypeEnum {
@JsonValue(r'image/png')
imageSlashPng(r'image/png'),
@JsonValue(r'image/jpeg')
imageSlashJpeg(r'image/jpeg'),
@JsonValue(r'image/tiff')
imageSlashTiff(r'image/tiff'),
@JsonValue(r'application/czml+json')
applicationSlashCzmlPlusJson(r'application/czml+json'),
@JsonValue(r'audio/mpeg')
audioSlashMpeg(r'audio/mpeg'),
@JsonValue(r'audio/ogg')
audioSlashOgg(r'audio/ogg');

const MediaContentTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


/// `document` (141): a generated non-image document such as CZML, stored with an expiry; `audio` (141): synthesized speech of a briefing run, expiring with the briefing-run retention. Neither has thumbnails
enum MediaRoleEnum {
@JsonValue(r'image')
image(r'image'),
@JsonValue(r'thumbnail')
thumbnail(r'thumbnail'),
@JsonValue(r'projected')
projected(r'projected'),
@JsonValue(r'map')
map(r'map'),
@JsonValue(r'document')
document(r'document'),
@JsonValue(r'audio')
audio(r'audio');

const MediaRoleEnum(this.value);

final String value;

@override
String toString() => value;
}


