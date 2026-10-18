//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'track_properties.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TrackProperties {
  /// Returns a new [TrackProperties] instance.
  TrackProperties({

    required  this.sourceId,

    required  this.externalId,

     this.kind,

    required  this.from,

    required  this.to,

    required  this.times,

     this.values,

     this.live,

     this.pointsScanned,

    required  this.pointsReturned,

     this.toleranceMeters,

    required  this.segments,

     this.approximate,

     this.downsampled,

     this.truncated,

    required  this.watermarkSeq,
  });

  @JsonKey(
    
    name: r'sourceId',
    required: true,
    includeIfNull: false,
  )


  final String sourceId;



  @JsonKey(
    
    name: r'externalId',
    required: true,
    includeIfNull: false,
  )


  final String externalId;



  @JsonKey(
    
    name: r'kind',
    required: false,
    includeIfNull: false,
  )


  final String? kind;



  @JsonKey(
    
    name: r'from',
    required: true,
    includeIfNull: false,
  )


  final DateTime from;



  @JsonKey(
    
    name: r'to',
    required: true,
    includeIfNull: false,
  )


  final DateTime to;



      /// Epoch-ms per point, parallel to each segment
  @JsonKey(
    
    name: r'times',
    required: true,
    includeIfNull: false,
  )


  final List<List<int>> times;



      /// Requested numeric fields, parallel arrays per segment
  @JsonKey(
    
    name: r'values',
    required: false,
    includeIfNull: false,
  )


  final Map<String, List<List<num?>>>? values;



      /// The last point is the current record_live position, newer than the last version
  @JsonKey(
    
    name: r'live',
    required: false,
    includeIfNull: false,
  )


  final bool? live;



  @JsonKey(
    
    name: r'pointsScanned',
    required: false,
    includeIfNull: false,
  )


  final int? pointsScanned;



  @JsonKey(
    
    name: r'pointsReturned',
    required: true,
    includeIfNull: false,
  )


  final int pointsReturned;



  @JsonKey(
    
    name: r'toleranceMeters',
    required: false,
    includeIfNull: false,
  )


  final num? toleranceMeters;



  @JsonKey(
    
    name: r'segments',
    required: true,
    includeIfNull: false,
  )


  final int segments;



  @JsonKey(
    
    name: r'approximate',
    required: false,
    includeIfNull: false,
  )


  final bool? approximate;



  @JsonKey(
    
    name: r'downsampled',
    required: false,
    includeIfNull: false,
  )


  final bool? downsampled;



      /// More than 200,000 rows existed
  @JsonKey(
    
    name: r'truncated',
    required: false,
    includeIfNull: false,
  )


  final bool? truncated;



  @JsonKey(
    
    name: r'watermarkSeq',
    required: true,
    includeIfNull: false,
  )


  final int watermarkSeq;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TrackProperties &&
      other.sourceId == sourceId &&
      other.externalId == externalId &&
      other.kind == kind &&
      other.from == from &&
      other.to == to &&
      other.times == times &&
      other.values == values &&
      other.live == live &&
      other.pointsScanned == pointsScanned &&
      other.pointsReturned == pointsReturned &&
      other.toleranceMeters == toleranceMeters &&
      other.segments == segments &&
      other.approximate == approximate &&
      other.downsampled == downsampled &&
      other.truncated == truncated &&
      other.watermarkSeq == watermarkSeq;

    @override
    int get hashCode =>
        sourceId.hashCode +
        externalId.hashCode +
        kind.hashCode +
        from.hashCode +
        to.hashCode +
        times.hashCode +
        values.hashCode +
        live.hashCode +
        pointsScanned.hashCode +
        pointsReturned.hashCode +
        toleranceMeters.hashCode +
        segments.hashCode +
        approximate.hashCode +
        downsampled.hashCode +
        truncated.hashCode +
        watermarkSeq.hashCode;

  factory TrackProperties.fromJson(Map<String, dynamic> json) => _$TrackPropertiesFromJson(json);

  Map<String, dynamic> toJson() => _$TrackPropertiesToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

