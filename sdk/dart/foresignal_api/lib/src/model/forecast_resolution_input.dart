//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'forecast_resolution_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ForecastResolutionInput {
  /// Returns a new [ForecastResolutionInput] instance.
  ForecastResolutionInput({

     this.outcome,

     this.value,

    required  this.evidence,

     this.note,
  });

      /// Binary `yes`/`no`, or a label of a multiple/ordered question
  @JsonKey(
    
    name: r'outcome',
    required: false,
    includeIfNull: false,
  )


  final String? outcome;



      /// Numeric questions
  @JsonKey(
    
    name: r'value',
    required: false,
    includeIfNull: false,
  )


  final num? value;



      /// Pinned evidence refs
  @JsonKey(
    
    name: r'evidence',
    required: true,
    includeIfNull: false,
  )


  final List<String> evidence;



  @JsonKey(
    
    name: r'note',
    required: false,
    includeIfNull: false,
  )


  final String? note;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ForecastResolutionInput &&
      other.outcome == outcome &&
      other.value == value &&
      other.evidence == evidence &&
      other.note == note;

    @override
    int get hashCode =>
        (outcome == null ? 0 : outcome.hashCode) +
        (value == null ? 0 : value.hashCode) +
        evidence.hashCode +
        (note == null ? 0 : note.hashCode);

  factory ForecastResolutionInput.fromJson(Map<String, dynamic> json) => _$ForecastResolutionInputFromJson(json);

  Map<String, dynamic> toJson() => _$ForecastResolutionInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

