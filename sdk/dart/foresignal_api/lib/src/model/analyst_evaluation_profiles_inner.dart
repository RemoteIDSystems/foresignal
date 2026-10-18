//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/analyst_evaluation_profiles_inner_tokens_per_day_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'analyst_evaluation_profiles_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AnalystEvaluationProfilesInner {
  /// Returns a new [AnalystEvaluationProfilesInner] instance.
  AnalystEvaluationProfilesInner({

    required  this.profileId,

    required  this.runsByStatus,

     this.noopShare,

     this.tokensPerDay,

     this.proposalAcceptanceRate,

     this.notesDismissedShare,

     this.usefulRatio,

     this.wrongRatio,

     this.citationsDroppedPerRun,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'profileId',
    required: true,
    includeIfNull: false,
  )


  final String profileId;



  @JsonKey(
    
    name: r'runsByStatus',
    required: true,
    includeIfNull: false,
  )


  final Map<String, int> runsByStatus;



  @JsonKey(
    
    name: r'noopShare',
    required: false,
    includeIfNull: false,
  )


  final num? noopShare;



  @JsonKey(
    
    name: r'tokensPerDay',
    required: false,
    includeIfNull: false,
  )


  final List<AnalystEvaluationProfilesInnerTokensPerDayInner>? tokensPerDay;



      /// confirmed ÷ decided
  @JsonKey(
    
    name: r'proposalAcceptanceRate',
    required: false,
    includeIfNull: false,
  )


  final num? proposalAcceptanceRate;



  @JsonKey(
    
    name: r'notesDismissedShare',
    required: false,
    includeIfNull: false,
  )


  final num? notesDismissedShare;



  @JsonKey(
    
    name: r'usefulRatio',
    required: false,
    includeIfNull: false,
  )


  final num? usefulRatio;



  @JsonKey(
    
    name: r'wrongRatio',
    required: false,
    includeIfNull: false,
  )


  final num? wrongRatio;



  @JsonKey(
    
    name: r'citationsDroppedPerRun',
    required: false,
    includeIfNull: false,
  )


  final num? citationsDroppedPerRun;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AnalystEvaluationProfilesInner &&
      other.profileId == profileId &&
      other.runsByStatus == runsByStatus &&
      other.noopShare == noopShare &&
      other.tokensPerDay == tokensPerDay &&
      other.proposalAcceptanceRate == proposalAcceptanceRate &&
      other.notesDismissedShare == notesDismissedShare &&
      other.usefulRatio == usefulRatio &&
      other.wrongRatio == wrongRatio &&
      other.citationsDroppedPerRun == citationsDroppedPerRun;

    @override
    int get hashCode =>
        profileId.hashCode +
        runsByStatus.hashCode +
        noopShare.hashCode +
        tokensPerDay.hashCode +
        (proposalAcceptanceRate == null ? 0 : proposalAcceptanceRate.hashCode) +
        (notesDismissedShare == null ? 0 : notesDismissedShare.hashCode) +
        (usefulRatio == null ? 0 : usefulRatio.hashCode) +
        (wrongRatio == null ? 0 : wrongRatio.hashCode) +
        (citationsDroppedPerRun == null ? 0 : citationsDroppedPerRun.hashCode);

  factory AnalystEvaluationProfilesInner.fromJson(Map<String, dynamic> json) => _$AnalystEvaluationProfilesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$AnalystEvaluationProfilesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

