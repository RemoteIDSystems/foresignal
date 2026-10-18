//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'system_info_raster_formats_webp.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class SystemInfoRasterFormatsWebp {
  /// Returns a new [SystemInfoRasterFormatsWebp] instance.
  SystemInfoRasterFormatsWebp({

     this.available,

     this.reason,
  });

  @JsonKey(
    
    name: r'available',
    required: false,
    includeIfNull: false,
  )


  final bool? available;



  @JsonKey(
    
    name: r'reason',
    required: false,
    includeIfNull: false,
  )


  final String? reason;





    @override
    bool operator ==(Object other) => identical(this, other) || other is SystemInfoRasterFormatsWebp &&
      other.available == available &&
      other.reason == reason;

    @override
    int get hashCode =>
        available.hashCode +
        (reason == null ? 0 : reason.hashCode);

  factory SystemInfoRasterFormatsWebp.fromJson(Map<String, dynamic> json) => _$SystemInfoRasterFormatsWebpFromJson(json);

  Map<String, dynamic> toJson() => _$SystemInfoRasterFormatsWebpToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

