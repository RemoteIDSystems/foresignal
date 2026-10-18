//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'transit_catalog_entry.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class TransitCatalogEntry {
  /// Returns a new [TransitCatalogEntry] instance.
  TransitCatalogEntry({

    required  this.id,

    required  this.provider,

     this.countryCode,

    required  this.entityType,

    required  this.directDownloadUrl,

     this.licenseUrl,

     this.staticReference,

    required  this.enabled,

     this.sourceId,

     this.sourceIds,
  });

      /// Mobility Database id
  @JsonKey(
    
    name: r'id',
    required: true,
    includeIfNull: false,
  )


  final String id;



  @JsonKey(
    
    name: r'provider',
    required: true,
    includeIfNull: false,
  )


  final String provider;



  @JsonKey(
    
    name: r'countryCode',
    required: false,
    includeIfNull: false,
  )


  final String? countryCode;



      /// The GTFS-RT entity types the URL serves, `|`-joined as in the catalog (`vp` vehicle positions, `tu` trip updates, `sa` service alerts; for example `tu|vp`); empty when the catalog declares none
  @JsonKey(
    
    name: r'entityType',
    required: true,
    includeIfNull: false,
  )


  final String entityType;



  @JsonKey(
    
    name: r'directDownloadUrl',
    required: true,
    includeIfNull: false,
  )


  final String directDownloadUrl;



  @JsonKey(
    
    name: r'licenseUrl',
    required: false,
    includeIfNull: false,
  )


  final String? licenseUrl;



  @JsonKey(
    
    name: r'staticReference',
    required: false,
    includeIfNull: false,
  )


  final String? staticReference;



      /// Listed in `foresignal.transit.catalog-feeds` and registered
  @JsonKey(
    
    name: r'enabled',
    required: true,
    includeIfNull: false,
  )


  final bool enabled;



      /// The first registered source id (`transit-mdb-{id}` or `transit-alerts-mdb-{id}`) when enabled; trip updates are not ingested. `{id}` is the catalog id lower-cased with other characters replaced by hyphens (shortened with a hash suffix past 64 characters)
  @JsonKey(
    
    name: r'sourceId',
    required: false,
    includeIfNull: false,
  )


  final String? sourceId;



      /// Every source id registered for the row (a `sa|tu|vp` row registers both `transit-mdb-{id}` and `transit-alerts-mdb-{id}`); empty when not enabled
  @JsonKey(
    
    name: r'sourceIds',
    required: false,
    includeIfNull: false,
  )


  final List<String>? sourceIds;





    @override
    bool operator ==(Object other) => identical(this, other) || other is TransitCatalogEntry &&
      other.id == id &&
      other.provider == provider &&
      other.countryCode == countryCode &&
      other.entityType == entityType &&
      other.directDownloadUrl == directDownloadUrl &&
      other.licenseUrl == licenseUrl &&
      other.staticReference == staticReference &&
      other.enabled == enabled &&
      other.sourceId == sourceId &&
      other.sourceIds == sourceIds;

    @override
    int get hashCode =>
        id.hashCode +
        provider.hashCode +
        (countryCode == null ? 0 : countryCode.hashCode) +
        entityType.hashCode +
        directDownloadUrl.hashCode +
        (licenseUrl == null ? 0 : licenseUrl.hashCode) +
        (staticReference == null ? 0 : staticReference.hashCode) +
        enabled.hashCode +
        (sourceId == null ? 0 : sourceId.hashCode) +
        sourceIds.hashCode;

  factory TransitCatalogEntry.fromJson(Map<String, dynamic> json) => _$TransitCatalogEntryFromJson(json);

  Map<String, dynamic> toJson() => _$TransitCatalogEntryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

