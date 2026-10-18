//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/icd_term.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_change.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisChange {
  /// Returns a new [HypothesisChange] instance.
  HypothesisChange({

    required  this.previousProbability,

     this.previousTerm,

    required  this.delta,

     this.sinceRevisionId,
  });

          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'previousProbability',
    required: true,
    includeIfNull: false,
  )


  final num previousProbability;



  @JsonKey(
    
    name: r'previousTerm',
    required: false,
    includeIfNull: false,
  )


  final IcdTerm? previousTerm;



  @JsonKey(
    
    name: r'delta',
    required: true,
    includeIfNull: false,
  )


  final num delta;



      /// ULID identifier
  @JsonKey(
    
    name: r'sinceRevisionId',
    required: false,
    includeIfNull: false,
  )


  final String? sinceRevisionId;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisChange &&
      other.previousProbability == previousProbability &&
      other.previousTerm == previousTerm &&
      other.delta == delta &&
      other.sinceRevisionId == sinceRevisionId;

    @override
    int get hashCode =>
        previousProbability.hashCode +
        previousTerm.hashCode +
        delta.hashCode +
        sinceRevisionId.hashCode;

  factory HypothesisChange.fromJson(Map<String, dynamic> json) => _$HypothesisChangeFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisChangeToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

