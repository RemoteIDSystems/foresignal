//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sensor_all_of_capabilities.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SensorAllOfCapabilities {
  /// Returns a new [SensorAllOfCapabilities] instance.
  SensorAllOfCapabilities({

     this.operator_,

     this.sensorTypes,

     this.resolutionM,

     this.swathKm,
  });

  @JsonKey(
    
    name: r'operator',
    required: false,
    includeIfNull: false,
  )


  final String? operator_;



  @JsonKey(
    
    name: r'sensorTypes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sensorTypes;



  @JsonKey(
    
    name: r'resolutionM',
    required: false,
    includeIfNull: false,
  )


  final num? resolutionM;



  @JsonKey(
    
    name: r'swathKm',
    required: false,
    includeIfNull: false,
  )


  final num? swathKm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SensorAllOfCapabilities &&
      other.operator_ == operator_ &&
      other.sensorTypes == sensorTypes &&
      other.resolutionM == resolutionM &&
      other.swathKm == swathKm;

    @override
    int get hashCode =>
        (operator_ == null ? 0 : operator_.hashCode) +
        sensorTypes.hashCode +
        (resolutionM == null ? 0 : resolutionM.hashCode) +
        (swathKm == null ? 0 : swathKm.hashCode);

  factory SensorAllOfCapabilities.fromJson(Map<String, dynamic> json) => _$SensorAllOfCapabilitiesFromJson(json);

  Map<String, dynamic> toJson() => _$SensorAllOfCapabilitiesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

