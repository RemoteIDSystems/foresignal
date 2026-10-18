//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sample_value_derived.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SampleValueDerived {
  /// Returns a new [SampleValueDerived] instance.
  SampleValueDerived({

     this.speedMs,

     this.fromDeg,

     this.compass,
  });

  @JsonKey(
    
    name: r'speedMs',
    required: false,
    includeIfNull: false,
  )


  final num? speedMs;



          // minimum: 0
          // maximum: 359
  @JsonKey(
    
    name: r'fromDeg',
    required: false,
    includeIfNull: false,
  )


  final int? fromDeg;



  @JsonKey(
    
    name: r'compass',
    required: false,
    includeIfNull: false,
  )


  final String? compass;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SampleValueDerived &&
      other.speedMs == speedMs &&
      other.fromDeg == fromDeg &&
      other.compass == compass;

    @override
    int get hashCode =>
        speedMs.hashCode +
        fromDeg.hashCode +
        compass.hashCode;

  factory SampleValueDerived.fromJson(Map<String, dynamic> json) => _$SampleValueDerivedFromJson(json);

  Map<String, dynamic> toJson() => _$SampleValueDerivedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

