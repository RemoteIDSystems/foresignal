//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'collab_attachment_links.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CollabAttachmentLinks {
  /// Returns a new [CollabAttachmentLinks] instance.
  CollabAttachmentLinks({

     this.content,
  });

      /// `/v1/attachments/{id}/content`
  @JsonKey(
    
    name: r'content',
    required: false,
    includeIfNull: false,
  )


  final String? content;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CollabAttachmentLinks &&
      other.content == content;

    @override
    int get hashCode =>
        content.hashCode;

  factory CollabAttachmentLinks.fromJson(Map<String, dynamic> json) => _$CollabAttachmentLinksFromJson(json);

  Map<String, dynamic> toJson() => _$CollabAttachmentLinksToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

