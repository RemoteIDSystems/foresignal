//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/collection_gap_recommendations_inner.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'collection_gap.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CollectionGap {
  /// Returns a new [CollectionGap] instance.
  CollectionGap({

    required  this.id,

    required  this.setId,

    required  this.indicatorId,

    required  this.potentialEig,

    required  this.actualEig,

     this.pDetect,

    required  this.recommendations,

     this.createdAt,

     this.updatedAt,
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
    
    name: r'setId',
    required: true,
    includeIfNull: false,
  )


  final String setId;



      /// ULID identifier
  @JsonKey(
    
    name: r'indicatorId',
    required: true,
    includeIfNull: false,
  )


  final String indicatorId;



  @JsonKey(
    
    name: r'potentialEig',
    required: true,
    includeIfNull: false,
  )


  final num potentialEig;



  @JsonKey(
    
    name: r'actualEig',
    required: true,
    includeIfNull: false,
  )


  final num actualEig;



  @JsonKey(
    
    name: r'pDetect',
    required: false,
    includeIfNull: false,
  )


  final num? pDetect;



  @JsonKey(
    
    name: r'recommendations',
    required: true,
    includeIfNull: false,
  )


  final List<CollectionGapRecommendationsInner> recommendations;



  @JsonKey(
    
    name: r'createdAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CollectionGap &&
      other.id == id &&
      other.setId == setId &&
      other.indicatorId == indicatorId &&
      other.potentialEig == potentialEig &&
      other.actualEig == actualEig &&
      other.pDetect == pDetect &&
      other.recommendations == recommendations &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        id.hashCode +
        setId.hashCode +
        indicatorId.hashCode +
        potentialEig.hashCode +
        actualEig.hashCode +
        (pDetect == null ? 0 : pDetect.hashCode) +
        recommendations.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory CollectionGap.fromJson(Map<String, dynamic> json) => _$CollectionGapFromJson(json);

  Map<String, dynamic> toJson() => _$CollectionGapToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

