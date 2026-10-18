//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'geofence_ref.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GeofenceRef {
  /// Returns a new [GeofenceRef] instance.
  GeofenceRef({

     this.country,

     this.admin1,

     this.county,
  });

  @JsonKey(
    
    name: r'country',
    required: false,
    includeIfNull: false,
  )


  final List<String>? country;



  @JsonKey(
    
    name: r'admin1',
    required: false,
    includeIfNull: false,
  )


  final List<String>? admin1;



  @JsonKey(
    
    name: r'county',
    required: false,
    includeIfNull: false,
  )


  final List<String>? county;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GeofenceRef &&
      other.country == country &&
      other.admin1 == admin1 &&
      other.county == county;

    @override
    int get hashCode =>
        country.hashCode +
        admin1.hashCode +
        county.hashCode;

  factory GeofenceRef.fromJson(Map<String, dynamic> json) => _$GeofenceRefFromJson(json);

  Map<String, dynamic> toJson() => _$GeofenceRefToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

