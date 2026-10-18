//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'sink_filter.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SinkFilter {
  /// Returns a new [SinkFilter] instance.
  SinkFilter({

     this.sources,

     this.kinds,

     this.bbox,

     this.placeId,

     this.watchlistId,

     this.includePrivate = false,
  });

  @JsonKey(
    
    name: r'sources',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sources;



  @JsonKey(
    
    name: r'kinds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kinds;



      /// [minLon, minLat, maxLon, maxLat] (WGS84)
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final List<num>? bbox;



      /// Place (036) whose geometry filters records
  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



      /// Watchlist (036) whose members are delivered
  @JsonKey(
    
    name: r'watchlistId',
    required: false,
    includeIfNull: false,
  )


  final String? watchlistId;



      /// Include private sources (039); allowed only for SERVE on loopback or a PUSH target that resolves to a private address
  @JsonKey(
    defaultValue: false,
    name: r'includePrivate',
    required: false,
    includeIfNull: false,
  )


  final bool? includePrivate;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SinkFilter &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.bbox == bbox &&
      other.placeId == placeId &&
      other.watchlistId == watchlistId &&
      other.includePrivate == includePrivate;

    @override
    int get hashCode =>
        sources.hashCode +
        kinds.hashCode +
        (bbox == null ? 0 : bbox.hashCode) +
        (placeId == null ? 0 : placeId.hashCode) +
        (watchlistId == null ? 0 : watchlistId.hashCode) +
        includePrivate.hashCode;

  factory SinkFilter.fromJson(Map<String, dynamic> json) => _$SinkFilterFromJson(json);

  Map<String, dynamic> toJson() => _$SinkFilterToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

