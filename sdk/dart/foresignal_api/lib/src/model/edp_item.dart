//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_delete_item.dart';
import 'package:foresignal_api/src/model/edp_forecast_item.dart';
import 'package:foresignal_api/src/model/edp_geometry.dart';
import 'package:foresignal_api/src/model/edp_document_item.dart';
import 'package:foresignal_api/src/model/edp_forecast_item_all_of_question.dart';
import 'package:foresignal_api/src/model/edp_document_item_media_inner.dart';
import 'package:foresignal_api/src/model/edp_event_item.dart';
import 'package:foresignal_api/src/model/edp_status_item.dart';
import 'package:foresignal_api/src/model/edp_feature_item.dart';
import 'package:foresignal_api/src/model/edp_observation_item.dart';
import 'package:foresignal_api/src/model/quantiles.dart';
import 'package:foresignal_api/src/model/edp_origin.dart';
import 'package:foresignal_api/src/model/edp_document_item_author.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpItem {
  /// Returns a new [EdpItem] instance.
  EdpItem({

    required  this.stream,

    required  this.type,

    required  this.id,

     this.observedAt,

     this.properties,

     this.geometry,

    required  this.eventTime,

     this.validUntil,

     this.origin,

     this.upstreamEventId,

    required  this.seriesKey,

    required  this.t,

    required  this.value,

     this.knownAt,

     this.unit,

    required  this.documentType,

    required  this.text,

     this.title,

     this.url,

     this.author,

     this.language,

     this.publishedAt,

     this.about,

     this.media,

     this.engagement,

     this.question,

     this.questionRef,

     this.probability,

     this.quantiles,

    required  this.madeAt,

    required  this.health,

     this.message,
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


  final EdpItemTypeEnum type;



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
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final EdpGeometry? geometry;



  @JsonKey(
    
    name: r'eventTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime eventTime;



  @JsonKey(
    
    name: r'validUntil',
    required: false,
    includeIfNull: false,
  )


  final DateTime? validUntil;



  @JsonKey(
    
    name: r'origin',
    required: false,
    includeIfNull: false,
  )


  final EdpOrigin? origin;



  @JsonKey(
    
    name: r'upstreamEventId',
    required: false,
    includeIfNull: false,
  )


  final String? upstreamEventId;



  @JsonKey(
    
    name: r'seriesKey',
    required: true,
    includeIfNull: false,
  )


  final String seriesKey;



      /// Period or instant: YYYY, YYYY-Qn, YYYY-MM, YYYY-Www (ISO week), YYYY-MM-DD, or an RFC 3339 timestamp with offset. Periods are UTC intervals [start, end); one series uses one granularity.
  @JsonKey(
    
    name: r't',
    required: true,
    includeIfNull: false,
  )


  final String t;



  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: true,
  )


  final num? value;



      /// Provider-claimed availability time; stored as given. Foresignal uses max(knownAt, its own receipt time) for as-of and look-ahead rules.
  @JsonKey(
    
    name: r'knownAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? knownAt;



  @JsonKey(
    
    name: r'unit',
    required: false,
    includeIfNull: false,
  )


  final String? unit;



  @JsonKey(
    
    name: r'documentType',
    required: true,
    includeIfNull: false,
  )


  final EdpItemDocumentTypeEnum documentType;



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
    
    name: r'engagement',
    required: false,
    includeIfNull: false,
  )


  final Map<String, num?>? engagement;



  @JsonKey(
    
    name: r'question',
    required: false,
    includeIfNull: false,
  )


  final EdpForecastItemAllOfQuestion? question;



  @JsonKey(
    
    name: r'questionRef',
    required: false,
    includeIfNull: false,
  )


  final String? questionRef;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: false,
    includeIfNull: false,
  )


  final num? probability;



  @JsonKey(
    
    name: r'quantiles',
    required: false,
    includeIfNull: false,
  )


  final Quantiles? quantiles;



      /// Provider-claimed time; the recorded made_at is max(madeAt, receipt time)
  @JsonKey(
    
    name: r'madeAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime madeAt;



  @JsonKey(
    
    name: r'health',
    required: true,
    includeIfNull: false,
  )


  final EdpItemHealthEnum health;



  @JsonKey(
    
    name: r'message',
    required: false,
    includeIfNull: false,
  )


  final String? message;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpItem &&
      other.stream == stream &&
      other.type == type &&
      other.id == id &&
      other.observedAt == observedAt &&
      other.properties == properties &&
      other.geometry == geometry &&
      other.eventTime == eventTime &&
      other.validUntil == validUntil &&
      other.origin == origin &&
      other.upstreamEventId == upstreamEventId &&
      other.seriesKey == seriesKey &&
      other.t == t &&
      other.value == value &&
      other.knownAt == knownAt &&
      other.unit == unit &&
      other.documentType == documentType &&
      other.text == text &&
      other.title == title &&
      other.url == url &&
      other.author == author &&
      other.language == language &&
      other.publishedAt == publishedAt &&
      other.about == about &&
      other.media == media &&
      other.engagement == engagement &&
      other.question == question &&
      other.questionRef == questionRef &&
      other.probability == probability &&
      other.quantiles == quantiles &&
      other.madeAt == madeAt &&
      other.health == health &&
      other.message == message;

    @override
    int get hashCode =>
        stream.hashCode +
        type.hashCode +
        id.hashCode +
        observedAt.hashCode +
        properties.hashCode +
        (geometry == null ? 0 : geometry.hashCode) +
        eventTime.hashCode +
        (validUntil == null ? 0 : validUntil.hashCode) +
        (origin == null ? 0 : origin.hashCode) +
        (upstreamEventId == null ? 0 : upstreamEventId.hashCode) +
        seriesKey.hashCode +
        t.hashCode +
        (value == null ? 0 : value.hashCode) +
        (knownAt == null ? 0 : knownAt.hashCode) +
        (unit == null ? 0 : unit.hashCode) +
        documentType.hashCode +
        text.hashCode +
        (title == null ? 0 : title.hashCode) +
        (url == null ? 0 : url.hashCode) +
        (author == null ? 0 : author.hashCode) +
        (language == null ? 0 : language.hashCode) +
        (publishedAt == null ? 0 : publishedAt.hashCode) +
        about.hashCode +
        media.hashCode +
        (engagement == null ? 0 : engagement.hashCode) +
        (question == null ? 0 : question.hashCode) +
        (questionRef == null ? 0 : questionRef.hashCode) +
        (probability == null ? 0 : probability.hashCode) +
        (quantiles == null ? 0 : quantiles.hashCode) +
        madeAt.hashCode +
        health.hashCode +
        (message == null ? 0 : message.hashCode);

  factory EdpItem.fromJson(Map<String, dynamic> json) => _$EdpItemFromJson(json);

  Map<String, dynamic> toJson() => _$EdpItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpItemTypeEnum {
@JsonValue(r'feature')
feature(r'feature'),
@JsonValue(r'event')
event(r'event'),
@JsonValue(r'observation')
observation(r'observation'),
@JsonValue(r'document')
document(r'document'),
@JsonValue(r'forecast')
forecast(r'forecast'),
@JsonValue(r'delete')
delete(r'delete'),
@JsonValue(r'status')
status(r'status');

const EdpItemTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EdpItemDocumentTypeEnum {
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

const EdpItemDocumentTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EdpItemHealthEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'degraded')
degraded(r'degraded'),
@JsonValue(r'blocked')
blocked(r'blocked'),
@JsonValue(r'layout-changed')
layoutChanged(r'layout-changed'),
@JsonValue(r'geo-restricted')
geoRestricted(r'geo-restricted');

const EdpItemHealthEnum(this.value);

final String value;

@override
String toString() => value;
}


