//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'event_stream_changes_filter.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EventStreamChangesFilter {
  /// Returns a new [EventStreamChangesFilter] instance.
  EventStreamChangesFilter({

     this.source_,

     this.kind,

     this.externalId,

     this.bbox,

     this.near,

     this.radiusKm,

     this.live = true,

     this.touches = false,

     this.payload = false,

     this.place,

     this.view,

     this.inWatchlist,
  });

      /// Source ids
  @JsonKey(
    
    name: r'source',
    required: false,
    includeIfNull: false,
  )


  final List<String>? source_;



      /// Kinds
  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final List<String>? kind;



      /// External ids
  @JsonKey(
    
    name: r'externalId',
    required: false,
    includeIfNull: false,
  )


  final List<String>? externalId;



      /// minLon,minLat,maxLon,maxLat; minLon > maxLon crosses the antimeridian
  @JsonKey(
    
    name: r'bbox',
    required: false,
    includeIfNull: false,
  )


  final String? bbox;



      /// `lon,lat`; requires radiusKm
  @JsonKey(
    
    name: r'near',
    required: false,
    includeIfNull: false,
  )


  final String? near;



          // minimum: 0
          // maximum: 2000
  @JsonKey(
    
    name: r'radiusKm',
    required: false,
    includeIfNull: false,
  )


  final num? radiusKm;



      /// Send `live` events
  @JsonKey(
    defaultValue: true,
    name: r'live',
    required: false,
    includeIfNull: false,
  )


  final bool? live;



      /// Send `touch` events
  @JsonKey(
    defaultValue: false,
    name: r'touches',
    required: false,
    includeIfNull: false,
  )


  final bool? touches;



      /// Include payloads on `change` events
  @JsonKey(
    defaultValue: false,
    name: r'payload',
    required: false,
    includeIfNull: false,
  )


  final bool? payload;



      /// Place ids (036): their areas (buffered by radiusKm when near is absent); needs a token
  @JsonKey(
    
    name: r'place',
    required: false,
    includeIfNull: false,
  )


  final List<String>? place;



      /// Saved view id or slug (036); a view referencing a place or watchlist needs a token
  @JsonKey(
    
    name: r'view',
    required: false,
    includeIfNull: false,
  )


  final String? view;



      /// Watchlist id (036); needs a token
  @JsonKey(
    
    name: r'inWatchlist',
    required: false,
    includeIfNull: false,
  )


  final String? inWatchlist;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EventStreamChangesFilter &&
      other.source_ == source_ &&
      other.kind == kind &&
      other.externalId == externalId &&
      other.bbox == bbox &&
      other.near == near &&
      other.radiusKm == radiusKm &&
      other.live == live &&
      other.touches == touches &&
      other.payload == payload &&
      other.place == place &&
      other.view == view &&
      other.inWatchlist == inWatchlist;

    @override
    int get hashCode =>
        source_.hashCode +
        kind.hashCode +
        externalId.hashCode +
        (bbox == null ? 0 : bbox.hashCode) +
        (near == null ? 0 : near.hashCode) +
        (radiusKm == null ? 0 : radiusKm.hashCode) +
        live.hashCode +
        touches.hashCode +
        payload.hashCode +
        place.hashCode +
        (view == null ? 0 : view.hashCode) +
        (inWatchlist == null ? 0 : inWatchlist.hashCode);

  factory EventStreamChangesFilter.fromJson(Map<String, dynamic> json) => _$EventStreamChangesFilterFromJson(json);

  Map<String, dynamic> toJson() => _$EventStreamChangesFilterToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

