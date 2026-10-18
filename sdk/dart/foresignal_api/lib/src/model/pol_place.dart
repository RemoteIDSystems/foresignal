//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pol_place.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PolPlace {
  /// Returns a new [PolPlace] instance.
  PolPlace({

    required  this.id,

    required  this.subjectId,

    required  this.clusterId,

    required  this.centroid,

    required  this.radiusM,

    required  this.visits,

    required  this.totalDwellMinutes,

    required  this.hourOfWeek,

    required  this.firstSeen,

    required  this.lastSeen,

     this.label,

     this.private,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



      /// ULID identifier
  @JsonKey(
    
    name: r'subjectId',
    required: true,
    includeIfNull: false,
  )


  final String subjectId;



  @JsonKey(
    
    name: r'clusterId',
    required: true,
    includeIfNull: false,
  )


  final int clusterId;



  @JsonKey(
    
    name: r'centroid',
    required: true,
    includeIfNull: false,
  )


  final Geometry centroid;



      /// p90 distance of member stays
  @JsonKey(
    
    name: r'radiusM',
    required: true,
    includeIfNull: false,
  )


  final num radiusM;



  @JsonKey(
    
    name: r'visits',
    required: true,
    includeIfNull: false,
  )


  final int visits;



  @JsonKey(
    
    name: r'totalDwellMinutes',
    required: true,
    includeIfNull: false,
  )


  final num totalDwellMinutes;



      /// Dwell minutes per hour of week (168 buckets, Monday 00:00 operator time first)
  @JsonKey(
    
    name: r'hourOfWeek',
    required: true,
    includeIfNull: false,
  )


  final List<num> hourOfWeek;



  @JsonKey(
    
    name: r'firstSeen',
    required: true,
    includeIfNull: false,
  )


  final DateTime firstSeen;



  @JsonKey(
    
    name: r'lastSeen',
    required: true,
    includeIfNull: false,
  )


  final DateTime lastSeen;



      /// Operator-set label, e.g. home
  @JsonKey(
    
    name: r'label',
    required: false,
    includeIfNull: false,
  )


  final String? label;



  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PolPlace &&
      other.id == id &&
      other.subjectId == subjectId &&
      other.clusterId == clusterId &&
      other.centroid == centroid &&
      other.radiusM == radiusM &&
      other.visits == visits &&
      other.totalDwellMinutes == totalDwellMinutes &&
      other.hourOfWeek == hourOfWeek &&
      other.firstSeen == firstSeen &&
      other.lastSeen == lastSeen &&
      other.label == label &&
      other.private == private;

    @override
    int get hashCode =>
        id.hashCode +
        subjectId.hashCode +
        clusterId.hashCode +
        centroid.hashCode +
        radiusM.hashCode +
        visits.hashCode +
        totalDwellMinutes.hashCode +
        hourOfWeek.hashCode +
        firstSeen.hashCode +
        lastSeen.hashCode +
        (label == null ? 0 : label.hashCode) +
        private.hashCode;

  factory PolPlace.fromJson(Map<String, dynamic> json) => _$PolPlaceFromJson(json);

  Map<String, dynamic> toJson() => _$PolPlaceToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

