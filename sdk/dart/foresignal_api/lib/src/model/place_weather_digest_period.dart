//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/place_weather_digest_period_hazards_entered_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'place_weather_digest_period.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PlaceWeatherDigestPeriod {
  /// Returns a new [PlaceWeatherDigestPeriod] instance.
  PlaceWeatherDigestPeriod({

     this.from,

     this.to,

     this.partial,

     this.maxDbz,

     this.maxMeshMm,

     this.lightningTotal,

     this.hazardsEntered,

     this.notes,
  });

  @JsonKey(
    
    name: r'from',
    required: false,
    includeIfNull: false,
  )


  final DateTime? from;



  @JsonKey(
    
    name: r'to',
    required: false,
    includeIfNull: false,
  )


  final DateTime? to;



      /// Window starts below the retention floor (\"partial period\")
  @JsonKey(
    
    name: r'partial',
    required: false,
    includeIfNull: false,
  )


  final bool? partial;



      /// Largest radar.maxDbzInRadius of the ok place-weather versions in the window (null when none was ok); omitted when weatherProducts excludes mrms-reflectivity
  @JsonKey(
    
    name: r'maxDbz',
    required: false,
    includeIfNull: false,
  )


  final num? maxDbz;



      /// Largest hail.meshMaxMmInRadius of the ok place-weather versions (null when none); omitted when weatherProducts excludes mrms-mesh-60m
  @JsonKey(
    
    name: r'maxMeshMm',
    required: false,
    includeIfNull: false,
  )


  final num? maxMeshMm;



      /// GOES GLM flashes within the place radius over the window, from the 14-day flash archive (the largest count over satellites); null when GLM is unavailable
  @JsonKey(
    
    name: r'lightningTotal',
    required: false,
    includeIfNull: false,
  )


  final int? lightningTotal;



  @JsonKey(
    
    name: r'hazardsEntered',
    required: false,
    includeIfNull: false,
  )


  final List<PlaceWeatherDigestPeriodHazardsEnteredInner>? hazardsEntered;



      /// Why a period value is unknown or partial, for example \"lightning total unavailable: GOES GLM is disabled or not installed\"
  @JsonKey(
    
    name: r'notes',
    required: false,
    includeIfNull: false,
  )


  final List<String>? notes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PlaceWeatherDigestPeriod &&
      other.from == from &&
      other.to == to &&
      other.partial == partial &&
      other.maxDbz == maxDbz &&
      other.maxMeshMm == maxMeshMm &&
      other.lightningTotal == lightningTotal &&
      other.hazardsEntered == hazardsEntered &&
      other.notes == notes;

    @override
    int get hashCode =>
        from.hashCode +
        to.hashCode +
        partial.hashCode +
        (maxDbz == null ? 0 : maxDbz.hashCode) +
        (maxMeshMm == null ? 0 : maxMeshMm.hashCode) +
        (lightningTotal == null ? 0 : lightningTotal.hashCode) +
        hazardsEntered.hashCode +
        notes.hashCode;

  factory PlaceWeatherDigestPeriod.fromJson(Map<String, dynamic> json) => _$PlaceWeatherDigestPeriodFromJson(json);

  Map<String, dynamic> toJson() => _$PlaceWeatherDigestPeriodToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

