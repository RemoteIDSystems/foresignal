//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/edp_origin.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'edp_observation_item.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class EdpObservationItem {
  /// Returns a new [EdpObservationItem] instance.
  EdpObservationItem({

    required  this.stream,

    required  this.type,

    required  this.id,

     this.observedAt,

     this.properties,

    required  this.seriesKey,

    required  this.t,

    required  this.value,

     this.knownAt,

     this.unit,

     this.origin,
  });

  @JsonKey(
    
    name: r'stream',
    required: true,
    includeIfNull: false,
  )


  final String stream;



  @JsonKey(
    
    name: r'type',
    required: true,
    includeIfNull: false,
  )


  final EdpObservationItemTypeEnum type;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'observedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? observedAt;



      /// Item properties, validated against the stream's payloadSchema
  @JsonKey(
    
    name: r'properties',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? properties;



  @JsonKey(
    
    name: r'seriesKey',
    required: true,
    includeIfNull: false,
  )


  final String seriesKey;



      /// Period or instant: YYYY, YYYY-Qn, YYYY-MM, YYYY-Www (ISO week), YYYY-MM-DD, or an RFC 3339 timestamp with offset. Periods are UTC intervals [start, end); one series uses one granularity.
  @JsonKey(
    
    name: r't',
    required: true,
    includeIfNull: false,
  )


  final String t;



  @JsonKey(
    
    name: r'value',
    required: true,
    includeIfNull: true,
  )


  final num? value;



      /// Provider-claimed availability time; stored as given. Foresignal uses max(knownAt, its own receipt time) for as-of and look-ahead rules.
  @JsonKey(
    
    name: r'knownAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? knownAt;



  @JsonKey(
    
    name: r'unit',
    required: false,
    includeIfNull: false,
  )


  final String? unit;



  @JsonKey(
    
    name: r'origin',
    required: false,
    includeIfNull: false,
  )


  final EdpOrigin? origin;





    @override
    bool operator ==(Object other) => identical(this, other) || other is EdpObservationItem &&
      other.stream == stream &&
      other.type == type &&
      other.id == id &&
      other.observedAt == observedAt &&
      other.properties == properties &&
      other.seriesKey == seriesKey &&
      other.t == t &&
      other.value == value &&
      other.knownAt == knownAt &&
      other.unit == unit &&
      other.origin == origin;

    @override
    int get hashCode =>
        stream.hashCode +
        type.hashCode +
        id.hashCode +
        observedAt.hashCode +
        properties.hashCode +
        seriesKey.hashCode +
        t.hashCode +
        (value == null ? 0 : value.hashCode) +
        (knownAt == null ? 0 : knownAt.hashCode) +
        (unit == null ? 0 : unit.hashCode) +
        (origin == null ? 0 : origin.hashCode);

  factory EdpObservationItem.fromJson(Map<String, dynamic> json) => _$EdpObservationItemFromJson(json);

  Map<String, dynamic> toJson() => _$EdpObservationItemToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum EdpObservationItemTypeEnum {
@JsonValue(r'observation')
observation(r'observation');

const EdpObservationItemTypeEnum(this.value);

final String value;

@override
String toString() => value;
}


