//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_device_setup_gpslogger.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestDeviceSetupGpslogger {
  /// Returns a new [IngestDeviceSetupGpslogger] instance.
  IngestDeviceSetupGpslogger({

     this.urlTemplate,

     this.method,
  });

  @JsonKey(
    
    name: r'urlTemplate',
    required: false,
    includeIfNull: false,
  )


  final String? urlTemplate;



  @JsonKey(
    
    name: r'method',
    required: false,
    includeIfNull: false,
  )


  final String? method;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestDeviceSetupGpslogger &&
      other.urlTemplate == urlTemplate &&
      other.method == method;

    @override
    int get hashCode =>
        urlTemplate.hashCode +
        method.hashCode;

  factory IngestDeviceSetupGpslogger.fromJson(Map<String, dynamic> json) => _$IngestDeviceSetupGpsloggerFromJson(json);

  Map<String, dynamic> toJson() => _$IngestDeviceSetupGpsloggerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

