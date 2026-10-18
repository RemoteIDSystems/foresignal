//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'hypothesis_coincidence_baseline.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HypothesisCoincidenceBaseline {
  /// Returns a new [HypothesisCoincidenceBaseline] instance.
  HypothesisCoincidenceBaseline({

    required  this.placeboRate,

    required  this.method,

     this.eventStudyId,

     this.nPlacebo,
  });

          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'placeboRate',
    required: true,
    includeIfNull: false,
  )


  final num placeboRate;



  @JsonKey(
    
    name: r'method',
    required: true,
    includeIfNull: false,
  )


  final String method;



  @JsonKey(
    
    name: r'eventStudyId',
    required: false,
    includeIfNull: false,
  )


  final String? eventStudyId;



  @JsonKey(
    
    name: r'nPlacebo',
    required: false,
    includeIfNull: false,
  )


  final int? nPlacebo;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HypothesisCoincidenceBaseline &&
      other.placeboRate == placeboRate &&
      other.method == method &&
      other.eventStudyId == eventStudyId &&
      other.nPlacebo == nPlacebo;

    @override
    int get hashCode =>
        placeboRate.hashCode +
        method.hashCode +
        (eventStudyId == null ? 0 : eventStudyId.hashCode) +
        (nPlacebo == null ? 0 : nPlacebo.hashCode);

  factory HypothesisCoincidenceBaseline.fromJson(Map<String, dynamic> json) => _$HypothesisCoincidenceBaselineFromJson(json);

  Map<String, dynamic> toJson() => _$HypothesisCoincidenceBaselineToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

