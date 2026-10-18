//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/icd_term.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_board_band_changes_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelBoardBandChangesInner {
  /// Returns a new [IntelBoardBandChangesInner] instance.
  IntelBoardBandChangesInner({

    required  this.setId,

    required  this.hypothesisId,

     this.question,

     this.label,

     this.fromTerm,

    required  this.toTerm,

     this.probability,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'setId',
    required: true,
    includeIfNull: false,
  )


  final String setId;



      /// ULID identifier
  @JsonKey(
    
    name: r'hypothesisId',
    required: true,
    includeIfNull: false,
  )


  final String hypothesisId;



  @JsonKey(
    
    name: r'question',
    required: false,
    includeIfNull: false,
  )


  final String? question;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;



  @JsonKey(
    
    name: r'fromTerm',
    required: false,
    includeIfNull: false,
  )


  final IcdTerm? fromTerm;



  @JsonKey(
    
    name: r'toTerm',
    required: true,
    includeIfNull: false,
  )


  final IcdTerm toTerm;



          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'probability',
    required: false,
    includeIfNull: false,
  )


  final num? probability;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelBoardBandChangesInner &&
      other.setId == setId &&
      other.hypothesisId == hypothesisId &&
      other.question == question &&
      other.label == label &&
      other.fromTerm == fromTerm &&
      other.toTerm == toTerm &&
      other.probability == probability &&
      other.at == at;

    @override
    int get hashCode =>
        setId.hashCode +
        hypothesisId.hashCode +
        question.hashCode +
        label.hashCode +
        fromTerm.hashCode +
        toTerm.hashCode +
        probability.hashCode +
        at.hashCode;

  factory IntelBoardBandChangesInner.fromJson(Map<String, dynamic> json) => _$IntelBoardBandChangesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$IntelBoardBandChangesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

