//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/tuning_suggestion_record.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tuning_suggestion_list.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TuningSuggestionList {
  /// Returns a new [TuningSuggestionList] instance.
  TuningSuggestionList({

    required  this.ruleId,

    required  this.since,

    required  this.suggesterVersion,

    required  this.suggestions,

     this.simulationJobId,

     this.jobId,
  });

  @JsonKey(
    
    name: r'ruleId',
    required: true,
    includeIfNull: false,
  )


  final String ruleId;



  @JsonKey(
    
    name: r'since',
    required: true,
    includeIfNull: false,
  )


  final DateTime since;



  @JsonKey(
    
    name: r'suggesterVersion',
    required: true,
    includeIfNull: false,
  )


  final String suggesterVersion;



  @JsonKey(
    
    name: r'suggestions',
    required: true,
    includeIfNull: false,
  )


  final List<TuningSuggestionRecord> suggestions;



      /// Backtest job id when simulate=true
  @JsonKey(
    
    name: r'simulationJobId',
    required: false,
    includeIfNull: false,
  )


  final String? simulationJobId;



      /// Alias of simulationJobId (103)
  @JsonKey(
    
    name: r'jobId',
    required: false,
    includeIfNull: false,
  )


  final String? jobId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TuningSuggestionList &&
      other.ruleId == ruleId &&
      other.since == since &&
      other.suggesterVersion == suggesterVersion &&
      other.suggestions == suggestions &&
      other.simulationJobId == simulationJobId &&
      other.jobId == jobId;

    @override
    int get hashCode =>
        ruleId.hashCode +
        since.hashCode +
        suggesterVersion.hashCode +
        suggestions.hashCode +
        (simulationJobId == null ? 0 : simulationJobId.hashCode) +
        (jobId == null ? 0 : jobId.hashCode);

  factory TuningSuggestionList.fromJson(Map<String, dynamic> json) => _$TuningSuggestionListFromJson(json);

  Map<String, dynamic> toJson() => _$TuningSuggestionListToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

