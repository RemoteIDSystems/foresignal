//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_device_setup_overland.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestDeviceSetupOverland {
  /// Returns a new [IngestDeviceSetupOverland] instance.
  IngestDeviceSetupOverland({

     this.endpoint,

     this.token,
  });

  @JsonKey(
    
    name: r'endpoint',
    required: false,
    includeIfNull: false,
  )


  final String? endpoint;



  @JsonKey(
    
    name: r'token',
    required: false,
    includeIfNull: false,
  )


  final String? token;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestDeviceSetupOverland &&
      other.endpoint == endpoint &&
      other.token == token;

    @override
    int get hashCode =>
        endpoint.hashCode +
        token.hashCode;

  factory IngestDeviceSetupOverland.fromJson(Map<String, dynamic> json) => _$IngestDeviceSetupOverlandFromJson(json);

  Map<String, dynamic> toJson() => _$IngestDeviceSetupOverlandToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

