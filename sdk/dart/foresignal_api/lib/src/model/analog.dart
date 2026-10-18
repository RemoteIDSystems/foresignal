//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/analog_outcomes.dart';
import 'package:foresignal_api/src/model/time_range.dart';
import 'package:foresignal_api/src/model/situation_theme.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analog.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Analog {
  /// Returns a new [Analog] instance.
  Analog({

     this.situationId,

    required  this.title,

     this.theme,

    required  this.regionCodes,

    required  this.period,

    required  this.rank,

    required  this.similarity,

     this.featureSimilarity,

     this.embeddingSimilarity,

    required  this.outcomes,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;



  @JsonKey(
    
    name: r'theme',
    required: false,
    includeIfNull: false,
  )


  final SituationTheme? theme;



  @JsonKey(
    
    name: r'regionCodes',
    required: true,
    includeIfNull: false,
  )


  final List<String> regionCodes;



  @JsonKey(
    
    name: r'period',
    required: true,
    includeIfNull: false,
  )


  final TimeRange period;



          // minimum: 1
  @JsonKey(
    
    name: r'rank',
    required: true,
    includeIfNull: false,
  )


  final int rank;



      /// Reciprocal-rank-fused score
  @JsonKey(
    
    name: r'similarity',
    required: true,
    includeIfNull: false,
  )


  final num similarity;



      /// Feature cosine
  @JsonKey(
    
    name: r'featureSimilarity',
    required: false,
    includeIfNull: false,
  )


  final num? featureSimilarity;



      /// Summary embedding cosine (null without embeddings)
  @JsonKey(
    
    name: r'embeddingSimilarity',
    required: false,
    includeIfNull: false,
  )


  final num? embeddingSimilarity;



  @JsonKey(
    
    name: r'outcomes',
    required: true,
    includeIfNull: false,
  )


  final AnalogOutcomes outcomes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Analog &&
      other.situationId == situationId &&
      other.title == title &&
      other.theme == theme &&
      other.regionCodes == regionCodes &&
      other.period == period &&
      other.rank == rank &&
      other.similarity == similarity &&
      other.featureSimilarity == featureSimilarity &&
      other.embeddingSimilarity == embeddingSimilarity &&
      other.outcomes == outcomes;

    @override
    int get hashCode =>
        (situationId == null ? 0 : situationId.hashCode) +
        title.hashCode +
        (theme == null ? 0 : theme.hashCode) +
        regionCodes.hashCode +
        period.hashCode +
        rank.hashCode +
        similarity.hashCode +
        (featureSimilarity == null ? 0 : featureSimilarity.hashCode) +
        (embeddingSimilarity == null ? 0 : embeddingSimilarity.hashCode) +
        outcomes.hashCode;

  factory Analog.fromJson(Map<String, dynamic> json) => _$AnalogFromJson(json);

  Map<String, dynamic> toJson() => _$AnalogToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

