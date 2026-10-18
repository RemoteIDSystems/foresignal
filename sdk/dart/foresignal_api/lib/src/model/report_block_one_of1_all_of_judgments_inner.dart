//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/icd_term.dart';
import 'package:foresignal_api/src/model/confidence_level.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'report_block_one_of1_all_of_judgments_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ReportBlockOneOf1AllOfJudgmentsInner {
  /// Returns a new [ReportBlockOneOf1AllOfJudgmentsInner] instance.
  ReportBlockOneOf1AllOfJudgmentsInner({

    required  this.text,

     this.probability,

     this.icdTerm,

     this.confidence,

     this.questionRef,

     this.hypothesisSetRef,
  });

  @JsonKey(
    
    name: r'text',
    required: true,
    includeIfNull: false,
  )


  final String text;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: false,
    includeIfNull: false,
  )


  final num? probability;



  @JsonKey(
    
    name: r'icdTerm',
    required: false,
    includeIfNull: false,
  )


  final IcdTerm? icdTerm;



  @JsonKey(
    
    name: r'confidence',
    required: false,
    includeIfNull: false,
  )


  final ConfidenceLevel? confidence;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'questionRef',
    required: false,
    includeIfNull: false,
  )


  final String? questionRef;



      /// Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`: read-only material received from another instance.
  @JsonKey(
    
    name: r'hypothesisSetRef',
    required: false,
    includeIfNull: false,
  )


  final String? hypothesisSetRef;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ReportBlockOneOf1AllOfJudgmentsInner &&
      other.text == text &&
      other.probability == probability &&
      other.icdTerm == icdTerm &&
      other.confidence == confidence &&
      other.questionRef == questionRef &&
      other.hypothesisSetRef == hypothesisSetRef;

    @override
    int get hashCode =>
        text.hashCode +
        (probability == null ? 0 : probability.hashCode) +
        (icdTerm == null ? 0 : icdTerm.hashCode) +
        (confidence == null ? 0 : confidence.hashCode) +
        (questionRef == null ? 0 : questionRef.hashCode) +
        (hypothesisSetRef == null ? 0 : hypothesisSetRef.hashCode);

  factory ReportBlockOneOf1AllOfJudgmentsInner.fromJson(Map<String, dynamic> json) => _$ReportBlockOneOf1AllOfJudgmentsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ReportBlockOneOf1AllOfJudgmentsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

