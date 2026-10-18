//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'peer_selection.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PeerSelection {
  /// Returns a new [PeerSelection] instance.
  PeerSelection({

     this.sources,

     this.kinds,

     this.bbox,

     this.placeId,

     this.maxAge,

     this.backfill = 'none',

     this.live = false,

     this.maxHops = 3,

     this.trackTtl = '10m',
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



  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



      /// Drop items whose observedAt is older than this. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h).
  @JsonKey(
    
    name: r'maxAge',
    required: false,
    includeIfNull: false,
  )


  final String? maxAge;



      /// `none` (current snapshot only) or a duration of history to page from /changes
  @JsonKey(
    defaultValue: 'none',
    name: r'backfill',
    required: false,
    includeIfNull: false,
  )


  final String? backfill;



      /// Also receive the live lane
  @JsonKey(
    defaultValue: false,
    name: r'live',
    required: false,
    includeIfNull: false,
  )


  final bool? live;



          // minimum: 1
  @JsonKey(
    defaultValue: 3,
    name: r'maxHops',
    required: false,
    includeIfNull: false,
  )


  final int? maxHops;



      /// TTL of peer-{id}-trk when the remote kind's TTL is unknown. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h).
  @JsonKey(
    defaultValue: '10m',
    name: r'trackTtl',
    required: false,
    includeIfNull: false,
  )


  final String? trackTtl;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PeerSelection &&
      other.sources == sources &&
      other.kinds == kinds &&
      other.bbox == bbox &&
      other.placeId == placeId &&
      other.maxAge == maxAge &&
      other.backfill == backfill &&
      other.live == live &&
      other.maxHops == maxHops &&
      other.trackTtl == trackTtl;

    @override
    int get hashCode =>
        sources.hashCode +
        kinds.hashCode +
        (bbox == null ? 0 : bbox.hashCode) +
        (placeId == null ? 0 : placeId.hashCode) +
        (maxAge == null ? 0 : maxAge.hashCode) +
        backfill.hashCode +
        live.hashCode +
        maxHops.hashCode +
        trackTtl.hashCode;

  factory PeerSelection.fromJson(Map<String, dynamic> json) => _$PeerSelectionFromJson(json);

  Map<String, dynamic> toJson() => _$PeerSelectionToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

