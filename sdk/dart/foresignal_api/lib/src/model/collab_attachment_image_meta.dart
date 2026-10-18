//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'collab_attachment_image_meta.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CollabAttachmentImageMeta {
  /// Returns a new [CollabAttachmentImageMeta] instance.
  CollabAttachmentImageMeta({

    required  this.width,

    required  this.height,

    required  this.exifStripped,
  });

  @JsonKey(
    
    name: r'width',
    required: true,
    includeIfNull: false,
  )


  final int width;



  @JsonKey(
    
    name: r'height',
    required: true,
    includeIfNull: false,
  )


  final int height;



  @JsonKey(
    
    name: r'exifStripped',
    required: true,
    includeIfNull: false,
  )


  final bool exifStripped;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CollabAttachmentImageMeta &&
      other.width == width &&
      other.height == height &&
      other.exifStripped == exifStripped;

    @override
    int get hashCode =>
        width.hashCode +
        height.hashCode +
        exifStripped.hashCode;

  factory CollabAttachmentImageMeta.fromJson(Map<String, dynamic> json) => _$CollabAttachmentImageMetaFromJson(json);

  Map<String, dynamic> toJson() => _$CollabAttachmentImageMetaToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

