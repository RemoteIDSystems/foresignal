//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/geometry.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pol_stay.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PolStay {
  /// Returns a new [PolStay] instance.
  PolStay({

    required  this.id,

    required  this.subjectId,

    required  this.arrive,

    required  this.depart,

    required  this.centroid,

     this.placeId,

     this.clusterId,

     this.fixes,
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
    
    name: r'arrive',
    required: true,
    includeIfNull: false,
  )


  final DateTime arrive;



  @JsonKey(
    
    name: r'depart',
    required: true,
    includeIfNull: false,
  )


  final DateTime depart;



  @JsonKey(
    
    name: r'centroid',
    required: true,
    includeIfNull: false,
  )


  final Geometry centroid;



      /// ULID identifier
  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



      /// null = unclustered (noise)
  @JsonKey(
    
    name: r'clusterId',
    required: false,
    includeIfNull: false,
  )


  final int? clusterId;



  @JsonKey(
    
    name: r'fixes',
    required: false,
    includeIfNull: false,
  )


  final int? fixes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PolStay &&
      other.id == id &&
      other.subjectId == subjectId &&
      other.arrive == arrive &&
      other.depart == depart &&
      other.centroid == centroid &&
      other.placeId == placeId &&
      other.clusterId == clusterId &&
      other.fixes == fixes;

    @override
    int get hashCode =>
        id.hashCode +
        subjectId.hashCode +
        arrive.hashCode +
        depart.hashCode +
        centroid.hashCode +
        (placeId == null ? 0 : placeId.hashCode) +
        (clusterId == null ? 0 : clusterId.hashCode) +
        fixes.hashCode;

  factory PolStay.fromJson(Map<String, dynamic> json) => _$PolStayFromJson(json);

  Map<String, dynamic> toJson() => _$PolStayToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

