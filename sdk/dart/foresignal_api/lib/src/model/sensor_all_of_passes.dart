//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sensor_all_of_passes.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SensorAllOfPasses {
  /// Returns a new [SensorAllOfPasses] instance.
  SensorAllOfPasses({

    required  this.aos,

     this.tca,

    required  this.los,

    required  this.maxElevationDeg,
  });

  @JsonKey(
    
    name: r'aos',
    required: true,
    includeIfNull: false,
  )


  final DateTime aos;



  @JsonKey(
    
    name: r'tca',
    required: false,
    includeIfNull: false,
  )


  final DateTime? tca;



  @JsonKey(
    
    name: r'los',
    required: true,
    includeIfNull: false,
  )


  final DateTime los;



  @JsonKey(
    
    name: r'maxElevationDeg',
    required: true,
    includeIfNull: false,
  )


  final num maxElevationDeg;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SensorAllOfPasses &&
      other.aos == aos &&
      other.tca == tca &&
      other.los == los &&
      other.maxElevationDeg == maxElevationDeg;

    @override
    int get hashCode =>
        aos.hashCode +
        tca.hashCode +
        los.hashCode +
        maxElevationDeg.hashCode;

  factory SensorAllOfPasses.fromJson(Map<String, dynamic> json) => _$SensorAllOfPassesFromJson(json);

  Map<String, dynamic> toJson() => _$SensorAllOfPassesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

