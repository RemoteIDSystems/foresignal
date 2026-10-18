//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'custom_source_formats_owntracks.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CustomSourceFormatsOwntracks {
  /// Returns a new [CustomSourceFormatsOwntracks] instance.
  CustomSourceFormatsOwntracks({

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
    bool operator ==(Object other) => identical(this, other) || other is CustomSourceFormatsOwntracks &&
      other.maxAccuracyMeters == maxAccuracyMeters;

    @override
    int get hashCode =>
        maxAccuracyMeters.hashCode;

  factory CustomSourceFormatsOwntracks.fromJson(Map<String, dynamic> json) => _$CustomSourceFormatsOwntracksFromJson(json);

  Map<String, dynamic> toJson() => _$CustomSourceFormatsOwntracksToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

