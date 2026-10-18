//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'situation_speculation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SituationSpeculation {
  /// Returns a new [SituationSpeculation] instance.
  SituationSpeculation({

    required  this.label,

     this.textOrigin,

    required  this.brief,

     this.briefGeneratedAt,

     this.briefModel,

     this.manifestId,
  });

  @JsonKey(
    
    name: r'label',
    required: true,
    includeIfNull: false,
  )


  final SituationSpeculationLabelEnum label;



  @JsonKey(
    
    name: r'textOrigin',
    required: false,
    includeIfNull: false,
  )


  final SituationSpeculationTextOriginEnum? textOrigin;



      /// Markdown brief, null when none was generated
  @JsonKey(
    
    name: r'brief',
    required: true,
    includeIfNull: true,
  )


  final String? brief;



  @JsonKey(
    
    name: r'briefGeneratedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? briefGeneratedAt;



  @JsonKey(
    
    name: r'briefModel',
    required: false,
    includeIfNull: false,
  )


  final String? briefModel;



      /// ULID identifier
  @JsonKey(
    
    name: r'manifestId',
    required: false,
    includeIfNull: false,
  )


  final String? manifestId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SituationSpeculation &&
      other.label == label &&
      other.textOrigin == textOrigin &&
      other.brief == brief &&
      other.briefGeneratedAt == briefGeneratedAt &&
      other.briefModel == briefModel &&
      other.manifestId == manifestId;

    @override
    int get hashCode =>
        label.hashCode +
        textOrigin.hashCode +
        (brief == null ? 0 : brief.hashCode) +
        (briefGeneratedAt == null ? 0 : briefGeneratedAt.hashCode) +
        (briefModel == null ? 0 : briefModel.hashCode) +
        (manifestId == null ? 0 : manifestId.hashCode);

  factory SituationSpeculation.fromJson(Map<String, dynamic> json) => _$SituationSpeculationFromJson(json);

  Map<String, dynamic> toJson() => _$SituationSpeculationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum SituationSpeculationLabelEnum {
@JsonValue(r'speculation')
speculation(r'speculation');

const SituationSpeculationLabelEnum(this.value);

final String value;

@override
String toString() => value;
}


enum SituationSpeculationTextOriginEnum {
@JsonValue(r'llm')
llm(r'llm');

const SituationSpeculationTextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


