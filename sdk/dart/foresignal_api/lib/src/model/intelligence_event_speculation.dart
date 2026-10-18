//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/intel_event_alternative.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intelligence_event_speculation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelligenceEventSpeculation {
  /// Returns a new [IntelligenceEventSpeculation] instance.
  IntelligenceEventSpeculation({

    required  this.label,

    required  this.alternatives,

     this.narrative,

     this.narrativeOrigin,
  });

  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final IntelligenceEventSpeculationLabelEnum label;



  @JsonKey(
    
    name: r'alternatives',
    required: true,
    includeIfNull: false,
  )


  final List<IntelEventAlternative> alternatives;



      /// Optional LLM text (102 annotate_event appends here)
  @JsonKey(
    
    name: r'narrative',
    required: false,
    includeIfNull: false,
  )


  final String? narrative;



  @JsonKey(
    
    name: r'narrativeOrigin',
    required: false,
    includeIfNull: false,
  )


  final IntelligenceEventSpeculationNarrativeOriginEnum? narrativeOrigin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelligenceEventSpeculation &&
      other.label == label &&
      other.alternatives == alternatives &&
      other.narrative == narrative &&
      other.narrativeOrigin == narrativeOrigin;

    @override
    int get hashCode =>
        label.hashCode +
        alternatives.hashCode +
        (narrative == null ? 0 : narrative.hashCode) +
        (narrativeOrigin == null ? 0 : narrativeOrigin.hashCode);

  factory IntelligenceEventSpeculation.fromJson(Map<String, dynamic> json) => _$IntelligenceEventSpeculationFromJson(json);

  Map<String, dynamic> toJson() => _$IntelligenceEventSpeculationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IntelligenceEventSpeculationLabelEnum {
@JsonValue(r'speculation')
speculation(r'speculation');

const IntelligenceEventSpeculationLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


enum IntelligenceEventSpeculationNarrativeOriginEnum {
@JsonValue(r'llm')
llm(r'llm');

const IntelligenceEventSpeculationNarrativeOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


