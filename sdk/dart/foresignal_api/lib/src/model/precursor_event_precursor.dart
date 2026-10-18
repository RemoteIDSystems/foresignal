//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'precursor_event_precursor.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class PrecursorEventPrecursor {
  /// Returns a new [PrecursorEventPrecursor] instance.
  PrecursorEventPrecursor({

    required  this.id,

    required  this.kind,

    required  this.status,

     this.statusReason,

    required  this.description,

     this.lift,
  });

      /// ULID identifier
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'kind',
    required: true,
    includeIfNull: false,
  )


  final PrecursorEventPrecursorKindEnum kind;



  @JsonKey(
    
    name: r'status',
    required: true,
    includeIfNull: false,
  )


  final PrecursorEventPrecursorStatusEnum status;



  @JsonKey(
    
    name: r'statusReason',
    required: false,
    includeIfNull: false,
  )


  final String? statusReason;



  @JsonKey(
    
    name: r'description',
    required: true,
    includeIfNull: false,
  )


  final String description;



  @JsonKey(
    
    name: r'lift',
    required: false,
    includeIfNull: false,
  )


  final num? lift;





    @override
    bool operator ==(Object other) => identical(this, other) || other is PrecursorEventPrecursor &&
      other.id == id &&
      other.kind == kind &&
      other.status == status &&
      other.statusReason == statusReason &&
      other.description == description &&
      other.lift == lift;

    @override
    int get hashCode =>
        id.hashCode +
        kind.hashCode +
        status.hashCode +
        (statusReason == null ? 0 : statusReason.hashCode) +
        description.hashCode +
        (lift == null ? 0 : lift.hashCode);

  factory PrecursorEventPrecursor.fromJson(Map<String, dynamic> json) => _$PrecursorEventPrecursorFromJson(json);

  Map<String, dynamic> toJson() => _$PrecursorEventPrecursorToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum PrecursorEventPrecursorKindEnum {
@JsonValue(r'event')
event(r'event'),
@JsonValue(r'series')
series(r'series');

const PrecursorEventPrecursorKindEnum(this.value);

final String value;

@override
String toString() => value;
}


enum PrecursorEventPrecursorStatusEnum {
@JsonValue(r'candidate')
candidate(r'candidate'),
@JsonValue(r'validated')
validated(r'validated'),
@JsonValue(r'stale')
stale(r'stale'),
@JsonValue(r'retired')
retired(r'retired');

const PrecursorEventPrecursorStatusEnum(this.value);

final String value;

@override
String toString() => value;
}


