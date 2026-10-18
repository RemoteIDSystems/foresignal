//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/pol_deviation_stay.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'pol_deviation.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PolDeviation {
  /// Returns a new [PolDeviation] instance.
  PolDeviation({

    required  this.subjectId,

     this.subjectRef,

    required  this.deviationType,

    required  this.at,

     this.placeId,

     this.placeLabel,

     this.stay,

     this.hourOfWeek,

     this.bucketShare,

     this.lastSeenAt,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'subjectId',
    required: true,
    includeIfNull: false,
  )


  final String subjectId;



  @JsonKey(
    
    name: r'subjectRef',
    required: false,
    includeIfNull: false,
  )


  final String? subjectRef;



  @JsonKey(
    
    name: r'deviationType',
    required: true,
    includeIfNull: false,
  )


  final PolDeviationDeviationTypeEnum deviationType;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;



      /// ULID identifier
  @JsonKey(
    
    name: r'placeId',
    required: false,
    includeIfNull: false,
  )


  final String? placeId;



  @JsonKey(
    
    name: r'placeLabel',
    required: false,
    includeIfNull: false,
  )


  final String? placeLabel;



  @JsonKey(
    
    name: r'stay',
    required: false,
    includeIfNull: false,
  )


  final PolDeviationStay? stay;



          // minimum: 0
          // maximum: 167
  @JsonKey(
    
    name: r'hourOfWeek',
    required: false,
    includeIfNull: false,
  )


  final int? hourOfWeek;



      /// Share of the place's dwell in this hour-of-week bucket (UNUSUAL_TIME < 0.02; EXPECTED_ABSENT ≥ 0.6)
          // minimum: 0
          // maximum: 1
  @JsonKey(
    
    name: r'bucketShare',
    required: false,
    includeIfNull: false,
  )


  final num? bucketShare;



  @JsonKey(
    
    name: r'lastSeenAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastSeenAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PolDeviation &&
      other.subjectId == subjectId &&
      other.subjectRef == subjectRef &&
      other.deviationType == deviationType &&
      other.at == at &&
      other.placeId == placeId &&
      other.placeLabel == placeLabel &&
      other.stay == stay &&
      other.hourOfWeek == hourOfWeek &&
      other.bucketShare == bucketShare &&
      other.lastSeenAt == lastSeenAt;

    @override
    int get hashCode =>
        subjectId.hashCode +
        subjectRef.hashCode +
        deviationType.hashCode +
        at.hashCode +
        (placeId == null ? 0 : placeId.hashCode) +
        (placeLabel == null ? 0 : placeLabel.hashCode) +
        (stay == null ? 0 : stay.hashCode) +
        hourOfWeek.hashCode +
        (bucketShare == null ? 0 : bucketShare.hashCode) +
        (lastSeenAt == null ? 0 : lastSeenAt.hashCode);

  factory PolDeviation.fromJson(Map<String, dynamic> json) => _$PolDeviationFromJson(json);

  Map<String, dynamic> toJson() => _$PolDeviationToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PolDeviationDeviationTypeEnum {
@JsonValue(r'NEW_PLACE')
NEW_PLACE(r'NEW_PLACE'),
@JsonValue(r'UNUSUAL_TIME')
UNUSUAL_TIME(r'UNUSUAL_TIME'),
@JsonValue(r'EXPECTED_ABSENT')
EXPECTED_ABSENT(r'EXPECTED_ABSENT');

const PolDeviationDeviationTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


