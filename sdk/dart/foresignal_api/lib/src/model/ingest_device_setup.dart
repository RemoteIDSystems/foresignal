//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/ingest_device_setup_gpslogger.dart';
import 'package:foresignal_api/src/model/ingest_device_setup_overland.dart';
import 'package:foresignal_api/src/model/ingest_device_setup_traccar.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_device_setup.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestDeviceSetup {
  /// Returns a new [IngestDeviceSetup] instance.
  IngestDeviceSetup({

     this.owntracks,

     this.overland,

     this.traccar,

     this.gpslogger,
  });

      /// OwnTracks .otrc JSON (mode 3 HTTP, url, auth, username, deviceId)
  @JsonKey(
    
    name: r'owntracks',
    required: false,
    includeIfNull: false,
  )


  final Map<String, Object>? owntracks;



  @JsonKey(
    
    name: r'overland',
    required: false,
    includeIfNull: false,
  )


  final IngestDeviceSetupOverland? overland;



  @JsonKey(
    
    name: r'traccar',
    required: false,
    includeIfNull: false,
  )


  final IngestDeviceSetupTraccar? traccar;



  @JsonKey(
    
    name: r'gpslogger',
    required: false,
    includeIfNull: false,
  )


  final IngestDeviceSetupGpslogger? gpslogger;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestDeviceSetup &&
      other.owntracks == owntracks &&
      other.overland == overland &&
      other.traccar == traccar &&
      other.gpslogger == gpslogger;

    @override
    int get hashCode =>
        owntracks.hashCode +
        overland.hashCode +
        traccar.hashCode +
        gpslogger.hashCode;

  factory IngestDeviceSetup.fromJson(Map<String, dynamic> json) => _$IngestDeviceSetupFromJson(json);

  Map<String, dynamic> toJson() => _$IngestDeviceSetupToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

