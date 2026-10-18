//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'custom_source_formats_osmand.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class CustomSourceFormatsOsmand {
  /// Returns a new [CustomSourceFormatsOsmand] instance.
  CustomSourceFormatsOsmand({

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
    bool operator ==(Object other) => identical(this, other) || other is CustomSourceFormatsOsmand &&
      other.maxAccuracyMeters == maxAccuracyMeters;

    @override
    int get hashCode =>
        maxAccuracyMeters.hashCode;

  factory CustomSourceFormatsOsmand.fromJson(Map<String, dynamic> json) => _$CustomSourceFormatsOsmandFromJson(json);

  Map<String, dynamic> toJson() => _$CustomSourceFormatsOsmandToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

