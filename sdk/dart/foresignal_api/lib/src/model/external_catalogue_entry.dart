//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/attribution.dart';
import 'package:foresignal_api/src/model/external_catalogue_entry_time_dimension.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'external_catalogue_entry.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ExternalCatalogueEntry {
  /// Returns a new [ExternalCatalogueEntry] instance.
  ExternalCatalogueEntry({

     this.provider,

     this.layer,

     this.protocol,

    required  this.externalTemplate,

    required  this.tilingScheme,

    required  this.tileSize,

    required  this.maxNativeZoom,

     this.format,

    required  this.bounds,

     this.timeDimension,

     this.cors,

    required  this.attribution,
  });

      /// Upstream provider (gibs, nowcoast)
  @JsonKey(
    
    name: r'provider',
    required: false,
    includeIfNull: false,
  )


  final String? provider;



  @JsonKey(
    
    name: r'layer',
    required: false,
    includeIfNull: false,
  )


  final String? layer;



  @JsonKey(
    
    name: r'protocol',
    required: false,
    includeIfNull: false,
  )


  final ExternalCatalogueEntryProtocolEnum? protocol;



      /// Tile URL template clients fetch directly (Foresignal never proxies it)
  @JsonKey(
    
    name: r'externalTemplate',
    required: true,
    includeIfNull: false,
  )


  final String externalTemplate;



  @JsonKey(
    
    name: r'tilingScheme',
    required: true,
    includeIfNull: false,
  )


  final String tilingScheme;



  @JsonKey(
    
    name: r'tileSize',
    required: true,
    includeIfNull: false,
  )


  final int tileSize;



          // minimum: 0
  @JsonKey(
    
    name: r'maxNativeZoom',
    required: true,
    includeIfNull: false,
  )


  final int maxNativeZoom;



  @JsonKey(
    
    name: r'format',
    required: false,
    includeIfNull: false,
  )


  final ExternalCatalogueEntryFormatEnum? format;



      /// [west, south, east, north] in WGS84 degrees
  @JsonKey(
    
    name: r'bounds',
    required: true,
    includeIfNull: false,
  )


  final List<num> bounds;



  @JsonKey(
    
    name: r'timeDimension',
    required: false,
    includeIfNull: false,
  )


  final ExternalCatalogueEntryTimeDimension? timeDimension;



      /// Upstream `Access-Control-Allow-Origin` (checked 2026-10)
  @JsonKey(
    
    name: r'cors',
    required: false,
    includeIfNull: false,
  )


  final String? cors;



  @JsonKey(
    
    name: r'attribution',
    required: true,
    includeIfNull: false,
  )


  final Attribution attribution;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ExternalCatalogueEntry &&
      other.provider == provider &&
      other.layer == layer &&
      other.protocol == protocol &&
      other.externalTemplate == externalTemplate &&
      other.tilingScheme == tilingScheme &&
      other.tileSize == tileSize &&
      other.maxNativeZoom == maxNativeZoom &&
      other.format == format &&
      other.bounds == bounds &&
      other.timeDimension == timeDimension &&
      other.cors == cors &&
      other.attribution == attribution;

    @override
    int get hashCode =>
        provider.hashCode +
        layer.hashCode +
        protocol.hashCode +
        externalTemplate.hashCode +
        tilingScheme.hashCode +
        tileSize.hashCode +
        maxNativeZoom.hashCode +
        format.hashCode +
        bounds.hashCode +
        (timeDimension == null ? 0 : timeDimension.hashCode) +
        cors.hashCode +
        attribution.hashCode;

  factory ExternalCatalogueEntry.fromJson(Map<String, dynamic> json) => _$ExternalCatalogueEntryFromJson(json);

  Map<String, dynamic> toJson() => _$ExternalCatalogueEntryToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

enum ExternalCatalogueEntryProtocolEnum {
@JsonValue(r'wmts')
wmts(r'wmts'),
@JsonValue(r'wms')
wms(r'wms'),
@JsonValue(r'xyz')
xyz(r'xyz');

const ExternalCatalogueEntryProtocolEnum(this.value);

final String value;

@override
String toString() => value;
}


enum ExternalCatalogueEntryFormatEnum {
@JsonValue(r'png')
png(r'png'),
@JsonValue(r'jpg')
jpg(r'jpg');

const ExternalCatalogueEntryFormatEnum(this.value);

final String value;

@override
String toString() => value;
}


