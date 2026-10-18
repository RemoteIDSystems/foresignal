//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:foresignal_api/src/model/system_info_raster_formats_webp.dart';
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_raster_formats.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoRasterFormats {
  /// Returns a new [SystemInfoRasterFormats] instance.
  SystemInfoRasterFormats({

     this.webp,
  });

  @JsonKey(
    
    name: r'webp',
    required: false,
    includeIfNull: false,
  )


  final SystemInfoRasterFormatsWebp? webp;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoRasterFormats &&
      other.webp == webp;

    @override
    int get hashCode =>
        webp.hashCode;

  factory SystemInfoRasterFormats.fromJson(Map<String, dynamic> json) => _$SystemInfoRasterFormatsFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoRasterFormatsToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

