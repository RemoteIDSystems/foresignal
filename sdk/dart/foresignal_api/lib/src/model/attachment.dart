//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'attachment.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Attachment {
  /// Returns a new [Attachment] instance.
  Attachment({

    required  this.filename,

    required  this.contentType,

    required  this.bytes,

     this.mediaId,

     this.url,
  });

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



      /// Size in bytes (the API never carries the content)
  @JsonKey(
    
    name: r'bytes',
    required: true,
    includeIfNull: false,
  )


  final int bytes;



      /// ULID identifier
  @JsonKey(
    
    name: r'mediaId',
    required: false,
    includeIfNull: false,
  )


  final String? mediaId;



      /// `{publicBaseUrl}/v1/media/{id}`
  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Attachment &&
      other.filename == filename &&
      other.contentType == contentType &&
      other.bytes == bytes &&
      other.mediaId == mediaId &&
      other.url == url;

    @override
    int get hashCode =>
        filename.hashCode +
        contentType.hashCode +
        bytes.hashCode +
        (mediaId == null ? 0 : mediaId.hashCode) +
        (url == null ? 0 : url.hashCode);

  factory Attachment.fromJson(Map<String, dynamic> json) => _$AttachmentFromJson(json);

  Map<String, dynamic> toJson() => _$AttachmentToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

