//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'holding_pattern_payload_all_of_nearest_airport.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class HoldingPatternPayloadAllOfNearestAirport {
  /// Returns a new [HoldingPatternPayloadAllOfNearestAirport] instance.
  HoldingPatternPayloadAllOfNearestAirport({

    required  this.ident,

     this.name,

     this.distanceKm,
  });

  @JsonKey(
    
    name: r'ident',
    required: true,
    includeIfNull: false,
  )


  final String ident;



  @JsonKey(
    
    name: r'name',
    required: false,
    includeIfNull: false,
  )


  final String? name;



  @JsonKey(
    
    name: r'distanceKm',
    required: false,
    includeIfNull: false,
  )


  final num? distanceKm;





    @override
    bool operator ==(Object other) => identical(this, other) || other is HoldingPatternPayloadAllOfNearestAirport &&
      other.ident == ident &&
      other.name == name &&
      other.distanceKm == distanceKm;

    @override
    int get hashCode =>
        ident.hashCode +
        name.hashCode +
        distanceKm.hashCode;

  factory HoldingPatternPayloadAllOfNearestAirport.fromJson(Map<String, dynamic> json) => _$HoldingPatternPayloadAllOfNearestAirportFromJson(json);

  Map<String, dynamic> toJson() => _$HoldingPatternPayloadAllOfNearestAirportToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

