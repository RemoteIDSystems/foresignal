//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/doctor_report_llm_tokens.dart';
import 'package:foresignal_api/src/model/proposed_patch.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'doctor_report_llm.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DoctorReportLlm {
  /// Returns a new [DoctorReportLlm] instance.
  DoctorReportLlm({

    required  this.status,

     this.provider,

     this.model,

     this.tokens,

     this.summary,

     this.likelyCause,

     this.confidence,

     this.evidence,

     this.suggestedActions,

     this.patch_,

     this.contextTokens,

     this.detail,

     this.error,
  });

  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final DoctorReportLlmStatusEnum status;



  @JsonKey(
    
    name: r'provider',
    required: false,
    includeIfNull: false,
  )


  final String? provider;



  @JsonKey(
    
    name: r'model',
    required: false,
    includeIfNull: false,
  )


  final String? model;



  @JsonKey(
    
    name: r'tokens',
    required: false,
    includeIfNull: false,
  )


  final DoctorReportLlmTokens? tokens;



  @JsonKey(
    
    name: r'summary',
    required: false,
    includeIfNull: false,
  )


  final String? summary;



  @JsonKey(
    
    name: r'likelyCause',
    required: false,
    includeIfNull: false,
  )


  final String? likelyCause;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'confidence',
    required: false,
    includeIfNull: false,
  )


  final num? confidence;



  @JsonKey(
    
    name: r'evidence',
    required: false,
    includeIfNull: false,
  )


  final List<String>? evidence;



  @JsonKey(
    
    name: r'suggestedActions',
    required: false,
    includeIfNull: false,
  )


  final List<String>? suggestedActions;



  @JsonKey(
    
    name: r'patch',
    required: false,
    includeIfNull: false,
  )


  final ProposedPatch? patch_;



      /// Estimated tokens of the context sent (4 characters a token)
  @JsonKey(
    
    name: r'contextTokens',
    required: false,
    includeIfNull: false,
  )


  final int? contextTokens;



  @JsonKey(
    
    name: r'detail',
    required: false,
    includeIfNull: false,
  )


  final DoctorReportLlmDetailEnum? detail;



      /// Why the LLM step gave no answer, when it did not
  @JsonKey(
    
    name: r'error',
    required: false,
    includeIfNull: false,
  )


  final String? error;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DoctorReportLlm &&
      other.status == status &&
      other.provider == provider &&
      other.model == model &&
      other.tokens == tokens &&
      other.summary == summary &&
      other.likelyCause == likelyCause &&
      other.confidence == confidence &&
      other.evidence == evidence &&
      other.suggestedActions == suggestedActions &&
      other.patch_ == patch_ &&
      other.contextTokens == contextTokens &&
      other.detail == detail &&
      other.error == error;

    @override
    int get hashCode =>
        status.hashCode +
        (provider == null ? 0 : provider.hashCode) +
        (model == null ? 0 : model.hashCode) +
        tokens.hashCode +
        (summary == null ? 0 : summary.hashCode) +
        (likelyCause == null ? 0 : likelyCause.hashCode) +
        (confidence == null ? 0 : confidence.hashCode) +
        evidence.hashCode +
        suggestedActions.hashCode +
        (patch_ == null ? 0 : patch_.hashCode) +
        contextTokens.hashCode +
        detail.hashCode +
        (error == null ? 0 : error.hashCode);

  factory DoctorReportLlm.fromJson(Map<String, dynamic> json) => _$DoctorReportLlmFromJson(json);

  Map<String, dynamic> toJson() => _$DoctorReportLlmToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum DoctorReportLlmStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'off')
off(r'off'),
@JsonValue(r'no-provider')
noProvider(r'no-provider'),
@JsonValue(r'budget-refused')
budgetRefused(r'budget-refused'),
@JsonValue(r'timeout')
timeout(r'timeout'),
@JsonValue(r'failed')
failed(r'failed');

const DoctorReportLlmStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


enum DoctorReportLlmDetailEnum {
@JsonValue(r'compact')
compact(r'compact'),
@JsonValue(r'full')
full(r'full');

const DoctorReportLlmDetailEnum(this.value);

final String value;

@override
String toString() => value;
}


