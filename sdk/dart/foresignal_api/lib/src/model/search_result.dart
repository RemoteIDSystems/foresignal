//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:foresignal_api/src/model/record.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'search_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SearchResult {
  /// Returns a new [SearchResult] instance.
  SearchResult({

    required  this.docType,

    required  this.docId,

    required  this.score,

    required  this.title,

     this.snippet,

     this.observedAt,

     this.source_,

     this.kind,

     this.geometry,

     this.record,

     this.seq,

     this.effectiveTime,
  });

  @JsonKey(
    
    name: r'docType',
    required: true,
    includeIfNull: false,
  )


  final SearchResultDocTypeEnum docType;



  @JsonKey(
    
    name: r'docId',
    required: true,
    includeIfNull: false,
  )


  final String docId;



  @JsonKey(
    
    name: r'score',
    required: true,
    includeIfNull: false,
  )


  final num score;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'snippet',
    required: false,
    includeIfNull: false,
  )


  final String? snippet;



  @JsonKey(
    
    name: r'observedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? observedAt;



  @JsonKey(
    
    name: r'source',
    required: false,
    includeIfNull: false,
  )


  final String? source_;



  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



  @JsonKey(
    
    name: r'geometry',
    required: false,
    includeIfNull: false,
  )


  final Geometry? geometry;



  @JsonKey(
    
    name: r'record',
    required: false,
    includeIfNull: false,
  )


  final Record? record;



      /// Persisted version seq the document text came from (cite with it)
  @JsonKey(
    
    name: r'seq',
    required: false,
    includeIfNull: false,
  )


  final int? seq;



      /// COALESCE(eventTime, observedAt) of that version
  @JsonKey(
    
    name: r'effectiveTime',
    required: false,
    includeIfNull: false,
  )


  final DateTime? effectiveTime;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SearchResult &&
      other.docType == docType &&
      other.docId == docId &&
      other.score == score &&
      other.title == title &&
      other.snippet == snippet &&
      other.observedAt == observedAt &&
      other.source_ == source_ &&
      other.kind == kind &&
      other.geometry == geometry &&
      other.record == record &&
      other.seq == seq &&
      other.effectiveTime == effectiveTime;

    @override
    int get hashCode =>
        docType.hashCode +
        docId.hashCode +
        score.hashCode +
        title.hashCode +
        snippet.hashCode +
        (observedAt == null ? 0 : observedAt.hashCode) +
        (source_ == null ? 0 : source_.hashCode) +
        (kind == null ? 0 : kind.hashCode) +
        (geometry == null ? 0 : geometry.hashCode) +
        (record == null ? 0 : record.hashCode) +
        (seq == null ? 0 : seq.hashCode) +
        (effectiveTime == null ? 0 : effectiveTime.hashCode);

  factory SearchResult.fromJson(Map<String, dynamic> json) => _$SearchResultFromJson(json);

  Map<String, dynamic> toJson() => _$SearchResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SearchResultDocTypeEnum {
@JsonValue(r'record')
record(r'record'),
@JsonValue(r'briefing')
briefing(r'briefing'),
@JsonValue(r'alert')
alert(r'alert');

const SearchResultDocTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


