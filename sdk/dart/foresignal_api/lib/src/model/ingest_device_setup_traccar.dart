//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'ingest_device_setup_traccar.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class IngestDeviceSetupTraccar {
  /// Returns a new [IngestDeviceSetupTraccar] instance.
  IngestDeviceSetupTraccar({

     this.serverUrl,
  });

      /// URL with the key in the path
  @JsonKey(
    
    name: r'serverUrl',
    required: false,
    includeIfNull: false,
  )


  final String? serverUrl;





    @override
    bool operator ==(Object other) => identical(this, other) || other is IngestDeviceSetupTraccar &&
      other.serverUrl == serverUrl;

    @override
    int get hashCode =>
        serverUrl.hashCode;

  factory IngestDeviceSetupTraccar.fromJson(Map<String, dynamic> json) => _$IngestDeviceSetupTraccarFromJson(json);

  Map<String, dynamic> toJson() => _$IngestDeviceSetupTraccarToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

