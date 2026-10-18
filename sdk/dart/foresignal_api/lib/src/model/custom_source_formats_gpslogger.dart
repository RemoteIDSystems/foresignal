//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'custom_source_formats_gpslogger.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CustomSourceFormatsGpslogger {
  /// Returns a new [CustomSourceFormatsGpslogger] instance.
  CustomSourceFormatsGpslogger({

     this.maxAccuracyMeters = 200,
  });

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
    bool operator ==(Object other) => identical(this, other) || other is CustomSourceFormatsGpslogger &&
      other.maxAccuracyMeters == maxAccuracyMeters;

    @override
    int get hashCode =>
        maxAccuracyMeters.hashCode;

  factory CustomSourceFormatsGpslogger.fromJson(Map<String, dynamic> json) => _$CustomSourceFormatsGpsloggerFromJson(json);

  Map<String, dynamic> toJson() => _$CustomSourceFormatsGpsloggerToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

