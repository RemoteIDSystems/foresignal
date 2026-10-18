//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/chronicle_suggestion_estimate_layers_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_suggestion_estimate.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSuggestionEstimate {
  /// Returns a new [ChronicleSuggestionEstimate] instance.
  ChronicleSuggestionEstimate({

     this.totalRowsPerDay,

     this.totalBytesPerDay,

     this.layers,
  });

  @JsonKey(
    
    name: r'totalRowsPerDay',
    required: false,
    includeIfNull: false,
  )


  final num? totalRowsPerDay;



  @JsonKey(
    
    name: r'totalBytesPerDay',
    required: false,
    includeIfNull: false,
  )


  final num? totalBytesPerDay;



  @JsonKey(
    
    name: r'layers',
    required: false,
    includeIfNull: false,
  )


  final List<ChronicleSuggestionEstimateLayersInner>? layers;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSuggestionEstimate &&
      other.totalRowsPerDay == totalRowsPerDay &&
      other.totalBytesPerDay == totalBytesPerDay &&
      other.layers == layers;

    @override
    int get hashCode =>
        totalRowsPerDay.hashCode +
        totalBytesPerDay.hashCode +
        layers.hashCode;

  factory ChronicleSuggestionEstimate.fromJson(Map<String, dynamic> json) => _$ChronicleSuggestionEstimateFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSuggestionEstimateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

