//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'source_relevance.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SourceRelevance {
  /// Returns a new [SourceRelevance] instance.
  SourceRelevance({

    required  this.sourceId,

    required  this.name,

    required  this.kinds,

    required  this.verdict,

    required  this.reason,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// The kinds the source produces, sorted
  @JsonKey(
    
    name: r'kinds',
    required: true,
    includeIfNull: false,
  )


  final List<String> kinds;



      /// relevant: belongs with the others; marginal: related but adds little or mostly noise; not-meaningful: unrelated to the rest; unrated: not judged (withheld by the egress policy, left out by the model, or the set could not be rated), with the reason
  @JsonKey(
    
    name: r'verdict',
    required: true,
    includeIfNull: false,
  )


  final SourceRelevanceVerdictEnum verdict;



      /// One sentence: the model's reason, or why the source was not rated
  @JsonKey(
    
    name: r'reason',
    required: true,
    includeIfNull: false,
  )


  final String reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SourceRelevance &&
      other.sourceId == sourceId &&
      other.name == name &&
      other.kinds == kinds &&
      other.verdict == verdict &&
      other.reason == reason;

    @override
    int get hashCode =>
        sourceId.hashCode +
        name.hashCode +
        kinds.hashCode +
        verdict.hashCode +
        reason.hashCode;

  factory SourceRelevance.fromJson(Map<String, dynamic> json) => _$SourceRelevanceFromJson(json);

  Map<String, dynamic> toJson() => _$SourceRelevanceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// relevant: belongs with the others; marginal: related but adds little or mostly noise; not-meaningful: unrelated to the rest; unrated: not judged (withheld by the egress policy, left out by the model, or the set could not be rated), with the reason
enum SourceRelevanceVerdictEnum {
@JsonValue(r'relevant')
relevant(r'relevant'),
@JsonValue(r'marginal')
marginal(r'marginal'),
@JsonValue(r'not-meaningful')
notMeaningful(r'not-meaningful'),
@JsonValue(r'unrated')
unrated(r'unrated');

const SourceRelevanceVerdictEnum(this.value);

final String value;

@override
String toString() => value;
}


