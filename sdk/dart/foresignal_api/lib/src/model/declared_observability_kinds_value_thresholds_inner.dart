//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'declared_observability_kinds_value_thresholds_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DeclaredObservabilityKindsValueThresholdsInner {
  /// Returns a new [DeclaredObservabilityKindsValueThresholdsInner] instance.
  DeclaredObservabilityKindsValueThresholdsInner({

    required  this.field,

    required  this.min,

     this.area,
  });

  @JsonKey(
    
    name: r'field',
    required: true,
    includeIfNull: false,
  )


  final String field;



  @JsonKey(
    
    name: r'min',
    required: true,
    includeIfNull: false,
  )


  final num min;



  @JsonKey(
    
    name: r'area',
    required: false,
    includeIfNull: false,
  )


  final String? area;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DeclaredObservabilityKindsValueThresholdsInner &&
      other.field == field &&
      other.min == min &&
      other.area == area;

    @override
    int get hashCode =>
        field.hashCode +
        min.hashCode +
        area.hashCode;

  factory DeclaredObservabilityKindsValueThresholdsInner.fromJson(Map<String, dynamic> json) => _$DeclaredObservabilityKindsValueThresholdsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$DeclaredObservabilityKindsValueThresholdsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

