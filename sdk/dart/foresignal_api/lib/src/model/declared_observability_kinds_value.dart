//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/declared_observability_kinds_value_area.dart';
import 'package:foresignal_api/src/model/declared_observability_kinds_value_thresholds_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'declared_observability_kinds_value.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class DeclaredObservabilityKindsValue {
  /// Returns a new [DeclaredObservabilityKindsValue] instance.
  DeclaredObservabilityKindsValue({

    required  this.area,

     this.thresholds,

     this.typicalLatency,

     this.notes,
  });

  @JsonKey(
    
    name: r'area',
    required: true,
    includeIfNull: false,
  )


  final DeclaredObservabilityKindsValueArea area;



  @JsonKey(
    
    name: r'thresholds',
    required: false,
    includeIfNull: false,
  )


  final List<DeclaredObservabilityKindsValueThresholdsInner>? thresholds;



      /// Typical publication latency. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h).
  @JsonKey(
    
    name: r'typicalLatency',
    required: false,
    includeIfNull: false,
  )


  final String? typicalLatency;



  @JsonKey(
    
    name: r'notes',
    required: false,
    includeIfNull: false,
  )


  final String? notes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is DeclaredObservabilityKindsValue &&
      other.area == area &&
      other.thresholds == thresholds &&
      other.typicalLatency == typicalLatency &&
      other.notes == notes;

    @override
    int get hashCode =>
        area.hashCode +
        thresholds.hashCode +
        (typicalLatency == null ? 0 : typicalLatency.hashCode) +
        (notes == null ? 0 : notes.hashCode);

  factory DeclaredObservabilityKindsValue.fromJson(Map<String, dynamic> json) => _$DeclaredObservabilityKindsValueFromJson(json);

  Map<String, dynamic> toJson() => _$DeclaredObservabilityKindsValueToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

