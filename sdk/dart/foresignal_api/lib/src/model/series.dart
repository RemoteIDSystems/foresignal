//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/series_sample.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'series.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Series {
  /// Returns a new [Series] instance.
  Series({

    required  this.productId,

    required  this.lon,

    required  this.lat,

     this.placeId,

     this.timezone,

     this.units,

     this.label,

     this.from,

     this.to,

     this.run,

    required  this.items,

     this.attribution,

    required  this.serverTime,
  });

  @JsonKey(
    
    name: r'productId',
    required: true,
    includeIfNull: false,
  )


  final String productId;



  @JsonKey(
    
    name: r'lon',
    required: true,
    includeIfNull: false,
  )


  final num lon;



  @JsonKey(
    
    name: r'lat',
    required: true,
    includeIfNull: false,
  )


  final num lat;



  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



  @JsonKey(
    
    name: r'timezone',
    required: false,
    includeIfNull: false,
  )


  final String? timezone;



  @JsonKey(
    
    name: r'units',
    required: false,
    includeIfNull: false,
  )


  final String? units;



  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;



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



      /// Forecast run used for future steps
  @JsonKey(
    
    name: r'run',
    required: false,
    includeIfNull: false,
  )


  final String? run;



  @JsonKey(
    
    name: r'items',
    required: true,
    includeIfNull: false,
  )


  final List<SeriesSample> items;



  @JsonKey(
    
    name: r'attribution',
    required: false,
    includeIfNull: false,
  )


  final Attribution? attribution;



  @JsonKey(
    
    name: r'serverTime',
    required: true,
    includeIfNull: false,
  )


  final DateTime serverTime;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Series &&
      other.productId == productId &&
      other.lon == lon &&
      other.lat == lat &&
      other.placeId == placeId &&
      other.timezone == timezone &&
      other.units == units &&
      other.label == label &&
      other.from == from &&
      other.to == to &&
      other.run == run &&
      other.items == items &&
      other.attribution == attribution &&
      other.serverTime == serverTime;

    @override
    int get hashCode =>
        productId.hashCode +
        lon.hashCode +
        lat.hashCode +
        (placeId == null ? 0 : placeId.hashCode) +
        timezone.hashCode +
        (units == null ? 0 : units.hashCode) +
        (label == null ? 0 : label.hashCode) +
        from.hashCode +
        to.hashCode +
        (run == null ? 0 : run.hashCode) +
        items.hashCode +
        attribution.hashCode +
        serverTime.hashCode;

  factory Series.fromJson(Map<String, dynamic> json) => _$SeriesFromJson(json);

  Map<String, dynamic> toJson() => _$SeriesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

