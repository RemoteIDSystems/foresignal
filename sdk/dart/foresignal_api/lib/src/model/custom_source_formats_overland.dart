//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'custom_source_formats_overland.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CustomSourceFormatsOverland {
  /// Returns a new [CustomSourceFormatsOverland] instance.
  CustomSourceFormatsOverland({

     this.keepWifi = false,

     this.maxAccuracyMeters = 200,
  });

      /// Keep the Wi-Fi network name (`wifi`); dropped by default (039)
  @JsonKey(
    defaultValue: false,
    name: r'keepWifi',
    required: false,
    includeIfNull: false,
  )


  final bool? keepWifi;



      /// Fixes whose accuracy is worse than this many meters are rejected with reason LOW_ACCURACY (039)
          // minimum: 0
  @JsonKey(
    defaultValue: 200,
    name: r'maxAccuracyMeters',
    required: false,
    includeIfNull: false,
  )


  final num? maxAccuracyMeters;





    @override
    bool operator ==(Object other) => identical(this, other) || other is CustomSourceFormatsOverland &&
      other.keepWifi == keepWifi &&
      other.maxAccuracyMeters == maxAccuracyMeters;

    @override
    int get hashCode =>
        keepWifi.hashCode +
        maxAccuracyMeters.hashCode;

  factory CustomSourceFormatsOverland.fromJson(Map<String, dynamic> json) => _$CustomSourceFormatsOverlandFromJson(json);

  Map<String, dynamic> toJson() => _$CustomSourceFormatsOverlandToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

