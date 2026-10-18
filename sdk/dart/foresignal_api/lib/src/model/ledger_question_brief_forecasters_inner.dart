//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ledger_question_brief_forecasters_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class LedgerQuestionBriefForecastersInner {
  /// Returns a new [LedgerQuestionBriefForecastersInner] instance.
  LedgerQuestionBriefForecastersInner({

    required  this.forecasterId,

     this.probability,

     this.madeAt,

     this.knowledgeCutoff,
  });

  @JsonKey(
    
    name: r'forecasterId',
    required: true,
    includeIfNull: false,
  )


  final String forecasterId;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: false,
    includeIfNull: false,
  )


  final num? probability;



  @JsonKey(
    
    name: r'madeAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? madeAt;



  @JsonKey(
    
    name: r'knowledgeCutoff',
    required: false,
    includeIfNull: false,
  )


  final DateTime? knowledgeCutoff;





    @override
    bool operator ==(Object other) => identical(this, other) || other is LedgerQuestionBriefForecastersInner &&
      other.forecasterId == forecasterId &&
      other.probability == probability &&
      other.madeAt == madeAt &&
      other.knowledgeCutoff == knowledgeCutoff;

    @override
    int get hashCode =>
        forecasterId.hashCode +
        (probability == null ? 0 : probability.hashCode) +
        madeAt.hashCode +
        (knowledgeCutoff == null ? 0 : knowledgeCutoff.hashCode);

  factory LedgerQuestionBriefForecastersInner.fromJson(Map<String, dynamic> json) => _$LedgerQuestionBriefForecastersInnerFromJson(json);

  Map<String, dynamic> toJson() => _$LedgerQuestionBriefForecastersInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

