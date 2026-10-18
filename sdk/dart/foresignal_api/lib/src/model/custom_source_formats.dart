//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/custom_source_formats_alertmanager.dart';
import 'package:foresignal_api/src/model/custom_source_formats_osmand.dart';
import 'package:foresignal_api/src/model/custom_source_formats_cap.dart';
import 'package:foresignal_api/src/model/custom_source_formats_owntracks.dart';
import 'package:foresignal_api/src/model/custom_source_formats_cloudevents.dart';
import 'package:foresignal_api/src/model/custom_source_formats_overland.dart';
import 'package:foresignal_api/src/model/custom_source_formats_gpslogger.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'custom_source_formats.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CustomSourceFormats {
  /// Returns a new [CustomSourceFormats] instance.
  CustomSourceFormats({

     this.native_,

     this.alertmanager,

     this.cloudevents,

     this.cap,

     this.mapping,

     this.owntracks,

     this.osmand,

     this.overland,

     this.gpslogger,
  });

      /// native GeoJSON / records format options
  @JsonKey(
    
    name: r'native',
    required: false,
    includeIfNull: false,
  )


  final Object? native_;



  @JsonKey(
    
    name: r'alertmanager',
    required: false,
    includeIfNull: false,
  )


  final CustomSourceFormatsAlertmanager? alertmanager;



  @JsonKey(
    
    name: r'cloudevents',
    required: false,
    includeIfNull: false,
  )


  final CustomSourceFormatsCloudevents? cloudevents;



  @JsonKey(
    
    name: r'cap',
    required: false,
    includeIfNull: false,
  )


  final CustomSourceFormatsCap? cap;



      /// Not accepted since 059 (400): the mapping is the top-level `mapping` member of CustomSourceInput (a RecordMapping). The 024 JSON-Pointer shape was replaced.
  @JsonKey(
    
    name: r'mapping',
    required: false,
    includeIfNull: false,
  )


  final Object? mapping;



  @JsonKey(
    
    name: r'owntracks',
    required: false,
    includeIfNull: false,
  )


  final CustomSourceFormatsOwntracks? owntracks;



  @JsonKey(
    
    name: r'osmand',
    required: false,
    includeIfNull: false,
  )


  final CustomSourceFormatsOsmand? osmand;



  @JsonKey(
    
    name: r'overland',
    required: false,
    includeIfNull: false,
  )


  final CustomSourceFormatsOverland? overland;



  @JsonKey(
    
    name: r'gpslogger',
    required: false,
    includeIfNull: false,
  )


  final CustomSourceFormatsGpslogger? gpslogger;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CustomSourceFormats &&
      other.native_ == native_ &&
      other.alertmanager == alertmanager &&
      other.cloudevents == cloudevents &&
      other.cap == cap &&
      other.mapping == mapping &&
      other.owntracks == owntracks &&
      other.osmand == osmand &&
      other.overland == overland &&
      other.gpslogger == gpslogger;

    @override
    int get hashCode =>
        native_.hashCode +
        alertmanager.hashCode +
        cloudevents.hashCode +
        cap.hashCode +
        (mapping == null ? 0 : mapping.hashCode) +
        owntracks.hashCode +
        osmand.hashCode +
        overland.hashCode +
        gpslogger.hashCode;

  factory CustomSourceFormats.fromJson(Map<String, dynamic> json) => _$CustomSourceFormatsFromJson(json);

  Map<String, dynamic> toJson() => _$CustomSourceFormatsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

