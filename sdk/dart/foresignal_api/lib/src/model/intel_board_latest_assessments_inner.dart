//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'intel_board_latest_assessments_inner.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IntelBoardLatestAssessmentsInner {
  /// Returns a new [IntelBoardLatestAssessmentsInner] instance.
  IntelBoardLatestAssessmentsInner({

    required  this.cadence,

    required  this.assessmentId,

     this.headline,

    required  this.createdAt,
  });

  @JsonKey(
    
    name: r'cadence',
    required: true,
    includeIfNull: false,
  )


  final IntelBoardLatestAssessmentsInnerCadenceEnum cadence;



      /// ULID identifier
  @JsonKey(
    
    name: r'assessmentId',
    required: true,
    includeIfNull: false,
  )


  final String assessmentId;



  @JsonKey(
    
    name: r'headline',
    required: false,
    includeIfNull: false,
  )


  final String? headline;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IntelBoardLatestAssessmentsInner &&
      other.cadence == cadence &&
      other.assessmentId == assessmentId &&
      other.headline == headline &&
      other.createdAt == createdAt;

    @override
    int get hashCode =>
        cadence.hashCode +
        assessmentId.hashCode +
        (headline == null ? 0 : headline.hashCode) +
        createdAt.hashCode;

  factory IntelBoardLatestAssessmentsInner.fromJson(Map<String, dynamic> json) => _$IntelBoardLatestAssessmentsInnerFromJson(json);

  Map<String, dynamic> toJson() => _$IntelBoardLatestAssessmentsInnerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum IntelBoardLatestAssessmentsInnerCadenceEnum {
@JsonValue(r'daily')
daily(r'daily'),
@JsonValue(r'weekly')
weekly(r'weekly'),
@JsonValue(r'monthly')
monthly(r'monthly');

const IntelBoardLatestAssessmentsInnerCadenceEnum(this.value);

final String value;

@override
String toString() => value;
}


