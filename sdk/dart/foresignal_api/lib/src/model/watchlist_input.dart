//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'watchlist_input.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class WatchlistInput {
  /// Returns a new [WatchlistInput] instance.
  WatchlistInput({

    required  this.name,

    required  this.identity,

     this.normalize,

     this.description,
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


  final WatchlistInputIdentityEnum identity;



      /// Defaults: icao24/registration/callsign upper, mmsi/imo digits, entity/custom none
  @JsonKey(
    
    name: r'normalize',
    required: false,
    includeIfNull: false,
  )


  final WatchlistInputNormalizeEnum? normalize;



  @JsonKey(
    
    name: r'description',
    required: false,
    includeIfNull: false,
  )


  final String? description;





    @override
    bool operator ==(Object other) => identical(this, other) || other is WatchlistInput &&
      other.name == name &&
      other.identity == identity &&
      other.normalize == normalize &&
      other.description == description;

    @override
    int get hashCode =>
        name.hashCode +
        identity.hashCode +
        normalize.hashCode +
        (description == null ? 0 : description.hashCode);

  factory WatchlistInput.fromJson(Map<String, dynamic> json) => _$WatchlistInputFromJson(json);

  Map<String, dynamic> toJson() => _$WatchlistInputToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum WatchlistInputIdentityEnum {
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

const WatchlistInputIdentityEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Defaults: icao24/registration/callsign upper, mmsi/imo digits, entity/custom none
enum WatchlistInputNormalizeEnum {
@JsonValue(r'upper')
upper(r'upper'),
@JsonValue(r'lower')
lower(r'lower'),
@JsonValue(r'digits')
digits(r'digits'),
@JsonValue(r'none')
none(r'none');

const WatchlistInputNormalizeEnum(this.value);

final String value;

@override
String toString() => value;
}


