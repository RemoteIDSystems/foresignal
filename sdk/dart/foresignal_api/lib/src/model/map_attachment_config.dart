//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'map_attachment_config.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class MapAttachmentConfig {
  /// Returns a new [MapAttachmentConfig] instance.
  MapAttachmentConfig({

     this.attachMap,

     this.mapProducts,

     this.mapRadiusKm = 50,

     this.maxAttachmentBytes,
  });

      /// Attach a static map to geolocated alerts (cloud chat channels are opt-in)
  @JsonKey(
    
    name: r'attachMap',
    required: false,
    includeIfNull: false,
  )


  final MapAttachmentConfigAttachMapEnum? attachMap;



      /// Products drawn (default [\"mrms-reflectivity\"] or the product that matched the rule)
  @JsonKey(
    
    name: r'mapProducts',
    required: false,
    includeIfNull: false,
  )


  final List<String>? mapProducts;



          // minimum: 1
          // maximum: 2000
  @JsonKey(
    defaultValue: 50,
    name: r'mapRadiusKm',
    required: false,
    includeIfNull: false,
  )


  final num? mapRadiusKm;



      /// Per-type default: discord 8 MB, telegram 10 MB, ntfy 15 MB, pushover 5,242,880, email 10 MB
          // minimum: 1024
          // maximum: 104857600
  @JsonKey(
    
    name: r'maxAttachmentBytes',
    required: false,
    includeIfNull: false,
  )


  final int? maxAttachmentBytes;





    @override
    bool operator ==(Object other) => identical(this, other) || other is MapAttachmentConfig &&
      other.attachMap == attachMap &&
      other.mapProducts == mapProducts &&
      other.mapRadiusKm == mapRadiusKm &&
      other.maxAttachmentBytes == maxAttachmentBytes;

    @override
    int get hashCode =>
        attachMap.hashCode +
        mapProducts.hashCode +
        mapRadiusKm.hashCode +
        maxAttachmentBytes.hashCode;

  factory MapAttachmentConfig.fromJson(Map<String, dynamic> json) => _$MapAttachmentConfigFromJson(json);

  Map<String, dynamic> toJson() => _$MapAttachmentConfigToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Attach a static map to geolocated alerts (cloud chat channels are opt-in)
enum MapAttachmentConfigAttachMapEnum {
@JsonValue(r'none')
none(r'none'),
@JsonValue(r'static')
static_(r'static');

const MapAttachmentConfigAttachMapEnum(this.value);

final String value;

@override
String toString() => value;
}


