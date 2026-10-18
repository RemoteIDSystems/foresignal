//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/precursor_spatial.dart';
import 'package:foresignal_api/src/model/precursor_side.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_candidate.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorCandidate {
  /// Returns a new [PrecursorCandidate] instance.
  PrecursorCandidate({

     this.name,

    required  this.kind,

    required  this.antecedent,

    required  this.consequent,

     this.spatial,

    required  this.lagMin,

    required  this.lagMax,

     this.regionCodes,

    required  this.id,

    required  this.createdBy,

    required  this.createdAt,
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


  final PrecursorCandidateKindEnum kind;



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
    required: true,
    includeIfNull: false,
  )


  final String lagMin;



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



      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'createdBy',
    required: true,
    includeIfNull: false,
  )


  final String createdBy;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorCandidate &&
      other.name == name &&
      other.kind == kind &&
      other.antecedent == antecedent &&
      other.consequent == consequent &&
      other.spatial == spatial &&
      other.lagMin == lagMin &&
      other.lagMax == lagMax &&
      other.regionCodes == regionCodes &&
      other.id == id &&
      other.createdBy == createdBy &&
      other.createdAt == createdAt;

    @override
    int get hashCode =>
        (name == null ? 0 : name.hashCode) +
        kind.hashCode +
        antecedent.hashCode +
        consequent.hashCode +
        spatial.hashCode +
        lagMin.hashCode +
        lagMax.hashCode +
        regionCodes.hashCode +
        id.hashCode +
        createdBy.hashCode +
        createdAt.hashCode;

  factory PrecursorCandidate.fromJson(Map<String, dynamic> json) => _$PrecursorCandidateFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorCandidateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorCandidateKindEnum {
@JsonValue(r'event')
event(r'event'),
@JsonValue(r'series')
series(r'series');

const PrecursorCandidateKindEnum(this.value);

final String value;

@override
String toString() => value;
}


