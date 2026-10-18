//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/trip_hazard.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'trip_run.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TripRun {
  /// Returns a new [TripRun] instance.
  TripRun({

    required  this.id,

    required  this.tripId,

    required  this.occurrence,

    required  this.status,

    required  this.clear,

    required  this.hazards,

     this.briefing,

     this.deliveredAt,

    required  this.createdAt,
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
    
    name: r'tripId',
    required: true,
    includeIfNull: false,
  )


  final String tripId;



  @JsonKey(
    
    name: r'occurrence',
    required: true,
    includeIfNull: false,
  )


  final DateTime occurrence;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final TripRunStatusEnum status;



      /// No hazards found ('route clear')
  @JsonKey(
    
    name: r'clear',
    required: true,
    includeIfNull: false,
  )


  final bool clear;



  @JsonKey(
    
    name: r'hazards',
    required: true,
    includeIfNull: false,
  )


  final List<TripHazard> hazards;



      /// Markdown trip briefing (029 digest renderer)
  @JsonKey(
    
    name: r'briefing',
    required: false,
    includeIfNull: false,
  )


  final String? briefing;



  @JsonKey(
    
    name: r'deliveredAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? deliveredAt;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TripRun &&
      other.id == id &&
      other.tripId == tripId &&
      other.occurrence == occurrence &&
      other.status == status &&
      other.clear == clear &&
      other.hazards == hazards &&
      other.briefing == briefing &&
      other.deliveredAt == deliveredAt &&
      other.createdAt == createdAt;

    @override
    int get hashCode =>
        id.hashCode +
        tripId.hashCode +
        occurrence.hashCode +
        status.hashCode +
        clear.hashCode +
        hazards.hashCode +
        briefing.hashCode +
        (deliveredAt == null ? 0 : deliveredAt.hashCode) +
        createdAt.hashCode;

  factory TripRun.fromJson(Map<String, dynamic> json) => _$TripRunFromJson(json);

  Map<String, dynamic> toJson() => _$TripRunToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TripRunStatusEnum {
@JsonValue(r'delivered')
delivered(r'delivered'),
@JsonValue(r'skipped')
skipped(r'skipped'),
@JsonValue(r'dry-run')
dryRun(r'dry-run'),
@JsonValue(r'failed')
failed(r'failed');

const TripRunStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


