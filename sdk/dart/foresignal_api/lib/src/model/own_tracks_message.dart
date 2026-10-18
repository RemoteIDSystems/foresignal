//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'own_tracks_message.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class OwnTracksMessage {
  /// Returns a new [OwnTracksMessage] instance.
  OwnTracksMessage({

    required  this.typeUnderscore,

     this.lat,

     this.lon,

     this.tst,

     this.acc,

     this.alt,

     this.vel,

     this.cog,

     this.batt,

     this.bs,

     this.conn,

     this.tid,

     this.event,

     this.desc,

     this.wtst,
  });

  @JsonKey(
    
    name: r'_type',
    required: true,
    includeIfNull: false,
  )


  final OwnTracksMessageTypeUnderscoreEnum typeUnderscore;



  @JsonKey(
    
    name: r'lat',
    required: false,
    includeIfNull: false,
  )


  final num? lat;



  @JsonKey(
    
    name: r'lon',
    required: false,
    includeIfNull: false,
  )


  final num? lon;



      /// Epoch seconds → observedAt
  @JsonKey(
    
    name: r'tst',
    required: false,
    includeIfNull: false,
  )


  final int? tst;



      /// Accuracy (m) → quality.accuracyMeters
  @JsonKey(
    
    name: r'acc',
    required: false,
    includeIfNull: false,
  )


  final num? acc;



  @JsonKey(
    
    name: r'alt',
    required: false,
    includeIfNull: false,
  )


  final num? alt;



      /// km/h → speedMs
  @JsonKey(
    
    name: r'vel',
    required: false,
    includeIfNull: false,
  )


  final num? vel;



  @JsonKey(
    
    name: r'cog',
    required: false,
    includeIfNull: false,
  )


  final num? cog;



  @JsonKey(
    
    name: r'batt',
    required: false,
    includeIfNull: false,
  )


  final int? batt;



      /// Battery status → charging
  @JsonKey(
    
    name: r'bs',
    required: false,
    includeIfNull: false,
  )


  final int? bs;



  @JsonKey(
    
    name: r'conn',
    required: false,
    includeIfNull: false,
  )


  final String? conn;



  @JsonKey(
    
    name: r'tid',
    required: false,
    includeIfNull: false,
  )


  final String? tid;



  @JsonKey(
    
    name: r'event',
    required: false,
    includeIfNull: false,
  )


  final String? event;



  @JsonKey(
    
    name: r'desc',
    required: false,
    includeIfNull: false,
  )


  final String? desc;



  @JsonKey(
    
    name: r'wtst',
    required: false,
    includeIfNull: false,
  )


  final int? wtst;





    @override
    bool operator ==(Object other) => identical(this, other) || other is OwnTracksMessage &&
      other.typeUnderscore == typeUnderscore &&
      other.lat == lat &&
      other.lon == lon &&
      other.tst == tst &&
      other.acc == acc &&
      other.alt == alt &&
      other.vel == vel &&
      other.cog == cog &&
      other.batt == batt &&
      other.bs == bs &&
      other.conn == conn &&
      other.tid == tid &&
      other.event == event &&
      other.desc == desc &&
      other.wtst == wtst;

    @override
    int get hashCode =>
        typeUnderscore.hashCode +
        lat.hashCode +
        lon.hashCode +
        tst.hashCode +
        acc.hashCode +
        alt.hashCode +
        vel.hashCode +
        cog.hashCode +
        batt.hashCode +
        bs.hashCode +
        conn.hashCode +
        tid.hashCode +
        event.hashCode +
        desc.hashCode +
        wtst.hashCode;

  factory OwnTracksMessage.fromJson(Map<String, dynamic> json) => _$OwnTracksMessageFromJson(json);

  Map<String, dynamic> toJson() => _$OwnTracksMessageToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum OwnTracksMessageTypeUnderscoreEnum {
@JsonValue(r'location')
location(r'location'),
@JsonValue(r'transition')
transition(r'transition'),
@JsonValue(r'waypoint')
waypoint(r'waypoint'),
@JsonValue(r'lwt')
lwt(r'lwt'),
@JsonValue(r'card')
card(r'card'),
@JsonValue(r'status')
status(r'status'),
@JsonValue(r'encrypted')
encrypted(r'encrypted');

const OwnTracksMessageTypeUnderscoreEnum(this.value);

final String value;

@override
String toString() => value;
}


