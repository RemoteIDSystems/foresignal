//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/icd_term.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_set_event_band_changes_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisSetEventBandChangesInner {
  /// Returns a new [HypothesisSetEventBandChangesInner] instance.
  HypothesisSetEventBandChangesInner({

    required  this.hypothesisId,

     this.label,

    required  this.fromTerm,

    required  this.toTerm,

    required  this.probability,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'hypothesisId',
    required: true,
    includeIfNull: false,
  )


  final String hypothesisId;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;



  @JsonKey(
    
    name: r'fromTerm',
    required: true,
    includeIfNull: false,
  )


  final IcdTerm fromTerm;



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
    required: true,
    includeIfNull: false,
  )


  final num probability;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisSetEventBandChangesInner &&
      other.hypothesisId == hypothesisId &&
      other.label == label &&
      other.fromTerm == fromTerm &&
      other.toTerm == toTerm &&
      other.probability == probability;

    @override
    int get hashCode =>
        hypothesisId.hashCode +
        label.hashCode +
        fromTerm.hashCode +
        toTerm.hashCode +
        probability.hashCode;

  factory HypothesisSetEventBandChangesInner.fromJson(Map<String, dynamic> json) => _$HypothesisSetEventBandChangesInnerFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisSetEventBandChangesInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

