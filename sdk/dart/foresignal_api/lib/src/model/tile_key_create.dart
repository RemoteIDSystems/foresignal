//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/read_restriction.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'tile_key_create.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TileKeyCreate {
  /// Returns a new [TileKeyCreate] instance.
  TileKeyCreate({

    required  this.name,

    required  this.prefixes,

    required  this.ttl,

     this.restrictions,
  });

  @JsonKey(
    
    name: r'name',
    required: true,
    includeIfNull: false,
  )


  final String name;



      /// Allowed canonical prefixes (anything else → 400 VALIDATION_FAILED)
  @JsonKey(
    
    name: r'prefixes',
    required: true,
    includeIfNull: false,
  )


  final Set<TileKeyCreatePrefixesEnum> prefixes;



      /// Lifetime, at most 30 days. ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h)
  @JsonKey(
    
    name: r'ttl',
    required: true,
    includeIfNull: false,
  )


  final String ttl;



  @JsonKey(
    
    name: r'restrictions',
    required: false,
    includeIfNull: false,
  )


  final ReadRestriction? restrictions;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TileKeyCreate &&
      other.name == name &&
      other.prefixes == prefixes &&
      other.ttl == ttl &&
      other.restrictions == restrictions;

    @override
    int get hashCode =>
        name.hashCode +
        prefixes.hashCode +
        ttl.hashCode +
        restrictions.hashCode;

  factory TileKeyCreate.fromJson(Map<String, dynamic> json) => _$TileKeyCreateFromJson(json);

  Map<String, dynamic> toJson() => _$TileKeyCreateToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum TileKeyCreatePrefixesEnum {
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

const TileKeyCreatePrefixesEnum(this.value);

final String value;

@override
String toString() => value;
}


