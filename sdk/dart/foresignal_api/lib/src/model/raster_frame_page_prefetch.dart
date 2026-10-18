//
// AUTO-GENERATED FILE, DO NOT MODIFY!
//

// ignore_for_file: unused_element
import 'package:copy_with_extension/copy_with_extension.dart';
import 'package:json_annotation/json_annotation.dart';

part 'raster_frame_page_prefetch.g.dart';


@CopyWith()
@JsonSerializable(
  checked: true,
  createToJson: true,
  disallowUnrecognizedKeys: false,
  explicitToJson: true,
)
class RasterFramePagePrefetch {
  /// Returns a new [RasterFramePagePrefetch] instance.
  RasterFramePagePrefetch({

    required  this.recommendedFrames,

     this.maxZoomForAnimation,
  });

  @JsonKey(
    
    name: r'recommendedFrames',
    required: true,
    includeIfNull: false,
  )


  final int recommendedFrames;



  @JsonKey(
    
    name: r'maxZoomForAnimation',
    required: false,
    includeIfNull: false,
  )


  final int? maxZoomForAnimation;





    @override
    bool operator ==(Object other) => identical(this, other) || other is RasterFramePagePrefetch &&
      other.recommendedFrames == recommendedFrames &&
      other.maxZoomForAnimation == maxZoomForAnimation;

    @override
    int get hashCode =>
        recommendedFrames.hashCode +
        maxZoomForAnimation.hashCode;

  factory RasterFramePagePrefetch.fromJson(Map<String, dynamic> json) => _$RasterFramePagePrefetchFromJson(json);

  Map<String, dynamic> toJson() => _$RasterFramePagePrefetchToJson(this);

  @override
  String toString() {
    return toJson().toString();
  }

}

