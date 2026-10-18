//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/channel_actions_config.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'channel_input_config.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class ChannelInputConfig {
  /// Returns a new [ChannelInputConfig] instance.
  ChannelInputConfig({

     this.attachMap,

     this.mapProducts,

     this.mapRadiusKm = 50,

     this.maxAttachmentBytes,

     this.actions,

     this.allowedUserIds,

     this.allowedSenders,

     this.privatePayload = false,

     this.units,
  });

      /// Attach a static map to geolocated alerts (cloud chat channels are opt-in)
  @JsonKey(
    
    name: r'attachMap',
    required: false,
    includeIfNull: false,
  )


  final ChannelInputConfigAttachMapEnum? attachMap;



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



  @JsonKey(
    
    name: r'actions',
    required: false,
    includeIfNull: false,
  )


  final ChannelActionsConfig? actions;



      /// telegram: who may press action buttons (empty = any chat member)
  @JsonKey(
    
    name: r'allowedUserIds',
    required: false,
    includeIfNull: false,
  )


  final List<int>? allowedUserIds;



      /// matrix: an array of user ids who may acknowledge by reaction; meshtastic and meshcore (084): an object of sender (`!xxxxxxxx`, `mc:` + 12 hex, or `name:` + advertised name) to role `report` or `ack`
  @JsonKey(
    
    name: r'allowedSenders',
    required: false,
    includeIfNull: false,
  )


  final Object? allowedSenders;



      /// Admin opt-in to include private-source payloads (039); otherwise rule name, severity and a link only
  @JsonKey(
    defaultValue: false,
    name: r'privatePayload',
    required: false,
    includeIfNull: false,
  )


  final bool? privatePayload;



      /// Units of the {{near.text}} placeholder in this channel's templates (040)
  @JsonKey(
    
    name: r'units',
    required: false,
    includeIfNull: false,
  )


  final ChannelInputConfigUnitsEnum? units;





    @override
    bool operator ==(Object other) => identical(this, other) || other is ChannelInputConfig &&
      other.attachMap == attachMap &&
      other.mapProducts == mapProducts &&
      other.mapRadiusKm == mapRadiusKm &&
      other.maxAttachmentBytes == maxAttachmentBytes &&
      other.actions == actions &&
      other.allowedUserIds == allowedUserIds &&
      other.allowedSenders == allowedSenders &&
      other.privatePayload == privatePayload &&
      other.units == units;

    @override
    int get hashCode =>
        attachMap.hashCode +
        mapProducts.hashCode +
        mapRadiusKm.hashCode +
        maxAttachmentBytes.hashCode +
        actions.hashCode +
        allowedUserIds.hashCode +
        (allowedSenders == null ? 0 : allowedSenders.hashCode) +
        privatePayload.hashCode +
        units.hashCode;

  factory ChannelInputConfig.fromJson(Map<String, dynamic> json) => _$ChannelInputConfigFromJson(json);

  Map<String, dynamic> toJson() => _$ChannelInputConfigToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

/// Attach a static map to geolocated alerts (cloud chat channels are opt-in)
enum ChannelInputConfigAttachMapEnum {
@JsonValue(r'none')
none(r'none'),
@JsonValue(r'static')
static_(r'static');

const ChannelInputConfigAttachMapEnum(this.value);

final String value;

@override
String toString() => value;
}


/// Units of the {{near.text}} placeholder in this channel's templates (040)
enum ChannelInputConfigUnitsEnum {
@JsonValue(r'metric')
metric(r'metric'),
@JsonValue(r'imperial')
imperial(r'imperial');

const ChannelInputConfigUnitsEnum(this.value);

final String value;

@override
String toString() => value;
}


