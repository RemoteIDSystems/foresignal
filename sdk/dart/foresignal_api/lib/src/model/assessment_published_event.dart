//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'assessment_published_event.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class AssessmentPublishedEvent {
  /// Returns a new [AssessmentPublishedEvent] instance.
  AssessmentPublishedEvent({

    required  this.assessmentId,

     this.cycleId,

     this.situationId,

    required  this.cadence,

    required  this.status,

     this.periodStart,

     this.periodEnd,

     this.headline,

     this.private,

    required  this.at,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'assessmentId',
    required: true,
    includeIfNull: false,
  )


  final String assessmentId;



      /// ULID identifier
  @JsonKey(
    
    name: r'cycleId',
    required: false,
    includeIfNull: false,
  )


  final String? cycleId;



      /// ULID identifier
  @JsonKey(
    
    name: r'situationId',
    required: false,
    includeIfNull: false,
  )


  final String? situationId;



  @JsonKey(
    
    name: r'cadence',
    required: true,
    includeIfNull: false,
  )


  final AssessmentPublishedEventCadenceEnum cadence;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final AssessmentPublishedEventStatusEnum status;



  @JsonKey(
    
    name: r'periodStart',
    required: false,
    includeIfNull: false,
  )


  final DateTime? periodStart;



  @JsonKey(
    
    name: r'periodEnd',
    required: false,
    includeIfNull: false,
  )


  final DateTime? periodEnd;



  @JsonKey(
    
    name: r'headline',
    required: false,
    includeIfNull: false,
  )


  final String? headline;



  @JsonKey(
    
    name: r'private',
    required: false,
    includeIfNull: false,
  )


  final bool? private;



  @JsonKey(
    
    name: r'at',
    required: true,
    includeIfNull: false,
  )


  final DateTime at;





    @override
    bool operator ==(Object other) => identical(this, other) || other is AssessmentPublishedEvent &&
      other.assessmentId == assessmentId &&
      other.cycleId == cycleId &&
      other.situationId == situationId &&
      other.cadence == cadence &&
      other.status == status &&
      other.periodStart == periodStart &&
      other.periodEnd == periodEnd &&
      other.headline == headline &&
      other.private == private &&
      other.at == at;

    @override
    int get hashCode =>
        assessmentId.hashCode +
        (cycleId == null ? 0 : cycleId.hashCode) +
        (situationId == null ? 0 : situationId.hashCode) +
        cadence.hashCode +
        status.hashCode +
        periodStart.hashCode +
        periodEnd.hashCode +
        (headline == null ? 0 : headline.hashCode) +
        private.hashCode +
        at.hashCode;

  factory AssessmentPublishedEvent.fromJson(Map<String, dynamic> json) => _$AssessmentPublishedEventFromJson(json);

  Map<String, dynamic> toJson() => _$AssessmentPublishedEventToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum AssessmentPublishedEventCadenceEnum {
@JsonValue(r'daily')
daily(r'daily'),
@JsonValue(r'weekly')
weekly(r'weekly'),
@JsonValue(r'monthly')
monthly(r'monthly'),
@JsonValue(r'on-demand')
onDemand(r'on-demand');

const AssessmentPublishedEventCadenceEnum(this.value);

final String value;

@override
String toString() => value;
}


enum AssessmentPublishedEventStatusEnum {
@JsonValue(r'ok')
ok(r'ok'),
@JsonValue(r'partial')
partial(r'partial'),
@JsonValue(r'digest-only')
digestOnly(r'digest-only'),
@JsonValue(r'failed')
failed(r'failed');

const AssessmentPublishedEventStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


