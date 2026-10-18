//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/read_restriction.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tile_key_created.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TileKeyCreated {
  /// Returns a new [TileKeyCreated] instance.
  TileKeyCreated({

    required  this.id,

    required  this.name,

    required  this.keyPrefix,

    required  this.prefixes,

     this.restrictions,

    required  this.createdAt,

    required  this.expiresAt,

     this.lastUsedAt,

     this.revokedAt,

    required  this.key,

    required  this.urlBase,
  });

  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// First characters of the key (identification only)
  @JsonKey(
    
    name: r'keyPrefix',
    required: true,
    includeIfNull: false,
  )


  final String keyPrefix;



      /// Canonical path prefixes the key may read
  @JsonKey(
    
    name: r'prefixes',
    required: true,
    includeIfNull: false,
  )


  final List<TileKeyCreatedPrefixesEnum> prefixes;



  @JsonKey(
    
    name: r'restrictions',
    required: false,
    includeIfNull: false,
  )


  final ReadRestriction? restrictions;



  @JsonKey(
    
    name: r'createdAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime createdAt;



  @JsonKey(
    
    name: r'expiresAt',
    required: true,
    includeIfNull: false,
  )


  final DateTime expiresAt;



  @JsonKey(
    
    name: r'lastUsedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? lastUsedAt;



  @JsonKey(
    
    name: r'revokedAt',
    required: false,
    includeIfNull: false,
  )


  final DateTime? revokedAt;



      /// Plaintext `fsk_…` key, shown once
  @JsonKey(
    
    name: r'key',
    required: true,
    includeIfNull: false,
  )


  final String key;



      /// `{publicBaseUrl}/v1/k/{key}`; append the canonical path after `/v1/`
  @JsonKey(
    
    name: r'urlBase',
    required: true,
    includeIfNull: false,
  )


  final String urlBase;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TileKeyCreated &&
      other.id == id &&
      other.name == name &&
      other.keyPrefix == keyPrefix &&
      other.prefixes == prefixes &&
      other.restrictions == restrictions &&
      other.createdAt == createdAt &&
      other.expiresAt == expiresAt &&
      other.lastUsedAt == lastUsedAt &&
      other.revokedAt == revokedAt &&
      other.key == key &&
      other.urlBase == urlBase;

    @override
    int get hashCode =>
        id.hashCode +
        name.hashCode +
        keyPrefix.hashCode +
        prefixes.hashCode +
        (restrictions == null ? 0 : restrictions.hashCode) +
        createdAt.hashCode +
        expiresAt.hashCode +
        (lastUsedAt == null ? 0 : lastUsedAt.hashCode) +
        (revokedAt == null ? 0 : revokedAt.hashCode) +
        key.hashCode +
        urlBase.hashCode;

  factory TileKeyCreated.fromJson(Map<String, dynamic> json) => _$TileKeyCreatedFromJson(json);

  Map<String, dynamic> toJson() => _$TileKeyCreatedToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TileKeyCreatedPrefixesEnum {
@JsonValue(r'/v1/products/')
slashV1SlashProductsSlash(r'/v1/products/'),
@JsonValue(r'/v1/styles')
slashV1SlashStyles(r'/v1/styles'),
@JsonValue(r'/v1/media/')
slashV1SlashMediaSlash(r'/v1/media/'),
@JsonValue(r'/v1/tiles/')
slashV1SlashTilesSlash(r'/v1/tiles/'),
@JsonValue(r'/v1/maps/static')
slashV1SlashMapsSlashStatic(r'/v1/maps/static'),
@JsonValue(r'/v1/sample')
slashV1SlashSample(r'/v1/sample'),
@JsonValue(r'/v1/ground-stations/')
slashV1SlashGroundStationsSlash(r'/v1/ground-stations/'),
@JsonValue(r'/v1/changes/stream')
slashV1SlashChangesSlashStream(r'/v1/changes/stream'),
@JsonValue(r'/v1/events/stream')
slashV1SlashEventsSlashStream(r'/v1/events/stream'),
@JsonValue(r'/v1/map/')
slashV1SlashMapSlash(r'/v1/map/');

const TileKeyCreatedPrefixesEnum(this.value);

final String value;

@override
String toString() => value;
}


