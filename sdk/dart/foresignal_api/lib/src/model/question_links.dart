//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'question_links.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class QuestionLinks {
  /// Returns a new [QuestionLinks] instance.
  QuestionLinks({

     this.situationId,

     this.hypothesisSetId,

     this.precursorId,

     this.findingId,

     this.seriesRef,

     this.outlookId,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;



      /// ULID identifier
  @JsonKey(
    
    name: r'hypothesisSetId',
    required: false,
    includeIfNull: false,
  )


  final String? hypothesisSetId;



      /// ULID identifier
  @JsonKey(
    
    name: r'precursorId',
    required: false,
    includeIfNull: false,
  )


  final String? precursorId;



      /// ULID identifier
  @JsonKey(
    
    name: r'findingId',
    required: false,
    includeIfNull: false,
  )


  final String? findingId;



      /// Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left, center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement, every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the platform-language denominator; complete and paged collection only)
  @JsonKey(
    
    name: r'seriesRef',
    required: false,
    includeIfNull: false,
  )


  final String? seriesRef;



      /// ULID identifier
  @JsonKey(
    
    name: r'outlookId',
    required: false,
    includeIfNull: false,
  )


  final String? outlookId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is QuestionLinks &&
      other.situationId == situationId &&
      other.hypothesisSetId == hypothesisSetId &&
      other.precursorId == precursorId &&
      other.findingId == findingId &&
      other.seriesRef == seriesRef &&
      other.outlookId == outlookId;

    @override
    int get hashCode =>
        (situationId == null ? 0 : situationId.hashCode) +
        (hypothesisSetId == null ? 0 : hypothesisSetId.hashCode) +
        (precursorId == null ? 0 : precursorId.hashCode) +
        (findingId == null ? 0 : findingId.hashCode) +
        (seriesRef == null ? 0 : seriesRef.hashCode) +
        (outlookId == null ? 0 : outlookId.hashCode);

  factory QuestionLinks.fromJson(Map<String, dynamic> json) => _$QuestionLinksFromJson(json);

  Map<String, dynamic> toJson() => _$QuestionLinksToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

