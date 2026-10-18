//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_suggestion_estimate_layers_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSuggestionEstimateLayersInner {
  /// Returns a new [ChronicleSuggestionEstimateLayersInner] instance.
  ChronicleSuggestionEstimateLayersInner({

    required  this.name,

     this.rowsPerDay,

     this.bytesPerDay,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'rowsPerDay',
    required: false,
    includeIfNull: false,
  )


  final num? rowsPerDay;



  @JsonKey(
    
    name: r'bytesPerDay',
    required: false,
    includeIfNull: false,
  )


  final num? bytesPerDay;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSuggestionEstimateLayersInner &&
      other.name == name &&
      other.rowsPerDay == rowsPerDay &&
      other.bytesPerDay == bytesPerDay;

    @override
    int get hashCode =>
        name.hashCode +
        rowsPerDay.hashCode +
        bytesPerDay.hashCode;

  factory ChronicleSuggestionEstimateLayersInner.fromJson(Map<String, dynamic> json) => _$ChronicleSuggestionEstimateLayersInnerFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSuggestionEstimateLayersInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

