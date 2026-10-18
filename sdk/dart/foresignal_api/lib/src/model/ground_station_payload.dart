//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ground_station_payload.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class GroundStationPayload {
  /// Returns a new [GroundStationPayload] instance.
  GroundStationPayload({

    required  this.state,

     this.currentTarget,

     this.frequencyHz,

     this.az,

     this.el,

     this.snrDb,

     this.nextAos,

     this.nextLos,

     this.rotatorEngaged,

     this.satdumpMode,

    required  this.title,
  });

  @JsonKey(
    
    name: r'state',
    required: true,
    includeIfNull: false,
  )


  final GroundStationPayloadStateEnum state;



  @JsonKey(
    
    name: r'currentTarget',
    required: false,
    includeIfNull: false,
  )


  final String? currentTarget;



  @JsonKey(
    
    name: r'frequencyHz',
    required: false,
    includeIfNull: false,
  )


  final int? frequencyHz;



      /// 1° steps
  @JsonKey(
    
    name: r'az',
    required: false,
    includeIfNull: false,
  )


  final int? az;



      /// 1° steps
  @JsonKey(
    
    name: r'el',
    required: false,
    includeIfNull: false,
  )


  final int? el;



      /// 1 dB steps
  @JsonKey(
    
    name: r'snrDb',
    required: false,
    includeIfNull: false,
  )


  final int? snrDb;



  @JsonKey(
    
    name: r'nextAos',
    required: false,
    includeIfNull: false,
  )


  final DateTime? nextAos;



  @JsonKey(
    
    name: r'nextLos',
    required: false,
    includeIfNull: false,
  )


  final DateTime? nextLos;



  @JsonKey(
    
    name: r'rotatorEngaged',
    required: false,
    includeIfNull: false,
  )


  final bool? rotatorEngaged;



  @JsonKey(
    
    name: r'satdumpMode',
    required: false,
    includeIfNull: false,
  )


  final GroundStationPayloadSatdumpModeEnum? satdumpMode;



  @JsonKey(
    
    name: r'title',
    required: true,
    includeIfNull: false,
  )


  final String title;





    @override
    bool operator ==(Object other) => identical(this, other) || other is GroundStationPayload &&
      other.state == state &&
      other.currentTarget == currentTarget &&
      other.frequencyHz == frequencyHz &&
      other.az == az &&
      other.el == el &&
      other.snrDb == snrDb &&
      other.nextAos == nextAos &&
      other.nextLos == nextLos &&
      other.rotatorEngaged == rotatorEngaged &&
      other.satdumpMode == satdumpMode &&
      other.title == title;

    @override
    int get hashCode =>
        state.hashCode +
        (currentTarget == null ? 0 : currentTarget.hashCode) +
        (frequencyHz == null ? 0 : frequencyHz.hashCode) +
        (az == null ? 0 : az.hashCode) +
        (el == null ? 0 : el.hashCode) +
        (snrDb == null ? 0 : snrDb.hashCode) +
        (nextAos == null ? 0 : nextAos.hashCode) +
        (nextLos == null ? 0 : nextLos.hashCode) +
        rotatorEngaged.hashCode +
        (satdumpMode == null ? 0 : satdumpMode.hashCode) +
        title.hashCode;

  factory GroundStationPayload.fromJson(Map<String, dynamic> json) => _$GroundStationPayloadFromJson(json);

  Map<String, dynamic> toJson() => _$GroundStationPayloadToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum GroundStationPayloadStateEnum {
@JsonValue(r'idle')
idle(r'idle'),
@JsonValue(r'tracking')
tracking(r'tracking'),
@JsonValue(r'receiving')
receiving(r'receiving'),
@JsonValue(r'locked')
locked(r'locked'),
@JsonValue(r'unknown')
unknown(r'unknown');

const GroundStationPayloadStateEnum(this.value);

final String value;

@override
String toString() => value;
}


enum GroundStationPayloadSatdumpModeEnum {
@JsonValue(r'live')
live(r'live'),
@JsonValue(r'autotrack')
autotrack(r'autotrack');

const GroundStationPayloadSatdumpModeEnum(this.value);

final String value;

@override
String toString() => value;
}


