//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/situation_link_decision.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_linker_test_result.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationLinkerTestResult {
  /// Returns a new [SituationLinkerTestResult] instance.
  SituationLinkerTestResult({

    required  this.fromSeq,

    required  this.toSeq,

    required  this.linkerVersion,

     this.inputsConsidered,

    required  this.decisions,

     this.truncated,
  });

  @JsonKey(
    
    name: r'fromSeq',
    required: true,
    includeIfNull: false,
  )


  final int fromSeq;



  @JsonKey(
    
    name: r'toSeq',
    required: true,
    includeIfNull: false,
  )


  final int toSeq;



  @JsonKey(
    
    name: r'linkerVersion',
    required: true,
    includeIfNull: false,
  )


  final String linkerVersion;



  @JsonKey(
    
    name: r'inputsConsidered',
    required: false,
    includeIfNull: false,
  )


  final int? inputsConsidered;



  @JsonKey(
    
    name: r'decisions',
    required: true,
    includeIfNull: false,
  )


  final List<SituationLinkDecision> decisions;



      /// True when the decision list was capped
  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationLinkerTestResult &&
      other.fromSeq == fromSeq &&
      other.toSeq == toSeq &&
      other.linkerVersion == linkerVersion &&
      other.inputsConsidered == inputsConsidered &&
      other.decisions == decisions &&
      other.truncated == truncated;

    @override
    int get hashCode =>
        fromSeq.hashCode +
        toSeq.hashCode +
        linkerVersion.hashCode +
        inputsConsidered.hashCode +
        decisions.hashCode +
        truncated.hashCode;

  factory SituationLinkerTestResult.fromJson(Map<String, dynamic> json) => _$SituationLinkerTestResultFromJson(json);

  Map<String, dynamic> toJson() => _$SituationLinkerTestResultToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

