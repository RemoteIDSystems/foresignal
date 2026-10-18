//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pol_deviation_stay.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PolDeviationStay {
  /// Returns a new [PolDeviationStay] instance.
  PolDeviationStay({

     this.arrive,

     this.depart,

     this.centroid,
  });

  @JsonKey(
    
    name: r'arrive',
    required: false,
    includeIfNull: false,
  )


  final DateTime? arrive;



  @JsonKey(
    
    name: r'depart',
    required: false,
    includeIfNull: false,
  )


  final DateTime? depart;



  @JsonKey(
    
    name: r'centroid',
    required: false,
    includeIfNull: false,
  )


  final Geometry? centroid;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PolDeviationStay &&
      other.arrive == arrive &&
      other.depart == depart &&
      other.centroid == centroid;

    @override
    int get hashCode =>
        arrive.hashCode +
        (depart == null ? 0 : depart.hashCode) +
        centroid.hashCode;

  factory PolDeviationStay.fromJson(Map<String, dynamic> json) => _$PolDeviationStayFromJson(json);

  Map<String, dynamic> toJson() => _$PolDeviationStayToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

