//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'watchlist.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class Watchlist {
  /// Returns a new [Watchlist] instance.
  Watchlist({

    required  this.name,

    required  this.identity,

     this.normalize,

     this.description,

    required  this.id,

    required  this.itemCount,

    required  this.createdAt,

    required  this.updatedAt,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



  @JsonKey(
    
    name: r'identity',
    required: true,
    includeIfNull: false,
  )


  final WatchlistIdentityEnum identity;



      /// Defaults: icao24/registration/callsign upper, mmsi/imo digits, entity/custom none
  @JsonKey(
    
    name: r'normalize',
    required: false,
    includeIfNull: false,
  )


  final WatchlistNormalizeEnum? normalize;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;



  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'itemCount',
    required: true,
    includeIfNull: false,
  )


  final int itemCount;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'updatedAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime updatedAt;





    @override
    bool operator ==(Object other) => identical(this, other) || other is Watchlist &&
      other.name == name &&
      other.identity == identity &&
      other.normalize == normalize &&
      other.description == description &&
      other.id == id &&
      other.itemCount == itemCount &&
      other.createdAt == createdAt &&
      other.updatedAt == updatedAt;

    @override
    int get hashCode =>
        name.hashCode +
        identity.hashCode +
        normalize.hashCode +
        (description == null ? 0 : description.hashCode) +
        id.hashCode +
        itemCount.hashCode +
        createdAt.hashCode +
        updatedAt.hashCode;

  factory Watchlist.fromJson(Map<String, dynamic> json) => _$WatchlistFromJson(json);

  Map<String, dynamic> toJson() => _$WatchlistToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum WatchlistIdentityEnum {
@JsonValue(r'icao24')
icao24(r'icao24'),
@JsonValue(r'registration')
registration(r'registration'),
@JsonValue(r'callsign')
callsign(r'callsign'),
@JsonValue(r'mmsi')
mmsi(r'mmsi'),
@JsonValue(r'imo')
imo(r'imo'),
@JsonValue(r'entity')
entity(r'entity'),
@JsonValue(r'custom')
custom(r'custom');

const WatchlistIdentityEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Defaults: icao24/registration/callsign upper, mmsi/imo digits, entity/custom none
enum WatchlistNormalizeEnum {
@JsonValue(r'upper')
upper(r'upper'),
@JsonValue(r'lower')
lower(r'lower'),
@JsonValue(r'digits')
digits(r'digits'),
@JsonValue(r'none')
none(r'none');

const WatchlistNormalizeEnum(this.value);

final String value;

@override
String toString() => value;
}


