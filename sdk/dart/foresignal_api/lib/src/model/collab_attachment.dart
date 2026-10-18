//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/collab_attachment_image_meta.dart';
import 'package:foresignal_api/src/model/principal_summary.dart';
import 'package:foresignal_api/src/model/collab_attachment_links.dart';
import 'package:foresignal_api/src/model/visibility.dart';
import 'package:foresignal_api/src/model/tlp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'collab_attachment.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CollabAttachment {
  /// Returns a new [CollabAttachment] instance.
  CollabAttachment({

    required  this.id,

    required  this.mediaId,

    required  this.filename,

    required  this.contentType,

    required  this.size,

    required  this.sha256,

     this.text,

     this.textTruncated,

     this.imageMeta,

    required  this.uploadedBy,

     this.owner,

     this.visibility,

    required  this.tlp,

    required  this.createdAt,

     this.links,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// 043 content-addressed media id
  @JsonKey(
    
    name: r'mediaId',
    required: true,
    includeIfNull: false,
  )


  final String mediaId;



  @JsonKey(
    
    name: r'filename',
    required: true,
    includeIfNull: false,
  )


  final String filename;



  @JsonKey(
    
    name: r'contentType',
    required: true,
    includeIfNull: false,
  )


  final String contentType;



  @JsonKey(
    
    name: r'size',
    required: true,
    includeIfNull: false,
  )


  final int size;



  @JsonKey(
    
    name: r'sha256',
    required: true,
    includeIfNull: false,
  )


  final String sha256;



      /// Extracted text (PDF via PDFBox, plain text, HTML stripped), ≤ 1 MB
  @JsonKey(
    
    name: r'text',
    required: false,
    includeIfNull: false,
  )


  final String? text;



  @JsonKey(
    
    name: r'textTruncated',
    required: false,
    includeIfNull: false,
  )


  final bool? textTruncated;



  @JsonKey(
    
    name: r'imageMeta',
    required: false,
    includeIfNull: false,
  )


  final CollabAttachmentImageMeta? imageMeta;



  @JsonKey(
    
    name: r'uploadedBy',
    required: true,
    includeIfNull: false,
  )


  final PrincipalSummary uploadedBy;



  @JsonKey(
    
    name: r'owner',
    required: false,
    includeIfNull: false,
  )


  final PrincipalSummary? owner;



  @JsonKey(
    
    name: r'visibility',
    required: false,
    includeIfNull: false,
  )


  final Visibility? visibility;



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



  @JsonKey(
    
    name: r'links',
    required: false,
    includeIfNull: false,
  )


  final CollabAttachmentLinks? links;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CollabAttachment &&
      other.id == id &&
      other.mediaId == mediaId &&
      other.filename == filename &&
      other.contentType == contentType &&
      other.size == size &&
      other.sha256 == sha256 &&
      other.text == text &&
      other.textTruncated == textTruncated &&
      other.imageMeta == imageMeta &&
      other.uploadedBy == uploadedBy &&
      other.owner == owner &&
      other.visibility == visibility &&
      other.tlp == tlp &&
      other.createdAt == createdAt &&
      other.links == links;

    @override
    int get hashCode =>
        id.hashCode +
        mediaId.hashCode +
        filename.hashCode +
        contentType.hashCode +
        size.hashCode +
        sha256.hashCode +
        (text == null ? 0 : text.hashCode) +
        textTruncated.hashCode +
        (imageMeta == null ? 0 : imageMeta.hashCode) +
        uploadedBy.hashCode +
        owner.hashCode +
        visibility.hashCode +
        tlp.hashCode +
        createdAt.hashCode +
        links.hashCode;

  factory CollabAttachment.fromJson(Map<String, dynamic> json) => _$CollabAttachmentFromJson(json);

  Map<String, dynamic> toJson() => _$CollabAttachmentToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

