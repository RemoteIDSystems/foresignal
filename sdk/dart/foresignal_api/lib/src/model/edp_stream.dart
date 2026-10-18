//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_coverage.dart';
import 'package:foresignal_api/src/model/edp_presentation.dart';
import 'package:foresignal_api/src/model/edp_suggested_binding.dart';
import 'package:foresignal_api/src/model/edp_stream_fields.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_stream.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpStream {
  /// Returns a new [EdpStream] instance.
  EdpStream({

    required  this.id,

    required  this.mode,

    required  this.itemType,

    required  this.kind,

     this.title,

     this.description,

     this.geometry,

     this.update,

     this.payloadSchema,

     this.fields,

     this.ttl,

     this.retentionClass,

     this.unit,

     this.documentType,

     this.presentation,

     this.coverage,

     this.complete = false,

     this.suggestedBindings,

     this.snapshotScope,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'mode',
    required: true,
    includeIfNull: false,
  )


  final EdpStreamModeEnum mode;



  @JsonKey(
    
    name: r'itemType',
    required: true,
    includeIfNull: false,
  )


  final EdpStreamItemTypeEnum itemType;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final String kind;



  @JsonKey(
    
    name: r'title',
    required: false,
    includeIfNull: false,
  )


  final String? title;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final EdpStreamGeometryEnum? geometry;



  @JsonKey(
    
    name: r'update',
    required: false,
    includeIfNull: false,
  )


  final EdpStreamUpdateEnum? update;



      /// JSON Schema 2020-12 for item properties
  @JsonKey(
    
    name: r'payloadSchema',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? payloadSchema;



  @JsonKey(
    
    name: r'fields',
    required: false,
    includeIfNull: false,
  )


  final EdpStreamFields? fields;



  @JsonKey(
    
    name: r'ttl',
    required: false,
    includeIfNull: false,
  )


  final String? ttl;



  @JsonKey(
    
    name: r'retentionClass',
    required: false,
    includeIfNull: false,
  )


  final EdpStreamRetentionClassEnum? retentionClass;



  @JsonKey(
    
    name: r'unit',
    required: false,
    includeIfNull: false,
  )


  final String? unit;



  @JsonKey(
    
    name: r'documentType',
    required: false,
    includeIfNull: false,
  )


  final EdpStreamDocumentTypeEnum? documentType;



  @JsonKey(
    
    name: r'presentation',
    required: false,
    includeIfNull: false,
  )


  final EdpPresentation? presentation;



  @JsonKey(
    
    name: r'coverage',
    required: false,
    includeIfNull: false,
  )


  final EdpCoverage? coverage;



      /// The stream is a complete record of its scope (social volume baselines may use it)
  @JsonKey(
    defaultValue: false,
    name: r'complete',
    required: false,
    includeIfNull: false,
  )


  final bool? complete;



  @JsonKey(
    
    name: r'suggestedBindings',
    required: false,
    includeIfNull: false,
  )


  final List<EdpSuggestedBinding>? suggestedBindings;



      /// For update: snapshot. Null = the whole stream is one scope; otherwise the item property whose value names the scope (e.g. a region code), so a snapshot only removes items of its own scope.
  @JsonKey(
    
    name: r'snapshotScope',
    required: false,
    includeIfNull: false,
  )


  final String? snapshotScope;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpStream &&
      other.id == id &&
      other.mode == mode &&
      other.itemType == itemType &&
      other.kind == kind &&
      other.title == title &&
      other.description == description &&
      other.geometry == geometry &&
      other.update == update &&
      other.payloadSchema == payloadSchema &&
      other.fields == fields &&
      other.ttl == ttl &&
      other.retentionClass == retentionClass &&
      other.unit == unit &&
      other.documentType == documentType &&
      other.presentation == presentation &&
      other.coverage == coverage &&
      other.complete == complete &&
      other.suggestedBindings == suggestedBindings &&
      other.snapshotScope == snapshotScope;

    @override
    int get hashCode =>
        id.hashCode +
        mode.hashCode +
        itemType.hashCode +
        kind.hashCode +
        (title == null ? 0 : title.hashCode) +
        (description == null ? 0 : description.hashCode) +
        geometry.hashCode +
        update.hashCode +
        payloadSchema.hashCode +
        fields.hashCode +
        (ttl == null ? 0 : ttl.hashCode) +
        retentionClass.hashCode +
        (unit == null ? 0 : unit.hashCode) +
        (documentType == null ? 0 : documentType.hashCode) +
        presentation.hashCode +
        coverage.hashCode +
        complete.hashCode +
        suggestedBindings.hashCode +
        (snapshotScope == null ? 0 : snapshotScope.hashCode);

  factory EdpStream.fromJson(Map<String, dynamic> json) => _$EdpStreamFromJson(json);

  Map<String, dynamic> toJson() => _$EdpStreamToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpStreamModeEnum {
@JsonValue(r'layer')
layer(r'layer'),
@JsonValue(r'atomic')
atomic(r'atomic');

const EdpStreamModeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EdpStreamItemTypeEnum {
@JsonValue(r'feature')
feature(r'feature'),
@JsonValue(r'event')
event(r'event'),
@JsonValue(r'observation')
observation(r'observation'),
@JsonValue(r'document')
document(r'document'),
@JsonValue(r'forecast')
forecast(r'forecast');

const EdpStreamItemTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EdpStreamGeometryEnum {
@JsonValue(r'Point')
point(r'Point'),
@JsonValue(r'LineString')
lineString(r'LineString'),
@JsonValue(r'Polygon')
polygon(r'Polygon'),
@JsonValue(r'MultiPolygon')
multiPolygon(r'MultiPolygon'),
@JsonValue(r'any')
any(r'any'),
@JsonValue(r'none')
none(r'none');

const EdpStreamGeometryEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EdpStreamUpdateEnum {
@JsonValue(r'incremental')
incremental(r'incremental'),
@JsonValue(r'snapshot')
snapshot(r'snapshot');

const EdpStreamUpdateEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EdpStreamRetentionClassEnum {
@JsonValue(r'event')
event(r'event'),
@JsonValue(r'bulk-event')
bulkEvent(r'bulk-event'),
@JsonValue(r'series')
series(r'series'),
@JsonValue(r'state')
state(r'state'),
@JsonValue(r'track')
track(r'track'),
@JsonValue(r'reference')
reference(r'reference'),
@JsonValue(r'social')
social(r'social');

const EdpStreamRetentionClassEnum(this.value);

final String value;

@override
String toString() => value;
}


enum EdpStreamDocumentTypeEnum {
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

const EdpStreamDocumentTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


