//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/series_point.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'chronicle_series.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChronicleSeries {
  /// Returns a new [ChronicleSeries] instance.
  ChronicleSeries({

    required  this.chronicleId,

    required  this.ref,

     this.from,

     this.to,

    required  this.points,

     this.backfilled,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'chronicleId',
    required: true,
    includeIfNull: false,
  )


  final String chronicleId;



      /// Series reference (Intelligence-Design §4.3): `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-chokepoints/{portId}` for PortWatch daily transits), `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}` (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily news coverage in evidence groups by lean bucket left, center, right, unrated or state); or `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or engagement, every language summed unless a language bucket is given; `social:all/{platform}/denominator[/{language}]` is the platform-language denominator; complete and paged collection only)
  @JsonKey(
    
    name: r'ref',
    required: true,
    includeIfNull: false,
  )


  final String ref;



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



  @JsonKey(
    
    name: r'points',
    required: true,
    includeIfNull: false,
  )


  final List<SeriesPoint> points;



  @JsonKey(
    
    name: r'backfilled',
    required: false,
    includeIfNull: false,
  )


  final bool? backfilled;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChronicleSeries &&
      other.chronicleId == chronicleId &&
      other.ref == ref &&
      other.from == from &&
      other.to == to &&
      other.points == points &&
      other.backfilled == backfilled;

    @override
    int get hashCode =>
        chronicleId.hashCode +
        ref.hashCode +
        (from == null ? 0 : from.hashCode) +
        (to == null ? 0 : to.hashCode) +
        points.hashCode +
        backfilled.hashCode;

  factory ChronicleSeries.fromJson(Map<String, dynamic> json) => _$ChronicleSeriesFromJson(json);

  Map<String, dynamic> toJson() => _$ChronicleSeriesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

