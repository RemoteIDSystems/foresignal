//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_document_item_media_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpDocumentItemMediaInner {
  /// Returns a new [EdpDocumentItemMediaInner] instance.
  EdpDocumentItemMediaInner({

     this.type,

    required  this.url,

     this.thumbUrl,
  });

  @JsonKey(
    
    name: r'type',
    required: false,
    includeIfNull: false,
  )


  final String? type;



  @JsonKey(
    
    name: r'url',
    required: true,
    includeIfNull: false,
  )


  final String url;



  @JsonKey(
    
    name: r'thumbUrl',
    required: false,
    includeIfNull: false,
  )


  final String? thumbUrl;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpDocumentItemMediaInner &&
      other.type == type &&
      other.url == url &&
      other.thumbUrl == thumbUrl;

    @override
    int get hashCode =>
        type.hashCode +
        url.hashCode +
        (thumbUrl == null ? 0 : thumbUrl.hashCode);

  factory EdpDocumentItemMediaInner.fromJson(Map<String, dynamic> json) => _$EdpDocumentItemMediaInnerFromJson(json);

  Map<String, dynamic> toJson() => _$EdpDocumentItemMediaInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

