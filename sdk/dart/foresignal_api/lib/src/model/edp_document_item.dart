//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_geometry.dart';
import 'package:foresignal_api/src/model/edp_origin.dart';
import 'package:foresignal_api/src/model/edp_document_item_media_inner.dart';
import 'package:foresignal_api/src/model/edp_document_item_author.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_document_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpDocumentItem {
  /// Returns a new [EdpDocumentItem] instance.
  EdpDocumentItem({

    required  this.stream,

    required  this.type,

    required  this.id,

     this.observedAt,

     this.properties,

    required  this.documentType,

    required  this.text,

     this.title,

     this.url,

     this.author,

     this.language,

     this.publishedAt,

     this.about,

     this.media,

     this.geometry,

     this.engagement,

     this.origin,
  });

  @JsonKey(
    
    name: r'stream',
    required: true,
    includeIfNull: false,
  )


  final String stream;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final EdpDocumentItemTypeEnum type;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'observedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? observedAt;



      /// Item properties, validated against the stream's payloadSchema
  @JsonKey(
    
    name: r'properties',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? properties;



  @JsonKey(
    
    name: r'documentType',
    required: true,
    includeIfNull: false,
  )


  final EdpDocumentItemDocumentTypeEnum documentType;



  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'url',
    required: false,
    includeIfNull: false,
  )


  final String? url;



  @JsonKey(
    
    name: r'author',
    required: false,
    includeIfNull: false,
  )


  final EdpDocumentItemAuthor? author;



  @JsonKey(
    
    name: r'language',
    required: false,
    includeIfNull: false,
  )


  final String? language;



  @JsonKey(
    
    name: r'publishedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? publishedAt;



      /// Foresignal object references or URLs
  @JsonKey(
    
    name: r'about',
    required: false,
    includeIfNull: false,
  )


  final List<String>? about;



  @JsonKey(
    
    name: r'media',
    required: false,
    includeIfNull: false,
  )


  final List<EdpDocumentItemMediaInner>? media;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final EdpGeometry? geometry;



  @JsonKey(
    
    name: r'engagement',
    required: false,
    includeIfNull: false,
  )


  final Map<String, num?>? engagement;



  @JsonKey(
    
    name: r'origin',
    required: false,
    includeIfNull: false,
  )


  final EdpOrigin? origin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpDocumentItem &&
      other.stream == stream &&
      other.type == type &&
      other.id == id &&
      other.observedAt == observedAt &&
      other.properties == properties &&
      other.documentType == documentType &&
      other.text == text &&
      other.title == title &&
      other.url == url &&
      other.author == author &&
      other.language == language &&
      other.publishedAt == publishedAt &&
      other.about == about &&
      other.media == media &&
      other.geometry == geometry &&
      other.engagement == engagement &&
      other.origin == origin;

    @override
    int get hashCode =>
        stream.hashCode +
        type.hashCode +
        id.hashCode +
        observedAt.hashCode +
        properties.hashCode +
        documentType.hashCode +
        text.hashCode +
        (title == null ? 0 : title.hashCode) +
        (url == null ? 0 : url.hashCode) +
        (author == null ? 0 : author.hashCode) +
        (language == null ? 0 : language.hashCode) +
        (publishedAt == null ? 0 : publishedAt.hashCode) +
        about.hashCode +
        media.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        (engagement == null ? 0 : engagement.hashCode) +
        (origin == null ? 0 : origin.hashCode);

  factory EdpDocumentItem.fromJson(Map<String, dynamic> json) => _$EdpDocumentItemFromJson(json);

  Map<String, dynamic> toJson() => _$EdpDocumentItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpDocumentItemTypeEnum {
@JsonValue(r'document')
document(r'document');

const EdpDocumentItemTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EdpDocumentItemDocumentTypeEnum {
@JsonValue(r'social-post')
socialPost(r'social-post'),
@JsonValue(r'article')
article(r'article'),
@JsonValue(r'report')
report(r'report'),
@JsonValue(r'transcript')
transcript(r'transcript'),
@JsonValue(r'note')
note(r'note');

const EdpDocumentItemDocumentTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


