//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/precursor_spatial.dart';
import 'package:foresignal_api/src/model/precursor_side.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_candidate_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorCandidateInput {
  /// Returns a new [PrecursorCandidateInput] instance.
  PrecursorCandidateInput({

     this.name,

    required  this.kind,

    required  this.antecedent,

    required  this.consequent,

     this.spatial,

     this.lagMin,

    required  this.lagMax,

     this.regionCodes,
  });

  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final PrecursorCandidateInputKindEnum kind;



  @JsonKey(
    
    name: r'antecedent',
    required: true,
    includeIfNull: false,
  )


  final PrecursorSide antecedent;



  @JsonKey(
    
    name: r'consequent',
    required: true,
    includeIfNull: false,
  )


  final PrecursorSide consequent;



  @JsonKey(
    
    name: r'spatial',
    required: false,
    includeIfNull: false,
  )


  final PrecursorSpatial? spatial;



      /// ISO-8601 duration
  @JsonKey(
    
    name: r'lagMin',
    required: false,
    includeIfNull: false,
  )


  final String? lagMin;



      /// ISO-8601 duration
  @JsonKey(
    
    name: r'lagMax',
    required: true,
    includeIfNull: false,
  )


  final String lagMax;



  @JsonKey(
    
    name: r'regionCodes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? regionCodes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorCandidateInput &&
      other.name == name &&
      other.kind == kind &&
      other.antecedent == antecedent &&
      other.consequent == consequent &&
      other.spatial == spatial &&
      other.lagMin == lagMin &&
      other.lagMax == lagMax &&
      other.regionCodes == regionCodes;

    @override
    int get hashCode =>
        (name == null ? 0 : name.hashCode) +
        kind.hashCode +
        antecedent.hashCode +
        consequent.hashCode +
        spatial.hashCode +
        lagMin.hashCode +
        lagMax.hashCode +
        regionCodes.hashCode;

  factory PrecursorCandidateInput.fromJson(Map<String, dynamic> json) => _$PrecursorCandidateInputFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorCandidateInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorCandidateInputKindEnum {
@JsonValue(r'event')
event(r'event'),
@JsonValue(r'series')
series(r'series');

const PrecursorCandidateInputKindEnum(this.value);

final String value;

@override
String toString() => value;
}


