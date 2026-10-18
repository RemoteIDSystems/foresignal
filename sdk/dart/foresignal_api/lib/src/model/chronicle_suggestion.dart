//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_input.dart';
import 'package:foresignal_api/src/model/chronicle_suggestion_estimate.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_suggestion.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSuggestion {
  /// Returns a new [ChronicleSuggestion] instance.
  ChronicleSuggestion({

    required  this.draft,

     this.estimate,

     this.rationale,

     this.textOrigin,
  });

  @JsonKey(
    
    name: r'draft',
    required: true,
    includeIfNull: false,
  )


  final ChronicleInput draft;



  @JsonKey(
    
    name: r'estimate',
    required: false,
    includeIfNull: false,
  )


  final ChronicleSuggestionEstimate? estimate;



  @JsonKey(
    
    name: r'rationale',
    required: false,
    includeIfNull: false,
  )


  final List<String>? rationale;



  @JsonKey(
    
    name: r'textOrigin',
    required: false,
    includeIfNull: false,
  )


  final ChronicleSuggestionTextOriginEnum? textOrigin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSuggestion &&
      other.draft == draft &&
      other.estimate == estimate &&
      other.rationale == rationale &&
      other.textOrigin == textOrigin;

    @override
    int get hashCode =>
        draft.hashCode +
        estimate.hashCode +
        rationale.hashCode +
        textOrigin.hashCode;

  factory ChronicleSuggestion.fromJson(Map<String, dynamic> json) => _$ChronicleSuggestionFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSuggestionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ChronicleSuggestionTextOriginEnum {
@JsonValue(r'deterministic')
deterministic(r'deterministic'),
@JsonValue(r'llm')
llm(r'llm');

const ChronicleSuggestionTextOriginEnum(this.value);

final String value;

@override
String toString() => value;
}


